<template>
  <n-space vertical>
    <n-layout has-sider sider-placement="right" position="absolute" style="top: 0; bottom: 0">
      <n-layout-content>
        <!-- 文件管理 -->
        <div v-if="manageIf">
          <n-spin :show="isOverlay">
            <n-card :bordered="false">
              <div style="width: 50%;display:inline-block;">
                <n-space :size="40">
                  <n-badge value="上传" type="info" :offset="[8, -8]">
                    <n-upload :default-upload="false"
                              :multiple="true"
                              :show-file-list="false"
                              @update:file-list="addUploadFile" >
                      <n-button text style="font-size: 24px">
                        <n-icon>
                          <cloud-upload-outline />
                        </n-icon>
                      </n-button>
                    </n-upload>
                  </n-badge>
                  <n-badge value="下载" type="info" :offset="[8, -8]">
                    <n-button text style="font-size: 24px" :disabled="!rowKeys || rowKeys.length == 0" @click="downloads">
                      <n-icon>
                        <cloud-download-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="移动" type="info" :offset="[8, -8]">
                    <n-button @click="showMoveDirectory = true;onMoveLoad();rowKey = null;" :disabled="!rowKeys || rowKeys.length == 0"  text style="font-size: 24px">
                      <n-icon>
                        <return-down-forward />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="目录" type="info" :offset="[8, -8]">
                    <n-button @click="showAddDirectory = true" text style="font-size: 24px">
                      <n-icon>
                        <add-circle-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="删除" type="info" :offset="[8, -8]">
                    <n-button :disabled="!rowKeys || rowKeys.length == 0" @click="delsDirectory" text style="font-size: 24px">
                      <n-icon>
                        <trash-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                </n-space>
              </div>
              <div style="width: 50%;display:inline-block;">
                <n-space justify="end" >
                  <n-input-group>
                    <n-input v-model:value="keyword" placeholder="搜索"/>
                    <n-button ghost @click="onLoad">
                      <n-icon>
                        <search-outline />
                      </n-icon>
                    </n-button>
                  </n-input-group>
                </n-space>
              </div>
              <n-divider />
              <div style="width: 80%;display:inline-block;">
                <n-breadcrumb>
                  <n-breadcrumb-item v-for="(item) in openPath" @click="jump(item)">{{item.name}}</n-breadcrumb-item>
                </n-breadcrumb>
              </div>
              <div style="width: 20%;display:inline-block;">
                <n-space justify="end" >
                  <n-tag :bordered="false" :color="{ color: '#FFF'}">已全部加载,共{{folderData.length}}项</n-tag>
                </n-space>
              </div>
              <n-data-table
                  :columns="folderColumns"
                  :data="folderData"
                  :pagination="false"
                  :bordered="false"
                  :row-key="setRowKey"
                  :checked-row-keys="rowKeys"
                  @update:checked-row-keys="handleCheck"
                  virtual-scroll
                  :max-height="maxHeight - 50"
              />
            </n-card>

            <n-modal
                v-model:show="showAddDirectory"
                :mask-closable="false"
                preset="dialog"
                title="新建目录"
                positive-text="确认"
                negative-text="取消"
                @positive-click="addDirectory"
                @negative-click="showAddDirectory = false"
            >
              <n-divider />
              <n-input v-model:value="simpleField" placeholder="请输入目录名称"/>
            </n-modal>

            <n-drawer :mask-closable="false" v-model:show="showMoveDirectory" :width="502">
              <n-drawer-content title="选择移动的目的地" closable>
                <n-data-table
                    :columns="moveColumns"
                    :data="moveData"
                    :row-key="setRowKey"
                    :pagination="false"
                    :bordered="false"
                    virtual-scroll
                    :max-height="maxHeight"
                    @update:checked-row-keys="handleCheckSingle"
                />
                <n-card :bordered="false" style="position: absolute;bottom: 0">
                  <n-space>
                    <n-button type="info" @click="okMoveDirectory(null)">确定</n-button>
                    <n-button type="info" @click="okMoveDirectory(0)">移动到根目录</n-button>
                    <n-button @click="showMoveDirectory = false;rowKeys=[];">取消</n-button>
                  </n-space>
                </n-card>
              </n-drawer-content>
            </n-drawer>

            <n-drawer :mask-closable="false" v-model:show="showImages" :width="500">
              <n-drawer-content title="图片预览列表" closable>
                <n-image-group>
                  <n-space>
                    <n-image v-for="m in openImagesUrls"
                        width="100"
                        :src="m.src" :alt="m.alt"
                    />
                  </n-space>
                </n-image-group>
              </n-drawer-content>
            </n-drawer>

            <n-drawer :mask-closable="false" v-model:show="showVideos" :width="500">
              <n-drawer-content title="视频预览列表" closable>
                <n-image-group>
                  <n-space>
                    <n-image v-for="m in openImagesUrls"
                             width="100"
                             :src="m.src" :alt="m.alt"
                             preview-disabled
                             @click="createPlayUrl(m)"
                             style="cursor:pointer"
                    />
                  </n-space>
                </n-image-group>
              </n-drawer-content>
            </n-drawer>

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

            <n-modal
                v-model:show="showUpdateName"
                :mask-closable="false"
                preset="dialog"
                title="重命名目录"
                positive-text="确认"
                negative-text="取消"
                @positive-click="updateName"
                @negative-click="showUpdateName = false;rowKeys=[];"
            >
              <n-divider />
              <n-input v-model:value="simpleField" placeholder="请输入新的目录名称"/>
            </n-modal>

          </n-spin>
        </div>
        <!-- 下载列表 -->
        <div v-if="downloadListIf" style="width: 100%;height: 100%;">
          <n-spin :show="isOverlay">
            <n-space vertical v-if="downloadList && Object.keys(downloadList).length !== 0">
              <n-card :title="value.data.name" v-for="value in downloadList" :bordered="false">
                <n-space vertical>
                  <n-progress type="line" status="success" :percentage="value.data.scale">
                    文件大小 {{Math.floor(value.data.total/1024/1024)}}(MB) 进度 {{value.data.scale}}%
                  </n-progress>
                  <n-space justify="end">
                    <n-button @click="stopFileDownload(value.data.fileMd5)">取消</n-button>
                  </n-space>
                </n-space>
                <n-divider />
              </n-card>
            </n-space>
          </n-spin>
          <div v-if="!downloadList || Object.keys(downloadList).length === 0" class="div-center">
            <n-empty size="large" description="没有正在下载的文件" />
          </div>
        </div>
        <!-- 上传列表 -->
        <div v-if="uploadListIf" style="width: 100%;height: 100%;">
          <n-spin :show="isOverlay">
            <n-space vertical v-if="uploadList && Object.keys(uploadList).length !== 0">
              <n-card :title="value.file.name" v-for="value in uploadList" :bordered="false">
                <n-space vertical>
                  <n-progress type="line" status="success" :percentage="value.scale">
                    文件大小 {{Math.floor(value.sliceInfo.totalSize/1024/1024)}}(MB) 进度 {{value.scale}}%
                  </n-progress>
                  <n-space justify="end">
                    <n-button @click="stopFileUpload(value.md5)">取消</n-button>
                  </n-space>
                </n-space>
                <n-divider />
              </n-card>
            </n-space>
          </n-spin>
          <div v-if="!uploadList || Object.keys(uploadList).length === 0" class="div-center">
            <n-empty size="large" description="没有正在上传的文件" />
          </div>
        </div>
        <!-- 回收站 -->
        <div v-if="dustbinIf" style="width: 100%;height: 100%;">
          <n-spin :show="isOverlay">
            <h1>未实现</h1>
          </n-spin>
        </div>
      </n-layout-content>
      <n-layout-sider
          bordered
          collapse-mode="width"
          :width="150"
          :collapsed-width="64"
          :collapsed="collapsed"
          show-trigger
          @collapse="collapsed = true"
          @expand="collapsed = false"
      >
        <n-menu
            v-model:value="activeKey"
            :collapsed="collapsed"
            :collapsed-width="64"
            :collapsed-icon-size="22"
            :options="menuOptions"
            @update:value="handleUpdateValue"
        />
      </n-layout-sider>
    </n-layout>
  </n-space>
</template>

<script>
import { h, ref } from "vue";
import { NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider ,NDescriptions,NDescriptionsItem,
  NAnchorLink,NAnchor,NPopconfirm,NButton,NLayoutContent,NImage,NSpin,NProgress,NDataTable,
  NForm,NFormItem,NInput,NInputGroup,NLayoutFooter,NLayoutHeader,NCard,NModal,NBadge,NUpload,
  NAvatar,NDivider,NTag,NBreadcrumb,NBreadcrumbItem,NDrawer,NDrawerContent,NEmpty,NImageGroup,
  createDiscreteApi
} from "naive-ui";
import {
  ExitOutline, EnterOutline, PersonAddOutline, BackspaceOutline, AddCircleOutline,
  InformationCircleOutline, ShareSocialOutline,TrashOutline,CloudUploadOutline,
  MailOutline,CloudOutline,FolderOutline,FolderOpenOutline,CloudDownloadOutline,
  BuildOutline, OptionsOutline,PersonOutline,ReturnDownForward,SearchOutline,ListOutline
} from "@vicons/ionicons5";
import JSZip from "jszip";
import {key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";
import 'video.js/dist/video-js.css';
import videojs from "video.js";


import {
  StopFileDownload,
  FileDownload,
  FileSoundOut,
  getDownloadList,
  FileDoownloadSmall,
  FileDoownloadAppoint
} from "@/global/FileDownload";
import {
  Base64toBlob,
  CountFileSliceInfo,
  FileMd5,
  GetVideoCoverBase64,
  HeicToCommon,
  LivpToCommon
} from "@/global/StandaloneTools";
import {FileUpload, getUploadList, StopFileUpload} from "@/global/FileUpload";
import {showToast} from "vant";

const { notification,dialog} = createDiscreteApi(['notification','dialog'])

const createColumns = ({
                         buttons,
                         cols
                       }) => {

  if(buttons){
    for (let i = 0; i < buttons.length; i++) {
      const b = buttons[i];
      cols.push(
          {
            title: b.title,
            key: b.key,
            width:b.width,
            titleColSpan:b.titleColSpan,
            render(row) {
              return h(
                  NButton,
                  {
                    strong: true,
                    tertiary: true,
                    size: "small",
                    disabled: b.disabled ? b.disabled(row) : false,
                    onClick: () => b.play(row)
                  },
                  { default: () => b.name }
              );
            }
          }
      );
    }
  }
  return cols;
};

export default {
  name: "Disk",
  setup() {
    function renderIcon(icon) {
      return () => h(NIcon, null, { default: () => h(icon) });
    }
    const menuOptions = [
      {
        label: "文件管理",
        key: "manage",
        icon:renderIcon(FolderOpenOutline)
      },
      {
        label: "上传列表",
        key: "uploadList",
        icon:renderIcon(CloudUploadOutline)
      },
      {
        label: "下载列表",
        key: "downloadList",
        icon:renderIcon(CloudDownloadOutline)
      },
      {
        label: "回收站",
        key: "dustbin",
        icon:renderIcon(TrashOutline)
      }
    ];
    return {
      collapsed: ref(false),
      activeKey: ref('manage'),
      menuOptions,
      CloudUploadOutline,
      CloudDownloadOutline,
      ReturnDownForward,
      SearchOutline,
    }
  },
  components: {
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NAnchorLink,NAnchor,NDescriptions,NDescriptionsItem,NUpload,
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,NDataTable,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,NEmpty,
    NLayoutHeader,AddCircleOutline,NModal,NTag,FolderOutline,TrashOutline,CloudUploadOutline,CloudDownloadOutline,
    ReturnDownForward,NBadge,NBreadcrumb,NBreadcrumbItem,SearchOutline,ListOutline,NDrawer,NDrawerContent,NImageGroup,
  },
  props: {

  },
  created() {
    if(localStorage.getItem(key().authorization) != null){
      this.onLoad();
      //添加定时任务,刷新下载列表
      let self = this;
      setInterval(function (){
        //更新下载列表进度
        let list = getDownloadList();
        self.downloadList = [];
        for (const listKey in list) {
          let v = list[listKey];
          v.data['scale'] = v.data.progress == 0 ? 0 : Math.floor(v.data.progress/v.data.sliceNum*100);
          self.downloadList.push(v);
        }
        //更新上传列表进度
        let uplist = getUploadList();
        self.uploadList = [];
        for (const uplistKey in uplist) {
          let v = uplist[uplistKey];
          v['scale'] = v.progress == 0 ? 0 : Math.floor(v.progress/v.sliceInfo.sliceNum*100);
          self.uploadList.push(v);
        }
      }, 500);
    }
  },
  data(){
    return {
      showImages:false,
      showVideos:false,
      showPlayVideo:false,
      dustbinIf:false,
      uploadListIf:false,
      downloadList:[],
      uploadList:[],
      roleWrite:localStorage.getItem(key().useSpaceRole) == 'WRITE',
      showMoveDirectory:false,
      showAddDirectory:false,
      showUpdateName:false,
      simpleField:"",
      maxHeight:document.documentElement.clientHeight - 250,
      manageIf:true,
      downloadListIf:false,
      isOverlay:false,
      folderData:[],
      pid:0,
      keyword:'',
      rowKeys:[],
      rowKey:null,
      folderColumns:createColumns({
        buttons:[
          {
            name:"删除",
            title:"操作",
            titleColSpan:5,
            width:60,
            key:"s",
            play:this.delDirectory
          },
          {
            name:"下载",
            width:60,
            key:"x",
            disabled:(row)=> {
              return row.type == 'FOLDER';
            },
            play:this.download
          },
          {
            name:"移动",
            width:60,
            key:"y",
            play:this.moveSingle
          },
          {
            name:"重命名",
            width:75,
            key:"c",
            play:this.renameSingle
          },
          {
            name:"分享",
            width:70,
            key:"f",
            play:null
          }
        ],
        cols:[
          {
            type: "selection"
          },
          {
            title: "目录名",
            key: "name",
            cellProps:this.cellProps
          },
          {
            title: "大小(MB)",
            width:100,
            key: "size"
          },
          {
            title: "修改时间",
            width:200,
            key: "updateTime"
          }
        ]
      }),
      openPath:[{name:'文件管理',id:0}],
      moveColumns:createColumns({
        cols:[
          {
            type: "selection",
            multiple: false
          },
          {
            title: "目录名",
            key: "name"
          }
        ]
      }),
      moveData:[],
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
      playerSelectd:0,//播放器选择,0使用的插件播放器,1原生播放器
    }
  },
  methods:{
    //添加新的下载文件
    addUploadFile:async function (e) {
      let last = e[e.length - 1];//最后添加的文件,如果选择多个文件,这个方法会被触发多次
      //判断文件是否重复添加
      for (let i = 0; i < e.length - 1; i++) {
        if(last.name == e[i].name){
          e.splice(i,1);//删除掉添加的内容
          this.showToast("error",`文件名:[${last.name}]正在上传,请勿重复上传`);
          return;
        }
      }
      //计算分片大小
      let sliceInfo = CountFileSliceInfo(last.file);
      //计算md5值
      let md5 = await FileMd5(last.file,sliceInfo.sliceSize,sliceInfo.sliceNum);
      let self = this;
      let ret = await FileUpload(sliceInfo,last.file,md5,'CLOUDDISK',this.pid,function (q) {
        //将文件与网盘目录建立关系
        axios.post('/netdisk/addDirectory', {
          name: q.file.name,
          pid: q.pid,
          fileMd5: q.md5,
          type:"FILE",
          mediaType:q.file.type
        }).then(function (response) {
          if(response.data.result && response.data.data){
            self.onLoad();
          }
          self.showToast(response.data.result ? "success" : "error", response.data.message);
        }).catch(function (error) {
          console.log(error);
        });
      });
      this.showToast(ret.state ? null : "error",ret.msg);
    },
    //关闭播放视频
    closePlayVideo:function (){
      //停止播放
      try {
        if(this.$refs.originalVideoPlayer){
          this.$refs.originalVideoPlayer.pause();
          this.$refs.originalVideoPlayer.src = "";
        }
        if(this.myPlayer){
          this.myPlayer.dispose();
          this.myPlayer.pause();
        }
        this.videoOptions.sources = [];
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
    //组织在线播放视频的地址
    createPlayUrl:function (item){
      let url = key().baseURL+"file/media/play/CLOUDDISK/"+localStorage.getItem(key().authorization)  + "/" + item.fileMd5;
      this.videoOptions.sources = [{src:url,type:item.mediaType}];
      this.showPlayVideo = true;
      this.showPlayVideoName = item.name;
      this.playerSelectd = 0;//设置打开后默认播放器
    },
    //打开视频预览列表,并不是播放视频
    openVideo:async function(){
      this.openImagesUrls = [];
      this.openImagesUrlsIndex = {};
      let ds = [];
      //将列表中所有是图片的文件分析出来,开始下载图片,提供预览
      for (let j = 0; j < this.folderData.length; j++) {
        let data = this.folderData[j];
        if (data.type == 'FILE') {
          let mediaType = data.mediaType.toUpperCase();
          let mediaTypes = mediaType.split("/");
          if (mediaTypes[0] == "VIDEO") {
            ds.push(data);
          }
        }
      }
      if(ds.length != 0) {
        this.showVideos = true;
        let self = this;
        //先循环一下列表,添加占位
        for (let i = 0; i < ds.length; i++) {
          let item = ds[i];
          self.openImagesUrlsIndex[item.fileMd5] = self.openImagesUrls.length;
          self.openImagesUrls.push({src: "/img/pc/loading.png", alt: null, fileMd5: item.fileMd5,mediaType:item.mediaType,name:item.name});//先添加进去预占位
        }
        //再循环列表,真正的加载照片
        for (let i = 0; i < ds.length; i++) {
          let item = ds[i];
          let index = self.openImagesUrlsIndex[item.fileMd5];
          if(!this.showVideos || !(index >= 0)){//每次下载时判断是否已经关闭预览了,避免浪费不必要的流量
            return
          }
          let fso = await FileSoundOut(item.fileMd5,item.name,'CLOUDDISK');
          if(!fso.state){
            self.openImagesUrls[index].src = "/img/pc/loadfail.png";
            self.openImagesUrls[index].alt = fso.msg;
            return;
          }
          //试探成功,开始下载
          //开始下载视频的第一帧,当做视频的封面
          //只取1mb,清晰度很高的视频想获取到第一帧,1mb肯定是不够的,不过为了不浪费流量,提高加载速度,能预览出大部分的即可
          let fd = await FileDoownloadAppoint(fso,0,1024*1024*1);
          if(fd.state){
            let blob = new Blob([fd.bytes]);
            let url = window.URL.createObjectURL(blob);
            let base64Promise = GetVideoCoverBase64(url);
            base64Promise.then(async function(base64){
              let b = await Base64toBlob(base64);
              self.openImagesUrls[index].src = window.URL.createObjectURL(b);//封面地址
            }).catch(function (e){
              self.openImagesUrls[index].src = "/img/pc/play.png";
              self.openImagesUrls[index].alt = "点击播放";
            });
          }else{
            self.openImagesUrls[index].src = "/img/pc/loadfail.png";
            self.openImagesUrls[index].alt = fd.msg;
          }
        }
      }
    },
    //打开苹果设备拍摄的实况图片
    openLivp:async function (append) {
      if(!append){//不是追加打开,就清除
        this.openImagesUrls = [];
        this.openImagesUrlsIndex = {};
      }
      let ds = [];
      //将列表中所有是苹果设备拍摄的实况图片的文件分析出来,开始下载图片,提供预览
      for (let j = 0; j < this.folderData.length; j++) {
        let data = this.folderData[j];
        if (data.type == 'FILE') {
          let index = data.name.lastIndexOf(".");
          if(index != -1) {
            let suffix = data.name.substring(index).toUpperCase();
            if(suffix == ".LIVP"){
              ds.push(data);
            }
          }
        }
      }
      if(ds.length != 0){
        this.showImages = true;
        let self = this;
        //先循环一下列表,添加占位
        for (let i = 0; i < ds.length; i++) {
          let item = ds[i];
          self.openImagesUrlsIndex[item.fileMd5] = self.openImagesUrls.length;
          self.openImagesUrls.push({src: "/img/pc/loading.png", alt: null, fileMd5: item.fileMd5});//先添加进去预占位
        }
        //再循环列表,真正的加载照片
        for (let i = 0; i < ds.length; i++) {
          let item = ds[i];
          let index = self.openImagesUrlsIndex[item.fileMd5];
          if(!this.showImages || !(index >= 0)){//每次下载时判断是否已经关闭预览了,避免浪费不必要的流量
            return
          }
          let fso = await FileSoundOut(item.fileMd5,item.name,'CLOUDDISK');
          if(!fso.state){
            self.openImagesUrls[index].src = "/img/pc/loadfail.png";
            self.openImagesUrls[index].alt = fso.msg;
            return;
          }
          //试探成功,开始下载
          let fd = await FileDoownloadSmall(fso);
          if(fd.state){
            let blob = new Blob([fd.bytes]);
            try{
              blob = await LivpToCommon(blob);
            }catch (e) {}
            let src = window.URL.createObjectURL(blob);
            self.openImagesUrls[index].src = src;
          }else{
            self.openImagesUrls[index].src = "/img/pc/loadfail.png";
            self.openImagesUrls[index].alt = fd.msg;
          }
        }
      }
      if(!append){
        await this.openImage(true)//同时打开普通图片
      }
    },
    //打开图片预览
    openImage:async function (append) {
      if(!append){//不是追加打开,就清除
        this.openImagesUrls = [];
        this.openImagesUrlsIndex = {};
      }
      let ds = [];
      //将列表中所有是图片的文件分析出来,开始下载图片,提供预览
      for (let j = 0; j < this.folderData.length; j++) {
        let data = this.folderData[j];
        if (data.type == 'FILE') {
          let mediaType = data.mediaType.toUpperCase();
          let mediaTypes = mediaType.split("/");
          if (mediaTypes[0] == "IMAGE") {
            ds.push(data);
          }
        }
      }
      if(ds.length != 0){
        this.showImages = true;
        let self = this;
        //先循环一下列表,添加占位
        for (let i = 0; i < ds.length; i++) {
          let item = ds[i];
          self.openImagesUrlsIndex[item.fileMd5] = self.openImagesUrls.length;
          self.openImagesUrls.push({src: "/img/pc/loading.png", alt: null, fileMd5: item.fileMd5});//先添加进去预占位
        }
        //再循环列表,真正的加载照片
        for (let i = 0; i < ds.length; i++) {
          let item = ds[i];
          let index = self.openImagesUrlsIndex[item.fileMd5];
          if(!this.showImages || !(index >= 0)){//每次下载时判断是否已经关闭预览了,避免浪费不必要的流量
            return
          }
          let fso = await FileSoundOut(item.fileMd5,item.name,'CLOUDDISK');
          if(!fso.state){
            self.openImagesUrls[index].src = "/img/pc/loadfail.png";
            self.openImagesUrls[index].alt = fso.msg;
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
            let src = window.URL.createObjectURL(blob);
            self.openImagesUrls[index].src = src;
          }else{
            self.openImagesUrls[index].src = "/img/pc/loadfail.png";
            self.openImagesUrls[index].alt = fd.msg;
          }
        }
      }
      if(!append){
        await this.openLivp(true)//同时打开livp图片
      }
    },
    //取消上传
    stopFileUpload:function (md5) {
      let self = this;
      StopFileUpload(md5).then(function (sf){
        self.showToast(sf.state ? "success" : "error",sf.msg);
      });
    },
    //取消下载
    stopFileDownload:function (md5) {
      let self = this;
      StopFileDownload(md5).then(function (sf){
        self.showToast(sf.state ? "success" : "error",sf.msg);
      });
    },
    //下载多个
    downloads:async function (){
      let ds = [];
      for (let i = 0; i < this.rowKeys.length; i++) {
        for (let j = 0; j < this.folderData.length; j++) {
          let data = this.folderData[j];
          if(this.rowKeys[i] == data.id){
            if(data.type == 'FILE'){
              ds.push(data);
            }
            break;
          }
        }
      }
      if(ds.length == 0){
        this.showToast("error","请选择要下载的文件,而不是目录.");
        return;
      }
      for (let i = 0; i < ds.length; i++) {
        await this.download(ds[i]);
      }
    },
    //下载单个文件,支持超大文件,分片方式下载,边下边存
    download:async function (item){
      let self = this;
      let fso = await FileSoundOut(item.fileMd5,item.name,'CLOUDDISK');
      if(!fso.state){
        self.showToast("error",fso.msg);
        return;
      }
      //试探成功,开始下载
      let fd = await FileDownload(fso);
      if(fd){
        self.showToast(fd.state ? "success" : "error",fd.msg);
      }
    },
    //选择单个重命名
    renameSingle:function (item) {
      this.showUpdateName = true;
      this.simpleField = item.name;
      this.rowKeys = [item.id];
    },
    //确定重命名
    updateName:function(){
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/updateName', {
        name: this.simpleField,
        id:this.rowKeys[0]
      }).then(function (response) {
        if(response.data.result){
          self.showUpdateName = false;
          self.simpleField = "";
          self.rowKeys = [];
          self.onLoad();
        }
        self.showToast(response.data.result ? "success" : "error", response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //选择单个移动
    moveSingle:function (item) {
      this.showMoveDirectory = true;
      this.onMoveLoad();
      this.rowKey = null;
      this.rowKeys = [item.id];
    },
    //确定移动
    okMoveDirectory: async function (def){
      if(def == 0){
        this.rowKey = def;
      }
      if(this.rowKey == null && this.rowKey != 0){
        this.showToast("error","请选择要移动的目标目录");
        return;
      }
      this.isOverlay = true;
      for (let i = 0; i < this.rowKeys.length; i++) {
        let ret = await axios.post('/netdisk/moveDirectory', {
          targetDirId: this.rowKey,
          id:this.rowKeys[i]
        });
        if(!ret.data.result){
          this.showToast("error", ret.data.message);
        }
      };
      this.isOverlay = false;
      this.showMoveDirectory = false;
      this.onLoad();
    },
    //表格选中时触发事件,适用单选
    handleCheckSingle:function (rowKeys) {
      this.rowKey = rowKeys[0];
    },
    //加载移动目录
    onMoveLoad: function () {
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/findDirectoryTree', {
        type: 'FOLDER'
      }).then(function (response) {
        if(response.data.result){
          self.moveData = response.data.datas;
        }else{
          self.showToast("error",response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //批量删除
     delsDirectory:function () {
      if(!this.rowKeys || this.rowKeys.length == 0){
        return;
      }
      let self = this;
      this.showDialog("warning",`是否批量删除${self.rowKeys.length}个目录!`,async function (){
        self.isOverlay = true;
        for (let i = 0; i < self.rowKeys.length; i++) {
          let ret = await axios.post('/netdisk/delDirectory', {
            id: self.rowKeys[i]
          });
          if(!ret.data.result){
            self.showToast("error", ret.data.message);
          }
        }
        self.isOverlay = false;
        self.onLoad();
      });
    },
    //添加目录
    addDirectory:function (){
      if(!this.simpleField){
        this.showToast("error", "请输入目录名称");
        return false;
      }
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/addDirectory', {
        name: this.simpleField,
        pid: this.pid,
        type:"FOLDER"
      }).then(function (response) {
        if(response.data.result){
          self.onLoad();
          self.simpleField = "";
          self.showAddDirectory = false;
        }
        self.showToast(response.data.result ? "success" : "error", response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
      return false;
    },
    //删除目录
    delDirectory:function(item){
      let self = this;
      this.showDialog("warning",'是否删除目录:' + item.name + '!',function (){
        self.isOverlay = true;
        axios.post('/netdisk/delDirectory', {
          id: item.id
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
    //数据表格,每一行的key设置方法
    setRowKey: function (row) {
      return row.id;
    },
    //数据表格被选中时触发事件
    handleCheck(rowKeys) {
      this.rowKeys = rowKeys;
    },
    //行点击时可以添加事件
    cellProps:function (row,index){
      let self = this;
      return {
        style: "cursor: pointer;",
        onClick:function () {
          if(row.type == 'FOLDER'){
            self.pid = row.id;
            self.onLoad();
            //将打开的目录追加到路径中
            self.openPath.push({name:row.name,id:row.id});
          }else if(row.type == 'FILE'){
            self.openFile(row);
          }
        }
      };
    },
    //打开文件
    openFile:function (item) {
      let mediaType = item.mediaType.toUpperCase();
      let mediaTypes = mediaType.split("/");
      switch (mediaTypes[0]) {
        case "IMAGE"://图片
          this.openImage();
          break
        case "VIDEO"://视频
          this.openVideo();
          break
        case "AUDIO"://音频
        default:
          //兼容特殊文件的打开
          let index = item.name.lastIndexOf(".");
          if(index != -1){
            let suffix = item.name.substring(index).toUpperCase();
            switch (suffix) {
              case ".LIVP"://livp,苹果设备拍摄的实况图片
                this.openLivp();
                return;
            }
          }
          this.showToast("warning",`不识别的类型[${mediaType}],不能在线预览,请下载文件到本地打开.`);
      }
    },
    //面包屑跳转
    jump: function (item){
      this.pid = item.id;
      this.keyword = "";
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
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/findDirectory', {
        keyword: this.keyword,
        pid: this.pid
      }).then(function (response) {
        if(response.data.result){
          self.folderData = response.data.datas;
          self.rowKeys = [];
        }else{
          self.showToast(response.data.result ? "success" : "error", response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //菜单切换
    menu:function (open){
      this.manageIf = false;
      this.dustbinIf = false;
      this.uploadListIf = false;
      this.downloadListIf = false;

      this[open] = true;
    },
    //根据点击不同的子菜单,显示不同的功能面板
    handleUpdateValue:function (key, item){
      switch (key){
        case "manage":
          this.menu("manageIf");
          break
        case "dustbin":
          this.menu("dustbinIf");
          break
        case "uploadList":
          this.menu("uploadListIf");
          break
        case "downloadList":
          this.menu("downloadListIf");
          break
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
  },
  beforeCreate() {
    //因为使用 streamSaver 下载 会生成 300*150 大小的 iframe 窗口,属性是隐藏的, 它会造成 body高度增加, 所以在页面创建之前,给body 添加忽略隐藏属性的高度
    document.querySelector('body').setAttribute('style', 'overflow:hidden;')
  },
}
</script>

<style scoped>
.div-center{
  width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;
}
</style>