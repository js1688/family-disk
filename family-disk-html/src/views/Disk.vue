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
        <van-button square type="primary" @click="" text="重命名" />
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
  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
import {Search, List, Cell, Tag, NavBar, showToast,BackTop,Popover,Button,ActionSheet,Field,Form,CellGroup} from 'vant';
import { Overlay,Loading } from 'vant';
import {ref} from "vue";
import axios from "axios";
import { SwipeCell } from 'vant';
import { showConfirmDialog } from 'vant';

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
    [SwipeCell.name]:SwipeCell
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
      list:[],
      openPath:[
          {name:'/',id:0}
      ]
    }
  },
  methods:{
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
          showToast("添加文件");
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