<template>

  <van-search
      v-model="keyword"
      show-action
      placeholder="请输入搜索关键词"
  >
    <template #action>
      <div @click="onLoad">搜索</div>
    </template>
  </van-search>

  <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
  >
    <van-cell-group v-for="item in journalList">
      <van-cell is-link :title="item.title" :label="item.date" />
    </van-cell-group>
  </van-list>
</template>

<script>
import axios from "axios";
import {List,CellGroup,Cell,Search} from 'vant';
import '@vant/touch-emulator';//vant适配桌面端

export default {
  name: "Journal",
  components:{
    [List.name]:List,
    [CellGroup.name]:CellGroup,
    [Cell.name]:Cell,
    [Search.name]:Search
  },
  setup(){

  },
  data(){
    return {
      loading:false,
      finished:false,
      keyword:"",
      journalList:[
        {title:"测试标题",date:"2023年01月13日"},
        {title:"测试标题2",date:"2023年01月14日"}
      ]
    }
  },
  methods:{
    onLoad:function (){

    }
  }
}
</script>

<style scoped>

</style>