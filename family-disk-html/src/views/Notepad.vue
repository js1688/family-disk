<template>
  <van-overlay :show="isOverlay">
    <div class="wrapper">
      <van-loading />
    </div>
  </van-overlay>
  <van-search
      v-model="keyword"
      show-action
      placeholder="请输入搜索关键字"
  >
    <template #action>
      <div @click="onLoad">搜索</div>
    </template>
  </van-search>

  <van-dropdown-menu>
    <van-dropdown-item @change="onLoad" v-model="menuTypeValue" :options="menuTypeOptions" />
    <van-dropdown-item @change="onLoad" v-model="menuLabelValue" :options="menuLabelOptions" />
  </van-dropdown-menu>

  <div style="position: fixed;right: 25px;bottom: 200px;">
    <van-popover placement="left" v-model:show="showPopover" :actions="addActions" @select="addSelect">
      <template #reference>
        <van-button icon="plus" type="primary"/>
      </template>
    </van-popover>
  </div>

  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { onBeforeUnmount, ref, shallowRef, onMounted } from 'vue'
import { Editor, Toolbar} from '@wangeditor/editor-for-vue'
import {
  Popover,
  Button,
  BackTop,
  Overlay,
  Search,
  Loading,
  showToast,
  DropdownMenu,
  DropdownItem
} from 'vant';
import axios from "axios";
export default {
  name: "Notepad",
  components: { Editor, Toolbar ,
    [Popover.name]:Popover,
    [Button.name]:Button,
    [BackTop.name]:BackTop,
    [Overlay.name]:Overlay,
    [Search.name]:Search,
    [Loading.name]:Loading,
    [DropdownMenu.name]:DropdownMenu,
    [DropdownItem.name]:DropdownItem
  },
  setup() {
    const addActions = [
      { text: '笔记', icon: 'notes-o',name:'note'},
      { text: '备忘录', icon: 'passed',name:'memorandum'}
    ];
    const showPopover = ref(false);

    const menuTypeOptions = [
      { text: '笔记', value: 0 },
      { text: '备忘录', value: 1 }
    ];
    return {
      addActions,
      showPopover,
      menuTypeOptions
    };
  },
  data(){
    return {
      isOverlay:false,
      loading:false,
      finished:false,
      keyword:"",
      menuTypeValue:0,
      menuLabelValue:null,
      menuLabelOptions:[]
    }
  },
  created(){
    this.getMenuLabelOption();
  },
  methods:{
    //用户标签获取
    getMenuLabelOption:function (){
      let list = [{ text: '个人', value: 0 }];//默认一个
      //获取用户添加的标签
      list.push({ text: '生活', value: 1 });
      list.push({ text: '工作', value: 2 });
      this.menuLabelOptions = list;
      this.menuLabelValue = list[0].value;
    },
    //加载笔记和备忘录
    onLoad:function (){
      //this.isOverlay = true;
      let self = this;
      console.log(this.menuTypeValue);
      console.log(this.menuLabelValue);
      this.loading = false;//加载完毕
      this.finished = true;//全部数据加载完毕
    },
    //选择功能
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
  }
}
</script>

<style scoped>

</style>