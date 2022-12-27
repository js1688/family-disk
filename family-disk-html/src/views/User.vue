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
    <p style="margin-top: 100px;">{{userName}}</p>
  </div>


  <van-cell is-link title="登录" @click="logonShow = true" v-if="notLoginShow == true"/>
  <van-action-sheet v-model:show="logonShow" title="用户登录">
    <div class="content">
      <van-form @submit="logon">
        <van-cell-group inset>
          <van-field
              required
              v-model="email"
              name="邮箱"
              label="邮箱"
              placeholder="请输入邮箱"
              :rules="[{ required: true, message: '请输入邮箱' }]"
          />
          <van-field
              required
              v-model="password"
              type="password"
              name="密码"
              label="密码"
              placeholder="请输入密码"
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

  <van-cell is-link title="注册" @click="registerShow = true" v-if="notLoginShow == true" />
  <van-action-sheet v-model:show="registerShow" title="用户注册">
    <div class="content">
      <van-form @submit="register">
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
              placeholder="请输入邮箱"
              :rules="[{ required: true, message: '请输入邮箱' }]"
          />
          <van-field
              required
              v-model="pwd"
              type="password"
              name="密码"
              label="密码"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
          />
          <van-field
              required
              v-model="password"
              type="password"
              name="确认密码"
              label="确认密码"
              placeholder="请二次输入密码"
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
              <van-button size="small" :disabled="sendyzm" type="primary" @click="sendCaptcha">{{sendyzmjs == 0 ? sendyzmName : sendyzmName+'(' + sendyzmjs +')'}}</van-button>
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

  <van-cell is-link title="退出登录" @click="logoutShow = true" v-if="notLoginShow == false"/>
  <van-action-sheet close-on-click-action v-model:show="logoutShow" :actions="okActions" @select="logoutOnSelect" />

  <van-cell is-link title="创建空间" @click="createSpaceShow = true" v-if="notLoginShow == false" />
  <van-action-sheet close-on-click-action v-model:show="createSpaceShow" :actions="okActions" @select="createSpace" />

  <van-cell is-link title="查看空间" @click="getSpaceInfo" v-if="notLoginShow == false" />
  <van-action-sheet v-model:show="querySpaceShow" title="查看空间信息">
    <div class="content">
      <van-cell-group inset>
        <van-field label="空间名：" label-align="top" :model-value="title" readonly  />
        <van-field label="最大存储(MB)：" label-align="top" :model-value="maxSize" readonly  />
        <van-field label="已使用存储(MB)：" label-align="top" :model-value="useSize" readonly  />
        <van-field label="剩余存储(MB)：" label-align="top" :model-value="maxSize - useSize" readonly  />
      </van-cell-group>
    </div>
  </van-action-sheet>
</template>

<script>
import { ref } from 'vue';
import {showToast} from 'vant';
import { Cell, ActionSheet,Image,Form, Field, CellGroup,Button } from 'vant';
import { Overlay,Loading } from 'vant';
import axios from 'axios';
import kg from "@/global/KeyGlobal";
import { showConfirmDialog } from 'vant';

export default {
  name: "User",
  setup() {
    const logonShow = ref(false);
    const registerShow = ref(false);
    const logoutShow = ref(false);
    const createSpaceShow = ref(false);
    const querySpaceShow = ref(false);

    const okActions = [
      { name: '确定',code:1 },
      { name: '取消',code:0 }
    ];

    return {
      logoutShow,
      registerShow,
      logonShow,
      okActions,
      createSpaceShow,
      querySpaceShow
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
      captcha:"",
      name:"",
      logonPng:localStorage.getItem(kg.data().authorization) == null ? "/logon0.png" : "/logon1.png",
      userName: localStorage.getItem(kg.data().authorization) == null ? "请先登录" : localStorage.getItem(kg.data().userName),
      isOverlay: false,
      sendyzm:false,
      sendyzmName:'发送验证码',
      sendyzmjs:0,
      notLoginShow: localStorage.getItem(kg.data().authorization) == null ? true : false,
      maxSize:0,
      title:"",
      useSize:0
    };
  },
  methods: {
    //查看空间
    getSpaceInfo: function(){
      this.querySpaceShow = true;
      this.isOverlay = true;
      let self = this;
      axios.get('/user/space/getSpaceInfo').then(function (res){
        if(res.data.result){
          self.title = res.data.data.title;
          self.maxSize = res.data.data.maxSize;
          self.useSize = res.data.data.useSize;
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
    //创建空间
    createSpace: function(item){
      if(item.code == 1){
        this.isOverlay = true;
        let self = this;
        axios.post('/user/space/createSpace', {
          title: localStorage.getItem(kg.data().userName) + "的空间"
        }).then(function (response) {
          if(response.data.result){
            localStorage.setItem(kg.data().useSpaceId,response.data.data.id);
            localStorage.setItem(kg.data().useSpaceRole,'WRITE');//自己创建的空间,权限是读写
          }
          showToast(response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        })
      }
    },
    //注册
    register: function(){
      this.isOverlay = true;
      let self = this;
      axios.post('/user/info/createUserInfo', {
        email: this.email,
        name: this.name,
        password: this.password,
        captcha: this.captcha
      }).then(function (response) {
        if(response.data.result){
          //注册成功,清除数据
          self.email="";
          self.name="";
          self.password="";
          self.captcha="";
          self.registerShow=false;
        }
        showToast(response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      })
    },
    //发送注册验证码
    sendCaptcha: function(){
      if(!this.email){
        showToast("请先输入邮箱");
      }else{
        this.isOverlay = true;
        let self = this;
        axios.post('/user/info/sendRegisterEmailCaptcha', {
          email: this.email
        }).then(function (response) {
          self.sendyzm = true;
          showToast(response.data.message);
          self.isOverlay = false;
          self.sendyzmjs = 60;
          let timer = setInterval(function (){
            self.sendyzmjs--;
            if(self.sendyzmjs == 0){
              clearInterval(timer);
              self.sendyzm = false;
            }
          }, 1000);
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        })
      }
    },
    //退出登录
    logoutOnSelect: function(item){
      if(item.code == 1){
        this.logonPng = "/logon0.png";
        this.notLoginShow = true;
        this.userName = '请先登录';
        //退出登录后移除本地存储的数据
        localStorage.removeItem(kg.data().authorization);//移除token
        localStorage.removeItem(kg.data().userName);
        localStorage.removeItem(kg.data().userEmail);
        localStorage.removeItem(kg.data().userId);
        localStorage.removeItem(kg.data().useSpaceId);
        localStorage.removeItem(kg.data().useSpaceRole);
      }
    },
    //登录
    logon: function(){
      this.isOverlay = true;
      let self = this;
      axios.post('/user/info/emailPasswordLogin', {
        email: this.email,
        password: this.password
      }).then(function (response) {
        if(response.data.result){//登录成功
          localStorage.setItem(kg.data().authorization,response.data.data);//将token存储
          //登录成功后获取用户信息
          axios.get('/user/info/getUserInfo').then(function (res){
            if(res.data.result){//获得信息成功
              //登录后存储一堆数据到本地
              localStorage.setItem(kg.data().userName,res.data.data.name);
              localStorage.setItem(kg.data().userEmail,res.data.data.email);
              localStorage.setItem(kg.data().userId,res.data.data.id);
              if(res.data.data.spaces != null && res.data.data.spaces.length > 0){
                localStorage.setItem(kg.data().useSpaceId,res.data.data.spaces[0].spaceId);
                localStorage.setItem(kg.data().useSpaceRole,res.data.data.spaces[0].role);
              }
              self.logonPng = "/logon1.png";
              self.notLoginShow = false;
              self.logonShow = false;
              self.userName = res.data.data.name;
              //登录成功后,清除字段值
              self.email = "";
              self.password = "";
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