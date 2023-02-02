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

  <div style="">
    <Toolbar
        style="border-bottom: 1px solid #ccc"
        :editor="editorRef"
        :defaultConfig="toolbarConfig"
        :mode="mode"
    />
    <Editor
        style="height: 500px; overflow-y: hidden;"
        v-model="valueHtml"
        :defaultConfig="editorConfig"
        :mode="mode"
        @onCreated="handleCreated"
    />
  </div>

  <van-back-top ight="15vw" bottom="10vh" />
</template>

<script>
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { onBeforeUnmount, ref, shallowRef, onMounted } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import { Boot} from "@wangeditor/editor";
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
  ActionSheet
} from 'vant';
import axios from "axios";

//自定义工具栏功能,保存,退出
class MyButtonMenu {
  constructor() {
    this.title = '保存' // 自定义菜单标题
    // this.iconSvg = '<svg>...</svg>' // 可选
    this.tag = 'button'
  }

  // 获取菜单执行时的 value ，用不到则返回空 字符串或 false
  getValue(editor) {                              // JS 语法
    return ' hello '
  }

  // 菜单是否需要激活（如选中加粗文本，“加粗”菜单会激活），用不到则返回 false
  isActive(editor) {                    // JS 语法
    return false
  }

  // 菜单是否需要禁用（如选中 H1 ，“引用”菜单被禁用），用不到则返回 false
  isDisabled(editor) {                     // JS 语法
    return false
  }

  // 点击菜单时触发的函数
  exec(editor, value) {                              // JS 语法
    if (this.isDisabled(editor)) return
    editor.insertText(value) // value 即 this.value(editor) 的返回值
  }

}

const menu1Conf = {
  key: 'menu1', // 定义 menu key ：要保证唯一、不重复（重要）
  factory() {
    return new MyButtonMenu() // 把 `YourMenuClass` 替换为你菜单的 class
  },
}

const module = {                      // JS 语法
  menus: [menu1Conf]
}
Boot.registerModule(module)

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
    [DropdownItem.name]:DropdownItem,
    [ActionSheet.name]:ActionSheet
  },
  setup() {
    const addActions = [
      { text: '笔记', icon: 'notes-o',name:'note'},
      { text: '待办', icon: 'passed',name:'matter'}
    ];
    const showPopover = ref(false);

    const menuTypeOptions = [
      { text: '笔记', value: 0 },
      { text: '待办', value: 1 }
    ];

    // 编辑器实例，必须用 shallowRef
    const editorRef = shallowRef()

    // 内容 HTML
    const valueHtml = ref('<p>hello</p>')

    // 模拟 ajax 异步获取内容
    onMounted(() => {
      setTimeout(() => {
        valueHtml.value = '<p>模拟 Ajax 异步设置内容</p>'
      }, 1500)
    })

    const toolbarConfig = {
      excludeKeys:["fullScreen"],//屏蔽工具
      insertKeys:{index:0,keys: ['menu1']}//添加额外的工具
    }
    const editorConfig = { placeholder: '请输入内容...'}

    // 组件销毁时，也及时销毁编辑器
    onBeforeUnmount(() => {
      const editor = editorRef.value
      if (editor == null) return
      editor.destroy()
    })

    const handleCreated = (editor) => {
      editorRef.value = editor // 记录 editor 实例，重要！
      editorRef.value.fullScreen();//默认打开全屏

    }
    return {
      addActions,
      showPopover,
      menuTypeOptions,

      editorRef,
      valueHtml,
      mode: 'simple', // default 或 'simple'
      toolbarConfig,
      editorConfig,
      handleCreated
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
      menuLabelOptions:[],
      showNote:false
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
        case 'note':
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