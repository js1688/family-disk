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

  <van-cell is-link title="加入空间" @click="joinSpaceShow = true" v-if="notLoginShow == false" />
  <van-action-sheet
      v-model:show="joinSpaceShow"
      @open="spaceCode=''"
      title="申请加入空间">
    <div>
      <van-form @submit="joinSpace">
        <van-cell-group inset>
          <van-field required v-model="spaceCode"
                     :rules="[{ required: true, message: '请输入要加入的空间编码' }]"
                     label="空间编码" placeholder="请输入要加入的空间编码" />
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit">
            提交申请
          </van-button>
        </div>
      </van-form>
    </div>
  </van-action-sheet>

  <van-cell is-link title="管理空间" @click="adminSpaceShow = true" v-if="notLoginShow == false" />
  <van-action-sheet
      v-model:show="adminSpaceShow"
      :lock-scroll="false"
      @open="spaceAdminOpen"
      title="空间关联的账号">
    <div>
      <van-list
          :v-model:loading="false"
          :finished="true"
          finished-text="没有更多了"
      >
        <van-swipe-cell v-for="item in list">
          <van-cell is-link arrow-direction="right"
                    :title="item.userName">
            <van-tag plain type="primary">{{item.state == 'USE' ? '正在使用' :
                item.state == 'APPROVAL' ? '待审批' :
                    item.state == 'NOTUSED' ? '未使用' : ''}}</van-tag>
          </van-cell>
          <template #right>
            <van-button square hairline type="danger"  @click="removeRel(item)" text="移除" />
            <van-button square hairline type="success"  @click="setRelRole('READ',item)" text="设置只读" />
            <van-button square hairline type="success"  @click="setRelRole('WRITE',item)" text="设置读写" />
          </template>
        </van-swipe-cell>
      </van-list>
    </div>
  </van-action-sheet>

  <van-cell is-link title="切换空间" @click="switchSpaceShow = true" v-if="notLoginShow == false"/>
  <van-action-sheet close-on-click-action v-model:show="switchSpaceShow" :actions="spaceOptions" @select="switchSpaceOnSelect" />

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

  <van-cell is-link title="分享管理" @click="queryShareShow = true" v-if="notLoginShow == false" />
  <van-action-sheet :lock-scroll="false" v-model:show="queryShareShow" title="管理分享链接" @open="linkShareOpen">
    <div class="content">
      <van-list
          :v-model:loading="false"
          :finished="true"
          finished-text="没有更多了"
      >
        <van-swipe-cell v-for="item in linkList">
          <van-cell is-link arrow-direction="right" :label="item.invalidTime"
                    :title="item.keyword">
            <van-tag plain type="primary">{{item.bodyType == 'NOTE' ? '笔记' : ''}}</van-tag>
          </van-cell>
          <template #right>
            <van-button square hairline type="danger"  @click="delLink(item)" text="移除" />
            <van-button :disabled="item.password == null || item.password == ''" square hairline type="primary"  @click="openPassword(item.password)" text="复制密码" />
            <van-button square hairline type="success"  @click="copyLink(item.url)" text="复制地址" />
          </template>
        </van-swipe-cell>
      </van-list>
    </div>
  </van-action-sheet>
</template>

<script>
import { ref } from 'vue';
import {showConfirmDialog, showToast} from 'vant';
import { Cell,Image,Form, Field, CellGroup,Button,ActionSheet,Tag,SwipeCell,List} from 'vant';
import { Overlay,Loading } from 'vant';
import axios from 'axios';
import gws from "@/global/WebSocket";
import {key} from "@/global/KeyGlobal";
import '@vant/touch-emulator';//vant适配桌面端
import VueClipboard from 'vue-clipboard2'
export default {
  name: "User",
  setup() {
    const logonShow = ref(false);
    const registerShow = ref(false);
    const logoutShow = ref(false);
    const createSpaceShow = ref(false);
    const querySpaceShow = ref(false);
    const joinSpaceShow = ref(false);
    const adminSpaceShow = ref(false);
    const switchSpaceShow = ref(false);
    const queryShareShow = ref(false);
    const okActions = [
      { name: '确定',code:1 },
      { name: '取消',code:0 }
    ];

    return {
      switchSpaceShow,
      joinSpaceShow,
      adminSpaceShow,
      logoutShow,
      registerShow,
      logonShow,
      okActions,
      createSpaceShow,
      querySpaceShow,
      queryShareShow
    };
  },
  components: {
    VueClipboard,
    [SwipeCell.name]:SwipeCell,
    [List.name]:List,
    [Tag.name]:Tag,
    [Cell.name]: Cell,
    [ActionSheet.name]:ActionSheet,
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
      loading:false,
      finished:false,
      email:"",
      password:"",
      pwd:"",
      captcha:"",
      name:"",
      logonPng:localStorage.getItem(key().authorization) == null ? "/logon0.png" : "/logon1.png",
      userName: localStorage.getItem(key().authorization) == null ? "请先登录" : localStorage.getItem(key().userName),
      isOverlay: false,
      sendyzm:false,
      sendyzmName:'发送验证码',
      sendyzmjs:0,
      notLoginShow: localStorage.getItem(key().authorization) == null ? true : false,
      maxSize:0,
      title:"",
      useSize:0,
      spaceOptions:null,
      spaceCode:'',
      list:[],
      linkList:[]
    };
  },
  created() {
    this.getUserInfo();
  },
  methods: {
    //删除链接
    delLink:function (item){
      let self = this;
      showConfirmDialog({
        title: '删除',
        message:'是否删除分享:' + item.keyword + ',删除后不可恢复!'
      }).then(() => {
        this.isOverlay = true;
        axios.post('/note/share/delNote', {bodyId:item.id}).then(function (response) {
          if(response.data.result){
            self.linkShareOpen();
          }
          self.isOverlay = false;
          showToast(response.data.message);
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }).catch(function (error) {
      });
    },
    //显示密码并复制
    openPassword:function (password) {
      this.$copyText(password).then(function (e) {
        showToast(`密码已复制:${password}`);
      }, function (e) {
        showToast(`复制失败:${password}`);
      })
    },
    //复制分享地址
    copyLink:function (url){
      let link = window.location.protocol + '//' + window.location.host + '/#/share/notepad/?' + url;
      this.$copyText(link).then(function (e) {
        showToast('分享地址已复制,请粘贴给有需要的人');
      }, function (e) {
        showToast(`复制失败:${link}`);
      })
    },
    //设置用户与空间的权限级别
    setRelRole:function (role,item) {
      let self = this;
      this.isOverlay = true;
      axios.post('/user/space/setRelRole', {
        targetUserId: item.userId,
        role:role
      }).then(function (response) {
        if(response.data.result){
          self.spaceAdminOpen();
        }
        showToast(response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //删除用户与空间关系
    removeRel:function (item) {
      let self = this;
      showConfirmDialog({
        title: '删除',
        message:'是否删除权限:' + item.userName + ',删除后不可恢复!'
      }).then(() => {
        this.isOverlay = true;
        axios.post('/user/space/removeRel', {
          removeUserId: item.userId
        }).then(function (response) {
          if(response.data.result){
            self.spaceAdminOpen();
          }
          showToast(response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }).catch(function (error) {
      });
    },
    //链接分享管理面板打开时
    linkShareOpen:function () {
      this.isOverlay = true;
      let self = this;
      this.linkList = [];
      axios.get('/note/share/getLinkList').then(function (res){
        if(res.data.result){
          self.linkList = res.data.datas;
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
    //空间管理面板打开时
    spaceAdminOpen:function (){
      this.isOverlay = true;
      let self = this;
      this.list = [];
      axios.get('/user/space/getUserInfoBySpaceId').then(function (res){
        if(res.data.result){
          self.list = res.data.datas;
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
    //加入空间
    joinSpace:function (){
      this.isOverlay = true;
      let self = this;
      axios.post('/user/space/joinSpace', {
        targetSpaceCode: this.spaceCode
      }).then(function (response) {
        showToast(response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
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
          title: localStorage.getItem(key().userName) + "的空间"
        }).then(function (response) {
          if(response.data.result){
            localStorage.setItem(key().useSpaceId,response.data.data.id);
            localStorage.setItem(key().useSpaceRole,'WRITE');//自己创建的空间,权限是读写
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
    //切换空间
    switchSpaceOnSelect:function (item) {
      this.isOverlay = true;
      let self = this;
      axios.post('/user/space/switchSpace', {
        targetSpaceId: item.code
      }).then(function (response) {
        if(response.data.result){
          localStorage.removeItem(key().useSpaceId);
          self.getUserInfo();//重新获取用户信息并设置
        }
        self.isOverlay = false;
        showToast(response.data.message);
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //退出登录
    logoutOnSelect: function(item){
      if(item.code == 1){
        this.logonPng = "/logon0.png";
        this.notLoginShow = true;
        this.userName = '请先登录';
        //退出登录后移除本地存储的数据
        localStorage.removeItem(key().authorization);//移除token
        localStorage.removeItem(key().userName);
        localStorage.removeItem(key().userEmail);
        localStorage.removeItem(key().userId);
        localStorage.removeItem(key().useSpaceId);
        localStorage.removeItem(key().useSpaceRole);
        localStorage.removeItem(key().userAllSpaceRole);
        gws.methods.wsDisconnect();//断开socket
      }
    },
    //获取用户信息并设置在本地缓存
    getUserInfo:function (){
      this.isOverlay = true;
      let self = this;
      axios.get('/user/info/getUserInfo').then(function (res){
        if(res.data.result){//获得信息成功
          //登录后存储一堆数据到本地
          localStorage.setItem(key().userName,res.data.data.name);
          localStorage.setItem(key().userEmail,res.data.data.email);
          localStorage.setItem(key().userId,res.data.data.id);
          self.spaceOptions = [];
          if(res.data.data.spaces != null && res.data.data.spaces.length > 0){
            //找出正在使用的空间设置到缓存
            for (let i = 0; i < res.data.data.spaces.length; i++) {
              let tp = res.data.data.spaces[i];
              if(tp.state == 'USE'){
                localStorage.setItem(key().useSpaceId,tp.spaceId);
                localStorage.setItem(key().useSpaceRole,tp.role);
              }else{//切换空间只能切到未使用的空间中去
                self.spaceOptions.push({name:tp.title,code:tp.spaceId});
              }
            }
            localStorage.setItem(key().userAllSpaceRole,JSON.stringify(res.data.data.spaces));
          }
          self.userName = res.data.data.name;
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
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
          localStorage.setItem(key().authorization,response.data.data);//将token存储
          //登录成功后获取用户信息
          self.isOverlay = false;
          self.getUserInfo();
          gws.methods.wsConnection(null);//连接socket
          //登录成功后,清除字段值
          self.email = "";
          self.password = "";
          self.notLoginShow = false;
          self.logonShow = false;
          self.logonPng = "/logon1.png";
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