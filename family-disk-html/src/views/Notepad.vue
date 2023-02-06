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


  <van-action-sheet v-model:show="showNote" :round="false">
    <div>
      <v-md-editor
          v-if="showNote"
          v-model="text"
          :disabled-menus="disabledMenus"
          :right-toolbar="rightToolbar"
          :left-toolbar="leftToolbar"
          :toolbar="toolbar"
          :default-fullscreen="defaultFullscreen"
          :autofocus="true"
          :mode="mode"
          @save="save"
      >
      </v-md-editor>
    </div>
  </van-action-sheet>



  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
import {ref} from 'vue';
import {
  Popover,
  Button,
  BackTop,
  Overlay,
  Search,
  Loading,
  showToast,
  DropdownMenu,
  DropdownItem,
  ActionSheet, showConfirmDialog
} from 'vant';
import VMdEditor from '@kangc/v-md-editor';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
import '@kangc/v-md-editor/lib/theme/style/github.css';
import createTodoListPlugin from '@kangc/v-md-editor/lib/plugins/todo-list/index';
import hljs from 'highlight.js';
VMdEditor.use(githubTheme, {
  Hljs: hljs,
});
import axios from "axios";


export default {
  name: "Notepad",
  components: {
    VMdEditor,
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
    const addActions = [
      { text: '笔记', icon: 'notes-o',name:'note'},
      // { text: '待办', icon: 'passed',name:'matter'}
    ];
    const showPopover = ref(false);

    const menuTypeOptions = [
      { text: '笔记', value: 0 },
      // { text: '待办', value: 1 }
    ];

    return {
      addActions,
      showPopover,
      menuTypeOptions
    };
  },
  data(){
    let self = this;
    self.toolbar = {
      exitToolbar:{
        icon:"v-md-icon-undo",
        title:'退出',
        action(editor) {
          if(self.text != self.originalText){
            this.isOverlay = true;
            showConfirmDialog({
              title: '保存?',
              message:'内容已发生改变,是否保存?'
            }).then(() => {
              self.isOverlay = false;
              self.showNote = false;
            }).catch(function (error) {
              console.log(error);
              self.isOverlay = false;
            });
          }else{
            self.showNote = false;
          }
        },
      },
      checkboxToolbar:{
        icon:"v-md-icon-checkbox",
        title:'复选框',
        action(editor) {
          editor.insert(function (selected) {
            return {
              text: '- [ ] ',
              selected: selected,
            };
          });
        }
      }
    };
    return {
      defaultFullscreen:true,
      mode:"edit",
      disabledMenus:['image/upload-image','h/h4','h/h5','h/h6'],
      rightToolbar:'exitToolbar',
      leftToolbar:"h bold strikethrough quote ul ol checkboxToolbar hr link image code | save",
      text:'',
      originalText:'',
      isOverlay:false,
      loading:false,
      finished:false,
      keyword:"",
      menuTypeValue:0,
      menuLabelValue:null,
      menuLabelOptions:[],
      showNote:false
    };
  },
  //页面打开时初始化
  created(){
    let self = this;
    this.getMenuLabelOption();

  },
  methods:{
    //富文本编辑器触发保存
    save:function (text, html){
      console.log(text);
      console.log(html);
      showToast('点击保存');
    },
    //用户标签获取
    getMenuLabelOption:function (){
      let list = [{ text: '全部', value: 0 }];//默认一个
      //获取用户添加的标签
      list.push({ text: '个人', value: 1 });
      list.push({ text: '生活', value: 2 });
      list.push({ text: '工作', value: 3 });
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
        case 'note':
          this.originalText='原始文字';
          this.text = this.originalText;
          // this.mode='preview';
          // this.defaultFullscreen = false;
          this.showNote = true;
          break;
        case 'matter':
          showToast("待办");
          break;
      }
    },
  }
}
</script>

<style scoped>

</style>