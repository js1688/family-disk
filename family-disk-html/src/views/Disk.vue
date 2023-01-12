<template>

  <van-overlay :show="isOverlay">
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
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <van-swipe-cell v-for="item in list">
      <van-cell is-link arrow-direction="right"
                :title="item.name"
                @click="open(item)">
        <van-tag plain type="primary">{{item.type == 'FOLDER' ? '目录':'文件'}}</van-tag>
      </van-cell>
      <template #right>
        <van-button square hairline type="danger"  @click="delDirectory(item)" text="删除" />
        <van-button square hairline type="primary"  @click="moveDirectory(item)" text="移动" />
        <van-button square hairline type="primary"   @click="updateName(item)" text="重命名" />
        <van-button :disabled="item.type != 'FILE'" square hairline type="success"  @click="download(item)" text="下载" />
      </template>
    </van-swipe-cell>
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

  <van-action-sheet v-model:show="showUpload" title="上传文件">
    <div>
      <div style="margin: 16px;">
        <van-uploader :max-size="1024 * 1024 * 32" @oversize="onOversize" :max-count="10" :before-read="beforeRead" :disabled="uploadDisabled" accept="*" v-model="uploadFiles" multiple>
          <van-button block hairline icon="plus" type="default">选择文件</van-button>
        </van-uploader>
      </div>
      <div style="margin: 16px;">
        <van-button round block :disabled="uploadDisabled" type="primary" @click="submitUpload" native-type="submit">
          开始上传
        </van-button>
      </div>
    </div>
  </van-action-sheet>

  <van-action-sheet @opened="playVideo" @close="pauseVideo" title="媒体播放" round v-model:show="showVideo">
    <div style="padding-bottom: 20px" id="videoBody">
    </div>
  </van-action-sheet>
  <van-action-sheet v-model:show="showLargeUpload" title="大文件上传">
    <div>
      <div style="margin: 16px;">
        <van-uploader accept="*" :after-read="largeUpload">
          <van-button style="margin-left: 15px;" icon="plus" size="small" block type="default" />
        </van-uploader>
        <van-cell v-for="item in largeFileUploadList" :title="item.fileName">
          <div style="padding-top: 10px;">
            <van-progress :percentage="item.progress" />
          </div>
          <van-button style="margin-top: 10px;" @click="largeDel(item)" v-if="item.del" type="default" size="mini">删除</van-button>
        </van-cell>
      </div>
    </div>
  </van-action-sheet>

  <van-dialog :show-confirm-button="false" :show-cancel-button="false" v-model:show="showDownloadProgress" :title="downloadFileName">
    <div style="height: 30px;margin: 10px;">
      <van-progress :percentage="downloadFileProgress" />
    </div>
  </van-dialog>

  <van-back-top ight="15vw" bottom="10vh" />
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
  Dialog
} from 'vant';
import { Overlay,Loading,Collapse,CollapseItem,ImagePreview} from 'vant';
import {createApp, ref, shallowRef} from "vue";
import axios from "axios";
import { SwipeCell,Uploader } from 'vant';
import { showConfirmDialog } from 'vant';
import gws from "@/global/WebSocket";
import {isSpace, isToken, key,fileMd5} from "@/global/KeyGlobal";
import 'video.js/dist/video-js.css';
import videojs from "video.js";
import { saveAs } from 'file-saver';
import '@vant/touch-emulator';//vant适配桌面端


export default {
  name: "Disk",
  components: {
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
    [Dialog.name]:Dialog
  },
  setup() {
    const addActions = [
      { text: '新建目录', icon: 'wap-nav',name:'addDirectory'},
      { text: '上传文件', icon: 'upgrade',name:'addFile'},
      { text: '大件上传', icon: 'upgrade',name:'addLargeFile'},
      { text: '拍照上传', icon: 'photograph',name:'photograph'}
    ];
    const showPopover = ref(false);

    const onOversize = (f) => {
      showToast('此文件['+f.file.name+']太大,请走大件上传通道');
    };
    const activeNames = ref([]);

    return {
      activeNames,
      addActions,
      showPopover,
      onOversize
    };
  },
  data: function (){
    return {
      keyword:"",
      directoryName:"",
      pid:0,
      id:0,
      loading:false,
      finished:false,
      moveLoading:false,
      moveFinished:false,
      movePid:0,
      moveId:0,
      moveList:[],
      showAddDirectory:false,
      isOverlay: false,
      showMove: false,
      uploadDisabled:false,
      list:[],
      showUpdateName:false,
      showLargeUpload:false,
      showUpload:false,
      uploadFiles:[],
      showDownloadProgress:false,
      downloadFileName:"",
      downloadFileProgress:0,
      downloadFileSliceNum:0,
      openPath:[
          {name:'目录',id:0}
      ],
      isSubscribe:false,
      showVideo:false,
      myPlayer:null,
      largeFileUploadList:[],
      largeDownloadTemporaryStorage:null,
      videoOptions:{
        controls: true,
        playbackRates: [0.5, 1.0, 1.5, 2.0], //播放速度
        autoplay: true, //如果true,浏览器准备好时开始回放。
        muted: false, // 默认情况下将会消除任何音频。
        loop: false, // 导致视频一结束就重新开始。
        preload: "auto", // 建议浏览器在<video>加载元素后是否应该开始下载视频数据。auto浏览器选择最佳行为,立即开始加载视频（如果浏览器支持）
        language: "zh-CN",
        aspectRatio: "16:9", // 将播放器置于流畅模式，并在计算播放器的动态大小时使用该值。值应该代表一个比例 - 用冒号分隔的两个数字（例如"16:9"或"4:3"）
        fluid: true, // 当true时，Video.js player将拥有流体大小。换句话说，它将按比例缩放以适应其容器。
        flash:{hls:{withCredentials:true}},//可以播放rtmp视频
        html5:{hls:{withCredentials:true}},//可以播放m3u8视频
        //poster: "@/assets/camera.png", //你的封面地址
        width: document.documentElement.clientWidth,
        height: document.documentElement.clientHeight,
        notSupportedMessage: "此视频暂无法播放，请稍后再试", //允许覆盖Video.js无法播放媒体源时显示的默认信息。
        controlBar: {
          timeDivider: true,// 当前时间和持续时间的分隔符
          durationDisplay: true,// 显示持续时间
          remainingTimeDisplay: true,// 是否显示剩余时间功能
          fullscreenToggle: true, //全屏按钮
        },
        sources:[]
      }
    }
  },
  methods:{
    //分片上传
    sliceUpload:function (fileMd5,totalLength,start,end,file,chunk,i,sliceNum,callback){
      let data = new FormData();
      data.append('originalFileName', file.name);
      data.append('f', chunk,fileMd5 + "-" + i);
      data.append('s', 'CLOUDDISK');
      data.append('m', file.type);
      data.append('n', sliceNum);
      data.append('start', start);
      data.append('end', end);
      data.append('fileMd5', fileMd5);
      data.append('totalLength', totalLength);

      axios.post("/file/slice/addFile", data, {
        header:{
          'Content-Type': 'multipart/form-data'
        }
      }).then((res) => {
        if(!res.data.result && res.data.data == 'retry'){//分片失败,服务端主动要求重试
          this.sliceUpload(fileMd5,totalLength,start,end,file,chunk,i,sliceNum,callback);
        }else if(res.data.result && res.data.data){
          callback(res.data);//执行回调
        }else if(!res.data.result){
          showToast(res.data.message);
        }
      }).catch((error) => {
        console.log(error);
        this.sliceUpload(fileMd5,totalLength,start,end,file,chunk,i,sliceNum,callback);//重试
      });
    },
    //大文件上传增加进度条
    incrProgress:function (md5,add){
      for (let i = 0; i < this.largeFileUploadList.length; i++) {
        if(this.largeFileUploadList[i].fileMd5 == md5){
          if(add == -1){
            this.largeFileUploadList.splice(i,1);
          }else if(add == 100 || add == 0){
            this.largeFileUploadList[i].progress = add;
          }else{
            this.largeFileUploadList[i].progress = this.largeFileUploadList[i].progress + add;
          }
          return this.largeFileUploadList[i];
        }
      }
    },
    //
    largeDel: function (item){
      this.incrProgress(item.fileMd5,-1);
    },
    //大文件上传
    largeUpload:function (f){
      let file = f.file;
      let sliceSize = 1024 * 1024 * 16;
      if(file.size < sliceSize){
        sliceSize = file.size;
      }
      let sliceNum = Math.ceil(file.size/sliceSize);//分片数量
      let start = 0;
      let self = this;
      //前端获取md5值
      fileMd5(file,sliceSize,sliceNum).then(e=>{
        //将文件添加到上传展示列表中
        self.largeFileUploadList.push({fileMd5:e,fileName:file.name,progress:0,sliceNum:sliceNum,del:false});
        for (let i = 0; i < sliceNum; i++) {
          let end = sliceSize + start;
          let chunk = f.file.slice(start,end);
          //发送分片
          this.sliceUpload(e, file.size, start, end, file, chunk, i, sliceNum, function (d){
            if(d.result && d.data && d.data != 'ok' && d.data != 'retry'){//所有分片合并完毕
              self.incrProgress(e,100);//进度条
              //将文件与网盘目录建立关系
              axios.post('/netdisk/addDirectory', {
                name: file.name,
                pid: self.pid,
                fileMd5: d.data,
                type:"FILE",
                mediaType:file.type
              }).then(function (response) {
                if(response.data.result && response.data.data){
                  self.list.push(response.data.data);
                }
                showToast(response.data.message);
              }).catch(function (error) {
                console.log(error);
              });
            }else if(d.result && d.data == 'ok'){//分片上传成功
              self.incrProgress(e,Math.ceil(100 / sliceNum));
            }
          });
          start = end;
        }
      });
    },
    //暂停播放视频
    pauseVideo:function (){
      try {
        this.videoOptions.sources = [];
        this.myPlayer.dispose();
        this.myPlayer.pause();
      }catch (e){}
    },
    //开始播放视频
    playVideo:function (){
      let myVideoDiv = document.getElementById("videoBody")
      myVideoDiv.innerHTML = '<video id="videoPlayer" class="video-js vjs-default-skin" />';
      this.myPlayer = videojs("videoPlayer",this.videoOptions);
    },
    //打开图片,图片文件不会很大,直接下载完打开即可
    openImage:function (item){
      this.isOverlay = true;
      let self = this;
      axios.post('/file/getFile', {
        fileMd5: item.fileMd5,
        name: item.name,
        source:"CLOUDDISK"
      },{
        responseType:"blob"
      }).then(function (response) {
        self.isOverlay = false;
        const { data, headers } = response;
        const blob = new Blob([data], {type: headers['content-type']});
        let url = window.URL.createObjectURL(blob);
        showImagePreview({
          images: [url],
          closeable: true,
          showIndex: false
        });
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //打开视频,视频流通常会很大,所以需要做到边播边缓存
    openVideo:function (item){
      this.videoOptions.sources.push({src:
            key().baseURL+"file/media/play/CLOUDDISK/"+item.fileMd5+"/"+localStorage.getItem(key().useSpaceId)+"/"+localStorage.getItem(key().authorization),
        type:item.mediaType});
      this.showVideo = true;
    },
    //打开文件
    openFile:function (item) {
      let mediaType = item.mediaType.toUpperCase();
      mediaType = mediaType.substring(0,mediaType.indexOf("/"));
      switch (mediaType) {
        case "IMAGE"://图片
          this.openImage(item);
          break
        case "VIDEO"://视频
        case "AUDIO"://音频
          this.openVideo(item);
          break
        default:
          showToast("不识别的类型,不能在线预览,请下载文件.");
          break
      }
    },
    //大文件下载,分片方式
    sliceDownload:function (item,s) {
      let self = this;
      axios.post('/file/slice/getFile', {
        fileMd5: item.fileMd5,
        name: item.name,
        source:"CLOUDDISK"
      },{
        responseType:"arraybuffer",
        headers:{
          Range:"bytes="+s+"-"
        }
      }).then(function (response) {
        const { data, headers } = response;
        try {
          let msg = JSON.parse(data);
          self.showDownloadProgress = false;
          showToast(msg.message);
          return;
        }catch (e){}
        let contentRange = headers["content-range"];
        let contentLength = headers["content-length"] * 1;
        let start = contentRange.substring(6,contentRange.indexOf("-")) * 1;
        let end = contentRange.substring(contentRange.indexOf("-") + 1,contentRange.indexOf("/")) * 1;
        let total = contentRange.substring(contentRange.indexOf("/") + 1) * 1;
        if(start == 0){//第一次请求,重置缓存
          self.largeDownloadTemporaryStorage = new Uint8Array(total);
          //计算出它有多少分片
          self.downloadFileSliceNum = Math.ceil(total / contentLength);
        }
        let retArr = new Uint8Array(data);
        for (let i = 0; i < retArr.byteLength; i++) {
          self.largeDownloadTemporaryStorage[start + i] = retArr[i];
        }
        if(end < total-1 && response.status == 206){//还有文件分片,继续请求
          self.downloadFileProgress = self.downloadFileProgress + (Math.ceil(100 / self.downloadFileSliceNum))
          self.sliceDownload(item,start+contentLength);
        }else{
          //下载完毕
          let bl = new Blob([self.largeDownloadTemporaryStorage]);
          self.downloadFileProgress = 100;
          saveAs(bl,item.name);
          self.largeDownloadTemporaryStorage = null;
          self.showDownloadProgress = false;
        }
      }).catch(function (error) {
        self.largeDownloadTemporaryStorage = null;
        self.showDownloadProgress = false;
        console.log(error);
      });
    },
    //下载文件
    download:function (item) {
      this.showDownloadProgress = true;
      this.downloadFileName = item.name;
      this.downloadFileProgress = 0;
      this.downloadFileSliceNum = 0;
      this.sliceDownload(item,0);//使用分片下载方式
    },
    //检查文件是否都上传完毕
    checkEnd:function (){
      for (let i = 0; i < this.uploadFiles.length; i++) {
        let f = this.uploadFiles[i];
        if(f.status == 'uploading'){//还有上传中
          return false;
        }
      }
      return true;
    },
    //选择文件前校验
    beforeRead: function (files){
      if(this.uploadFiles == null || this.uploadFiles.length == 0){
        return true;
      }
      B:for (let j = 0; j < this.uploadFiles.length; j++) {
        let oldFile = this.uploadFiles[j];
        if(files.length){
          A:for (let i = 0; i < files.length; i++) {
            let nowFile = files[i];
            if(nowFile.name == oldFile.file.name){
              showToast("重复文件名:" + nowFile.name);
              return false;
            }
          }
        }else{
          if(files.name == oldFile.file.name){
            showToast("重复文件名:" + files.name);
            return false;
          }
        }
      }
      return true;
    },
    //提交上传
    submitUpload: function(){
      if(this.uploadFiles.length == 0){
        return;
      }
      //设置禁用
      this.uploadDisabled = true;
      //开始循环上传文件
      for (let i = 0; i < this.uploadFiles.length; i++) {
        let f = this.uploadFiles[i];
        f.status = "uploading";
        f.message = "上传中";
        let data = new FormData();
        data.append('f', f.file);
        data.append('s', 'CLOUDDISK');
        data.append('m', f.file.type);
        let self = this;
        axios.post("/file/addFile", data, {
          header:{
            'Content-Type': 'multipart/form-data'
          }
        }).then((res) => {
          if(res.data.result){
            //将文件与网盘目录建立关系
            axios.post('/netdisk/addDirectory', {
              name: f.file.name,
              pid: self.pid,
              fileMd5: res.data.data,
              type:"FILE",
              mediaType:f.file.type
            }).then(function (response) {
              if(response.data.result){
                if(response.data.data){
                  self.list.push(response.data.data);
                }
                f.status = "done";
                f.message = "完成";
              }else{
                f.status = "failed";
                f.message = response.data.message;
              }
              if(self.checkEnd()){
                showToast("文件全部上传完毕");
                //全部上传完毕后取消禁用
                self.uploadDisabled = false;
              }
            }).catch(function (error) {
              f.status = "failed";
              f.message = "失败";
              if(self.checkEnd()){
                showToast("文件全部上传完毕");
                //全部上传完毕后取消禁用
                self.uploadDisabled = false;
              }
              console.log(error);
            });
          }else{
            f.status = "failed";
            f.message = res.data.message;
            if(self.checkEnd()){
              showToast("文件全部上传完毕");
              //全部上传完毕后取消禁用
              self.uploadDisabled = false;
            }
          }
        }).catch((error) => {
          console.log(error);
          f.status = "failed";
          f.message = "失败";
          if(self.checkEnd()){
            showToast("文件全部上传完毕");
            //全部上传完毕后取消禁用
            self.uploadDisabled = false;
          }
        });
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
        this.directoryName = "";
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
      let clean = true;
      switch (cz) {
        case 'addDirectory':
          this.showAddDirectory = true;
          break;
        case 'addLargeFile':
          this.showLargeUpload = true;
          //如果列表中全部进度都是100%,代表已经上传完毕且用户不需要太关心每个文件状态,清除列表即可
          for (let i = 0; i < this.largeFileUploadList.length; i++) {
            if(this.largeFileUploadList[i].progress != 100){
              clean = false;
            }
          }
          if(clean){
            this.largeFileUploadList = [];
          }
          break;
        case 'addFile':
          this.showUpload = true;
          //如果上传列表中全是上传成功,已经上传完了且用户不需要太关心每个文件的状态,需要清除掉上传列表
          for (let i = 0; i < this.uploadFiles.length; i++) {
            if(this.uploadFiles[i].status != "done"){
              clean = false;
            }
          }
          if(clean){
            this.uploadFiles = [];
          }
          break;
        case 'photograph':
          showToast("拍照上传");
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