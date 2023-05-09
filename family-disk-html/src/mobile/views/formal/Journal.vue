<template>
  <van-overlay :show="isOverlay" :z-index="9999">
    <div class="wrapper">
      <van-loading />
    </div>
  </van-overlay>
  <van-search
      v-model="keyword"
      show-action
      placeholder="请输入日记标题或日期(格式:2020年01月02日)"
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
    <van-swipe-cell v-for="item in journalList">
      <van-cell is-link :title="item.title" :label="item.happenTime" @click="open(item)" />
      <template #right>
        <van-button v-if="!roleWrite" style="height: 66px;" square hairline type="danger"  @click="delJournal(item)" text="删除" />
      </template>
    </van-swipe-cell>

    <van-back-top ight="15vw" bottom="10vh" target="#list"/>
  </van-list>


  <div v-if="!roleWrite" style="position: fixed;right: 25px;bottom: 200px;">
    <van-popover placement="left" v-model:show="showPopover" :actions="addActions" @select="addSelect">
      <template #reference>
        <van-button icon="plus" type="primary"/>
      </template>
    </van-popover>
  </div>

  <!-- 日记编辑面板 -->
  <van-action-sheet @close="closeJournalSave" v-model:show="showJournalSave" :lock-scroll="false" :title="journalDate">
    <div>
      <van-field
          v-model="journalTitle"
          clearable
          label="日记标题:"
          left-icon="records"
          placeholder="请输入日记标题"
          :readonly="uploadDisabled"
      />
      <van-cell-group inset>
        <van-field
            clearable
            v-model="journalBody"
            rows="10"
            autosize
            type="textarea"
            maxlength="500"
            placeholder="记录一些事情吧..."
            show-word-limit
            :readonly="uploadDisabled"
        />
      </van-cell-group>
      <div style="margin-left: 30px;margin-right: 30px;margin-bottom: 30px;">
        <van-uploader :deletable="uploadDisabled == false"
                      ref="uploader"
                      @click-preview="openPreview"
                      preview-size="80"
                      :max-size="1024 * 1024 * 10"
                      @oversize="onOversize"
                      :max-count="12"
                      :before-read="beforeRead"
                      :after-read="afterRead"
                      :readonly="uploadDisabled"
                      accept="image/*,video/*"
                      :preview-options="{closeable:true}"
                      v-model="uploadFiles" multiple>
        </van-uploader>
        <van-button round block :disabled="uploadDisabled" type="primary" @click="addJournal" native-type="submit">
          保存日记
        </van-button>
      </div>

    </div>
  </van-action-sheet>

  <van-dialog
      v-model:show="showVideo"
      :show-confirm-button="false"
      @close="closeVideo"
      @opened="$refs.previewVideoRef.src=videoUrl"
      :show-cancel-button="true">

    <video :poster="videoCoverBase64" ref="previewVideoRef" style="height: 100%;width: 100%" controls autoplay />
  </van-dialog>

</template>

<script>
import axios from "axios";
import {
  Loading,
  Overlay,
  Uploader,
  Field,
  ActionSheet,
  List,
  CellGroup,
  Cell,
  Search,
  BackTop,
  showToast,
  Popover,
  Button,
  SwipeCell,
  ImagePreview,
  Image,
  Dialog,
  showConfirmDialog
} from 'vant';
//vant适配桌面端
import '@vant/touch-emulator';
import {ref} from "vue";
import {key} from "@/global/KeyGlobal";
import {FileDoownloadAppoint, FileDoownloadSmall, FileSoundOut} from "@/global/FileDownload";
import {
  Base64toBlob,
  CountFileSliceInfo, FileMd5,
  FormatDateDay,
  GetVideoCoverBase64,
  HeicToCommon
} from "@/global/StandaloneTools";
import {FileUpload, getUploadList} from "@/global/FileUpload";


export default {
  name: "Journal",
  components:{
    [Image.name]:Image,
    [Loading.name]:Loading,
    [Overlay.name]:Overlay,
    [Uploader.name]:Uploader,
    [Field.name]:Field,
    [CellGroup.name]:CellGroup,
    [ActionSheet.name]:ActionSheet,
    [List.name]:List,
    [CellGroup.name]:CellGroup,
    [Cell.name]:Cell,
    [Search.name]:Search,
    [BackTop.name]:BackTop,
    [Popover.name]:Popover,
    [Button.name]:Button,
    [SwipeCell.name]:SwipeCell,
    [ImagePreview.name]:ImagePreview,
    [Dialog.name]:Dialog,
  },
  setup(){
    const addActions = [
      { text: '写日记', icon: 'records',name:'addJournal'}
    ];
    const showPopover = ref(false);
    const onOversize = (f) => {
      showToast(`大于10MB的文件请压缩后上传`);
    };
    return {
      onOversize,
      addActions,
      showPopover,
      maxHeight:document.documentElement.clientHeight - 110,
      fileDownloadUrl:'/stream/slice/getFile',
    }
  },
  data(){
    return {
      roleWrite:localStorage.getItem(key().useSpaceRole) != 'WRITE',
      isOverlay:false,
      loading:false,
      finished:false,
      showJournalSave:false,
      showVideo:false,
      journalDate:"",
      uploadDisabled:false,
      uploadFiles:[],
      uploadFilesIndex:{},
      journalTitle:"",
      keyword:"",
      journalBody:"",
      journalList:[],
      videoCoverBase64:'',
      videoUrl:''
    }
  },
  methods:{
    //选择文件后处理
    afterRead:async function (files) {
      let fs = files;
      if(!files.length){
        fs = [files];
      }
      for (let i = 0; i < fs.length; i++) {
        let last = fs[i];
        let file = last.file;
        //将文件添加到预览图片中
        //计算分片大小
        let sliceInfo = CountFileSliceInfo(last.file);
        //计算md5值
        let md5 = await FileMd5(last.file,sliceInfo.sliceSize,sliceInfo.sliceNum);
        let mediaType = file.type.toUpperCase();
        let mediaTypes = mediaType.split("/");
        let index = this.uploadFiles.length - 1 - i;
        this.uploadFilesIndex[md5] = index;
        let src = window.URL.createObjectURL(file);
        let imgSrc = src;
        let type = file.type;
        switch (mediaTypes[0]) {
          case "IMAGE"://图片
            break
          case "VIDEO"://视频
            let base64 = await GetVideoCoverBase64(src);
            let b = await Base64toBlob(base64);
            imgSrc = window.URL.createObjectURL(b);//封面地址
            //如果是视频,就将类型强行设置成图片,用来展示视频第一帧的截图
            type = 'image/jpeg';
            break
        }
        let config = {
          url2: src, url: imgSrc, status: "done", message: "", file:
              {name: file.name, type: type, type2: file.type,fileMd5:md5,sliceInfo:sliceInfo,file:file,scale:0}
        };
        this.uploadFiles[index] = config;
      }
    },
    //选择文件前校验
    beforeRead: function (files){
      if(this.uploadFiles == null || this.uploadFiles.length == 0){
        return true;
      }
      let fs = files;
      if(!files.length){
        fs = [files];
      }
      B:for (let j = 0; j < this.uploadFiles.length; j++) {
        let oldFile = this.uploadFiles[j];
        A:for (let i = 0; i < fs.length; i++) {
          let nowFile = fs[i];
          if(nowFile.name == oldFile.file.name){
            showToast("重复文件名:" + nowFile.name);
            return false;
          }
        }
      }
      return true;
    },
    //关闭视频播放器
    closeVideo:function () {
      //停止播放
      if(this.$refs.previewVideoRef){
        this.$refs.previewVideoRef.pause();
        this.$refs.previewVideoRef.src = "";
      }
    },
    //点击图片预览前回调
    openPreview:function (f) {
      let file = f.file;
      let mediaType = null;
      if(file.type2){//有真实的视频类型
        mediaType = file.type2.toUpperCase();
      }else{
        mediaType = file.type.toUpperCase();
      }
      mediaType = mediaType.substring(0,mediaType.indexOf("/"));
      switch (mediaType){
        case "VIDEO"://视频
        case "AUDIO"://音频
          this.$refs.uploader.closeImagePreview();
          //切换到播放器播放,有url的是远程资源
          let url = null;
          if(f.url && !f.url2){
            url = f.url;
          }else if(f.url2 && f.url){//如果2个都值,那么url2才是视频地址,url是封面
            this.videoCoverBase64 = f.url;//视频封面
            url = f.url2;
          }else{//本地资源
            let blob = file.slice(0,file.size);
            url = window.URL.createObjectURL(blob);
          }
          this.showVideo = true;
          this.videoUrl = url;
          return false;
      }
    },
    //添加日记
    addJournal:async function (){
      let self = this;
      if(!self.journalTitle && !self.journalBody){
        showToast("日记标题和内容不能都为空");
        return;
      }
      self.isOverlay = true;
      //先上传文件,再保存
      if(this.uploadFiles){
        for (let i = 0; i < this.uploadFiles.length; i++) {
          let u = this.uploadFiles[i];
          u.status = 'uploading';
          u.message = '上传中';
          let f = u.file;
          let ret = await FileUpload(f.sliceInfo,f.file,f.fileMd5,'JOURNAL',null,async function (q) {
            if(q.result){
              u.status = "done";
              f.scale = 100;
            }else{
              u.status = "failed";
              u.message = q.resultMsg;
            }
          });
          showToast(ret.msg);
        }
      }

      let interval = setInterval(function (){
        //更新上传列表进度
        let uplist = getUploadList();
        for (const uplistKey in uplist) {
          let v = uplist[uplistKey];
          let index = self.uploadFilesIndex[uplistKey];
          let u = self.uploadFiles[index];
          let f = u.file;
          f.scale = v.progress == 0 ? 0 : Math.floor(v.progress/v.sliceInfo.sliceNum*100);
          if(!v.result){
            u.status = "failed";
            u.message = v.resultMsg;
            self.stopFileUpload(v.md5);
          }
        }
        //检查所有文件是否都已经上传完毕
        let successFiles = [];
        if(self.uploadFiles){
          for (let i = 0; i < self.uploadFiles.length; i++) {
            let u = self.uploadFiles[i];
            let f = u.file;
            if(f.scale < 100){
              return;
            }
            successFiles.push({fileMd5:f.fileMd5,fileName:f.name,mediaType:f.type2});
          }
        }
        //文件都已经上传成功
        let data = {
          title: self.journalTitle,
          body: self.journalBody,
          happenTime: self.journalDate,
          files:successFiles,
        };
        axios.post('/journal/addJournalList', data).then(function (response) {
          if(response.data.result){
            self.onLoad();
            self.showJournalSave = false;
          }
          showToast(response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });

        clearInterval(interval);
      }, 500);
    },
    //删除日记
    delJournal:function (item) {
      showConfirmDialog({
        title: '删除',
        message:'是否删除日记:' + item.title + ',删除后不可恢复!'
      })
          .then(() => {
            this.isOverlay = true;
            let self = this;
            axios.post('/journal/delJournalList', {
              id: item.id
            }).then(function (response) {
              if(response.data.result){
                for (let i = 0; i < self.journalList.length; i++) {
                  if(self.journalList[i].id == item.id){
                    self.journalList.splice(i,1);
                    break;
                  }
                }
              }
              showToast(response.data.message);
              self.isOverlay = false;
            }).catch(() => {
                  // on cancel
            });
          }).catch(function (error) {
            self.isOverlay = false;
            console.log(error);
          });
    },
    //关闭写日记面板
    closeJournalSave:function (item){
      this.journalDate = "";
      this.uploadFiles = [];
      this.journalBody = "";
      this.journalTitle = "";
    },
    //写日记
    addSelect: function (item){
      let cz = item.name;
      switch (cz) {
        case 'addJournal':
          if(!this.journalDate){
            this.journalDate = FormatDateDay(new Date())
          }
          this.journalTitle = "";
          this.journalBody = "";
          this.uploadFiles = [];
          this.uploadDisabled = false;
          this.showJournalSave = true;
          break;
      }
    },
    //加载日记列表
    onLoad:function (){
      this.isOverlay = true;
      let self = this;
      axios.post('/journal/getJournalList', {
        keyword: this.keyword
      }).then(function (response) {
        if(response.data.result){
          self.journalList = response.data.datas;
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
    },
    //下载视频封面
    dVideo:async function(f){
      let index = this.uploadFilesIndex[f.fileMd5];
      let fso = await FileSoundOut(this.fileDownloadUrl,f.fileMd5,f.fileName,'JOURNAL');
      if(!fso.state){
        this.uploadFiles[index].status = "failed";
        this.uploadFiles[index].message = fso.msg;
        return;
      }
      let url = key().baseURL+"stream/media/play/JOURNAL/"+ localStorage.getItem(key().authorization) + "/" +f.fileMd5;
      this.uploadFiles[index].url2 = url;
      //试探成功,开始下载
      //开始下载视频的第一帧,当做视频的封面
      //只取1mb,清晰度很高的视频想获取到第一帧,1mb肯定是不够的,不过为了不浪费流量,提高加载速度,能预览出大部分的即可
      let fd = await FileDoownloadAppoint(this.fileDownloadUrl,fso,0,1024*1024*1);
      if(fd.state){
        let blob = new Blob([fd.bytes]);
        let url = window.URL.createObjectURL(blob);
        let base64Promise = GetVideoCoverBase64(url);
        let self = this;
        this.uploadFiles[index].status = "done";
        this.uploadFiles[index].message = "";
        base64Promise.then(async function(base64){
          let b = await Base64toBlob(base64);
          self.uploadFiles[index].url = window.URL.createObjectURL(b);//封面地址
        }).catch(function (e){
          self.uploadFiles[index].url = "/img/pc/play.png";
        });
      }else{
        this.uploadFiles[index].status = "failed";
        this.uploadFiles[index].message = fd.msg;
      }
    },
    //下载图片预览
    dImage:async function (f){
      let index = this.uploadFilesIndex[f.fileMd5];
      let fso = await FileSoundOut(this.fileDownloadUrl,f.fileMd5,f.fileName,'JOURNAL');
      if(!fso.state){
        this.uploadFiles[index].status = "failed";
        this.uploadFiles[index].message = fso.msg;
        return;
      }
      //试探成功,开始下载
      let fd = await FileDoownloadSmall(fso);
      if(fd.state){
        let blob = new Blob([fd.bytes],{type:f.mediaType});
        let src = window.URL.createObjectURL(blob);
        this.uploadFiles[index].url = src;
        this.uploadFiles[index].status = "done";
        this.uploadFiles[index].message = "";
      }else{
        this.uploadFiles[index].status = "failed";
        this.uploadFiles[index].message = fd.msg;
      }
    },
    //打开日记
    open:async function (item) {
      this.journalDate = item.happenTime;
      this.showJournalSave = true;
      this.uploadDisabled = true;
      this.journalTitle = item.title;
      this.journalBody = item.body;
      this.uploadFiles = [];
      this.uploadFilesIndex = {};
      //下载日记关联的文件
      if(item.files){
        for (let i = 0; i < item.files.length; i++) {
          let f = item.files[i];
          let mediaType = f.mediaType.toUpperCase();
          let mediaTypes = mediaType.split("/");
          let type = item.files[i].mediaType;
          if(mediaTypes[0] == "VIDEO"){
            //如果是视频,就将类型强行设置成图片,用来展示视频第一帧的截图
            type = 'image/jpeg';
          }
          //添加占位,在媒体资源未下载完之前就显示有哪些资源正在加载
          let config = {
            url2: '', url: '', status: "uploading", message: "加载中", file:
                {name: item.files[i].fileName, type: type, type2: item.files[i].mediaType,fileMd5:item.files[i].fileMd5}
          };
          this.uploadFilesIndex[item.files[i].fileMd5] = this.uploadFiles.length;
          this.uploadFiles.push(config);
        }

        for (let i = 0; i < item.files.length; i++) {
          let f = item.files[i];
          let mediaType = f.mediaType.toUpperCase();
          let mediaTypes = mediaType.split("/");
          switch (mediaTypes[0]) {
            case "IMAGE"://图片
              await this.dImage(f);
              break
            case "VIDEO"://视频
              await this.dVideo(f);
              break;
            case "AUDIO"://音频
              //理论上来讲日记不能上传音频,这里先不处理
          }
        }
      }
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