<template>
  <n-modal
      v-model:show="passwordShow"
      :mask-closable="false"
      :close-on-esc="false"
      preset="dialog"
      title="请输入密码后解锁内容"
      positive-text="确认"
      @positive-click="getBody"
  >
    <n-divider />
    <n-input v-model:value="param.password"
             type="password"
             show-password-on="mousedown"
             placeholder="请输入解锁密码"
    />
  </n-modal>


  <v-md-editor
      v-if="showNote"
      v-model="text"
      :autofocus="true"
      mode="preview"
  >
  </v-md-editor>
</template>

<script>
import { h, ref } from "vue";
import {
  NIcon, NLayout, NSwitch, NMenu, NSpace, NLayoutSider, NDescriptions, NDescriptionsItem,
  NAnchorLink, NAnchor, NPopconfirm, NButton, NLayoutContent, NImage, NSpin, NProgress, NDataTable,
  NForm, NFormItem, NInput, NInputGroup, NLayoutFooter, NLayoutHeader, NCard, NModal, NBadge, NList,
  NAvatar, NDivider, NTag, NBreadcrumb, NBreadcrumbItem, NDrawer, NDrawerContent, NTabs, NTabPane,
  createDiscreteApi, NListItem, NThing, NRadioGroup, NRadioButton, NDatePicker, NTimePicker, zhCN, dateZhCN,NConfigProvider
} from "naive-ui";
import {
  ExitOutline, EnterOutline, PersonAddOutline, BackspaceOutline, AddCircleOutline,
  InformationCircleOutline, ShareSocialOutline,TrashOutline,CloudUploadOutline,
  MailOutline,CloudOutline,FolderOutline,FolderOpenOutline,CloudDownloadOutline,BookmarkOutline,
  BuildOutline, OptionsOutline,PersonOutline,ReturnDownForward,SearchOutline,ListOutline
} from "@vicons/ionicons5";

import {key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";


import createTodoListPlugin from '@kangc/v-md-editor/lib/plugins/todo-list/index';
import '@kangc/v-md-editor/lib/plugins/todo-list/todo-list.css';

import VMdEditor from '@kangc/v-md-editor/lib/codemirror-editor';
import '@kangc/v-md-editor/lib/style/codemirror-editor.css';
import githubTheme from '@kangc/v-md-editor/lib/theme/github.js';
import '@kangc/v-md-editor/lib/theme/style/github.css';

// highlightjs
import hljs from 'highlight.js';

// codemirror 编辑器的相关资源
import Codemirror from 'codemirror';
// mode
import 'codemirror/mode/markdown/markdown';
import 'codemirror/mode/javascript/javascript';
import 'codemirror/mode/css/css';
import 'codemirror/mode/htmlmixed/htmlmixed';
import 'codemirror/mode/vue/vue';
// edit
import 'codemirror/addon/edit/closebrackets';
import 'codemirror/addon/edit/closetag';
import 'codemirror/addon/edit/matchbrackets';
// placeholder
import 'codemirror/addon/display/placeholder';
// active-line
import 'codemirror/addon/selection/active-line';
// scrollbar
import 'codemirror/addon/scroll/simplescrollbars';
import 'codemirror/addon/scroll/simplescrollbars.css';
// style
import 'codemirror/lib/codemirror.css';

import createCopyCodePlugin from '@kangc/v-md-editor/lib/plugins/copy-code/index';
import '@kangc/v-md-editor/lib/plugins/copy-code/copy-code.css';
import createTipPlugin from '@kangc/v-md-editor/lib/plugins/tip/index';
import '@kangc/v-md-editor/lib/plugins/tip/tip.css';
import createEmojiPlugin from '@kangc/v-md-editor/lib/plugins/emoji/index';
import '@kangc/v-md-editor/lib/plugins/emoji/emoji.css';

import createHighlightLinesPlugin from '@kangc/v-md-editor/lib/plugins/highlight-lines/index';
import '@kangc/v-md-editor/lib/plugins/highlight-lines/highlight-lines.css';

VMdEditor.Codemirror = Codemirror;
VMdEditor.use(githubTheme, {
  Hljs: hljs,
});
VMdEditor.use(createCopyCodePlugin());
VMdEditor.use(createTodoListPlugin());
VMdEditor.use(createTipPlugin());
VMdEditor.use(createEmojiPlugin());
VMdEditor.use(createHighlightLinesPlugin());
import VueClipboard from "vue-clipboard2";
import {showConfirmDialog, showToast} from "vant";
import {FormatDate, GetUrlParam} from "@/global/StandaloneTools";

const { notification,dialog} = createDiscreteApi(['notification','dialog'])


export default {
  name: "NotepadShare",
  components: {
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NAnchorLink,NAnchor,NDescriptions,NDescriptionsItem,BookmarkOutline,
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,NDataTable,NList,NThing,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,NTabPane,
    NLayoutHeader,AddCircleOutline,NModal,NTag,FolderOutline,TrashOutline,CloudUploadOutline,CloudDownloadOutline,
    ReturnDownForward,NBadge,NBreadcrumb,NBreadcrumbItem,SearchOutline,ListOutline,NDrawer,NDrawerContent,NTabs,
    NListItem,VMdEditor,NRadioGroup,NRadioButton,NDatePicker,NTimePicker,NConfigProvider
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
      let v = GetUrlParam(key);
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
      isOverlay:false,
      passwordShow:false,
      showNote:false,
      text:'',
      param : {lock:null,uuid:null,password:null}
    }
  },
  methods:{
    //获得分享内容
    getBody:function (){
      if(this.param.lock == 'true' && !this.param.password){
        this.showToast("error",'请输入解锁密码');
        return false;
      }
      this.isOverlay = true;
      let self = this;
      axios.get(`/note/share/getBody/${this.param.uuid}?password=${this.param.password == null ? '' : this.param.password}`).then(function (res){
        if(res.data.result){
          self.text = res.data.data;
          self.passwordShow = false;
          self.showNote = true;
        }else{
          self.showToast("error",res.data.message);
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
      return false;
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
  }
}
</script>

<style scoped>
</style>