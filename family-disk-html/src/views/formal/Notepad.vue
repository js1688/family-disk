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


  <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <van-swipe-cell v-for="item in list">
      <van-cell is-link :title="item.keyword" :label="item.updateTime" @click="open(item)" >
        <van-tag plain type="primary">{{tags[item.tag]}}</van-tag>
      </van-cell>
      <template #right>
        <van-button style="height: 66px;" square hairline type="danger"  @click="del(item)" text="删除" />
        <van-button style="height: 66px;" square hairline type="primary"  @click="update(item)" text="修改" />
        <van-button
            :disabled="shareDisabled"
                    style="height: 66px;" square hairline type="success"  @click="share(item)" text="分享" />
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

  <van-popup
      v-model:show="showPicker"
      round
      position="bottom"
  >
    <van-picker
        v-model="selectdLabelValue"
        :columns="showPickerOptions"
        @cancel="showPicker = false"
        @confirm="onConfirm"
    />
  </van-popup>

  <van-action-sheet
      v-model:show="showShare"
      title="创建分享链接">
    <div>
      <van-form @submit="createShare">
        <van-cell-group inset>
          <van-field v-model="shareParam.password"
                     type="password"
                     label="解锁密码" placeholder="请输入解锁密码" />
          <van-field label="失效日期" required readonly
                     v-model="shareParam.invalidTime"
                     placeholder="点击选择日期"
                     :rules="[{ required: true, message: '请选择失效日期' }]"
                     @click="showShareDate = true"
          />
          <van-field v-model="shareParam.url" readonly label="分享地址" >
            <template #button>
              <van-button
                  size="small"
                  type="primary"
                  @click="doCopy"
                  :disabled="shareParam.url == null || shareParam.url == ''"
              >复制</van-button>
            </template>
          </van-field>
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交申请
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-calendar v-model:show="showShareDate" :show-confirm="false" @confirm="onConfirmShare" :max-date="maxDate"/>

  <van-action-sheet
      :lock-scroll="false"
      v-model:show="showNote" :round="false"
      title="笔记查看">
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
  Cell,
  Popup,
  Picker,
  List,
  Tag,
  SwipeCell,
  DropdownItem,
  Form,
  CellGroup,
  DatePicker,
  Field,
  Calendar,
  ActionSheet, showConfirmDialog
} from 'vant';
import {key} from "@/global/KeyGlobal";
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
import VueClipboard from 'vue-clipboard2'
export default {
  name: "Notepad",
  components: {
    VMdEditor,
    VueClipboard,
    [Calendar.name]:Calendar,
    [CellGroup.name]:CellGroup,
    [Form.name]:Form,
    [Field.name]:Field,
    [DatePicker.name]:DatePicker,
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
      maxDate: new Date(new Date().getFullYear() + 1, 12, 31),
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
            let txt = editor.text;
            showConfirmDialog({
              title: '保存?',
              message:'内容已发生改变,是否保存?'
            }).then(() => {
              self.save(txt,'');
              self.isOverlay = false;
              self.showNote = false;
            }).catch(function (error) {
              self.showNote = false;
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
      showShareDate:false,
      shareDisabled:localStorage.getItem(key().useSpaceRole) != 'WRITE',
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
      menuLabelValue:0,
      selectdLabelValue:[],
      menuLabelOptions:[],
      showNote:false,
      list:[],
      tags:{1:'个人',2:'生活',3:'工作'},
      showPickerOptions:[],
      showPicker:false,
      docId:0,
      showShare:false,
      shareParam:{password:null,bodyId:null,invalidTime:null,url:null}
    };
  },
  //页面打开时初始化
  created(){
    this.getMenuLabelOption();
  },
  methods:{
    doCopy:function (){
      let self = this;
      this.$copyText(this.shareParam.url).then(function (e) {
        showToast('分享地址已复制,请粘贴给有需要的人');
        self.showShare = false;
      }, function (e) {
        showToast('复制失败,请手动复制地址');
      })
    },
    //格式化日期
    formatDate:function(date){
      return `${date.getFullYear()}-${(date.getMonth() + 1) < 10 ? '0' + (date.getMonth() + 1) : (date.getMonth() + 1)}-${date.getDate() < 10 ? '0' + date.getDate() : date.getDate()} 23:59:59`;
    },
    //日期选择回调
    onConfirmShare:function (value){
      this.showShareDate = false;
      this.shareParam.invalidTime = this.formatDate(value);
    },
    //创建分享
    createShare:function () {
      this.isOverlay = true;
      let self = this;
      axios.post('/note/share/create', this.shareParam).then(function (response) {
        if(response.data.result){
          self.shareParam.url = window.location.protocol + '//' + window.location.host + '#/share/notepad/?' + response.data.data;
          self.doCopy();
        }else{
          showToast(response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //打开分享面板
    share:function (item) {
      this.shareParam = {password:null,bodyId:item.id,invalidTime:this.formatDate(new Date()),url:null}
      this.showShare = true;
    },
    //修改
    update:function (item) {
      this.selectdLabelValue = ref([item.tag + '']);
      this.docId = item.id;
      //调用接口获取内容
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/getNoteText', {id:item.id}).then(function (response) {
        if(response.data.result){
          self.originalText = response.data.data;
          self.text = self.originalText;
          self.mode='edit';
          self.defaultFullscreen = true;
          self.showPicker = true;
        }else{
          showToast(response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //添加笔记时,选择标签
    onConfirm:function(e){
      this.showPicker = false;
      this.showNote = true;
    },
    //富文本编辑器触发保存
    save:function (text, html){
      let data = {
        html:html,
        text:text,
        tag:this.selectdLabelValue[0],
        id:this.docId
      };
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/saveNote', data).then(function (response) {
        if(response.data.result){
          self.docId = response.data.data;
          self.originalText = text;
          self.onLoad();
        }
        self.isOverlay = false;
        showToast(response.data.message);
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //删除
    del:function (item) {
      let self = this;
      showConfirmDialog({
        title: '删除',
        message:'是否删除笔记:' + item.keyword + ',删除后不可恢复!'
      }).then(() => {
        this.isOverlay = true;
        axios.post('/notebook/delNote', {id:item.id}).then(function (response) {
          if(response.data.result){
            self.onLoad();
          }
          self.isOverlay = false;
          showToast(response.data.message);
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }).catch(function (error) {
      });
    },
    //打开
    open:function (item) {
      //调用接口获取内容
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/getNoteText', {id:item.id}).then(function (response) {
        if(response.data.result){
          self.mode='preview';
          self.defaultFullscreen = false;
          self.text = response.data.data;
          self.showNote = true;
        }else{
          showToast(response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //用户标签获取
    getMenuLabelOption:function (){
      let list = [{ text: '全部', value: 0 }];//默认一个
      let list2 = [];
      //获取用户添加的标签
      for (const listKey in this.tags) {
        let j = { text: this.tags[listKey], value: listKey };
        list.push(j);
        list2.push(j);
      }
      this.menuLabelOptions = list;
      this.showPickerOptions = list2;
      this.menuLabelValue = list[0].value;
    },
    //加载笔记和备忘录
    onLoad:function (){
      this.isOverlay = true;
      let self = this;
      axios.post('/notebook/getList', {
        keyword: this.keyword,
        tag:this.menuLabelValue,
        type:this.menuTypeValue
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
    },
    //选择功能
    addSelect: function (item){
      let cz = item.name;
      this.docId = 0;//待办或笔记的暂存id,新增的时候需要清空
      switch (cz) {
        case 'note':
          this.selectdLabelValue = ref(['1']);
          this.mode='edit';
          this.defaultFullscreen = true;
          this.originalText = '';
          this.text = this.originalText;
          this.showPicker = true;
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
.wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>