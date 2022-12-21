<template>
  <header>

  </header>
  <body>
    <van-tabbar v-model="active">
      <van-tabbar-item>
        <span>网盘</span>
        <template #icon="props">
          <img :src="props.active ? icon.user1 : icon.user0" />
        </template>
      </van-tabbar-item>
      <van-tabbar-item>
        <span>日记</span>
        <template #icon="props">
          <img :src="props.active ? icon.journal1 : icon.journal0" />
        </template>
      </van-tabbar-item>
      <van-tabbar-item>
        <span>记事本</span>
        <template #icon="props">
          <img :src="props.active ? icon.user1 : icon.user0" />
        </template>
      </van-tabbar-item>
      <van-tabbar-item>
        <span>用户</span>
        <template #icon="props">
          <img :src="props.active ? icon.user1 : icon.user0" />
        </template>
      </van-tabbar-item>
    </van-tabbar>
  </body>
</template>
<script>
import { Tabbar, TabbarItem } from 'vant';
import { ref } from 'vue';
  export default {
    name: 'App',
    setup() {
      const active = ref(0);
      const icon = {
        user0: '/yonghu0.png',
        user1: '/yonghu1.png',
        journal0: '/riji0.png',
        journal1: '/riji1.png'
      };
      return {
        icon,
        active,
      }
    },
    components: {
      [Tabbar.name]: Tabbar,
      [TabbarItem.name]: TabbarItem

    },
    props: {
    },
    data: function() {
      return {
        name: "谭峻"
      };
    },
    methods: {
    }
  }
</script>
