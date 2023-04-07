<template>
  <n-space vertical>
    <n-layout has-sider sider-placement="right" position="absolute" style="top: 0; bottom: 0">
      <n-layout-content>
        <!-- 文件管理 -->
        <div v-if="manageIf">
          <n-spin :show="isOverlay">
            <n-card :bordered="false">
              <div style="width: 50%;display:inline-block;">
                <n-space :size="40">
                  <n-badge value="上传" type="info" :offset="[8, -8]">
                    <n-button text style="font-size: 24px">
                      <n-icon>
                        <cloud-upload-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="下载" type="info" :offset="[8, -8]">
                    <n-button text style="font-size: 24px">
                      <n-icon>
                        <cloud-download-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="移动" type="info" :offset="[8, -8]">
                    <n-button @click="showMoveDirectory = true;onMoveLoad();rowKey = null;" :disabled="!rowKeys || rowKeys.length == 0"  text style="font-size: 24px">
                      <n-icon>
                        <return-down-forward />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="目录" type="info" :offset="[8, -8]">
                    <n-button @click="showAddDirectory = true" text style="font-size: 24px">
                      <n-icon>
                        <add-circle-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="删除" type="info" :offset="[8, -8]">
                    <n-button :disabled="!rowKeys || rowKeys.length == 0" @click="delsDirectory" text style="font-size: 24px">
                      <n-icon>
                        <trash-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                </n-space>
              </div>
              <div style="width: 50%;display:inline-block;">
                <n-space justify="end" >
                  <n-input-group>
                    <n-input v-model:value="keyword" placeholder="搜索"/>
                    <n-button ghost @click="onLoad">
                      <n-icon>
                        <search-outline />
                      </n-icon>
                    </n-button>
                  </n-input-group>
                </n-space>
              </div>
              <n-divider />
              <div style="width: 80%;display:inline-block;">
                <n-breadcrumb>
                  <n-breadcrumb-item v-for="(item) in openPath" @click="jump(item)">{{item.name}}</n-breadcrumb-item>
                </n-breadcrumb>
              </div>
              <div style="width: 20%;display:inline-block;">
                <n-space justify="end" >
                  <n-tag :bordered="false" :color="{ color: '#FFF'}">已全部加载,共{{folderData.length}}项</n-tag>
                </n-space>
              </div>
              <n-data-table
                  :columns="folderColumns"
                  :data="folderData"
                  :pagination="false"
                  :bordered="false"
                  :row-key="setRowKey"
                  :checked-row-keys="rowKeys"
                  @update:checked-row-keys="handleCheck"
                  virtual-scroll
                  :max-height="maxHeight - 50"
              />
            </n-card>

            <n-modal
                v-model:show="showAddDirectory"
                :mask-closable="false"
                preset="dialog"
                title="新建目录"
                positive-text="确认"
                negative-text="取消"
                @positive-click="addDirectory"
                @negative-click="showAddDirectory = false"
            >
              <n-divider />
              <n-input v-model:value="simpleField" placeholder="请输入目录名称"/>
            </n-modal>

            <n-drawer :mask-closable="false" v-model:show="showMoveDirectory" :width="502">
              <n-drawer-content title="选择移动的目的地" closable>
                <n-data-table
                    :columns="moveColumns"
                    :data="moveData"
                    :row-key="setRowKey"
                    :pagination="false"
                    :bordered="false"
                    virtual-scroll
                    :max-height="maxHeight"
                    @update:checked-row-keys="handleCheckSingle"
                />
                <n-card :bordered="false" style="position: absolute;bottom: 0">
                  <n-space>
                    <n-button type="info" @click="okMoveDirectory(null)">确定</n-button>
                    <n-button type="info" @click="okMoveDirectory(0)">移动到根目录</n-button>
                    <n-button @click="showMoveDirectory = false;rowKeys=[];">取消</n-button>
                  </n-space>
                </n-card>
              </n-drawer-content>
            </n-drawer>

            <n-modal
                v-model:show="showUpdateName"
                :mask-closable="false"
                preset="dialog"
                title="重命名目录"
                positive-text="确认"
                negative-text="取消"
                @positive-click="updateName"
                @negative-click="showUpdateName = false;rowKeys=[];"
            >
              <n-divider />
              <n-input v-model:value="simpleField" placeholder="请输入新的目录名称"/>
            </n-modal>
<!--            -->
<!--            <n-image-group>-->
<!--              <n-space>-->
<!--                <n-image-->
<!--                    width="100"-->
<!--                    src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg"-->
<!--                />-->
<!--                <n-image-->
<!--                    width="100"-->
<!--                    src="https://gw.alipayobjects.com/zos/antfincdn/aPkFc8Sj7n/method-draw-image.svg"-->
<!--                />-->
<!--              </n-space>-->
<!--            </n-image-group>-->

          </n-spin>
        </div>
        <!-- 回收站 -->
      </n-layout-content>
      <n-layout-sider
          bordered
          collapse-mode="width"
          :width="150"
          :collapsed-width="64"
          :collapsed="collapsed"
          show-trigger
          @collapse="collapsed = true"
          @expand="collapsed = false"
      >
        <n-menu
            v-model:value="activeKey"
            :collapsed="collapsed"
            :collapsed-width="64"
            :collapsed-icon-size="22"
            :options="menuOptions"
            @update:value="handleUpdateValue"
        />
      </n-layout-sider>
    </n-layout>
  </n-space>
</template>

<script>
import { h, ref } from "vue";
import { NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider ,NDescriptions,NDescriptionsItem,
  NAnchorLink,NAnchor,NPopconfirm,NButton,NLayoutContent,NImage,NSpin,NProgress,NDataTable,
  NForm,NFormItem,NInput,NInputGroup,NLayoutFooter,NLayoutHeader,NCard,NModal,NBadge,
  NAvatar,NDivider,NTag,NBreadcrumb,NBreadcrumbItem,NDrawer,NDrawerContent,
  createDiscreteApi
} from "naive-ui";
import {
  ExitOutline, EnterOutline, PersonAddOutline, BackspaceOutline, AddCircleOutline,
  InformationCircleOutline, ShareSocialOutline,TrashOutline,CloudUploadOutline,
  MailOutline,CloudOutline,FolderOutline,FolderOpenOutline,CloudDownloadOutline,
  BuildOutline, OptionsOutline,PersonOutline,ReturnDownForward,SearchOutline,ListOutline
} from "@vicons/ionicons5";

import {key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";

const { notification,dialog} = createDiscreteApi(['notification','dialog'])

const createColumns = ({
                         buttons,
                         cols
                       }) => {

  if(buttons){
    for (let i = 0; i < buttons.length; i++) {
      const button = buttons[i];
      cols.push(
          {
            title: button.title,
            key: button.key,
            width:button.width,
            titleColSpan:button.titleColSpan,
            render(row) {
              return h(
                  NButton,
                  {
                    strong: true,
                    tertiary: true,
                    size: "small",
                    onClick: () => button.play(row)
                  },
                  { default: () => button.name }
              );
            }
          }
      );
    }
  }
  return cols;
};

export default {
  name: "Disk",
  setup() {
    function renderIcon(icon) {
      return () => h(NIcon, null, { default: () => h(icon) });
    }
    const menuOptions = [
      {
        label: "文件管理",
        key: "manage",
        icon:renderIcon(FolderOpenOutline)
      },
      {
        label: "上传列表",
        key: "uploadList",
        icon:renderIcon(CloudUploadOutline)
      },
      {
        label: "下载列表",
        key: "downloadList",
        icon:renderIcon(CloudDownloadOutline)
      },
      {
        label: "回收站",
        key: "dustbin",
        icon:renderIcon(TrashOutline)
      }
    ];
    return {
      collapsed: ref(false),
      activeKey: ref('manage'),
      menuOptions,
      CloudUploadOutline,
      CloudDownloadOutline,
      ReturnDownForward,
      SearchOutline
    }
  },
  components: {
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NAnchorLink,NAnchor,NDescriptions,NDescriptionsItem,
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,NDataTable,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,
    NLayoutHeader,AddCircleOutline,NModal,NTag,FolderOutline,TrashOutline,CloudUploadOutline,CloudDownloadOutline,
    ReturnDownForward,NBadge,NBreadcrumb,NBreadcrumbItem,SearchOutline,ListOutline,NDrawer,NDrawerContent,
  },
  props: {

  },
  created() {
    if(localStorage.getItem(key().authorization) != null){
      this.onLoad();
    }
  },
  data(){
    return {
      roleWrite:localStorage.getItem(key().useSpaceRole) == 'WRITE',
      showMoveDirectory:false,
      showAddDirectory:false,
      showUpdateName:false,
      simpleField:"",
      maxHeight:document.documentElement.clientHeight - 250,
      manageIf:true,
      isOverlay:false,
      folderData:[],
      pid:0,
      keyword:'',
      rowKeys:[],
      rowKey:null,
      folderColumns:createColumns({
        buttons:[
          {
            name:"删除",
            title:"操作",
            titleColSpan:5,
            width:60,
            key:"s",
            play:this.delDirectory
          },
          {
            name:"下载",
            width:60,
            key:"x",
            play:null
          },
          {
            name:"移动",
            width:60,
            key:"y",
            play:this.moveSingle
          },
          {
            name:"重命名",
            width:75,
            key:"c",
            play:this.renameSingle
          },
          {
            name:"分享",
            width:70,
            key:"f",
            play:null
          }
        ],
        cols:[
          {
            type: "selection"
          },
          {
            title: "目录名",
            key: "name",
            cellProps:this.cellProps
          },
          {
            title: "大小(MB)",
            width:100,
            key: "size"
          },
          {
            title: "修改时间",
            width:200,
            key: "updateTime"
          }
        ]
      }),
      openPath:[{name:'文件管理',id:0}],
      moveColumns:createColumns({
        cols:[
          {
            type: "selection",
            multiple: false
          },
          {
            title: "目录名",
            key: "name"
          }
        ]
      }),
      moveData:[]
    }
  },
  methods:{
    //选择单个重命名
    renameSingle:function (item) {
      this.showUpdateName = true;
      this.simpleField = item.name;
      this.rowKeys = [item.id];
    },
    //确定重命名
    updateName:function(){
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/updateName', {
        name: this.simpleField,
        id:this.rowKeys[0]
      }).then(function (response) {
        if(response.data.result){
          self.showUpdateName = false;
          self.simpleField = "";
          self.rowKeys = [];
          self.onLoad();
        }
        self.showToast(response.data.result ? "success" : "error", response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //选择单个移动
    moveSingle:function (item) {
      this.showMoveDirectory = true;
      this.onMoveLoad();
      this.rowKey = null;
      this.rowKeys = [item.id];
    },
    //确定移动
    okMoveDirectory: async function (def){
      if(def == 0){
        this.rowKey = def;
      }
      if(this.rowKey == null && this.rowKey != 0){
        this.showToast("error","请选择要移动的目标目录");
        return;
      }
      this.isOverlay = true;
      for (let i = 0; i < this.rowKeys.length; i++) {
        let ret = await axios.post('/netdisk/moveDirectory', {
          targetDirId: this.rowKey,
          id:this.rowKeys[i]
        });
        if(!ret.data.result){
          this.showToast("error", ret.data.message);
        }
      };
      this.isOverlay = false;
      this.showMoveDirectory = false;
      this.rowKeys = [];
      this.onLoad();
    },
    //表格选中时触发事件,适用单选
    handleCheckSingle:function (rowKeys) {
      this.rowKey = rowKeys[0];
    },
    //加载移动目录
    onMoveLoad: function () {
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/findDirectoryTree', {
        type: 'FOLDER'
      }).then(function (response) {
        if(response.data.result){
          self.moveData = response.data.datas;
        }else{
          self.showToast("error",response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //批量删除
     delsDirectory:function () {
      if(!this.rowKeys || this.rowKeys.length == 0){
        return;
      }
      let self = this;
      this.showDialog("warning",`是否批量删除${self.rowKeys.length}个目录!`,async function (){
        self.isOverlay = true;
        for (let i = 0; i < self.rowKeys.length; i++) {
          let ret = await axios.post('/netdisk/delDirectory', {
            id: self.rowKeys[i]
          });
          if(!ret.data.result){
            self.showToast("error", ret.data.message);
          }
        }
        self.isOverlay = false;
        self.onLoad();
      });
    },
    //添加目录
    addDirectory:function (){
      if(!this.simpleField){
        this.showToast("error", "请输入目录名称");
        return false;
      }
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/addDirectory', {
        name: this.simpleField,
        pid: this.pid,
        type:"FOLDER"
      }).then(function (response) {
        if(response.data.result){
          self.onLoad();
          self.simpleField = "";
          self.showAddDirectory = false;
        }
        self.showToast(response.data.result ? "success" : "error", response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
      return false;
    },
    //删除目录
    delDirectory:function(item){
      let self = this;
      this.showDialog("warning",'是否删除目录:' + item.name + '!',function (){
        self.isOverlay = true;
        axios.post('/netdisk/delDirectory', {
          id: item.id
        }).then(function (response) {
          if(response.data.result){
            self.onLoad();
          }
          self.showToast(response.data.result ? "success" : "error", response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
              self.isOverlay = false;
              console.log(error);
        });
      });
    },
    //数据表格,每一行的key设置方法
    setRowKey: function (row) {
      return row.id;
    },
    //数据表格被选中时触发事件
    handleCheck(rowKeys) {
      this.rowKeys = rowKeys;
    },
    //行点击时可以添加事件
    cellProps:function (row,index){
      let self = this;
      return {
        style: "cursor: pointer;",
        onClick:function () {
          if(row.type == 'FOLDER'){
            self.pid = row.id;
            self.onLoad();
            //将打开的目录追加到路径中
            self.openPath.push({name:row.name,id:row.id});
          }else if(row.type == 'FILE'){
            this.openFile(row);
          }
        }
      };
    },
    //打开文件
    openFile:function (item) {
      let mediaType = item.mediaType.toUpperCase();
      let mediaTypes = mediaType.split("/");
      switch (mediaTypes[0]) {
        case "IMAGE"://图片
          this.openImage(item);
          break
        case "VIDEO"://视频
        case "AUDIO"://音频
          //this.openVideo(item,mediaTypes[1]);
          //break
        default:
          this.showToast("warning","不识别的类型,不能在线预览,请下载文件到本地打开.");
          break
      }
    },
    //打开一张图片
    openImage:function () {

    },
    //面包屑跳转
    jump: function (item){
      this.pid = item.id;
      this.keyword = "";
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
    //加载列表
    onLoad: function () {
      this.isOverlay = true;
      let self = this;
      axios.post('/netdisk/findDirectory', {
        keyword: this.keyword,
        pid: this.pid
      }).then(function (response) {
        if(response.data.result){
          self.folderData = response.data.datas;
        }else{
          self.showToast(response.data.result ? "success" : "error", response.data.message);
        }
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //菜单切换
    menu:function (open){
      this.manageIf = false;
      this.dustbinIf = false;
      this.uploadListIf = false;
      this.downloadList = false;

      this[open] = true;
    },
    //根据点击不同的子菜单,显示不同的功能面板
    handleUpdateValue:function (key, item){
      switch (key){
        case "manage":
          this.menu("manageIf");
          break
        case "dustbin":
          this.menu("dustbinIf");
          break
        case "uploadList":
          this.menu("uploadListIf");
          break
        case "downloadList":
          this.menu("downloadListIf");
          break
      }
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