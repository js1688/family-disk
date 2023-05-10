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
      id="list"
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      :style="`height:${maxHeight}px;overflow:auto;`"
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

    <van-back-top ight="15vw" bottom="10vh" target="#list"/>
  </van-list>

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

  <div style="position: fixed;right: 25px;bottom: 200px;">
    <van-popover placement="left" v-model:show="showPopover" :actions="addActions" @select="addSelect">
      <template #reference>
        <van-button icon="plus" type="primary"/>
      </template>
    </van-popover>
  </div>

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
import axios from "axios";
import {isToken, key} from "@/global/KeyGlobal";
import {GetUrlParam, HeicToCommon, LivpToCommon} from "@/global/StandaloneTools";
import 'video.js/dist/video-js.css';
import {ref} from "vue";
import {FileDoownloadSmall, FileDownload, FileSoundOut, getDownloadList, StopFileDownload} from "@/global/FileDownload";
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
    const showPopover = ref(false);
    const addActions = [
      { text: '下载列表', icon: 'wap-nav',name:'downloadList'}
    ];
    return {
      maxHeight:document.documentElement.clientHeight - 160,
      fileDownloadUrl:'/netdisk/share/slice/getFile',
      addActions,
      showPopover
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
    }, 500);
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
      downloadPlan: [],
      idMap: {},
      tempToken:'',
      pid:0,
      keyword:'',
      isOverlay:false,
      passwordShow:false,
      param : {lock:null,uuid:null,password:null},
      showPreviewVideo:false,
      showDownloadList:false,
      videoUrl:'',
    }
  },
  methods:{
    //取消下载
    stopFileDownload:function (md5) {
      StopFileDownload(md5).then(function (sf){
        showToast(sf.msg);
      });
    },
    addSelect: function (item){
      let cz = item.name;
      switch (cz) {
        case 'downloadList':
          this.showDownloadList = true;
          break;
      }
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
    //下载文件
    download:async function (item) {
      let fso = await FileSoundOut(`${this.fileDownloadUrl}/${item.id}`,item.fileMd5,item.name,'CLOUDDISK');
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
    //打开苹果设备拍摄的实况图片
    openLivp:async function (item) {
      this.isOverlay = true;
      let fso = await FileSoundOut(`${this.fileDownloadUrl}/${item.id}`,item.fileMd5,item.name,'CLOUDDISK');
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
      let fso = await FileSoundOut(`${this.fileDownloadUrl}/${item.id}`,item.fileMd5,item.name,'CLOUDDISK');
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
      let fso = await FileSoundOut(`${this.fileDownloadUrl}/${item.id}`,item.fileMd5,item.name,'CLOUDDISK');
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
          this.list = v.children;
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
        if(v.type == "FOLDER" && v.children){
          this.rebuild(v.children);
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
          self.idMap[0] = {children:l};
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