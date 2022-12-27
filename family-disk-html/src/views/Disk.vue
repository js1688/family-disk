<template>
  <van-cell center icon="wap-home-o" >
    <a v-for="item in openPath" @click="jump($event)" :data-path="item">{{item}}</a>
  </van-cell>
  <van-search v-model="keyword" placeholder="请输入搜索关键词" />
  <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <van-cell is-link v-for="item in list" :key="item.id" :title="item.name" :data-path="item.name">
      <van-tag type="primary">{{item.type}}</van-tag>
    </van-cell>
  </van-list>
</template>

<script>
import {Search, List, Cell, Tag, NavBar, showToast} from 'vant';
export default {
  name: "Disk",
  components: {
    [Search.name]: Search,
    [List.name]: List,
    [Cell.name]: Cell,
    [Tag.name]: Tag,
    [NavBar.name]: NavBar
  },
  setup() {
    return {

    };
  },
  data: function (){
    return {
      keyword:"",
      loading:false,
      finished:false,
      list:[],
      openPath:["/","兔兔比比"]
    }
  },
  methods:{
    jump: function (e){
      console.log(e.target.dataset.path);
    },
    onLoad: function () {
      const l = [
        {
          "name":"兔兔比比",
          "type":"目录",
          "spaceId":0,
          "pid":0,
          "id":0
        },
        {
          "name":"丸子君",
          "type":"目录",
          "spaceId":0,
          "pid":0,
          "id":0
        }
      ];
      this.list = l;
      this.loading = false;//加载完毕
      this.finished = true;//全部数据加载完毕
    }
  }
}
</script>

<style scoped>

</style>