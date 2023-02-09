<template>
  <van-overlay :show="isOverlay">
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
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <van-swipe-cell v-for="item in journalList">
      <van-cell is-link :title="item.title" :label="item.happenTime" @click="open(item)" />
      <template #right>
        <van-button style="height: 66px;" square hairline type="danger"  @click="delJournal(item)" text="删除" />
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

  <!-- 日记编辑面板 -->
  <van-action-sheet @close="closeJournalSave" v-model:show="showJournalSave" :title="journalDate">
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
                      :max-size="1024 * 1024 * 32"
                      @oversize="onOversize"
                      :max-count="12"
                      :before-read="beforeRead"
                      :readonly="uploadDisabled"
                      accept="image/*,video/*,audio/*"
                      :preview-options="{closeable:true}"
                      v-model="uploadFiles" multiple>
        </van-uploader>
        <van-button round block :disabled="uploadDisabled" type="primary" @click="addJournal" native-type="submit">
          保存日记
        </van-button>
      </div>

    </div>
  </van-action-sheet>

  <van-image-preview
      :onClose="closeVideo"
      v-model:show="showVideo"
      :images="videoUrls"
      :showIndex="false"
      :beforeClose="videoBeforeClose"
      closeable>
    <template #image="{src}">
      <!-- 为了兼容移动端和pc端,需要绑定两种事件 -->
      <div @click.native="chickVideo" @touchend="chickVideo" style="position: absolute;top:50px;left: 0;bottom: 50px;right: 0;">
        <video id="videoId" :poster="videoCoverBase64" :src="src" ref="previewVideoRef" style="height: 100%;width: 100%" controls autoplay />
      </div>
    </template>
  </van-image-preview>

  <van-back-top ight="15vw" bottom="10vh" />
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
  showConfirmDialog, showImagePreview
} from 'vant';
//vant适配桌面端
import '@vant/touch-emulator';
import {ref} from "vue";
import {key} from "@/global/KeyGlobal";


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
    [ImagePreview.name]:ImagePreview
  },
  setup(){
    const addActions = [
      { text: '写日记', icon: 'records',name:'addJournal'}
    ];
    const showPopover = ref(false);
    const onOversize = (f) => {
      showToast('文件太大,请上传32MB以内的文件.');
    };
    return {
      onOversize,
      addActions,
      showPopover
    }
  },
  data(){
    return {
      isOverlay:false,
      loading:false,
      finished:false,
      showJournalSave:false,
      showVideo:false,
      videoUrls:[],
      journalDate:"",
      uploadDisabled:false,
      uploadFiles:[],
      journalTitle:"",
      keyword:"",
      journalBody:"",
      journalList:[],
      chickVideoValue:false,
      videoCoverBase64:''
    }
  },
  methods:{
    //原生方式预览视频时，点击画面会触发关闭弹窗，这个是组件本身的时间监听，尝试了好多办法都无法解决，最后使用事件监听，如果点击的是画面则不关闭弹窗
    chickVideo:function (){
      this.chickVideoValue = true;
    },
    videoBeforeClose:function (e) {
      let ret = !this.chickVideoValue;
      this.chickVideoValue = false;
      return ret;
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
    //关闭视频播放器
    closeVideo:function () {
      if(this.$refs.previewVideoRef){
        this.$refs.previewVideoRef.pause();
        this.$refs.previewVideoRef.src = "";
      }
      this.videoUrls = [];
      this.showVideo = false;
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
          this.videoUrls = [url];
          this.showVideo = true;
          return false;
      }
    },
    /**
     * 检查文件是否都上传完毕
     * @param success true=文件是否全部上传成功,false=文件是否全部上传了,不在乎成功失败
     * @returns {boolean}
     */
    checkEnd:function (success){
      for (let i = 0; i < this.uploadFiles.length; i++) {
        let f = this.uploadFiles[i];
        if(success && f.status != 'done'){//有不是成功的
          return false;
        }else if(!success && f.status == 'uploading'){//还有上传中
          return false;
        }
      }
      return true;
    },
    //添加日记
    addJournal:function (){
      let self = this;
      this.uploadDisabled = true;
      this.isOverlay = true;
      let successFiles = [];
      //如果没有文件,则直接保存日记
      if(this.uploadFiles == null || this.uploadFiles.length == 0){
        //全部成功上传,开始保存日志
        let data = {
          title: self.journalTitle,
          body: self.journalBody,
          happenTime: self.journalDate,
          files:successFiles,
        };
        axios.post('/journal/addJournalList', data).then(function (response) {
          if(response.data.result){
            self.onLoad();
          }else{
            self.uploadDisabled = false;
          }
          self.isOverlay = false;
          showToast(response.data.message);
        }).catch(function (error) {
          self.uploadDisabled = false;
          self.isOverlay = false;
          console.log(error);
        });
      }else{
        //先将文件都上传
        for (let i = 0; i < this.uploadFiles.length; i++) {
          let f = this.uploadFiles[i];
          f.status = "uploading";
          f.message = "上传中";
          let data = new FormData();
          data.append('f', f.file);
          data.append('s', 'JOURNAL');
          data.append('m', f.file.type);
          axios.post("/file/addFile", data, {
            header:{
              'Content-Type': 'multipart/form-data'
            }
          }).then((res) => {
            if(res.data.result){
              f.status = "done";
              f.message = "完成";
              successFiles.push({fileMd5:res.data.data,fileName:f.file.name,mediaType:f.file.type})
            }else{
              f.status = "failed";
              f.message = res.data.message;
            }
            if (self.checkEnd(true)){//文件是否全部成功上传
              //全部成功上传,开始保存日志
              let data = {
                title: self.journalTitle,
                body: self.journalBody,
                happenTime: self.journalDate,
                files:successFiles,
              };
              axios.post('/journal/addJournalList', data).then(function (response) {
                if(response.data.result){
                  data["id"] = response.data.data;
                  if(!data.title){
                    data.title = data.body.substring(0,data.body.length < 10 ? data.body.length : 10);
                  }
                  self.journalList.push(JSON.parse(JSON.stringify(data)));
                }else{
                  self.uploadDisabled = false;
                }
                self.isOverlay = false;
                showToast(response.data.message);
              }).catch(function (error) {
                self.uploadDisabled = false;
                self.isOverlay = false;
                console.log(error);
              });
            }else if(self.checkEnd(false)){//文件是否上传完毕,不在乎是否成功失败
              showToast("文件全部上传完毕");
              //全部上传完毕后取消禁用
              self.uploadDisabled = false;
              self.isOverlay = false;
            }
          }).catch((error) => {
            console.log(error);
            f.status = "failed";
            f.message = "失败";
            if(self.checkEnd(false)){
              showToast("文件全部上传完毕");
              //全部上传完毕后取消禁用
              self.uploadDisabled = false;
              self.isOverlay = false;
            }
          });
        }
      }
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
            let dt = new Date();
            let year = dt.getFullYear();
            let month = (dt.getMonth() + 1).toString().padStart(2,'0');
            let date = dt.getDate().toString().padStart(2,'0');
            this.journalDate = year+'-'+month+'-'+date;
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
    //根据文件名在文件缓存数组中找到对象
    uploadFilesFind:function (fileName) {
      if(this.uploadFiles){
        for (let i = 0; i < this.uploadFiles.length; i++) {
          if(this.uploadFiles[i].file.name == fileName){
            return this.uploadFiles[i];
          }
        }
      }
    },
    //从视频流中读取第一帧,获得base64
    getVideoCoverBase64(url,setCoverUrl) {
      let video = document.createElement("video");
      video.setAttribute('crossOrigin', 'anonymous');//处理跨域
      video.setAttribute('src', url);
      video.setAttribute('width', '100%');
      video.setAttribute('height', '100%');
      video.currentTime = 0.1;//设置获取那一帧,跳转到视频的0.1秒即可
      video.addEventListener('loadeddata', () => {
        let canvas = document.createElement("canvas");
        canvas.width = video.width;
        canvas.height = video.height;
        canvas.getContext("2d").drawImage(video, 0, 0, video.width, video.height); //绘制canvas
        setCoverUrl(canvas.toDataURL('image/jpeg'));//转换为base64
        video.remove();//及时销毁对象
      });
    },
    //base64转file
    dataURLtoBlob:function(base64Url){
      let arr = base64Url.split(",");
      let  mime = arr[0].match(/:(.*?);/)[1];
      let  str = window.atob(arr[1]);
      let  n = str.length;
      let  u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = str.charCodeAt(n);
      }
      return new Blob([u8arr], {type:mime});
    },
    //打开日记
    open:function (item) {
      this.journalDate = item.happenTime;
      this.showJournalSave = true;
      this.uploadDisabled = true;
      this.journalTitle = item.title;
      this.journalBody = item.body;
      this.uploadFiles = [];
      //下载日记关联的文件
      let self = this;
      if(item.files){
        for (let i = 0; i < item.files.length; i++) {
          //添加占位,在媒体资源未下载完之前就显示有哪些资源正在加载
          let config = {url2:'',url:'',status:"uploading",message:"加载中",file:
                {name:item.files[i].fileName,type:'',type2:''}
          };
          let mediaType = item.files[i].mediaType.toUpperCase();
          let mediaTypes = mediaType.split("/");
          //如果是视频或音频,改成变下边播的url
          let url = key().baseURL+"file/media/play/JOURNAL/"+item.files[i].fileMd5+"/"+localStorage.getItem(key().useSpaceId)+"/"+localStorage.getItem(key().authorization);
          switch (mediaTypes[0]) {
            case "VIDEO"://视频
              config.url2 = url;//真实媒体播放地址
              this.getVideoCoverBase64(url, function (u) {
                //预览小图
                let blob = self.dataURLtoBlob(u);
                let f = self.uploadFilesFind(config.file.name);
                f.url = window.URL.createObjectURL(blob);//封面地址
                f.file.type2 = mediaType;//真实媒体类型
                f.file.type = blob.type;//封面类型
                f.status = "done";
                f.message = "";
              });
              break;
            case "AUDIO"://音频
              config.status = "done";
              config.message = "";
              config.url = url;//真实媒体播放地址
              break;
            default:
              axios.post('/file/getFile', {
                fileMd5: item.files[i].fileMd5,
                name: item.files[i].fileName,
                source:"JOURNAL"
              },{
                responseType:"blob"
              }).then(function (response) {
                const { data, headers } = response;
                const blob = new Blob([data], {type: headers['content-type']});
                let url = window.URL.createObjectURL(blob);
                let f = self.uploadFilesFind(item.files[i].fileName);
                f.url = url;
                f.status = "done"
                f.message = "";
              }).catch(function (error) {
                console.log(error);
              });
          }
          config.file.type = item.files[i].mediaType;
          self.uploadFiles.push(config);
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