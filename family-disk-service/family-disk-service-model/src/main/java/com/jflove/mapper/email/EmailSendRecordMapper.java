package com.jflove.mapper.email;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jflove.po.email.EmailSendRecordPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author tanjun
 * @date 2022/12/9 15:04
 * @describe 邮件发送记录
 */
@Mapper
public interface EmailSendRecordMapper extends BaseMapper<EmailSendRecordPO> {
}
