<template>
  <n-card :bordered="false">
    <n-tabs type="line" animated>
      <n-tab-pane name="0" tab="日记">
        <n-spin :show="isOverlay">
          <n-layout has-sider>
            <n-layout-sider width="290" :bordered="true">
              <n-space style="margin: 5px;">
                <n-button v-if="roleWrite" @click="" text style="font-size: 24px;margin-left: 15px;">
                  <n-icon>
                    <add-circle-outline />
                  </n-icon>
                </n-button>

                <n-input-group>
                  <n-input :style="{ width: '230px'}" round v-model:value="keyword" placeholder="搜索"/>
                  <n-button round ghost @click="onLoad">
                    <n-icon>
                      <search-outline />
                    </n-icon>
                  </n-button>
                </n-input-group>
                <n-layout :style="`height:${maxHeight}px;overflow:auto;`" :native-scrollbar="false">
                  <n-timeline>
                    <n-timeline-item
                        v-for="item in list"
                        type="success"
                        :title="item.title"
                        :time="item.happenTime"
                        style="cursor:pointer;"
                        @click="openText(item)"
                    >
                      <n-card :bordered="false" hoverable>
                        {{item.body && item.body.length > 20 ? item.body.substring(0,20) + '...' : item.body}}
                      </n-card>
                    </n-timeline-item>
                  </n-timeline>
                </n-layout>
              </n-space>
            </n-layout-sider>
            <!-- todo 使用 v-if="saveIf" 判断是保存,还是查看,确定不同按钮的显示 -->
            <n-layout-content :style="`height:${maxHeight + 80}px;overflow:auto;`" :native-scrollbar="false">
              <n-card :title="openDb.happenTime" :bordered="false">
                <div style="width: 50%;display:inline-block;">
                  <n-h3 style="margin-bottom: 0px;">{{openDb.title}}</n-h3>
                </div>
                <div style="width: 50%;display:inline-block;">
                  <n-space :size="20" v-if="roleWrite" justify="end">
                    <n-button v-if="saveIf" text style="font-size: 24px" @click="">
                      <n-icon>
                        <save-outline />
                      </n-icon>
                    </n-button>
                    <n-button v-if="!saveIf" @click="del" text style="font-size: 24px">
                      <n-icon>
                        <trash-outline />
                      </n-icon>
                    </n-button>
                  </n-space>
                </div>
                <n-divider />
                <n-card :bordered="false" size="huge" :style="`max-height:${maxHeight - 100}px;overflow:auto;`">
                  <n-text type="default" :code="false">
                    {{openDb.body}}
                  </n-text>
                  <n-divider />
                  <n-image-group>
                    <n-space>
                      <n-image v-for="m in openImagesUrls"
                               width="200"
                               :src="m.src" :alt="m.alt"
                               :preview-disabled="previewDisabled(m.f)"
                               @click="openMedia(m.f)"
                               style="cursor:pointer"
                      />
                    </n-space>
                  </n-image-group>
                </n-card>
              </n-card>

              <n-modal
                  v-model:show="showPlayVideo"
                  :mask-closable="false"
                  preset="card"
                  :title="showPlayVideoName"
                  :style="{'max-width': '1000px','max-height':'650px'}"
                  :close-on-esc="false"
                  @after-leave="closePlayVideo"
                  @after-enter="playVideo"
              >
                <!-- videojs播放器 -->
                <div v-if="playerSelectd == 0" id="videoPlayerDiv" />
                <!-- 原生视频播放器 -->
                <div class="div-center">
                  <video v-if="playerSelectd == 1" :src="videoOptions.sources[0].src"
                         ref="originalVideoPlayer"
                         :style="{'height':'535px'}"
                         controls autoplay />
                </div>
              </n-modal>

            </n-layout-content>
          </n-layout>
        </n-spin>
      </n-tab-pane>
    </n-tabs>
  </n-card>
</template>

<script>

import {key} from "@/global/KeyGlobal";
import {
  createDiscreteApi,
  dateZhCN,
  NAnchor,
  NAnchorLink, NAvatar, NBadge, NBreadcrumb, NBreadcrumbItem, NButton, NCard, NConfigProvider, NDataTable, NDatePicker,
  NDescriptions,NTimeline,NImageGroup,
  NDescriptionsItem, NDivider, NDrawer, NDrawerContent, NForm, NFormItem,
  NIcon, NImage, NInput, NInputGroup,
  NLayout, NLayoutContent, NLayoutFooter, NLayoutHeader,
  NLayoutSider, NList, NListItem,NTimelineItem,
  NMenu, NModal, NPopconfirm, NProgress, NRadioButton, NRadioGroup,
  NSpace, NSpin,NH3,NH6,NText,
  NSwitch, NTabPane, NTabs, NTag, NThing, NTimePicker, zhCN
} from "naive-ui";
import {
  AddCircleOutline,
  BookmarkOutline, CloudDownloadOutline,
  CloudOutline, CloudUploadOutline,
  EnterOutline,
  ExitOutline, FolderOutline, ListOutline,
  MailOutline,SaveOutline,
  PersonOutline, ReturnDownForward, SearchOutline, TrashOutline
} from "@vicons/ionicons5";
import axios from "axios";
import {Base64toBlob, FormatDateDay, GetVideoCoverBase64, HeicToCommon} from '@/global/StandaloneTools';
import {FileDoownloadAppoint, FileDoownloadSmall, FileSoundOut} from "@/global/FileDownload";
import videojs from "video.js";

const { notification,dialog} = createDiscreteApi(['notification','dialog'])

export default {
  name: "Journal",
  setup(){
    return {
      locale: zhCN,
      dateLocale: dateZhCN,
      roleWrite:localStorage.getItem(key().useSpaceRole) == 'WRITE',
      fileDownloadUrl:'/stream/slice/getFile',
    }
  },
  created() {
    if(localStorage.getItem(key().authorization) != null){
      this.onLoad(true);
    }
  },
  data(){
    return{
      isOverlay:false,
      keyword:'',
      saveIf:false,
      maxHeight:document.documentElement.clientHeight - 250,
      list:[],
      openDb:{},
      openImagesUrls:[],
      openImagesUrlsIndex:{},
      myPlayer:null,
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
      showPlayVideoName:'',
      showPlayVideo:false,
      playerSelectd:0,//播放器选择,0使用的插件播放器,1原生播放器
    }

  },
  components: {
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NAnchorLink,NAnchor,NDescriptions,NDescriptionsItem,BookmarkOutline,
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,NDataTable,NList,NThing,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,NTabPane,NH3,NH6,
    NLayoutHeader,AddCircleOutline,NModal,NTag,FolderOutline,TrashOutline,CloudUploadOutline,CloudDownloadOutline,NImageGroup,
    ReturnDownForward,NBadge,NBreadcrumb,NBreadcrumbItem,SearchOutline,ListOutline,NDrawer,NDrawerContent,NTabs,NText,
    NListItem,NRadioGroup,NRadioButton,NDatePicker,NTimePicker,NConfigProvider,NTimelineItem,NTimeline,SaveOutline
  },
  methods:{
    //关闭播放视频
    closePlayVideo:function (){
      //停止播放
      try {
        if(this.$refs.audioRef){
          this.$refs.audioRef.pause();
          this.$refs.audioRef.src = "";
        }
        if(this.$refs.originalVideoPlayer){
          this.$refs.originalVideoPlayer.pause();
          this.$refs.originalVideoPlayer.src = "";
        }
        if(this.myPlayer){
          this.myPlayer.dispose();
          this.myPlayer.pause();
        }
        this.videoOptions.sources = [];
        this.audioUrls = [];
        this.audioUrl = {};
      }catch (e){}
    },
    //播放视频
    playVideo:function (){
      let self = this;
      let hook = function(player, err) {//当videojs无法播放视频时,则切换成原生播放器
        try{
          player.dispose();
          player.pause();
        }catch (e){}
        self.playerSelectd = 1;//切换到原生播放器
      }
      videojs.removeHook("error",hook);
      videojs.hook('error',hook);
      let myVideoDiv = document.getElementById("videoPlayerDiv")
      myVideoDiv.innerHTML = '<video id="videoPlayer" class="video-js vjs-default-skin" />';
      this.myPlayer = videojs("videoPlayer",this.videoOptions);
    },
    //是否屏蔽图片点击放大
    previewDisabled:function (item) {
      let mediaType = item.mediaType.toUpperCase();
      let mediaTypes = mediaType.split("/");
      switch (mediaTypes[0]) {
        case "IMAGE"://图片
          return false;
      }
      return true;
    },
    //打开媒体
    openMedia:function (item) {
      let mediaType = item.mediaType.toUpperCase();
      let mediaTypes = mediaType.split("/");
      switch (mediaTypes[0]){
        case "VIDEO"://视频
          let url = key().baseURL+"stream/media/play/JOURNAL/"+localStorage.getItem(key().authorization)  + "/" + item.fileMd5;
          this.videoOptions.sources = [{src:url,type:item.mediaType}];
          this.showPlayVideo = true;
          this.showPlayVideoName = item.name;
          this.playerSelectd = 0;//设置打开后默认播放器
          break
        case "AUDIO"://音频
          break;
      }
    },
    //删除
    del:function (){
      let self = this;
      this.showDialog("warning",`是否删除日记:${this.openDb.title}!`,function (){
        self.isOverlay = true;
        axios.post('/journal/delJournalList', {
          id: this.openDb.id
        }).then(function (response) {
          if(response.data.result){
            self.onLoad();
          }
          self.showToast(response.data.result ? "success" : "error", response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      });
    },
    //加载日记列表
    onLoad:function (init) {
      this.isOverlay = true;
      let self = this;
      axios.post('/journal/getJournalList', {
        keyword: this.keyword
      }).then(function (response) {
        if(response.data.result){
          self.list = response.data.datas;
          if(init){
            if(self.list.length > 0){
              self.openText(self.list[0]);
            }
          }
        }else{
          self.showToast("error",response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //打开日记详情
    openText:async function (item){
      this.openDb = item;
      this.openImagesUrls = [];
      this.openImagesUrlsIndex = {};
      //提取出日记中的媒体封面
      if(item.files){
        //先循环一下列表,添加占位
        for (let i = 0; i < item.files.length; i++) {
          let tp = item.files[i];
          this.openImagesUrlsIndex[tp.fileMd5] = this.openImagesUrls.length;
          this.openImagesUrls.push({src: "/img/pc/loading.png", alt: null, fileMd5: tp.fileMd5,f:tp});//先添加进去预占位
        }
        for (let i = 0; i < item.files.length; i++) {
          let f = item.files[i];
          let mediaType = f.mediaType.toUpperCase();
          let mediaTypes = mediaType.split("/");

          switch (mediaTypes[0]){
            case "IMAGE"://图片
              await this.dImage(f);
              break
            case "VIDEO"://视频
              await this.dVideo(f);
              break
            case "AUDIO"://音频
              let index = this.openImagesUrlsIndex[f.fileMd5];
              this.openImagesUrls[index].src = "/img/pc/play.png";
              this.openImagesUrls[index].alt = "点击播放";
              break;
          }
        }
      }
    },
    //下载视频封面
    dVideo:async function(f){
      let index = this.openImagesUrlsIndex[f.fileMd5];
      let fso = await FileSoundOut(this.fileDownloadUrl,f.fileMd5,f.fileName,'JOURNAL');
      if(!fso.state){
        this.openImagesUrls[index].src = "/img/pc/loadfail.png";
        this.openImagesUrls[index].alt = fso.msg;
        return;
      }
      //试探成功,开始下载
      //开始下载视频的第一帧,当做视频的封面
      //只取1mb,清晰度很高的视频想获取到第一帧,1mb肯定是不够的,不过为了不浪费流量,提高加载速度,能预览出大部分的即可
      let fd = await FileDoownloadAppoint(this.fileDownloadUrl,fso,0,1024*1024*1);
      if(fd.state){
        let blob = new Blob([fd.bytes]);
        let url = window.URL.createObjectURL(blob);
        let base64Promise = GetVideoCoverBase64(url);
        let self = this;
        base64Promise.then(async function(base64){
          let b = await Base64toBlob(base64);
          self.openImagesUrls[index].src = window.URL.createObjectURL(b);//封面地址
        }).catch(function (e){
          self.openImagesUrls[index].src = "/img/pc/play.png";
          self.openImagesUrls[index].alt = "点击播放";
        });
      }else{
        this.openImagesUrls[index].src = "/img/pc/loadfail.png";
        this.openImagesUrls[index].alt = fd.msg;
      }
    },
    //下载图片预览
    dImage:async function (f){
      let index = this.openImagesUrlsIndex[f.fileMd5];
      let fso = await FileSoundOut(this.fileDownloadUrl,f.fileMd5,f.fileName,'JOURNAL');
      if(!fso.state){
        this.openImagesUrls[index].src = "/img/pc/loadfail.png";
        this.openImagesUrls[index].alt = fso.msg;
        return;
      }
      //试探成功,开始下载
      let fd = await FileDoownloadSmall(fso);
      if(fd.state){
        let blob = new Blob([fd.bytes],{type:f.mediaType});
        let mediaType = f.mediaType.toUpperCase();
        let mediaTypes = mediaType.split("/");
        switch (mediaTypes[1]){
          case "HEIC": //苹果设备拍摄的 新图片格式
            try {
              //有时候这个类型也不一定是准确的,如果本身就可以被读取,就会拒绝转换,这里如果报错后使用原始blob
              blob = await HeicToCommon(blob);
            }catch (e) {}
            break;
        }
        let src = window.URL.createObjectURL(blob);
        this.openImagesUrls[index].src = src;
      }else{
        this.openImagesUrls[index].src = "/img/pc/loadfail.png";
        this.openImagesUrls[index].alt = fd.msg;
      }
    },
    //提示
    showToast:function(type,message){
      if(!type){
        type = "success";
      }
      notification[type]({
        title: "提示:",
        content: message,
        duration: 2500,
        keepAliveOnHover: true
      });
    },
    showDialog:function(type,content,okClick){
      if(!type){
        type = "success";
      }
      dialog[type]({
        title: "提示",
        content: content,
        positiveText: "确定",
        negativeText: "取消",
        maskClosable: false,
        onPositiveClick:() =>{
          okClick();
        }
      });
    },
  }
}

</script>

<style scoped>

</style>