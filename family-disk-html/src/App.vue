<template>
  <header>

  </header>
  <body>
    <van-tabbar v-model="active">
      <van-tabbar-item badge="3">
        <span>自定义</span>
        <template #icon="props">
          <img :src="props.active ? icon.active : icon.inactive" />
        </template>
      </van-tabbar-item>
      <van-tabbar-item icon="search">标签</van-tabbar-item>
      <van-tabbar-item icon="setting-o">标签</van-tabbar-item>
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
        active: 'https://fastly.jsdelivr.net/npm/@vant/assets/user-active.png',
        inactive:
            'https://fastly.jsdelivr.net/npm/@vant/assets/user-inactive.png',
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
