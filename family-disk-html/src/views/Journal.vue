<template>

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
      <van-cell is-link :title="item.title" :label="item.date" @click="open(item)" />
      <template #right>
        <van-button style="height: 66px;" square hairline type="danger"  @click="delJournal(item)" text="删除" />
        <van-button style="height: 66px;" square hairline type="primary"  @click="updateJournal(item)" text="修改" />
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
                      @click-preview="openPreview"
                      preview-size="50"
                      :max-size="1024 * 1024 * 32"
                      @oversize="onOversize"
                      :max-count="10"
                      :before-read="beforeRead"
                      :disabled="uploadDisabled"
                      accept="image/*,video/*,audio/*"
                      v-model="uploadFiles" multiple>
        </van-uploader>
        <van-button round block :disabled="uploadDisabled" type="primary" @click="" native-type="submit">
          保存日记
        </van-button>
      </div>

    </div>
  </van-action-sheet>

  <van-image-preview :onClose="closeVideo" v-model:show="showVideo" :images="videoUrls"  closeable>
    <template #image="{src}">
      <video :src="src" ref="previewVideoRef" style="width: 100%;" controls />
    </template>
  </van-image-preview>

  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
import axios from "axios";
import {Uploader,Field,ActionSheet,List, CellGroup, Cell, Search, BackTop, showToast,Popover,Button,SwipeCell,ImagePreview} from 'vant';
//vant适配桌面端
import '@vant/touch-emulator';
import {ref} from "vue";


export default {
  name: "Journal",
  components:{
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
      journalList:[
        {
          title:"测试标题",
          date:"2023-01-13",
          body:"这是一段很长很长的日记内容",
          urls:[
            {url:"/disk0.png"},
            {url:"/disk1.png"}
          ]
        }
      ]
    }
  },
  methods:{
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
      this.$refs.previewVideoRef.pause();
      this.videoUrls = [];
      this.showVideo = false;
    },
    //点击图片预览前回调
    openPreview:function (f) {
      let file = f.file;
      if(!file){
        //没有file对象,预览的是远程资源
      }
      let mediaType = file.type.toUpperCase();
      mediaType = mediaType.substring(0,mediaType.indexOf("/"));
      switch (mediaType){
        case "VIDEO"://视频
        case "AUDIO"://音频
            //切换到播放器播放
            let blob = file.slice(0,file.size);
            let url = window.URL.createObjectURL(blob);
            this.videoUrls = [url];
            this.showVideo = true;
            return false;
      }
      return true;
    },
    //修改日记
    updateJournal:function (item) {

    },
    //删除日记
    delJournal:function (item) {

    },
    //关闭写日记面板
    closeJournalSave:function (item){
      this.journalDate = "";
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

    },
    //打开日记
    open:function (item) {
      this.journalDate = item.date;
      this.showJournalSave = true;
      this.uploadDisabled = true;
      this.journalTitle = item.title;
      this.journalBody = item.body;
      this.uploadFiles = item.urls;
    }
  }
}
</script>

<style scoped>

</style>