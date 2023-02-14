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
      user0: '/yonghu0.png',
      user1: '/yonghu1.png',
      journal0: '/riji0.png',
      journal1: '/riji1.png',
      disk0: '/disk0.png',
      disk1: '/disk1.png',
      notepad0: '/notepad0.png',
      notepad1: '/notepad1.png'
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