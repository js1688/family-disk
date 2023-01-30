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
                      @click-preview="openPreview"
                      preview-size="80"
                      :max-size="1024 * 1024 * 32"
                      @oversize="onOversize"
                      :max-count="12"
                      :before-read="beforeRead"
                      :disabled="uploadDisabled"
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

  <van-image-preview :onClose="closeVideo" v-model:show="showVideo" :images="videoUrls"  closeable>
    <template #image="{src}">
      <video :src="src" ref="previewVideoRef" style="width: 100%;height: 800px;" controls autoplay />
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
      journalList:[]
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
      this.$refs.previewVideoRef.src = "";
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
            let url = null;
            if(f.url){
              url = f.url;
            }else{
              let blob = file.slice(0,file.size);
              url = window.URL.createObjectURL(blob);
            }
            this.videoUrls = [url];
            this.showVideo = true;
            return false;
      }
      return true;
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
            data["id"] = response.data.data;
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
          self.uploadFiles.push({status:"uploading",message:"加载中",file:
                {name:item.files[i].fileName,type:item.files[i].mediaType}
          });
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
            f["url"] = url;
            f.status = "done"
            f.message = "";
          }).catch(function (error) {
            console.log(error);
          });
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