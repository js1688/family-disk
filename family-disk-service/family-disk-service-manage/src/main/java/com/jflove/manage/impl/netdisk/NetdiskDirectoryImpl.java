package com.jflove.manage.impl.netdisk;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jflove.ResponseHeadDTO;
import com.jflove.file.api.IFileAdministration;
import com.jflove.file.em.FileSourceENUM;
import com.jflove.mapper.netdisk.NetdiskDirectoryMapper;
import com.jflove.netdisk.api.INetdiskDirectory;
import com.jflove.netdisk.dto.NetdiskDirectoryDTO;
import com.jflove.netdisk.em.NetdiskDirectoryENUM;
import com.jflove.po.netdisk.NetdiskDirectoryPO;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.config.annotation.DubboService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author tanjun
 * @date 2022/12/16 14:41
 * @describe
 */
@DubboService
@Log4j2
public class NetdiskDirectoryImpl implements INetdiskDirectory {

    @Autowired
    private NetdiskDirectoryMapper netdiskDirectoryMapper;

    @Autowired
    private IFileAdministration fileAdministration;

    @Override
    public ResponseHeadDTO<NetdiskDirectoryDTO> findDirectoryTree(Long spaceId, NetdiskDirectoryENUM type) {
        List<NetdiskDirectoryDTO> listDto = getNetdiskDirectoryDTOS(spaceId, 0l, null, type);
        appendDirectoryTree(listDto,type);
        return new ResponseHeadDTO<>(listDto);
    }

    private void appendDirectoryTree(List<NetdiskDirectoryDTO> list,NetdiskDirectoryENUM type){
        list.forEach(v->{
            if(v.getType() != NetdiskDirectoryENUM.FOLDER){
                return;
            }
            List<NetdiskDirectoryDTO> c = getNetdiskDirectoryDTOS(v.getSpaceId(), v.getId(), null, type);
            appendDirectoryTree(c,type);
            v.setChildren(c);
        });
    }

    @Override
    @Transactional
    public ResponseHeadDTO updateName(Long spaceId, Long id, String name) {
        NetdiskDirectoryPO po = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
                .eq(NetdiskDirectoryPO::getId,id)
        );
        if(po == null){
            return new ResponseHeadDTO(false,"没有这个目录");
        }
        if(netdiskDirectoryMapper.exists(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getPid,po.getPid())
                .eq(NetdiskDirectoryPO::getName,name)
                .ne(NetdiskDirectoryPO::getId,id)
        )){
            return new ResponseHeadDTO(false,"目录名称重复");
        }
        po.setName(name);
        po.setUpdateTime(null);
        netdiskDirectoryMapper.updateById(po);
        //如果是文件,还需要修改文件名
        if(NetdiskDirectoryENUM.FILE.getCode().equals(po.getType())){
            fileAdministration.updateName(po.getFileMd5(),spaceId,name,FileSourceENUM.CLOUDDISK);
        }
        return new ResponseHeadDTO<>(true,"修改成功");
    }

    @Override
    public ResponseHeadDTO<NetdiskDirectoryDTO> findDirectory(Long spaceId, Long pid,String keyword,NetdiskDirectoryENUM type) {
        List<NetdiskDirectoryDTO> listDto = getNetdiskDirectoryDTOS(spaceId, pid, keyword, type);
        return new ResponseHeadDTO<>(listDto);
    }

    @NotNull
    private List<NetdiskDirectoryDTO> getNetdiskDirectoryDTOS(Long spaceId, Long pid, String keyword, NetdiskDirectoryENUM type) {
        List<NetdiskDirectoryPO> list = netdiskDirectoryMapper.selectList(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(type != null,NetdiskDirectoryPO::getType,
                        Optional.ofNullable(type).orElse(NetdiskDirectoryENUM.FOLDER).getCode())
                .eq(NetdiskDirectoryPO::getSpaceId, spaceId)
                .eq(!StringUtils.hasLength(keyword),NetdiskDirectoryPO::getPid, Optional.ofNullable(pid).orElse(0l))
                .like(StringUtils.hasLength(keyword),NetdiskDirectoryPO::getName, keyword)
        );
        List<NetdiskDirectoryDTO> listDto = new ArrayList<>(list.size());
        list.forEach(v->{
            NetdiskDirectoryDTO dto = new NetdiskDirectoryDTO();
            BeanUtils.copyProperties(v,dto);
            dto.setType(NetdiskDirectoryENUM.valueOf(v.getType()));
            if(dto.getType() == NetdiskDirectoryENUM.FILE){
                ResponseHeadDTO<Long> sizeRet = fileAdministration.getFileSize(dto.getFileMd5(),spaceId,FileSourceENUM.CLOUDDISK);
                long mb = DataSize.ofBytes(sizeRet.getData()).toMegabytes();
                dto.setSize(String.valueOf(mb == 0l ? 1 : mb));
            }else{
                dto.setSize("-");
            }
            listDto.add(dto);
        });
        return listDto;
    }

    @Override
    @Transactional
    public ResponseHeadDTO<NetdiskDirectoryDTO> addDirectory(NetdiskDirectoryDTO dto) {
        //检查同级别下文件名是否已存在
        if(netdiskDirectoryMapper.exists(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getPid,dto.getPid())
                .eq(NetdiskDirectoryPO::getSpaceId,dto.getSpaceId())
                .eq(NetdiskDirectoryPO::getName,dto.getName())
        )){
            if(NetdiskDirectoryENUM.FILE == dto.getType()){//如果是文件,则不报重复,直接忽略即可,因为文件本身就会覆盖
                return new ResponseHeadDTO<>(true, "文件名重复,文件已覆盖");
            }else {
                return new ResponseHeadDTO<>(false, "添加失败,同级下目录名已存在");
            }
        }
        if(dto.getPid() != 0) {
            NetdiskDirectoryPO ppo = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                    .eq(NetdiskDirectoryPO::getSpaceId, dto.getSpaceId())
                    .eq(NetdiskDirectoryPO::getId, dto.getPid())
            );
            if (ppo == null) {
                return new ResponseHeadDTO<>(false, "添加失败,父目录不存在");
            }
            if (NetdiskDirectoryENUM.FILE.getCode().equals(ppo.getType())) {
                return new ResponseHeadDTO<>(false, "添加失败,父目录不是文件夹");
            }
        }
        NetdiskDirectoryPO po = new NetdiskDirectoryPO();
        BeanUtils.copyProperties(dto,po);
        po.setType(dto.getType().getCode());
        netdiskDirectoryMapper.insert(po);
        dto.setId(po.getId());
        return new ResponseHeadDTO<>(true,dto,"添加成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO<Integer> delDirectory(Long spaceId, Long dirId) {
        NetdiskDirectoryPO po = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
                .eq(NetdiskDirectoryPO::getId,dirId)
        );
        if(po == null){
            return new ResponseHeadDTO<>(false,"删除失败,没有这个目录");
        }
        if(NetdiskDirectoryENUM.FOLDER.getCode().equals(po.getType())){//是文件夹,先递归删除掉下面所有的内容
            List<NetdiskDirectoryPO> list = netdiskDirectoryMapper.selectList(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                    .eq(NetdiskDirectoryPO::getPid,po.getId())
                    .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
            );
            list.forEach(v->{
                delDirectory(v.getSpaceId(),v.getId());
            });
        }else{//是文件,通知文件删除
            fileAdministration.delFile(po.getFileMd5(),spaceId, FileSourceENUM.CLOUDDISK);
        }
        //删除自己
        int i = netdiskDirectoryMapper.deleteById(po.getId());
        return new ResponseHeadDTO<>(true,i,"删除成功");
    }

    @Override
    @Transactional
    public ResponseHeadDTO<NetdiskDirectoryDTO> moveDirectory(Long spaceId, Long dirId, Long targetDirId) {
        if(dirId.longValue() == targetDirId.longValue()){
            return new ResponseHeadDTO<>(false,"移动失败,不能移动到自己目录下");
        }
        NetdiskDirectoryPO po = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
                .eq(NetdiskDirectoryPO::getId,dirId)
        );
        if(po == null){
            return new ResponseHeadDTO<>(false,"移动失败,没有这个目录");
        }
        NetdiskDirectoryPO ppo = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getSpaceId,spaceId)
                .eq(NetdiskDirectoryPO::getId,targetDirId)
        );
        if(ppo == null && targetDirId.longValue() != 0){
            return new ResponseHeadDTO<>(false,"移动失败,目标目录不存在");
        }
        if(targetDirId.longValue() != 0 && NetdiskDirectoryENUM.FILE.getCode().equals(ppo.getType())){
            return new ResponseHeadDTO<>(false,"移动失败,目标目录不是文件夹");
        }
        //一直递归查找目标id的父节id,直至查不到数据位置,看中间有没有出现过父id是dirId,如果出现则目标节点是dirId的子节点
        long checkPid = targetDirId;
        while (checkPid != 0){
            NetdiskDirectoryPO pidPo = netdiskDirectoryMapper.selectOne(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                    .eq(NetdiskDirectoryPO::getId,checkPid)
                    .select(NetdiskDirectoryPO::getPid)
            );
            if(pidPo.getPid() == dirId.longValue()){
                return new ResponseHeadDTO<>(false,"移动失败,目标目录是被移动目录的子目录");
            }
            checkPid = pidPo.getPid();
        }
        //检查同级别下文件名是否已存在
        if(netdiskDirectoryMapper.exists(new LambdaQueryWrapper<NetdiskDirectoryPO>()
                .eq(NetdiskDirectoryPO::getPid,targetDirId)
                .eq(NetdiskDirectoryPO::getSpaceId,po.getSpaceId())
                .eq(NetdiskDirectoryPO::getName,po.getName())
        )){
            return new ResponseHeadDTO<>(false,"移动失败,目标目录下已存在被移动的目录名");
        }
        po.setPid(targetDirId);
        po.setUpdateTime(null);
        netdiskDirectoryMapper.updateById(po);
        NetdiskDirectoryDTO dto = new NetdiskDirectoryDTO();
        BeanUtils.copyProperties(po,dto);
        return new ResponseHeadDTO<>(true,dto,"移动成功");
    }

    @Override
    public ResponseHeadDTO<NetdiskDirectoryDTO> getDirectoryById(long id) {
        NetdiskDirectoryPO po = netdiskDirectoryMapper.selectById(id);
        if(po == null){
            return new ResponseHeadDTO<>(false,"目录不存在");
        }
        NetdiskDirectoryDTO dto = new NetdiskDirectoryDTO();
        BeanUtils.copyProperties(po,dto);
        dto.setType(NetdiskDirectoryENUM.valueOf(po.getType()));
        return new ResponseHeadDTO(dto);
    }
}
