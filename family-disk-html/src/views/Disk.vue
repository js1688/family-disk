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
      <van-cell is-link :arrow-direction="item.type == 'FOLDER' ? 'right' : 'down'"
                :title="item.name"
                @click="open(item)">
        <van-tag plain type="primary">{{item.type == 'FOLDER' ? '目录':'文件'}}</van-tag>
      </van-cell>
      <template #right>
        <van-button square type="danger" @click="delDirectory(item)" text="删除" />
        <van-button square type="primary" @click="moveDirectory(item)" text="移动" />
        <van-button square type="primary" @click="updateName(item)" text="重命名" />
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
    <div class="content">
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
    <div class="content">
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
    <div class="content">
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
    <div class="content">
      <div style="margin: 16px;">
        <van-uploader :before-read="beforeRead" :disabled="uploadDisabled" accept="*" v-model="uploadFiles" multiple>
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

  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
import {Search, List, Cell, Tag, NavBar, showToast,BackTop,Popover,Button,ActionSheet,Field,Form,CellGroup} from 'vant';
import { Overlay,Loading } from 'vant';
import {ref} from "vue";
import axios from "axios";
import { SwipeCell,Uploader } from 'vant';
import { showConfirmDialog } from 'vant';
import gws from "@/global/WebSocket";
import {key} from "@/global/KeyGlobal";


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
    [Uploader.name]: Uploader
  },
  setup() {
    const addActions = [
      { text: '新建目录', icon: 'wap-nav',name:'addDirectory'},
      { text: '上传文件', icon: 'upgrade',name:'addFile'},
      { text: '拍照上传', icon: 'photograph',name:'photograph'}
    ];
    const showPopover = ref(false);
    return {
      addActions,
      showPopover
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
      showUpload:false,
      uploadFiles:[],
      openPath:[
          {name:'/',id:0}
      ],
      subscribeAddFile:null
    }
  },
  methods:{
    //订阅文件上传结果
    subscribe:function (){
      if(this.subscribeAddFile == null){
        let topic = "/user/";
        topic += localStorage.getItem(key().userId) + "-" + localStorage.getItem(key().useSpaceId);
        topic += "/add/file/result";
        let self = this;
        this.subscribeAddFile = gws.methods.wsSubscribe(topic,function(msg){//订阅文件上传真实的写盘结果
          let ret = JSON.parse(msg.body);
          for (let i = 0; i < self.uploadFiles.length; i++) {
            let oldFile = self.uploadFiles[i];
            if (oldFile.file.name == ret.fileName) {//可以根据文件名称来判断,因为上传的时候,校验了文件名称在本批次必须是唯一的
              if (ret.result) {
                //将文件与网盘目录建立关系
                axios.post('/netdisk/addDirectory', {
                  name: ret.fileName,
                  pid: self.pid,
                  fileMd5: ret.fileMd5,
                  type:"FILE"
                }).then(function (response) {
                  if(response.data.result){
                    if(response.data.data){
                      self.list.push(response.data.data);
                    }
                    oldFile.status = "done";
                    oldFile.message = "完成";
                  }else{
                    oldFile.status = "failed";
                    oldFile.message = response.data.message;
                  }
                  if(self.checkEnd()){
                    showToast("文件全部上传完毕");
                    //全部上传完毕后取消禁用
                    self.uploadDisabled = false;
                  }
                }).catch(function (error) {
                  oldFile.status = "failed";
                  oldFile.message = "失败";
                  if(self.checkEnd()){
                    showToast("文件全部上传完毕");
                    //全部上传完毕后取消禁用
                    self.uploadDisabled = false;
                  }
                  console.log(error);
                });
              } else {
                oldFile.status = "failed";
                oldFile.message = "失败";
                if(self.checkEnd()){
                  showToast("文件全部上传完毕");
                  //全部上传完毕后取消禁用
                  self.uploadDisabled = false;
                }
              }
            }
          }
        });
      }
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
      //设置禁用
      this.uploadDisabled = true;
      this.subscribe();
      //开始循环上传文件
      for (let i = 0; i < this.uploadFiles.length; i++) {
        let f = this.uploadFiles[i];
        f.status = "uploading";
        f.message = "上传中";
        let data = new FormData();
        data.append('f', f.file);
        data.append('s', 'CLOUDDISK');
        let self = this;
        axios.post("/file/addFile", data, {
          header:{
            'Content-Type': 'multipart/form-data'
          }
        }).then((res) => {
          //这个结果如果是成功的不作为真正上传成功
          if(!res.data.result){
            f.status = "failed";
            f.message = "失败";
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
        this.openPath.push({name:item.name + "/",id:item.id});
      }else{//文件

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
        case 'addFile':
          this.showUpload = true;
          //如果上传列表中全是上传成功,已经上传完了且用户不需要太关心每个文件的状态,需要清除掉上传列表
          let clean = true;
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