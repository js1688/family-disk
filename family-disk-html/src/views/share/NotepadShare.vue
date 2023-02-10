<template>

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
            提交申请
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>


  <v-md-editor
      v-if="showNote"
      v-model="text"
      :autofocus="true"
      mode="preview"
  >
  </v-md-editor>
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
  ActionSheet, showConfirmDialog
} from 'vant';
//vant适配桌面端
import '@vant/touch-emulator';
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
  name: "NotepadShare",
  components: {
    VMdEditor,
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
    return {
    };
  },
  created() {
    /**
     * 分享笔记路径: 是否需要输入密码/链接uuid
     * http://localhost:5173/#/share/notepad?lock=true&uuid=asddhsdhiweq-sdas-666
     * @type {{lock: null, uuid: null}}
     */
    //从页面地址中获取到参数
    for (const key in this.param) {
      let v = decodeURIComponent((new RegExp('[?|&]'+key+'=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null;
      this.param[key] = v;
    }
    //是否需要输入密码
    if(this.param.lock == 'true'){
      this.passwordShow = true;
    }else{
      this.getBody();//不需要密码,直接获得内容
    }
  },
  data(){
    return {
      passwordShow:false,
      showNote:false,
      text:'',
      param : {lock:null,uuid:null,password:null}
    }
  },
  methods:{
    //获得分享内容
    getBody:function (){
      this.passwordShow = false;
      this.isOverlay = true;
      let self = this;
      axios.get(`/note/share/getBody/${this.param.uuid}/${this.param.password}`).then(function (res){
        if(res.data.result){
          this.text = res.data.data;
          this.showNote = true;
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