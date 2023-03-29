<template>
  <n-space vertical>
    <n-layout has-sider sider-placement="right" position="absolute" style="top: 0; bottom: 0">
      <n-layout-content>
        <!-- 文件管理 -->
        <div v-if="manageIf">
          <n-spin :show="isOverlay">
            <n-card>
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
                    <n-button text style="font-size: 24px">
                      <n-icon>
                        <return-down-forward />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="目录" type="info" :offset="[8, -8]">
                    <n-button text style="font-size: 24px">
                      <n-icon>
                        <add-circle-outline />
                      </n-icon>
                    </n-button>
                  </n-badge>
                  <n-badge value="删除" type="info" :offset="[8, -8]">
                    <n-button text style="font-size: 24px">
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
                  :row-key="rowKey"
                  @update:checked-row-keys="handleCheck"
                  virtual-scroll
                  :max-height="maxHeight"
              />
            </n-card>
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
  NAvatar,NDivider,NTag,NBreadcrumb,NBreadcrumbItem,
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
import {showToast} from "vant";

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
    ReturnDownForward,NBadge,NBreadcrumb,NBreadcrumbItem,SearchOutline,ListOutline
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
      maxHeight:document.documentElement.clientHeight - 250,
      manageIf:true,
      isOverlay:false,
      folderData:[],
      pid:0,
      keyword:'',
      folderColumns:createColumns({
        buttons:[
          {
            name:"删除",
            title:"操作",
            titleColSpan:5,
            width:50,
            key:"s",
            play:null
          },
          {
            name:"下载",
            width:50,
            key:"x",
            play:null
          },
          {
            name:"移动",
            width:50,
            key:"y",
            play:null
          },
          {
            name:"重命名",
            width:75,
            key:"c",
            play:null
          },
          {
            name:"分享",
            width:50,
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
            key: ""
          },
          {
            title: "修改时间",
            width:200,
            key: "updateTime"
          }
        ]
      }),
      openPath:[{name:'文件管理',id:0}]
    }
  },
  methods:{
    //数据表格,每一行的key设置方法
    rowKey: function (row) {
      return row.id;
    },
    //数据表格被选中时触发事件
    handleCheck(rowKeys) {
      console.log(rowKeys);
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
            self.showToast(null,"还不能打开");
          }
        }
      };
    },
    //面包屑跳转
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