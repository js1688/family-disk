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
        <van-button v-if="item.type == 'FILE'" square hairline type="success"  @click="download(item)" text="下载" />
      </template>
    </van-swipe-cell>
  </van-list>

  <van-dialog :show-confirm-button="false" :show-cancel-button="false" v-model:show="showDownloadProgress" :title="downloadFileName">
    <div style="height: 30px;margin: 10px;">
      <van-progress :percentage="downloadFileProgress" />
    </div>
  </van-dialog>

  <van-action-sheet
      v-model:show="passwordShow"
      :close-on-click-overlay="false"
      :closeable="false"
      title="请输入密码后解锁内容">
    <div class="content">
      <van-form @submit="getBody">
        <van-cell-group inset>
          <van-field required v-model="param.password"
                     type="password"
                     :rules="[{ required: true, message: '请输入解锁密码' }]"
                     label="解锁密码" placeholder="请输入解锁密码" />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-action-sheet @opened="playVideo" @close="pauseVideo" title="媒体播放" round v-model:show="showVideo">
    <div style="max-width: 1200px;" id="videoBody">
    </div>
  </van-action-sheet>

  <van-image-preview
      :onClose="closeVideo"
      v-model:show="showPreviewVideo"
      :images="videoUrls"
      :showIndex="false"
      :beforeClose="videoBeforeClose"
      closeable>
    <template #image="{src}">
      <!-- 为了兼容移动端和pc端,需要绑定两种事件 -->
      <div @click.native="chickVideo" @touchend="chickVideo" style="position: absolute;top:50px;left: 0;bottom: 50px;right: 0;">
        <video :src="src" ref="previewVideoRef" style="height: 100%;width: 100%" controls autoplay />
      </div>
    </template>
  </van-image-preview>

  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
//vant适配桌面端
import '@vant/touch-emulator';
import {
  Dialog,
  Progress,
  Popover,
  Button,
  BackTop,
  Overlay,
  Search,
  Loading,
  showToast,
  DropdownMenu,
  Cell,
  Popup,
  Picker,
  List,
  Tag,
  SwipeCell,
  DropdownItem,
  Field,
  CellGroup,
  Form,
  ImagePreview,
  ActionSheet, showConfirmDialog, showImagePreview
} from 'vant';
import { saveAs } from 'file-saver';
import axios from "axios";
import {isToken, key} from "@/global/KeyGlobal";
import {GetUrlParam} from "@/global/StandaloneTools";
import 'video.js/dist/video-js.css';
import videojs from "video.js";
export default {
  name: "DiskShare",
  components:{
    [ImagePreview.name]:ImagePreview,
    [Dialog.name]:Dialog,
    [Progress.name]:Progress,
    [Field.name]:Field,
    [CellGroup.name]:CellGroup,
    [Form.name]:Form,
    [Popup.name]:Popup,
    [Picker.name]:Picker,
    [Cell.name]:Cell,
    [Tag.name]:Tag,
    [SwipeCell.name]:SwipeCell,
    [List.name]:List,
    [Popover.name]:Popover,
    [Button.name]:Button,
    [BackTop.name]:BackTop,
    [Overlay.name]:Overlay,
    [Search.name]:Search,
    [Loading.name]:Loading,
    [DropdownMenu.name]:DropdownMenu,
    [DropdownItem.name]:DropdownItem,
    [ActionSheet.name]:ActionSheet
  },
  setup() {
    return {
    };
  },
  created(){
    /**
     * 分享网盘路径: 是否需要输入密码/链接uuid
     * http://localhost:5173/#/share/netdisk?lock=true&uuid=asddhsdhiweq-sdas-666
     * @type {{lock: null, uuid: null}}
     */
    //从页面地址中获取到参数
    for (const key in this.param) {
      let v = GetUrlParam(key);
      this.param[key] = v;
    }
    //是否需要输入密码
    if(this.param.lock == 'true'){
      this.passwordShow = true;
    }else{
      this.getBody();//不需要密码,直接获得内容
    }
  },
  data() {
    return {
      loading:false,
      finished:false,
      list:[],
      openPath:[
        {name:'目录',id:0}
      ],
      nameMap: {},
      idMap: {},
      tempToken:'',
      pid:0,
      keyword:'',
      isOverlay:false,
      passwordShow:false,
      param : {lock:null,uuid:null,password:null},
      showDownloadProgress:false,
      downloadFileName:"",
      downloadFileProgress:0,
      downloadFileSliceNum:0,
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
      },
      showVideo:false,
      showPreviewVideo:false,
      videoUrls:[],
      myPlayer:null,
      chickVideoValue:false
    }
  },
  methods:{
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
    //原生方式预览视频时，点击画面会触发关闭弹窗，这个是组件本身的时间监听，尝试了好多办法都无法解决，最后使用事件监听，如果点击的是画面则不关闭弹窗
    chickVideo:function (){
      this.chickVideoValue = true;
    },
    videoBeforeClose:function (e) {
      let ret = !this.chickVideoValue;
      this.chickVideoValue = false;
      return ret;
    },
    //关闭预览视频播放器
    closeVideo:function () {
      if(this.$refs.previewVideoRef){
        this.$refs.previewVideoRef.pause();
        this.$refs.previewVideoRef.src = "";
      }
      this.videoUrls = [];
      this.showPreviewVideo = false;
    },
    //下载文件
    download:function (item) {
      this.showDownloadProgress = true;
      this.downloadFileName = item.name;
      this.downloadFileProgress = 0;
      this.downloadFileSliceNum = 0;
      this.sliceDownload(item,0,function (bl){
        saveAs(bl,item.name);
      });//使用分片下载方式
    },
    //大文件下载,分片方式
    sliceDownload:function (item,s,callback) {
      let self = this;
      axios.get(`/netdisk/share/slice/getFile/${item.id}`, {
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
          let a = self.downloadFileProgress + Math.ceil(100 / self.downloadFileSliceNum);
          if(a < 100){
            self.downloadFileProgress = a;
          }
          self.sliceDownload(item,start+contentLength,callback);
        }else{
          //下载完毕
          let bl = new Blob([self.largeDownloadTemporaryStorage]);
          self.downloadFileProgress = 100;
          self.largeDownloadTemporaryStorage = null;
          self.showDownloadProgress = false;
          callback(bl);
        }
      }).catch(function (error) {
        self.largeDownloadTemporaryStorage = null;
        self.showDownloadProgress = false;
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
    //打开图片
    openImage:function (item){
      this.showDownloadProgress = true;
      this.downloadFileName = item.name;
      this.downloadFileProgress = 0;
      this.downloadFileSliceNum = 0;
      this.sliceDownload(item,0,function (bl){
        let url = window.URL.createObjectURL(bl);
        showImagePreview({
          images: [url],
          closeable: true,
          showIndex: false
        });
      });//使用分片下载方式
    },
    //打开视频,视频流通常会很大,所以需要做到边播边缓存
    openVideo:function (item,gs){
      //判断使用哪种播放器,如果是苹果公司的媒体资源则使用原生播放器播放
      let url = key().baseURL+"netdisk/share/media/play/"+ this.tempToken + "/" + item.id;
      switch (gs) {
        case "MP4":
          this.videoOptions.sources.push({src:url,type:item.mediaType});
          this.showVideo = true;
          break
        default:
          this.videoUrls = [url];
          this.showPreviewVideo = true;
      }
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
          this.openVideo(item,mediaTypes[1]);
          break
        default:
          showToast("不识别的类型,不能在线预览,请下载文件.");
          break
      }
    },
    //目录跳转
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
    //加载列表
    onLoad: function () {
      if(this.keyword){
        let l = [];
        for (const key in this.nameMap) {
          if(key.indexOf(this.keyword) != -1){
            l.push(this.nameMap[key]);
          }
        }
        this.list = l;
      }else{
        let v = this.idMap[this.pid];
        if(v){
          this.list = v.child;
        }else{
          this.list = [];
        }
      }
      this.loading = false;//加载完毕
      this.finished = true;//全部数据加载完毕
    },
    //将返回的目录数组,解析成map类型方便做搜索与跳转
    rebuild:function (l){
      for (let i = 0; i < l.length; i++) {
        let v = l[i];
        this.nameMap[v.name] = v;
        this.idMap[v.id] = v;
        if(v.type == "FOLDER" && v.child){
          this.rebuild(v.child);
        }
      }
    },
    //获得分享内容
    getBody:function (){
      this.passwordShow = false;
      this.isOverlay = true;
      let self = this;
      axios.get(`/netdisk/share/getBody/${this.param.uuid}?password=${this.param.password == null ? '' : this.param.password}`).then(function (res){
        if(res.data.result){
          //设置默认的根目录pid
          let l = res.data.data.list;
          self.idMap[0] = {child:l};
          self.rebuild(l);
          self.tempToken = res.data.data.tempToken;
          self.onLoad();

          //请求拦截设置头部,将得到的临时token设置进去
          axios.interceptors.request.use(config => {//声明请求拦截器
            config.headers[key().shareToken] = self.tempToken;
            return config;//一定要返回
          });
        }else{
          showToast(res.data.message);
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
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