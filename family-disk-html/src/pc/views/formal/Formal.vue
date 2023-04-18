<template>
  <n-space vertical>
    <n-layout has-sider position="absolute" style="top: 0px; bottom: 50px">
      <n-layout-sider
          bordered
          collapse-mode="width"
          :collapsed-width="64"
          :width="140"
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
        />
      </n-layout-sider>
      <n-layout-content>
        <!-- 正式功能面板 -->
        <router-view></router-view>
      </n-layout-content>
    </n-layout>
    <n-layout-footer position="absolute" bordered >
      <div style="width: 100%;height: 50px;display: flex;justify-content: center;align-items: Center;">
        <n-space justify="center">
          <a href="https://beian.miit.gov.cn/" target="_blank">
            <n-text type="info">
              湘ICP备20008279号-1
            </n-text>
          </a>
          <n-icon size="20">
            <logo-github />
          </n-icon>
          <a href="http://github.com/js1688/family-disk" target="_blank">
            <n-text type="info">
              github
            </n-text>
          </a>
        </n-space>
      </div>
    </n-layout-footer>
  </n-space>
</template>

<script>
import { h, ref } from "vue";
import { NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NLayoutFooter,NLayoutContent,NText} from "naive-ui";
import {FolderOutline,JournalOutline,ReceiptOutline,PersonOutline,LogoGithub} from "@vicons/ionicons5";
import { RouterLink } from "vue-router";

import {isToken, key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";




export default {
  name: 'Formal',
  setup() {
    function renderIcon(icon) {
      return () => h(NIcon, null, { default: () => h(icon) });
    }
    const isShow = localStorage.getItem(key().authorization) != null;
    const menuOptions = [
      {
        label: () => h(
            RouterLink,
            {
              to: {
                path:"/formal/user"
              }
            },
            { default: () => "用户" }
        ),
        key: "user",
        icon: renderIcon(PersonOutline)
      },
      {
        label: () => h(
            RouterLink,
            {
              to: {
                path:"/formal/disk"
              }
            },
            { default: () => "网盘" }
        ),
        key: "disk",
        icon: renderIcon(FolderOutline),
        show:isShow
      },
      {
        label: () => h(
            RouterLink,
            {
              to: {
                path:"/formal/journal"
              }
            },
            { default: () => "日记" }
        ),
        key: "journal",
        icon: renderIcon(JournalOutline),
        show:isShow
      },
      {
        label: () => h(
            RouterLink,
            {
              to: {
                path:"/formal/notepad"
              }
            },
            { default: () => "备忘录" }
        ),
        key: "notepad",
        icon: renderIcon(ReceiptOutline),
        show:isShow
      }
    ];
    const url = window.location.href;
    return {
      activeKey: ref(url.substring(url.lastIndexOf("/")+1)),
      collapsed: ref(true),
      menuOptions
    }
  },
  components: {
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NLayoutFooter,NLayoutContent,LogoGithub,NText
  },
  props: {

  },
  created() {
    //如果没有登陆,并且不是用户页面,则重定向到根目录
    if(localStorage.getItem(key().authorization) == null && window.location.hash.indexOf("user") == -1){
      window.location.href = window.location.protocol + '//' + window.location.host + window.location.pathname;
    }
    //请求拦截设置头部
    axios.interceptors.request.use(config => {//声明请求拦截器
      if(isToken()){//如果本地保存了token,则在头部传送token
        config.headers[key().authorization] = localStorage.getItem(key().authorization);
      }
      return config;//一定要返回
    });
    //响应拦截器
    axios.interceptors.response.use(response => {
      if(response.data.result == false && response.data.message == 'token已过期'){
        //token失效了,清空token存储,空间id存储
        localStorage.removeItem(key().authorization);
        gws.methods.wsDisconnect();//断开socket
      }
      return response;
    }, error => {
      return error;
    });
    //如果有token,则自动发起连接websocket
    if(isToken()){
      gws.methods.wsConnection(null);
    }
  },
  data: function() {
    return {

    };
  },
  methods: {
  }
}


</script>

<style scoped>

</style>