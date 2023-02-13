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
import {isToken} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
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