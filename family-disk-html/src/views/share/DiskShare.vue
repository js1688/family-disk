<template>
  <van-overlay :show="isOverlay">
    <div class="wrapper">
      <van-loading />
    </div>
  </van-overlay>

  <van-action-sheet
      v-model:show="passwordShow"
      :close-on-click-overlay="false"
      :closeable="false"
      title="请输入密码后解锁内容">
    <div class="content">
      <van-form @submit="">
        <van-cell-group inset>
          <van-field required v-model="param.password"
                     type="password"
                     :rules="[{ required: true, message: '请输入解锁密码' }]"
                     label="解锁密码" placeholder="请输入解锁密码" />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <h1>这是网盘分享</h1>
</template>

<script>
//vant适配桌面端
import '@vant/touch-emulator';
import {
  Popover,
  Button,
  BackTop,
  Overlay,
  Search,
  Loading,
  showToast,
  DropdownMenu,
  Cell,
  Popup,
  Picker,
  List,
  Tag,
  SwipeCell,
  DropdownItem,
  Field,
  CellGroup,
  Form,
  ActionSheet, showConfirmDialog
} from 'vant';

import axios from "axios";
export default {
  name: "DiskShare",
  components:{
    [Field.name]:Field,
    [CellGroup.name]:CellGroup,
    [Form.name]:Form,
    [Popup.name]:Popup,
    [Picker.name]:Picker,
    [Cell.name]:Cell,
    [Tag.name]:Tag,
    [SwipeCell.name]:SwipeCell,
    [List.name]:List,
    [Popover.name]:Popover,
    [Button.name]:Button,
    [BackTop.name]:BackTop,
    [Overlay.name]:Overlay,
    [Search.name]:Search,
    [Loading.name]:Loading,
    [DropdownMenu.name]:DropdownMenu,
    [DropdownItem.name]:DropdownItem,
    [ActionSheet.name]:ActionSheet
  },
  setup() {
    return {
    };
  },
  created(){
    /**
     * 分享网盘路径: 是否需要输入密码/链接uuid
     * http://localhost:5173/#/share/netdisk?lock=true&uuid=asddhsdhiweq-sdas-666
     * @type {{lock: null, uuid: null}}
     */
    //从页面地址中获取到参数
    for (const key in this.param) {
      let v = decodeURIComponent((new RegExp('[?|&]'+key+'=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null;
      this.param[key] = v;
    }
    //是否需要输入密码
    if(this.param.lock == 'true'){
      this.passwordShow = true;
    }else{
      //不需要密码,直接获得内容
    }
  },
  data() {
    return {
      isOverlay:false,
      passwordShow:false,
      param : {lock:null,uuid:null,password:null}
    }
  },
  methods:{

  }
}
</script>

<style scoped>
.wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>