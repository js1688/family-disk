<template>

  <van-overlay :show="isOverlay" :z-index="9999">
    <div class="wrapper">
      <van-loading />
    </div>
  </van-overlay>

  <van-cell center icon="wap-home-o" >
    <a v-for="(item) in openPath" @click="jump(item)" >{{item.name}}</a>
  </van-cell>

  <van-search
      v-model="keyword"
      show-action
      placeholder="请输入搜索关键词"
  >
    <template #action>
      <div @click="onLoad">搜索</div>
    </template>
  </van-search>

  <van-list
      id="list"
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
      :style="`height:${maxHeight}px;overflow:auto;`"
  >
    <van-swipe-cell v-for="item in list">
      <van-cell is-link arrow-direction="right"
                :title="item.name"
                @click="open(item)">
        <van-tag plain type="primary">{{item.type == 'FOLDER' ? '目录':'文件'}}</van-tag>
      </van-cell>
      <template #right>
        <van-button v-if="!roleWrite" square hairline type="danger"  @click="delDirectory(item)" text="删除" />
        <van-button v-if="!roleWrite" square hairline type="primary"  @click="moveDirectory(item)" text="移动" />
        <van-button v-if="!roleWrite" square hairline type="primary"   @click="updateName(item)" text="重命名" />
        <van-button v-if="item.type == 'FILE'" square hairline type="success"  @click="download(item)" text="下载" />
        <van-button v-if="!roleWrite" square hairline type="success"  @click="share(item)" text="分享" />
      </template>
    </van-swipe-cell>

    <van-back-top ight="15vw" bottom="10vh" target="#list"/>
  </van-list>

  <div style="position: fixed;right: 25px;bottom: 200px;">
    <van-popover placement="left" v-model:show="showPopover" :actions="addActions" @select="addSelect">
      <template #reference>
        <van-button icon="plus" type="primary"/>
      </template>
    </van-popover>
  </div>

  <van-action-sheet v-model:show="showAddDirectory" title="添加目录">
    <div>
      <van-form @submit="addDirectory">
        <van-cell-group inset>
          <van-field
              required
              v-model="directoryName"
              name="目录名称"
              label="目录名称"
              placeholder="请输入目录名称"
              :rules="[{ required: true, message: '请输入目录名称' }]"
          />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-action-sheet v-model:show="showMove" title="请进入移动的目的地">
    <div>
      <van-list
          v-model:loading="moveLoading"
          :finished="moveFinished"
          finished-text="没有更多了"
          @load="onMoveLoad"
      >
        <van-swipe-cell v-for="item in moveList">
          <van-cell is-link
                    :title="item.name"
                    @click="moveOpen(item)">
          </van-cell>
        </van-swipe-cell>
      </van-list>
      <div style="margin: 16px;">
        <van-button round block type="primary" @click="moveDirectory2" native-type="submit">
          确定移动到此目录
        </van-button>
      </div>
    </div>
  </van-action-sheet>

  <van-action-sheet v-model:show="showUpdateName" title="重命名">
    <div>
      <van-form @submit="updateName2">
        <van-cell-group inset>
          <van-field
              required
              v-model="directoryName"
              name="目录名称"
              label="目录名称"
              placeholder="请输入新的目录名称"
              :rules="[{ required: true, message: '请输入新的目录名称' }]"
          />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-action-sheet
      v-model:show="showShare"
      title="创建分享链接">
    <div>
      <van-form @submit="createShare">
        <van-cell-group inset>
          <van-field v-model="shareParam.password"
                     type="password"
                     label="解锁密码" placeholder="请输入解锁密码" />
          <van-field label="失效日期" required readonly
                     v-model="shareParam.invalidTime"
                     placeholder="点击选择日期"
                     :rules="[{ required: true, message: '请选择失效日期' }]"
                     @click="showShareDate = true"
          />
          <van-field v-model="shareParam.url" readonly label="分享地址" >
            <template #button>
              <van-button
                  size="small"
                  type="primary"
                  @click="doCopy"
                  :disabled="shareParam.url == null || shareParam.url == ''"
              >复制</van-button>
            </template>
          </van-field>
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交申请
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-action-sheet v-model:show="showLargeUpload" title="文件上传">
    <div>
      <div style="margin: 16px;">
        <van-uploader accept="*" :after-read="largeUpload"  multiple>
          <van-button style="margin-left: 15px;" icon="plus" size="small" block type="default" />
        </van-uploader>
        <van-cell v-for="value in largeFileUploadList" :title="value.file.name.length > 10 ? value.file.name.substring(0,10) : value.file.name">
          <div style="padding-top: 10px;">
            <van-progress :percentage="value.scale" />
          </div>
          <van-button style="margin-top: 10px;" @click="stopFileUpload(value.md5)" type="default" size="mini">取消</van-button>
        </van-cell>
      </div>
    </div>
  </van-action-sheet>

  <van-action-sheet v-model:show="showDownloadList" title="下载列表">
    <div>
      <div style="margin: 16px;">
        <van-cell v-for="value in downloadPlan" :title="value.data.name.length > 10 ? value.data.name.substring(0,10) : value.data.name">
          <div style="padding-top: 10px;">
            <van-progress :percentage="value.data.scale" />
          </div>
          <van-button style="margin-top: 10px;" @click="stopFileDownload(value.data.fileMd5)" type="default" size="mini">取消</van-button>
        </van-cell>
      </div>
    </div>
  </van-action-sheet>

  <van-action-sheet v-model:show="showOfflineList" title="离线下载">
    <div>
      <van-form @submit="addOffline">
        <van-cell-group inset>
          <van-field
              required
              v-model="offlineParam.uri"
              rows="5"
              label="地址:"
              type="textarea"
              :rules="[{ required: true, message: '请输入下载地址' }]"
              placeholder="请输入下载地址"
          />
          <van-field label="地址类型:" required readonly
                     v-model="offlineParam.uriTypeName"
                     placeholder="点击选择地址类型"
                     :rules="[{ required: true, message: '请选择地址类型' }]"
                     @click="showUriType = true"
          />
          <van-field label="存放目录:" required readonly
                     v-model="offlineParam.targetName"
                     placeholder="点击选择目录"
                     :rules="[{ required: true, message: '请选择存放目录' }]"
                     @click="showOfflineTarget = true"
          />
          <van-picker
              title="地址类型"
              :columns="[{text:'简单(HTTP/FTP/SFTP/BitTorrent)',value:'aria2cSimple'}]"
              @confirm="(v)=>{
                offlineParam.uriTypeName = v.selectedOptions[0].text;
                offlineParam.uriType = v.selectedOptions[0].value;
                showUriType = false;
                return false;
              }"
              v-if="showUriType"
              @cancel="showUriType = false"
          />
          <van-picker
              title="存放目录"
              :columns="targetSelectd"
              @confirm="(v)=>{
                offlineParam.targetName = v.selectedOptions[0].text;
                offlineParam.targetId = v.selectedOptions[0].value;
                showOfflineTarget = false;
                return false;
              }"
              v-if="showOfflineTarget"
              @cancel="showOfflineTarget = false"
          />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>

      <div style="margin: 16px;">
        <van-cell v-for="value in offlineData" :title="value.fileName.length > 10 ? value.fileName.substring(0,10) : value.fileName">
          <div style="padding-top: 10px;">
            <van-tag plain type="primary">进度:{{value.progress}},状态:{{value.statusName}}</van-tag>
          </div>
          <van-button v-if="value.status != 'removed'" style="margin-top: 10px;" @click="delOffline(value)" type="danger" size="mini">删除</van-button>
          <van-button v-if="value.status == 'active'" style="margin-top: 10px;" @click="offlineSwitch(value)" type="default" size="mini">暂停</van-button>
          <van-button v-if="value.status == 'paused' || value.status == 'waiting' || value.status == 'error'" style="margin-top: 10px;" @click="offlineSwitch(value)" type="default" size="mini">开始</van-button>
        </van-cell>
      </div>
    </div>
  </van-action-sheet>

  <van-calendar v-model:show="showShareDate" :show-confirm="false" @confirm="onConfirmShare" :max-date="maxDate"/>

  <van-dialog
      v-model:show="showPreviewVideo"
      :show-confirm-button="false"
      @close="closeVideo"
      @opened="$refs.previewVideoRef.src=videoUrl"
      :show-cancel-button="true">

    <video ref="previewVideoRef" style="height: 100%;width: 100%" controls autoplay />
  </van-dialog>
</template>

<script>
import {
  Search,
  List,
  Cell,
  Tag,
  NavBar,
  showToast,
  BackTop,
  Popover,
  Button,
  ActionSheet,
  Field,
  Form,
  CellGroup,
  Progress,
  showImagePreview,
  NumberKeyboard,
  Dialog, Calendar,Picker
} from 'vant';
import { Overlay,Loading,Collapse,CollapseItem,ImagePreview} from 'vant';
import {createApp, ref, shallowRef} from "vue";
import axios from "axios";
import { SwipeCell,Uploader } from 'vant';
import { showConfirmDialog } from 'vant';
import gws from "@/global/WebSocket";
import {isToken, key} from "@/global/KeyGlobal";
import {CountFileSliceInfo, FileMd5, FormatDate, HeicToCommon, LivpToCommon} from '@/global/StandaloneTools';
import 'video.js/dist/video-js.css';
import videojs from "video.js";
import { saveAs } from 'file-saver';
import '@vant/touch-emulator';
import {FileDoownloadSmall, FileDownload, FileSoundOut, getDownloadList, StopFileDownload} from "@/global/FileDownload";
import {FileUpload, getUploadList, StopFileUpload} from "@/global/FileUpload";
//vant适配桌面端


export default {
  name: "Disk",
  components: {
    [Calendar.name]:Calendar,
    [Search.name]: Search,
    [List.name]: List,
    [Cell.name]: Cell,
    [Tag.name]: Tag,
    [NavBar.name]: NavBar,
    [BackTop.name]: BackTop,
    [Popover.name]: Popover,
    [Button.name]: Button,
    [ActionSheet.name]: ActionSheet,
    [Field.name]: Field,
    [Form.name]: Form,
    [CellGroup.name]: CellGroup,
    [Overlay.name]: Overlay,
    [Loading.name]: Loading,
    [SwipeCell.name]:SwipeCell,
    [Uploader.name]: Uploader,
    [Collapse.name]: Collapse,
    [CollapseItem.name]: CollapseItem,
    [ImagePreview.name]:ImagePreview,
    [Progress.name]: Progress,
    [NumberKeyboard.name]:NumberKeyboard,
    [Dialog.name]:Dialog,
    [Picker.name]:Picker
  },
  setup() {
    let roleWrite = localStorage.getItem(key().useSpaceRole) != 'WRITE';
    const addActions = [
      { text: '新建目录', icon: 'wap-nav',name:'addDirectory'},
      { text: '文件上传', icon: 'arrow-up',name:'addLargeFile'},
      { text: '离线下载', icon: 'down',name:'offlineList'},
      { text: '下载列表', icon: 'arrow-down',name:'downloadList'}
    ];
    //如果没有写入权限就删掉文件上传目录
    if(roleWrite){
      addActions.splice(0,3);
    }
    const showPopover = ref(false);
    return {
      roleWrite:roleWrite,
      maxDate: new Date(new Date().getFullYear() + 1, 12, 31),
      maxHeight:document.documentElement.clientHeight - 160,
      fileDownloadUrl:'/stream/slice/getFile',
      addActions,
      showPopover
    };
  },
  data: function (){
    return {
      showShareDate:false,
      showShare:false,
      shareParam:{password:null,bodyId:null,invalidTime:null,url:null},
      keyword:"",
      directoryName:"",
      pid:0,
      id:0,
      loading:false,
      finished:false,
      moveLoading:false,
      moveFinished:false,
      showOfflineTarget:false,
      showUriType:false,
      movePid:0,
      moveId:0,
      moveList:[],
      downloadPlan:[],
      showAddDirectory:false,
      isOverlay: false,
      showMove: false,
      list:[],
      showUpdateName:false,
      showLargeUpload:false,
      showDownloadList:false,
      showOfflineList:false,
      openPath:[
          {name:'目录',id:0}
      ],
      showPreviewVideo:false,
      videoUrl:'',
      largeFileUploadList:[],
      offlineParam:{uriType:'aria2cSimple',uri:null,targetId:0,targetName:'根目录',uriTypeName:'简单(HTTP/FTP/SFTP/BitTorrent)'},
      offlineData:[],
      targetSelectd:[]
    }
  },
  created() {
    if(localStorage.getItem(key().authorization) != null){
      //添加定时任务,刷新下载列表
      let self = this;
      setInterval(function (){
        //更新下载列表进度
        let list = getDownloadList();
        self.downloadPlan = [];
        for (const listKey in list) {
          let v = list[listKey];
          v.data['scale'] = v.data.progress == 0 ? 0 : Math.floor(v.data.progress/v.data.sliceNum*100);
          self.downloadPlan.push(v);
        }
        //更新上传列表进度
        let uplist = getUploadList();
        self.largeFileUploadList = [];
        for (const uplistKey in uplist) {
          let v = uplist[uplistKey];
          v['scale'] = v.progress == 0 ? 0 : Math.floor(v.progress/v.sliceInfo.sliceNum*100);
          if(!v.result){
            showToast(v.resultMsg);
            self.stopFileUpload(v.md5);
          }else{
            self.largeFileUploadList.push(v);
          }
        }
      }, 500);
      setInterval(function (){
        //更新离线下载进度
        self.onOfflineLoad();
      },2000);

    }
  },
  methods:{
    //删除离线下载任务
    delOffline:function (item) {
      let self = this;
      showConfirmDialog({
        title: '删除',
        message:'是否删除下载任务:' + item.fileName + '!'
      })
          .then(() => {
            self.isOverlay = true;
            axios.post('/download/remove', {
              gid: item.gid
            }).then(function (response) {
              if(response.data.result){
                self.onOfflineLoad();
              }
              showToast(response.data.message);
              self.isOverlay = false;
            }).catch(function (error) {
              self.isOverlay = false;
              console.log(error);
            });
          }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //离线下载任务开始暂停
    offlineSwitch:function (item){
      let self = this;
      self.isOverlay = true;
      if(item.status == 'paused' || item.status == 'waiting' || item.status == 'error'){
        axios.post('/download/unpause', {
          gid: item.gid
        }).then(function (response) {
          if(response.data.result){
            self.onOfflineLoad();
          }
          showToast(response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }else{
        axios.post('/download/pause', {
          gid: item.gid
        }).then(function (response) {
          if(response.data.result){
            self.onOfflineLoad();
          }
          showToast(response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }
    },
    //离线下载
    addOffline:function () {
      let self = this;
      self.isOverlay = true;
      axios.post('/download/add', self.offlineParam).then(function (response) {
        if(response.data.result){
          self.offlineParam = {uriType:'aria2cSimple',uri:null,targetId:0,targetName:'根目录',uriTypeName:'简单(HTTP/FTP/SFTP/BitTorrent)'};
          self.showAddOffline = false;
          self.onOfflineLoad();
        }
        showToast(response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //离线下载列表
    onOfflineLoad: function () {
      let self = this;
      axios.post('/download/getFiles', {
        fileName: ''
      }).then(function (response) {
        if(response.data.result){
          self.offlineData = response.data.datas;
        }else{
          showToast(response.data.message);
        }
      }).catch(function (error) {
        console.log(error);
      });
    },
    //取消上传
    stopFileUpload:function (md5) {
      StopFileUpload(md5).then(function (sf){
        showToast(sf.msg);
      });
    },
    //取消下载
    stopFileDownload:function (md5) {
      StopFileDownload(md5).then(function (sf){
        showToast(sf.msg);
      });
    },
    //拷贝地址
    doCopy:function (){
      let self = this;
      this.$copyText(this.shareParam.url).then(function (e) {
        showToast('分享地址已复制,请粘贴给有需要的人');
        self.showShare = false;
      }, function (e) {
        showToast('复制失败,请手动复制地址');
      })
    },
    //日期选择回调
    onConfirmShare:function (value){
      this.showShareDate = false;
      this.shareParam.invalidTime = FormatDate(value);
    },
    //创建分享
    createShare:function () {
      this.isOverlay = true;
      let self = this;
      this.shareParam.bodyType = 'NETDISK';
      axios.post('/share/admin/create', this.shareParam).then(function (response) {
        if(response.data.result){
          self.shareParam.url = window.location.protocol + '//' + window.location.host + window.location.pathname +'#/share/netdisk/?' + response.data.data.url;
          self.doCopy();
        }else{
          showToast(response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //打开分享面板
    share:function (item) {
      this.shareParam = {password:null,bodyId:item.id,invalidTime:FormatDate(new Date()),url:null}
      this.showShare = true;
    },
    //关闭预览视频播放器
    closeVideo:function () {
      //停止播放
      if(this.$refs.previewVideoRef){
        this.$refs.previewVideoRef.pause();
        this.$refs.previewVideoRef.src = "";
      }
      this.showPreviewVideo = false;
    },
    //大文件上传
    largeUpload:async function (fs){
      if(!fs.length){//只选择了一个文件
        fs = [fs];
      }
      //判断文件是否重复添加
      for (let i = 0; i < this.largeFileUploadList.length; i++) {
        for (let i = 0; i < fs.length; i++) {
          let file = fs[i].file;
          if (file.name == this.largeFileUploadList[i].name) {
            fs.splice(i, 1);//删除掉添加的内容
            showToast(`文件名:[${file.name}]正在上传,请勿重复上传`);
            break;
          }
        }
      }
      //开始上传文件
      let self = this;
      for (let i = 0; i < fs.length; i++) {
        let last = fs[i];
        //计算分片大小
        let sliceInfo = CountFileSliceInfo(last.file);
        //计算md5值
        let md5 = await FileMd5(last.file,sliceInfo.sliceSize,sliceInfo.sliceNum);
        let ret = await FileUpload(sliceInfo,last.file,md5,'CLOUDDISK',self.pid,async function (q) {
          if(q.result){
            //将文件与网盘目录建立关系
            let response = await axios.post('/netdisk/addDirectory', {
              name: q.file.name,
              pid: q.pid,
              fileMd5: q.md5,
              type:"FILE",
              mediaType:q.file.type
            });
            if(response.data.result && response.data.data){
              self.onLoad();
            }
            showToast(response.data.message);
          }else{
            showToast(q.resultMsg);
          }
        });
        showToast(ret.msg);
      }
    },
    //打开苹果设备拍摄的实况图片
    openLivp:async function (item) {
      this.isOverlay = true;
      let fso = await FileSoundOut(this.fileDownloadUrl,item.fileMd5,item.name,'CLOUDDISK');
      if(!fso.state){
        this.isOverlay = false;
        showToast(fso.msg);
        return;
      }
      //试探成功,开始下载
      let fd = await FileDoownloadSmall(fso);
      if(fd.state){
        let blob = new Blob([fd.bytes]);
        try{
          blob = await LivpToCommon(blob);
        }catch (e) {}
        let url = window.URL.createObjectURL(blob);
        showImagePreview({
          images: [url],
          closeable: true,
          showIndex: false
        });
      }else{
        showToast(fd.msg);
      }
      this.isOverlay = false;
    },
    //打开图片
    openImage:async function (item){
      this.isOverlay = true;
      let fso = await FileSoundOut(this.fileDownloadUrl,item.fileMd5,item.name,'CLOUDDISK');
      if(!fso.state){
        this.isOverlay = false;
        showToast(fso.msg);
        return;
      }
      //试探成功,开始下载
      let fd = await FileDoownloadSmall(fso);
      if(fd.state){
        let mediaType = item.mediaType.toUpperCase();
        let mediaTypes = mediaType.split("/");
        let blob = new Blob([fd.bytes],{type:item.mediaType});
        switch (mediaTypes[1]){
          case "HEIC": //苹果设备拍摄的 新图片格式
            try {
              //有时候这个类型也不一定是准确的,如果本身就可以被读取,就会拒绝转换,这里如果报错后使用原始blob
              blob = await HeicToCommon(blob);
            }catch (e) {}
            break;
        }
        let url = window.URL.createObjectURL(blob);
        showImagePreview({
          images: [url],
          closeable: true,
          showIndex: false
        });
      }else{
        showToast(fd.msg);
      }
      this.isOverlay = false;
    },
    //打开视频,视频流通常会很大,所以需要做到边播边缓存
    openVideo:async function (item){
      //试探文件
      let fso = await FileSoundOut(this.fileDownloadUrl,item.fileMd5,item.name,'CLOUDDISK');
      if(!fso.state){
        showToast(fso.msg);
        return;
      }
      let url = key().baseURL+"stream/media/play/CLOUDDISK/"+localStorage.getItem(key().authorization)  + "/" + item.fileMd5;
      this.videoUrl = url;
      this.showPreviewVideo = true;
    },
    //打开文件
    openFile:function (item) {
      let mediaType = item.mediaType.toUpperCase();
      let mediaTypes = mediaType.split("/");
      switch (mediaTypes[0]) {
        case "IMAGE"://图片
          this.openImage(item);
          break
        case "VIDEO"://视频
        case "AUDIO"://音频
          this.openVideo(item);
          break
        default:
          //兼容特殊文件的打开
          let index = item.name.lastIndexOf(".");
          if(index != -1){
            let suffix = item.name.substring(index).toUpperCase();
            switch (suffix) {
              case ".LIVP"://livp,苹果设备拍摄的实况图片
                this.openLivp(item);
                return;
            }
          }
          showToast("不识别的类型,不能在线预览,请下载文件.");
          break
      }
    },
    //下载文件
    download:async function (item) {
      let fso = await FileSoundOut(this.fileDownloadUrl,item.fileMd5,item.name,'CLOUDDISK');
      if(!fso.state){
        showToast(fso.msg);
        return;
      }
      //试探成功,开始下载
      let fd = await FileDownload(fso);
      if(fd){
        showToast(fd.msg);
      }
    },
    //修改名称
    updateName2:function(){
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/updateName', {
        name: this.directoryName,
        id:this.id
      }).then(function (response) {
        if(response.data.result){
          self.showUpdateName = false;
          self.onLoad();
        }
        showToast(response.data.message);
        self.isOverlay = false;
        self.directoryName = "";
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    updateName: function (item) {
      this.showUpdateName = true;
      this.id = item.id;
    },
    //确定移动
    moveDirectory2: function (){
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/moveDirectory', {
        targetDirId: this.movePid,
        id:this.moveId
      }).then(function (response) {
        if(response.data.result){
          self.showMove = false;
          self.onLoad();
        }
        showToast(response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //加载移动目录
    onMoveLoad: function () {
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/findDirectory', {
        pid: this.movePid,
        type: 'FOLDER'
      }).then(function (response) {
        if(response.data.result){
          self.moveList = response.data.datas;
        }else{
          showToast(response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
      this.moveLoading = false;//加载完毕
      this.moveFinished = true;//全部数据加载完毕
    },
    //移动目录打开
    moveOpen:function (item){
      this.movePid = item.id;
      this.onMoveLoad();
    },
    moveDirectory: function(item){
      this.showMove = true;
      this.movePid = 0;
      this.moveId = item.id;
      this.onMoveLoad();
    },
    //删除目录
    delDirectory:function(item){
      showConfirmDialog({
        title: '删除',
        message:'是否删除目录:' + item.name + ',删除后不可恢复!'
      })
          .then(() => {
            this.isOverlay = true;
            let self = this;
            axios.post('/netdisk/delDirectory', {
              id: item.id
            }).then(function (response) {
              if(response.data.result){
                for (let i = 0; i < self.list.length; i++) {
                  if(self.list[i].id == item.id){
                    self.list.splice(i,1);
                    break;
                  }
                }
              }
              showToast(response.data.message);
              self.isOverlay = false;
          })
          .catch(() => {
            // on cancel
          });
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //打开目录
    open:function(item){
      this.keyword = "";//只要进入了目录或文件,都清除掉搜索条件
      if(item.type == 'FOLDER'){//文件夹
        this.pid = item.id;
        this.onLoad();
        //将打开的目录追加到路径中
        this.openPath.push({name:"/" + item.name,id:item.id});
      }else{//文件
        this.openFile(item);
      }
    },
    //添加目录
    addDirectory:function (){
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/addDirectory', {
        name: this.directoryName,
        pid: this.pid,
        type:"FOLDER"
      }).then(function (response) {
        if(response.data.result){
          self.list.push(response.data.data);
        }
        self.directoryName = "";
        showToast(response.data.message);
        self.isOverlay = false;
        self.showAddDirectory = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    addSelect: function (item){
      let cz = item.name;
      switch (cz) {
        case 'addDirectory':
          this.showAddDirectory = true;
          break;
        case 'downloadList':
          this.showDownloadList = true;
          break;
        case 'addLargeFile':
          this.showLargeUpload = true;
          break;
        case 'offlineList':
          let self = this;
          //获取一级目录,可设置的目的地目录的列表
          axios.post('/netdisk/findDirectory', {
            keyword: '',
            pid: 0,
            type:'FOLDER'
          }).then(function (response) {
            self.targetSelectd = [
              {
                text: '根目录',
                value: 0
              }
            ];

            if(response.data.result){
              for (let i = 0; i < response.data.datas.length; i++) {
                self.targetSelectd.push({text: response.data.datas[i].name,value: response.data.datas[i].id});
              }
            }

          }).catch(function (error) {
            console.log(error);
          });
          this.showOfflineList = true;
          break;

      }
    },
    jump: function (item){
      this.pid = item.id;
      this.onLoad();
      //只保留选择节点之前的路径
      let lj = [];
      for (let i = 0; i < this.openPath.length; i++) {
        lj.push(this.openPath[i]);
        if(this.openPath[i].id == item.id){
          break;
        }
      }
      this.openPath = lj;
    },
    onLoad: function () {
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/findDirectory', {
        keyword: this.keyword,
        pid: this.pid
      }).then(function (response) {
        if(response.data.result){
          self.list = response.data.datas;
        }else{
          showToast(response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
      this.loading = false;//加载完毕
      this.finished = true;//全部数据加载完毕
    }
  }
}
</script>

<style scoped>
  .wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }
</style>