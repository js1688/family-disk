<template>
  <van-overlay :show="isOverlay">
    <div class="wrapper">
      <van-loading />
    </div>
  </van-overlay>
  <div style="height: 250px;width: 100%;text-align: center;">
    <van-image
        width="6rem"
        height="6rem"
        style="top: 100px"
        :src="logonPng"
    />
    <p style="margin-top: 100px;">{{useSpaceName}}</p>
  </div>
  <van-cell is-link title="切换空间" @click="switchShow = true" />
  <van-action-sheet close-on-click-action v-model:show="switchShow" :actions="spaceActions" @select="switchOnSelect" />

  <van-cell is-link title="邀请加入空间" @click="inviteShow = true" />
  <van-action-sheet v-model:show="inviteShow" title="邀请用户加入空间">
    <div class="content">
      加入一个空间
    </div>
  </van-action-sheet>

  <van-cell is-link title="登录" @click="logonShow = true" />
  <van-action-sheet v-model:show="logonShow" title="用户登录">
    <div class="content">
      <van-form @submit="logon">
        <van-cell-group inset>
          <van-field
              required
              v-model="email"
              name="邮箱"
              label="邮箱"
              placeholder="邮箱"
              :rules="[{ required: true, message: '请输入邮箱' }]"
          />
          <van-field
              required
              v-model="password"
              type="password"
              name="密码"
              label="密码"
              placeholder="密码"
              :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-cell is-link title="注册" @click="registerShow = true" />
  <van-action-sheet v-model:show="registerShow" title="用户注册">
    <div class="content">
      <van-form @submit="">
        <van-cell-group inset>
          <van-field required v-model="name"
                     :rules="[{ required: true, message: '请输入用户名' }]"
                     label="用户名" placeholder="请输入用户名" />
        </van-cell-group>
        <van-cell-group inset>
          <van-field
              required
              v-model="email"
              name="邮箱"
              label="邮箱"
              placeholder="邮箱"
              :rules="[{ required: true, message: '请输入邮箱' }]"
          />
          <van-field
              required
              v-model="pwd"
              type="pwd"
              name="密码"
              label="密码"
              placeholder="密码"
              :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
              required
              v-model="password"
              type="password"
              name="确认密码"
              label="确认密码"
              placeholder="确认密码"
              :rules="[{ required: true, message: '请再次输入密码' },{ required: true, message: '两次密码不一致', pattern: new RegExp('^'+pwd + '$') }]"
          />
        </van-cell-group>
        <van-cell-group inset>
          <van-field
              v-model="captcha"
              center
              clearable
              required
              label="邮箱验证码"
              placeholder="请输入邮箱验证码"
              :rules="[{ required: true, message: '请输入邮箱验证码' }]"
          >
            <template #button>
              <van-button size="small" type="primary">发送验证码</van-button>
            </template>
          </van-field>
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-cell is-link title="退出登录" @click="logoutShow = true" />
  <van-action-sheet close-on-click-action v-model:show="logoutShow" :actions="okActions" @select="logoutOnSelect" />
</template>

<script>
import { ref } from 'vue';
import {showToast} from 'vant';
import { Cell, ActionSheet,Image,Form, Field, CellGroup,Button } from 'vant';
import { Overlay,Loading } from 'vant';
import axios from 'axios';
import kg from "@/global/KeyGlobal";

export default {
  name: "User",
  setup() {
    // 登录
    const logonShow = ref(false);

    //注册
    const registerShow = ref(false);

    //退出登录
    const logoutShow = ref(false);

    //切换空间
    const switchShow = ref(false);

    //邀请加入空间
    const inviteShow = ref(false);

    const spaceActions = [
      { name: '空间1' },
      { name: '空间2' }
    ];
    const okActions = [
      { name: '确定',code:1 },
      { name: '取消',code:0 }
    ];

    return {
      logoutShow,
      registerShow,
      logonShow,
      switchShow,
      spaceActions,
      okActions,
      inviteShow
    };
  },
  components: {
    [Cell.name]: Cell,
    [ActionSheet.name]: ActionSheet,
    [Image.name]: Image,
    [Form.name]: Form,
    [Field.name]: Field,
    [CellGroup.name]: CellGroup,
    [Button.name]: Button,
    [Overlay.name]: Overlay,
    [Loading.name]: Loading
  },
  props: {
  },
  data: function() {
    return {
      email:"",
      password:"",
      pwd:"",
      logonPng:"/logon0.png",
      captcha:"",
      name:"",
      useSpaceName:"请先登录",
      isOverlay:false
    };
  },
  methods: {
    switchOnSelect: function(item){
      showToast(item.name);
    },
    logoutOnSelect: function(item){
      if(item.code == 1){
        this.logonPng = "/logon0.png";
      }
    },
    logon: function(){
      this.isOverlay = true;
      let self = this;
      axios.post('/user/info/emailPasswordLogin', {
        email: this.email,
        password: this.password
      }).then(function (response) {
        if(response.data.result){//登录成功
          localStorage.setItem(kg.data().authorization,response.data.data);
          //登录成功后获取用户信息
          axios.get('/user/info/getUserInfo').then(function (res){
            if(res.data.result){//获得信息成功
              console.log(res.data.data);
              self.useSpaceName = res.data.data.name;
              //todo 做一系列动作
            }
            self.isOverlay = false;
          }).catch(function (err){
            self.isOverlay = false;
            console.log(err);
          });
        }else{//登录失败
          showToast(response.data.message);
          self.isOverlay = false;
        }
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    }
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