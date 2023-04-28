<template>
  <!-- 正式功能面板 -->
  <router-view></router-view>
  <van-tabbar route>
    <van-tabbar-item to="/formal/disk">
      <span>网盘</span>
      <template #icon="props">
        <img :src="props.active ? icon.disk1 : icon.disk0" />
      </template>
    </van-tabbar-item>
    <van-tabbar-item to="/formal/journal">
      <span>日记</span>
      <template #icon="props">
        <img :src="props.active ? icon.journal1 : icon.journal0" />
      </template>
    </van-tabbar-item>
    <van-tabbar-item to="/formal/notepad">
      <span>备忘录</span>
      <template #icon="props">
        <img :src="props.active ? icon.notepad1 : icon.notepad0" />
      </template>
    </van-tabbar-item>
    <van-tabbar-item to="/formal/user">
      <span>用户</span>
      <template #icon="props">
        <img :src="props.active ? icon.user1 : icon.user0" />
      </template>
    </van-tabbar-item>
  </van-tabbar>
</template>

<script>
import { Tabbar, TabbarItem } from 'vant';
import {isToken, key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";
export default {
  name: 'Formal',
  setup() {
    const icon = {
      user0: '/img/mobile/yonghu0.png',
      user1: '/img/mobile/yonghu1.png',
      journal0: '/img/mobile/riji0.png',
      journal1: '/img/mobile/riji1.png',
      disk0: '/img/mobile/disk0.png',
      disk1: '/img/mobile/disk1.png',
      notepad0: '/img/mobile/notepad0.png',
      notepad1: '/img/mobile/notepad1.png'
    };
    return {
      icon
    }
  },
  components: {
    [Tabbar.name]: Tabbar,
    [TabbarItem.name]: TabbarItem
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