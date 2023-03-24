<template>
    <n-space vertical>
      <n-layout has-sider sider-placement="right" position="absolute" style="top: 0; bottom: 0">
        <n-layout-content>
          <!-- 退出登录 -->
          <div v-if="exitOutlineIf" style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
            <n-space vertical>
              <n-image
                  width="300"
                  src="/img/pc/exit.png"
                  preview-disabled
              />
              <div style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
                <n-button round type="info" @click="exitLogon">
                  <template #icon>
                    <n-icon>
                      <exit-outline />
                    </n-icon>
                  </template>
                  退出
                </n-button>
              </div>
            </n-space>
          </div>
          <!-- 登录 -->
          <div v-if="enterOutlineIf" style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
            <n-spin :show="isOverlay">
              <n-space vertical>
                <n-image
                    width="300"
                    src="/img/pc/logon.png"
                    preview-disabled
                />
                <n-form
                    ref="enterformRef"
                    label-placement="left"
                    label-width="auto"
                    require-mark-placement="right-hanging"
                    :model="userInfo"
                    size="large"
                    :rules='{
                        email: {
                            required: true,
                            message: "请输入邮箱",
                            trigger: "blur"
                          },
                          password: {
                            required: true,
                            message: "请输入密码",
                            trigger: ["input", "blur"]
                          }
                      }'
                >
                  <n-form-item label="邮箱:" path="email">
                    <n-input v-model:value="userInfo.email" placeholder="请输入邮箱">
                      <template #prefix>
                        <n-icon :component="MailOutline" />
                      </template>
                    </n-input>
                  </n-form-item>

                  <n-form-item label="密码:" path="password">
                    <n-input v-model:value="userInfo.password"
                             type="password"
                             show-password-on="mousedown"
                             placeholder="请输入密码"
                    />
                  </n-form-item>
                  <div style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
                    <n-button round type="info" @click="logon">
                      <template #icon>
                        <n-icon><enter-outline /></n-icon>
                      </template>
                      登录
                    </n-button>
                  </div>
                </n-form>
              </n-space>
            </n-spin>
          </div>
          <!-- 注册 -->
          <div v-if="registerIf" style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
            <n-spin :show="isOverlay">
              <n-space vertical>
                <n-image
                    width="300"
                    src="/img/pc/register.png"
                    preview-disabled
                />
                <n-form
                    ref="registerformRef"
                    label-placement="left"
                    label-width="auto"
                    require-mark-placement="right-hanging"
                    :model="userInfo"
                    size="large"
                    :rules='{
                        email: {
                          required: true,
                          message: "请输入邮箱",
                          trigger: "blur"
                        },
                        userName: {
                          required: true,
                          message: "请输入用户名",
                          trigger: "blur"
                        },
                        captcha: {
                          required: true,
                          message: "请输入邮箱验证码",
                          trigger: "blur"
                        },
                        password: {
                          required: true,
                          message: "请输入密码",
                          trigger: ["input", "blur"]
                        },
                        reenteredPassword: [
                          {
                            required: true,
                            message: "请再次输入密码",
                            trigger: ["input", "blur"]
                          },
                          {
                            validator: validatePasswordStartWith,
                            message: "两次密码输入不一致",
                            trigger: "input"
                          },
                          {
                            validator: validatePasswordSame,
                            message: "两次密码输入不一致",
                            trigger: ["blur", "password-input"]
                          }
                        ]
                    }'
                >
                  <n-form-item label="用户名:" path="userName">
                    <n-input v-model:value="userInfo.userName" placeholder="请输入用户名">
                      <template #prefix>
                        <n-icon :component="PersonOutline" />
                      </template>
                    </n-input>
                  </n-form-item>
                  <n-form-item label="邮箱:" path="email">
                    <n-input v-model:value="userInfo.email" placeholder="请输入邮箱">
                      <template #prefix>
                        <n-icon :component="MailOutline" />
                      </template>
                    </n-input>
                  </n-form-item>

                  <n-form-item label="密码:" path="password">
                    <n-input v-model:value="userInfo.password"
                             type="password"
                             show-password-on="mousedown"
                             placeholder="请输入密码"
                             @input="handlePasswordInput"
                             @keydown.enter.prevent
                    />
                  </n-form-item>
                  <n-form-item
                      ref="rPasswordFormItemRef"
                      first
                      path="reenteredPassword"
                      label="确认密码"
                  >
                    <n-input
                        v-model:value="userInfo.reenteredPassword"
                        :disabled="!userInfo.password"
                        type="password"
                        placeholder="请第二次输入密码"
                        @keydown.enter.prevent
                    />
                  </n-form-item>
                  <n-form-item label="验证码:" path="captcha">
                    <n-input-group>
                      <n-input :style="{ width: '150px' }" v-model:value="userInfo.captcha" placeholder="请输入邮箱验证码"/>
                      <n-button type="info" ghost :disabled="sendyzm" @click="sendCaptcha">{{sendyzmjs == 0 ? sendyzmName : sendyzmName+'(' + sendyzmjs +')'}}</n-button>
                    </n-input-group>
                  </n-form-item>
                  <div style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
                    <n-button round type="info" @click="registerF">
                      <template #icon>
                        <n-icon><enter-outline /></n-icon>
                      </template>
                      注册
                    </n-button>
                  </div>
                </n-form>
              </n-space>
            </n-spin>
          </div>
          <div v-if="getSpaceIf">
            <n-spin :show="isOverlay">
              <div style="width: 100%;height: 100%;padding: 24px">
                <div style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
                  <n-avatar
                      round
                      :size="200"
                      src="/img/pc/user.png"
                  />
                </div>
                <n-divider />
                <n-card title="账号信息">
                  <n-descriptions label-align="center" label-placement="left" :column="2" size="large">
                    <n-descriptions-item label="用户邮箱">
                      {{userInfo.email}}
                    </n-descriptions-item>
                    <n-descriptions-item label="空间编码">
                      {{spaceInfo.spaceCode}}
                    </n-descriptions-item>
                    <n-descriptions-item label="空间名称">
                      {{spaceInfo.title}}
                    </n-descriptions-item>
                    <n-descriptions-item label="空间权限">
                      {{spaceInfo.role}}
                    </n-descriptions-item>
                  </n-descriptions>
                </n-card>
                <n-divider />
                <n-card :title="`空间存储（${Math.floor(spaceInfo.useSize/spaceInfo.maxSize*100)}%）`">
                  <n-progress type="line" status="success" :percentage="spaceInfo.useSize/spaceInfo.maxSize*100">
                    {{spaceInfo.useSize}}/{{spaceInfo.maxSize}}(MB)
                  </n-progress>
                </n-card>
              </div>
            </n-spin>
          </div>
          <!-- 创建空间 -->
          <div v-if="ratIf" style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
            <n-space vertical>
              <n-image
                  width="300"
                  src="/img/pc/cloudDisk.png"
                  preview-disabled
              />
              <div style="width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;">
                <n-button round type="info" @click="createSpace">
                  <template #icon>
                    <n-icon>
                      <cloud-outline />
                    </n-icon>
                  </template>
                  创建
                </n-button>
              </div>
            </n-space>
          </div>
        </n-layout-content>
        <n-layout-sider
            bordered
            collapse-mode="width"
            :width="150"
            :collapsed-width="64"
            :collapsed="collapsed"
            show-trigger
            @collapse="collapsed = true"
            @expand="collapsed = false"
        >
          <n-menu
              v-model:value="activeKey"
              :collapsed="collapsed"
              :collapsed-width="64"
              :collapsed-icon-size="22"
              :options="menuOptions"
              @update:value="handleUpdateValue"
          />
        </n-layout-sider>
      </n-layout>
    </n-space>
</template>

<script>
import { h, ref } from "vue";
import { NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider ,NDescriptions,NDescriptionsItem,
  NAnchorLink,NAnchor,NPopconfirm,NButton,NLayoutContent,NImage,NSpin,NProgress,
  NForm,NFormItem,NInput,NInputGroup,NLayoutFooter,NLayoutHeader,NCard,
    NAvatar,NDivider,
  createDiscreteApi
} from "naive-ui";
import {
  ExitOutline, EnterOutline, PersonAddOutline, BackspaceOutline, AddCircleOutline,
  InformationCircleOutline, ShareSocialOutline,
  MailOutline,CloudOutline,
  BuildOutline, OptionsOutline,PersonOutline
} from "@vicons/ionicons5";

import {isToken, key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";

const { notification } = createDiscreteApi(['notification'])

export default {
  name: "User",
  setup() {
    function renderIcon(icon) {
      return () => h(NIcon, null, { default: () => h(icon) });
    }
    //登陆之前的菜单
    const beforeLogging = [
      {
        label: "登录",
        key: "logon",
        icon:renderIcon(EnterOutline)
      },
      {
        label: "注册",
        key: "register",
        icon:renderIcon(PersonAddOutline)
      }
    ];
    //登陆之后的菜单
    const afterLogging = [
      {
        label: "退出登录",
        key: "logout",
        icon:renderIcon(ExitOutline)
      },
      {
        label: "创建空间",
        key: "rat",
        icon:renderIcon(BackspaceOutline)
      },
      {
        label: "加入空间",
        key: "joinSpace",
        icon:renderIcon(AddCircleOutline)
      },
      {
        label: "管理空间",
        key: "adminSpace",
        icon:renderIcon(BuildOutline)
      },
      {
        label: "切换空间",
        key: "switchSpace",
        icon:renderIcon(OptionsOutline)
      },
      {
        label: "查看空间",
        key: "getSpace",
        icon:renderIcon(InformationCircleOutline)
      },
      {
        label: "分享管理",
        key: "queryShare",
        icon:renderIcon(ShareSocialOutline)
      }
    ];
    const registerformRef = ref(null);
    const rPasswordFormItemRef = ref(null);
    return {
      collapsed: ref(false),
      menuOptions:localStorage.getItem(key().authorization) == null ? beforeLogging : afterLogging,
      MailOutline,CloudOutline,
      PersonOutline,
      enterformRef:ref(null),
      registerformRef,
      rPasswordFormItemRef,
      handlePasswordInput() {
        if (registerformRef.value.reenteredPassword) {
          this.rPasswordFormItemRef.value?.validate({ trigger: "password-input" });
        }
      }
    }
  },
  components: {
    NIcon,NLayout,NSwitch,NMenu,NSpace,NLayoutSider,NAnchorLink,NAnchor,NDescriptions,NDescriptionsItem,
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,
    NLayoutHeader
  },
  props: {

  },
  created() {
    this.getUserInfo();
    if(localStorage.getItem(key().authorization) != null){
      this.getSpaceInfo();
    }
  },
  data: function() {
    return {
      activeKey: ref(localStorage.getItem(key().authorization) == null ? 'logon' : 'getSpace'),
      exitOutlineIf:false,
      ratIf:false,
      enterOutlineIf:localStorage.getItem(key().authorization) == null ? true : false,
      registerIf:false,
      getSpaceIf:localStorage.getItem(key().authorization) != null ? true : false,
      userInfo:ref({
        password: "",
        email: "",
        userName:"",
        reenteredPassword:"",
        captcha:""
      }),
      spaceInfo:{
        title:"",
        maxSize:0,
        useSize:0,
        spaceCode:"",
        role:""
      },
      sendyzm:false,
      sendyzmName:'发送验证码',
      sendyzmjs:0,
      spaceOptions:[],
      isOverlay:false
    };
  },
  methods: {
    //查看空间
    getSpaceInfo: function(){
      this.isOverlay = true;
      let self = this;
      axios.get('/user/space/getSpaceInfo').then(function (res){
        if(res.data.result){
          self.spaceInfo.title = res.data.data.title;
          self.spaceInfo.maxSize = res.data.data.maxSize;
          self.spaceInfo.useSize = res.data.data.useSize;
          self.spaceInfo.spaceCode = res.data.data.code;
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
    //发送注册验证码
    sendCaptcha: function(){
      if(!this.userInfo.email){
        this.showToast("error", "请先输入邮箱");
        return;
      }
      let self = this;
      self.isOverlay = true;
      axios.post('/user/info/sendRegisterEmailCaptcha', {
        email: this.userInfo.email
      }).then(function (response) {
        if (response.data.result) {
          self.sendyzm = true;
          self.sendyzmjs = 60;
          let timer = setInterval(function () {
            self.sendyzmjs--;
            if (self.sendyzmjs == 0) {
              clearInterval(timer);
              self.sendyzm = false;
            }
          }, 1000);
        }
        self.isOverlay = false;
        self.showToast(response.data.result ? "success" : "error", response.data.message);
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      })
    },
    //提示
    showToast:function(type,message){
      notification[type]({
        title: "提示:",
        content: message,
        duration: 2500,
        keepAliveOnHover: true
      });
    },
    //菜单切换
    menu:function (open){
      this.enterOutlineIf = false;
      this.exitOutlineIf = false;
      this.registerIf = false;
      this.getSpaceIf = false;
      this.ratIf = false;

      this[open] = true;
    },
    //根据点击不同的子菜单,显示不同的功能面板
    handleUpdateValue:function (key, item){
      switch (key){
        case "logout":
          this.menu("exitOutlineIf");
          break
        case "logon":
          this.menu("enterOutlineIf");
          break
        case "register":
          this.menu("registerIf");
          break
        case "getSpace":
          this.getSpaceInfo();
          this.menu("getSpaceIf");
          break
        case "rat":
          this.menu("ratIf");
          break
      }
    },
    //创建空间
    createSpace:function (){
      this.isOverlay = true;
      let self = this;
      axios.post('/user/space/createSpace', {
        title: localStorage.getItem(key().userName) + "的空间"
      }).then(function (response) {
        if(response.data.result){
          localStorage.setItem(key().useSpaceRole,'WRITE');//自己创建的空间,权限是读写
        }
        self.showToast(response.data.result ? "success" : "error",response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      })
    },
    //退出登录
    exitLogon:function (){
      //退出登录后移除本地存储的数据
      localStorage.removeItem(key().authorization);//移除token
      localStorage.removeItem(key().userName);
      localStorage.removeItem(key().useSpaceRole);
      localStorage.removeItem(key().userAllSpaceRole);
      gws.methods.wsDisconnect();//断开socket
      location.reload();
    },
    //获取用户信息
    getUserInfo:function (){
      let self = this;
      axios.get('/user/info/getUserInfo').then(function (res){
        if(res.data.result){//获得信息成功
          //登录后存储一堆数据到本地
          localStorage.setItem(key().userName,res.data.data.name);
          self.spaceOptions = [];
          if(res.data.data.spaces != null && res.data.data.spaces.length > 0){
            //找出正在使用的空间设置到缓存
            for (let i = 0; i < res.data.data.spaces.length; i++) {
              let tp = res.data.data.spaces[i];
              if(tp.state == 'USE'){
                localStorage.setItem(key().useSpaceRole,tp.role);
                self.spaceInfo.role = tp.role;
                self.spaceOptions.push({name:`${tp.title}(当前空间)`,code:tp.spaceId});
              }else{//切换空间只能切到未使用的空间中去
                self.spaceOptions.push({name:tp.title,code:tp.spaceId});
              }
            }
            self.userInfo.email = res.data.data.email;
            localStorage.setItem(key().userAllSpaceRole,JSON.stringify());
          }
          self.userInfo.userName = res.data.data.name;
        }
      }).catch(function (err){
        console.log(err);
      });
    },
    //登录
    logon:function (e) {
      e.preventDefault();
      this.$refs.enterformRef.validate((errors) => {
        if (!errors) {
          let self = this;
          self.isOverlay = true;
          axios.post('/user/info/emailPasswordLogin', {
            email: this.userInfo.email,
            password: this.userInfo.password
          }).then(function (response) {
            self.isOverlay = false;
            if(response.data.result){//登录成功
              localStorage.setItem(key().authorization,response.data.data);//将token存储
              //登录成功后获取用户信息
              self.getUserInfo();
              gws.methods.wsConnection(null);//连接socket
              //登录成功后,清除字段值
              self.userInfo.email = "";
              self.userInfo.password = "";
              location.reload();
            }else{
              self.showToast(response.data.result ? "success" : "error",response.data.message);
            }
          }).catch(function (error) {
            self.isOverlay = false;
            console.log(error);
          });
        }
      });
    },
    //注册
    registerF:function (e) {
      e.preventDefault();
      this.$refs.registerformRef.validate((errors) => {
        if (!errors) {
          this.isOverlay = true;
          let self = this;
          axios.post('/user/info/createUserInfo', {
            email: this.userInfo.email,
            name: this.userInfo.userName,
            password: this.userInfo.password,
            captcha: this.userInfo.captcha
          }).then(function (response) {
            self.showToast(response.data.result ? "success" : "error",response.data.message);
            self.isOverlay = false;
            if(response.data.result){
              //注册成功,清除数据
              self.userInfo = ref({
                password: "",
                email: "",
                userName:"",
                reenteredPassword:"",
                captcha:""
              });
              //注册成功后切换到登陆面板
              self.activeKey = ref("logon");
              self.menu("enterOutlineIf");
            }
          }).catch(function (error) {
            self.isOverlay = false;
            console.log(error);
          })
        }
      });
    },
    validatePasswordStartWith:function(rule, value) {
      return !!this.userInfo.password && this.userInfo.password.startsWith(value) && this.userInfo.password.length >= value.length;
    },
    validatePasswordSame:function(rule, value) {
      return value === this.userInfo.password;
    }
  }
}
</script>

<style scoped>
.n-layout-header,
.n-layout-footer {
  padding: 24px;
}
</style>