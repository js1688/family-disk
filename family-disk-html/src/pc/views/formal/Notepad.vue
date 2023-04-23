<template>
  <n-card :bordered="false">
    <n-tabs type="line" animated v-model:value="tabsValue">
      <n-tab-pane name="0" tab="笔记">
        <n-spin :show="isOverlay">
          <n-layout has-sider>
            <n-layout-sider width="130" :bordered="true">
              <n-menu
                  :options="bookmarkOptions"
                  :icon-size="20"
                  v-model:value="activeKey"
                  @update:value="handleUpdateValue"
                  accordion
              />
            </n-layout-sider>
            <n-layout-content has-sider >
              <n-layout-sider width="290" :bordered="true">
                <n-space style="margin: 5px;">
                  <n-button v-if="roleWrite" @click="
                                                    mode='editable';
                                                    originalMdText='';
                                                    originalDocTag=activeKey == '0' ? '1' : activeKey;
                                                    mdText='';
                                                    docTag=originalDocTag;
                                                    docId=null;
                                            " text style="font-size: 24px;margin-left: 15px;">
                    <n-icon>
                      <add-circle-outline />
                    </n-icon>
                  </n-button>

                  <n-input-group>
                    <n-input :style="{ width: '230px'}" round v-model:value="keyword" placeholder="搜索"/>
                    <n-button round ghost @click="onLoad">
                      <n-icon>
                        <search-outline />
                      </n-icon>
                    </n-button>
                  </n-input-group>
                  <n-layout :style="`height:${maxHeight}px;overflow:auto;`" :native-scrollbar="false">
                    <n-list hoverable clickable >
                      <n-list-item v-for="item in list">
                        <n-thing @click="openText(item);mode='preview';" :title="item.keyword" :style="{ width: '230px' }">
                          <template #description>
                            <n-space size="small">
                              <n-tag :bordered="false" type="info" size="small">
                                {{tags[item.tag]}}
                              </n-tag>
                              <n-tag :bordered="false" type="info" size="small">
                                {{item.updateTime}}
                              </n-tag>
                            </n-space>
                          </template>
                        </n-thing>
                        <n-divider />
                        <n-space justify="end" v-if="roleWrite">
                          <n-button size="tiny" secondary strong @click="del(item)">删除</n-button>
                          <n-button size="tiny" secondary strong @click="share(item)">分享</n-button>
                          <n-button size="tiny" secondary strong @click="openText(item);mode='editable';">修改</n-button>
                        </n-space>
                      </n-list-item>
                    </n-list>
                  </n-layout>
                </n-space>
              </n-layout-sider>
              <n-layout-content :style="`height:${maxHeight + 80}px;overflow:auto;`" :native-scrollbar="false">
                <v-md-editor
                    v-model="mdText"
                    :height="`${maxHeight + 80}px`"
                    :right-toolbar="rightToolbar"
                    :left-toolbar="leftToolbar"
                    :toolbar="toolbar"
                    :autofocus="true"
                    :mode="mode"
                    @blur="blurMdText"
                    @save="saveMdText"
                >
                </v-md-editor>

                <n-modal
                    v-model:show="showTags"
                    :mask-closable="false"
                    preset="dialog"
                    title="请选择标签"
                >
                  <n-divider />
                  <n-space justify="center">
                    <n-radio-group v-model:value="docTag">
                      <n-radio-button
                          v-for="song in bookmarkOptions.slice(1, bookmarkOptions.length)"
                          :key="song.key"
                          :value="song.key"
                          :label="song.label"
                      />
                    </n-radio-group>
                  </n-space>
                </n-modal>

              </n-layout-content>
            </n-layout-content>

            <n-modal
                v-model:show="showShare"
                :mask-closable="false"
                preset="dialog"
                title="创建分享链接"
                positive-text="确认"
                negative-text="取消"
                @positive-click="createShare"
                @negative-click="showShare = false;"
            >
              <n-divider />
              <n-form
                  ref="shareformRef"
                  label-placement="left"
                  label-width="auto"
                  require-mark-placement="right-hanging"
                  :model="shareParam"
                  size="large"
                  :rules='{
                        invalidTime: {
                            required: true,
                            message: "请输入失效日期",
                            trigger: "blur"
                          }
                      }'
              >
                <n-form-item label="解锁密码:" path="password">
                  <n-input v-model:value="shareParam.password"
                           type="password"
                           show-password-on="mousedown"
                           placeholder="请输入解锁密码"
                  />
                </n-form-item>

                <n-form-item label="失效日期:" path="invalidTime">
                  <n-config-provider style="width: 100%;" :locale="locale" :date-locale="dateLocale">
                    <n-date-picker  type="datetime"
                                    input-readonly
                                    :is-date-disabled="invalidTimeRange"
                                    v-model:formatted-value="shareParam.invalidTime"
                                    value-format="yyyy-MM-dd HH:mm:ss"
                    />
                  </n-config-provider>
                </n-form-item>
              </n-form>
            </n-modal>
          </n-layout>
        </n-spin>
      </n-tab-pane>
      <n-tab-pane name="1" tab="待办">
        还未开发
      </n-tab-pane>
    </n-tabs>
  </n-card>
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
import {FormatDate} from "@/global/StandaloneTools";

const { notification,dialog} = createDiscreteApi(['notification','dialog'])


export default {
  name: "Notepad",
  setup() {
    function renderIcon(icon) {
      return () => h(NIcon, null, { default: () => h(icon) });
    }
    const tags = ["全部","个人","生活","工作"];
    const bookmarkOptions = [];
    for (let i = 0; i < tags.length; i++) {
      bookmarkOptions.push({
        label: tags[i],
        key: i+'',
        icon: renderIcon(BookmarkOutline),
      });
    }

    const mdTasg = [];
    for (let i = 1; i < tags.length; i++) {
      mdTasg.push({
        label: tags[i],
        key: i+'',
        icon: renderIcon(BookmarkOutline),
      });
    }

    return {
      locale: zhCN,
      dateLocale: dateZhCN,
      collapsed: ref(true),
      bookmarkOptions,
      tags,
      shareformRef:ref(null),
      rightToolbar:'preview sync-scroll fullscreen',
      leftToolbar:"undo redo clear h bold italic strikethrough quote ul ol table hr link image code checkboxToolbar emoji tip | tagSelected save",
    }
  },
  components:{
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NAnchorLink,NAnchor,NDescriptions,NDescriptionsItem,BookmarkOutline,
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,NDataTable,NList,NThing,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,NTabPane,
    NLayoutHeader,AddCircleOutline,NModal,NTag,FolderOutline,TrashOutline,CloudUploadOutline,CloudDownloadOutline,
    ReturnDownForward,NBadge,NBreadcrumb,NBreadcrumbItem,SearchOutline,ListOutline,NDrawer,NDrawerContent,NTabs,
    NListItem,VMdEditor,NRadioGroup,NRadioButton,NDatePicker,NTimePicker,NConfigProvider
  },
  props: {

  },
  created() {
    if(localStorage.getItem(key().authorization) != null){
      this.onLoad(true);
    }
  },
  data() {
    let self = this;
    self.toolbar = {
      checkboxToolbar:{
        icon:"v-md-icon-checkbox",
        title:'任务列表',
        action(editor) {
          editor.insert(function (selected) {
            return {
              text: '- [x] 任务列表',
              selected: selected,
            };
          });
        }
      },
      tagSelected:{
        icon:"v-md-icon-arrow-down",
        title:'书签',
        action(editor) {
          self.showTags = true;
        }
      }
    };


    return {
      roleWrite:localStorage.getItem(key().useSpaceRole) == 'WRITE',
      mode:"preview", //preview   edit  editable
      originalMdText:'',
      showTags:false,
      keyword:'',
      activeKey:'0',
      isOverlay:false,
      tabsValue:'0',
      list:[],
      mdText:'',
      docId:null,
      docTag:null,
      originalDocTag:null,
      maxHeight:document.documentElement.clientHeight - 250,
      shareParam:ref({password:null,bodyId:null,invalidTime:null,url:null,bodyType:null}),
      showShare:false,
    }
  },
  methods:{
    //失效时间只能选未来
    invalidTimeRange:function (ts){
      return ts <= Date.now();
    },
    //复制文本
    doCopy:function (text){
      if(!text){
        this.showToast("error","没有内容可复制");
        return
      }
      let self = this;
      this.$copyText(text).then(function (e) {
        self.showToast(null,'分享地址已复制,请粘贴给有需要的人');
        self.showShare = false;
      }, function (e) {
        self.showToast("error",'复制失败,请手动复制地址');
      });
    },
    //创建分享链接
    createShare:function () {
      let self = this;
      this.$refs.shareformRef.validate((errors) => {
        if (!errors) {
          self.isOverlay = true;
          self.shareParam.bodyType = 'NOTE';
          axios.post('/share/admin/create', self.shareParam).then(function (response) {
            if(response.data.result){
              self.shareParam.url = window.location.protocol + '//' + window.location.host + window.location.pathname +'#/share/notepad/?' + response.data.data.url;
              self.doCopy(self.shareParam.url);
            }else{
              self.showToast("error",response.data.message);
            }
            self.isOverlay = false;
          }).catch(function (error) {
            self.isOverlay = false;
            console.log(error);
          });
        }
      });
    },
    //打开分享面板
    share:function (item) {
      //new Date(new Date().toLocaleDateString()).getTime() + 24 * 60 * 60 * 1000 - 1
      this.shareParam.password = null;
      this.shareParam.bodyId = item.id;
      this.shareParam.invalidTime = ref(FormatDate(new Date()));
      this.shareParam.url = null;
      this.showShare = true;
    },
    //删除
    del:function (item) {
      let self = this;
      this.showDialog("warning",'是否删除笔记:' + item.keyword + ',删除后不可恢复!',function (){
        self.isOverlay = true;
        axios.post('/notebook/delNote', {id:item.id}).then(function (response) {
          if(response.data.result){
            if(item.id == self.docId){//把当前打开的删除了
              self.docId = null;
              self.docTag = null;
              self.originalDocTag = null;
              self.originalMdText = '';
              self.mdText = '';
              self.onLoad(true);
            }else{
              self.onLoad();
            }
          }
          self.isOverlay = false;
          self.showToast(response.data.result ? "success" : "error", response.data.message);
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      });
    },
    //失去焦点时保存
    blurMdText:function (event) {
      if(this.mdText == this.originalMdText && this.originalDocTag == this.docTag){
        return
      }
      let self = this;
      this.showDialog("warning",`文档内容已发生改变是否保存?`,function (){
        self.saveMdText(self.mdText,'');
      });
    },
    //保存文档
    saveMdText:function (text, html) {
      if(text == this.originalMdText && this.originalDocTag == this.docTag){
        return;//文档未发生改变
      }
      let data = {
        html:html,
        text:text,
        tag:this.docTag,
        id:this.docId
      };
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/saveNote', data).then(function (response) {
        if(response.data.result){
          self.originalMdText = text;
          self.originalDocTag = self.docTag;
          self.docId = response.data.data;
          self.onLoad();
        }
        self.isOverlay = false;
        self.showToast(response.data.result ? "success" : "error", response.data.message);
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //获取md文档
    openText:function (item) {
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/getNoteText', {id:item.id}).then(function (response) {
        if(response.data.result){
          self.mdText = response.data.data;
          self.originalMdText = response.data.data;
          self.docId = item.id;
          self.docTag = item.tag+'';
          self.originalDocTag = self.docTag;
        }else{
          self.showToast("error",response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //加载笔记列表
    onLoad:function (init) {
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/getList', {
        keyword: this.keyword,
        tag:this.activeKey,
        type:this.tabsValue
      }).then(function (response) {
        if(response.data.result){
          self.list = response.data.datas;
          if(init){
            if(self.list.length > 0){
              self.openText(self.list[0]);
              self.mode='preview';
            }
          }
        }else{
          self.showToast("error",response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //根据点击不同的子菜单,显示不同的功能面板
    handleUpdateValue:function (key, item){
      this.onLoad();
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