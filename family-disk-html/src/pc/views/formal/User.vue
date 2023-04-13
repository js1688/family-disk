<template>
    <n-space vertical>
      <n-layout has-sider sider-placement="right" position="absolute" style="top: 0; bottom: 0">
        <n-layout-content>
          <!-- 退出登录 -->
          <div v-if="exitOutlineIf" class="div-center">
            <n-space vertical>
              <n-image
                  width="300"
                  src="/img/pc/exit.png"
                  preview-disabled
              />
              <div class="div-center">
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
          <div v-if="enterOutlineIf" class="div-center">
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
                  <div class="div-center">
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
          <div v-if="registerIf" class="div-center">
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
                  <div class="div-center">
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
          <!-- 空间信息 -->
          <div v-if="getSpaceIf">
            <n-spin :show="isOverlay">
              <div style="width: 100%;height: 100%;padding: 24px">
                <div class="div-center">
                  <n-avatar
                      round
                      :size="200"
                      src="/img/pc/user.png"
                  />
                </div>
                <div style="padding-top:10px;" class="div-center">
                  <n-tag size = "large" :bordered="false">
                    {{userInfo.userName}}-{{userInfo.email}}
                  </n-tag>
                </div>
                <n-divider />
                <n-card :title="`${spaceInfo.title}（${spaceInfo.role == 'WRITE' ? '读写' : '只读'}）`">
                  <n-progress type="line" status="success" :percentage="spaceInfo.useSize/spaceInfo.maxSize*100">
                    {{spaceInfo.useSize}}/{{spaceInfo.maxSize}}(MB) {{Math.floor(spaceInfo.useSize/spaceInfo.maxSize*100)}}%
                  </n-progress>
                </n-card>
                <n-divider />
                <div style="width: 50%;display:inline-block;padding-right: 10px;">
                  <n-card title="我的空间人员管理">
                    <n-space vertical>
                      <n-space>
                        <n-button round @click="showInviteOthersSpace = true;simpleField=''" v-if="mySpace">
                          <template #icon>
                            <n-icon><add-circle-outline /></n-icon>
                          </template>
                          邀请人加入我的空间
                        </n-button>
                        <n-tag size = "large" :color="{ color: '#FFF'}" :bordered="false">
                          空间编码:{{spaceInfo.spaceCode}}
                        </n-tag>
                      </n-space>
                      <n-data-table v-if="mySpace"
                          :columns="myColumns"
                          :data="mySpaceOptions"
                          :pagination="false"
                          :bordered="false"
                      />
                      <n-button round type="info" v-if="!mySpace" @click="createSpace">
                        <template #icon>
                          <n-icon>
                            <cloud-outline />
                          </n-icon>
                        </template>
                        创建我的空间
                      </n-button>
                    </n-space>
                  </n-card>
                </div>
                <div style="width: 50%;display:inline-block;padding-left: 10px;">
                  <n-card title="我加入的空间" >
                    <n-space vertical>
                      <n-button round @click="showJoinOthersSpace = true;simpleField=''">
                        <template #icon>
                          <n-icon><add-circle-outline /></n-icon>
                        </template>
                        加入其他人的空间
                      </n-button>
                      <n-data-table
                          :columns="joinColumns"
                          :data="spaceOptions"
                          :pagination="false"
                          :bordered="false"
                      />
                    </n-space>
                  </n-card>
                </div>
              </div>

              <n-modal
                  v-model:show="showJoinOthersSpace"
                  :mask-closable="false"
                  preset="dialog"
                  title="申请加入其他人的空间"
                  positive-text="确认"
                  negative-text="取消"
                  @positive-click="joinSpace"
                  @negative-click="showJoinOthersSpace = false"
              >
                <n-divider />
                <n-input v-model:value="simpleField" placeholder="请输入对方的空间编码"/>
              </n-modal>

              <n-modal
                  v-model:show="showInviteOthersSpace"
                  :mask-closable="false"
                  preset="dialog"
                  title="邀请其他人加入到我的空间"
                  positive-text="确认"
                  negative-text="取消"
                  @positive-click="inviteSpace"
                  @negative-click="showInviteOthersSpace = false"
              >
                <n-divider />
                <n-input v-model:value="simpleField" placeholder="请输入对方的邮箱号"/>
              </n-modal>
            </n-spin>
          </div>

          <!-- 分享管理 -->
          <div v-if="queryShareIf">
            <n-spin :show="isOverlay">
              <n-card title="分享链接列表" >
                <n-data-table
                              :columns="shareColumns"
                              :data="shareData"
                              :pagination="false"
                              :bordered="false"
                />
              </n-card>
            </n-spin>
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
  NAnchorLink,NAnchor,NPopconfirm,NButton,NLayoutContent,NImage,NSpin,NProgress,NDataTable,
  NForm,NFormItem,NInput,NInputGroup,NLayoutFooter,NLayoutHeader,NCard,NModal,
    NAvatar,NDivider,NTag,
  createDiscreteApi
} from "naive-ui";
import {
  ExitOutline, EnterOutline, PersonAddOutline, BackspaceOutline, AddCircleOutline,
  InformationCircleOutline, ShareSocialOutline,
  MailOutline,CloudOutline,
  BuildOutline, OptionsOutline,PersonOutline
} from "@vicons/ionicons5";

import {key} from "@/global/KeyGlobal";
import gws from "@/global/WebSocket";
import axios from "axios";

const { notification,dialog} = createDiscreteApi(['notification','dialog'])

const createColumns = ({
                         buttons,
                         cols
                       }) => {

  if(buttons){
    for (let i = 0; i < buttons.length; i++) {
      const button = buttons[i];
      cols.push(
          {
            title: button.title,
            key: button.key,
            width:button.width,
            titleColSpan:button.titleColSpan,
            render(row) {
              return h(
                  NButton,
                  {
                    strong: true,
                    tertiary: true,
                    size: "small",
                    onClick: () => button.play(row)
                  },
                  { default: () => button.name }
              );
            }
          }
      );
    }
  }
  return cols;
};

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
        label: "空间信息",
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
      PersonOutline,AddCircleOutline,
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
    NPopconfirm,NButton,NLayoutContent,NImage,ExitOutline,NSpin,NCard,NAvatar,NDivider,NProgress,NDataTable,
    NForm,NFormItem,NInput,MailOutline,EnterOutline,PersonOutline,NInputGroup,NLayoutFooter,CloudOutline,
    NLayoutHeader,AddCircleOutline,NModal,NTag
  },
  props: {

  },
  created() {
    this.getUserInfo();
    if(localStorage.getItem(key().authorization) != null){
      this.getSpaceInfo();
      this.getMySpaceRel();
    }
  },
  data: function() {
    return {
      activeKey: ref(localStorage.getItem(key().authorization) == null ? 'logon' : 'getSpace'),
      exitOutlineIf:false,
      queryShareIf:false,
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
      mySpaceOptions:[],
      mySpace:true,
      isOverlay:false,
      myColumns:createColumns({
        buttons:[
          {
            name:"移除",
            title:"操作",
            titleColSpan:3,
            width:50,
            key:"yc",
            play:this.removeRel
          },
          {
            name:"设置只读",
            width:100,
            key:"zd",
            play:this.setRelRoleRead
          },
          {
            name:"设置读写",
            width:100,
            key:"dx",
            play:this.setRelRoleWrite
          },
        ],
        cols:[
          {
            title: "用户名称",
            key: "userName"
          },
          {
            title: "空间权限",
            width:100,
            key: "roleName"
          }
        ]
      }),
      joinColumns:createColumns({
        buttons:[
            {
              name:"切换",
              title:"操作",
              titleColSpan:2,
              width:50,
              key:"qh",
              play:this.switchSpaceOnSelect
            },
            {
              name:"退出",
              width:50,
              key:"tc",
              play:this.exitSpaceOnSelect
            },
        ],
        cols:[
          {
            title: "空间名称",
            key: "name"
          },
          {
            title: "空间权限",
            width:100,
            key: "roleName"
          }
        ]
      }),
      showJoinOthersSpace:false,
      simpleField:"",
      shareData:[],
      shareColumns:createColumns({
        buttons:[
          {
            name:"移除",
            title:"操作",
            width:50,
            titleColSpan:3,
            key:"action",
            play:this.delLink
          },
          {
            name:"复制密码",
            title:"操作",
            width:100,
            key:"action",
            play:this.openPassword
          },
          {
            name:"复制地址",
            title:"操作",
            width:100,
            key:"action",
            play:this.copyLink
          }
        ],
        cols:[
          {
            title: "目录名称",
            key: "keyword"
          },
          {
            title: "到期时间",
            key: "invalidTime",
            width:200
          },
          {
            title: "资源来源",
            key: "bodyTypeName",
            width:100
          }
        ]
      }),
      showInviteOthersSpace:false
    };
  },
  methods: {
    //删除链接
    delLink:function (item){
      let self = this;
      this.showDialog("warning",'是否删除分享:' + item.keyword + ',删除后不可恢复!',function () {
        self.isOverlay = true;
        axios.post('/share/admin/delLink', {bodyId:item.id}).then(function (response) {
          if(response.data.result){
            self.linkShareList();
          }
          self.isOverlay = false;
          self.showToast(response.data.result ? "success" : "error", response.data.message);
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      });
    },
    //显示密码并复制
    openPassword:function (row) {
      if(!row.password){
        this.showToast(null,"该链接没有设置密码");
        return
      }
      let self = this;
      this.$copyText(row.password).then(function (e) {
        self.showToast(null,`密码已复制:${row.password}`);
      }, function (e) {
        self.showToast("error",`复制失败:${row.password}`);
      })
    },
    //复制分享地址
    copyLink:function (item){
      let link = window.location.protocol + '//' + window.location.host + window.location.pathname;
      switch (item.bodyType){
        case 'NOTE':
          link += '#/share/notepad/?';
          break
        case 'NETDISK':
          link += '#/share/netdisk/?';
          break
        default:
          this.showToast("error","复制失败,链接内容不识别");
      }
      link += item.url;
      let self = this;
      this.$copyText(link).then(function (e) {
        self.showToast(null,'分享地址已复制,请粘贴给有需要的人');
      }, function (e) {
        self.showToast("error",`复制失败:${link}`);
      })
    },
    //链接分享管理面板打开时
    linkShareList:function () {
      this.isOverlay = true;
      let self = this;
      this.shareData = [];
      axios.get('/share/admin/getLinkList').then(function (res){
        if(res.data.result){
          for (let i = 0; i < res.data.datas.length; i++) {
            let type = res.data.datas[i].bodyType;
            res.data.datas[i]["bodyTypeName"] = type == 'NOTE' ? '笔记' : type == 'NETDISK' ? '网盘' : '';
            self.shareData.push(res.data.datas[i]);
          }
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
    //邀请人加入我的空间
    inviteSpace:function () {
      if(!this.simpleField){
        this.showToast("error", "请输入对方的邮箱号码");
      }else {
        this.isOverlay = true;
        let self = this;
        axios.post('/user/space/inviteSpace', {
          email: this.simpleField
        }).then(function (response) {
          self.showToast(response.data.result ? "success" : "error", response.data.message);
          self.isOverlay = false;
          if (response.data.result) {
            self.showInviteOthersSpace = false;
            self.getMySpaceRel();
          }
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }
      return false;
    },
    //加入空间
    joinSpace:function (){
      if(!this.simpleField){
        this.showToast("error", "请输入空间编码");
      }else{
        this.isOverlay = true;
        let self = this;
        axios.post('/user/space/joinSpace', {
          targetSpaceCode: this.simpleField
        }).then(function (response) {
          self.showToast(response.data.result ? "success" : "error", response.data.message);
          self.isOverlay = false;
          if(response.data.result){
            self.showJoinOthersSpace = false;
          }
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      }
      return false;
    },
    setRelRoleRead:function (item){
      this.setRelRole("READ",item);
    },
    setRelRoleWrite:function (item){
      let self = this;
      this.showDialog("warning","确定将用户:" + item.userName + ",设置成读写权限吗?",function () {
        self.setRelRole("WRITE",item);
      });
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
          self.getMySpaceRel();
        }
        self.showToast(response.data.result ? "success" : "error", response.data.message);
        self.isOverlay = false;
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
    },
    //删除用户与空间关系
    removeRel:function (item) {
      let self = this;
      this.showDialog("warning",'是否将:' + item.userName + ',从你的空间移除!',function (){
        self.isOverlay = true;
        axios.post('/user/space/removeRel', {
          removeUserId: item.userId
        }).then(function (response) {
          if(response.data.result){
            self.getMySpaceRel();
          }
          self.showToast(response.data.result ? "success" : "error", response.data.message);
          self.isOverlay = false;
        }).catch(function (error) {
          self.isOverlay = false;
          console.log(error);
        });
      });
    },
    //获取用户的空间关系
    getMySpaceRel:function (){
      this.isOverlay = true;
      let self = this;
      this.mySpaceOptions = [];
      axios.get('/user/space/getUserInfoBySpaceId').then(function (res){
        if(res.data.result && res.data.datas.length > 0){
          for (let i = 0; i < res.data.datas.length; i++) {
            res.data.datas[i].roleName = res.data.datas[i].state == 'APPROVAL' ? '待审批' : res.data.datas[i].role == 'WRITE' ? '读写' : '只读';
            self.mySpaceOptions.push(res.data.datas[i]);
          }
        }else{
          //判断自己有没有空间
          let spaces = JSON.parse(localStorage.getItem(key().userAllSpaceRole));
          for (let i = 0; i < spaces.length; i++) {
            if(spaces[i].createUserId + '' == localStorage.getItem(key().userId)){
              return;
            }
          }
          self.mySpace = false;
        }
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
    //退出空间
    exitSpaceOnSelect:function (row){
      this.isOverlay = true;
      let self = this;
      axios.post('/user/space/exitRel', {
        spaceId: row.code
      }).then(function (res){
        if(res.data.result){
          self.getUserInfo();
        }
        self.showToast(res.data.result ? "success" : "error", res.data.message);
        self.isOverlay = false;
      }).catch(function (err){
        self.isOverlay = false;
        console.log(err);
      });
    },
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
      if(!type){
        type = "success";
      }
      notification[type]({
        title: "提示:",
        content: message,
        duration: 2500,
        keepAliveOnHover: true
      });
    },
    showDialog:function(type,content,okClick){
      if(!type){
        type = "success";
      }
      dialog[type]({
        title: "提示",
        content: content,
        positiveText: "确定",
        negativeText: "取消",
        maskClosable: false,
        onPositiveClick:() =>{
          okClick();
        }
      });
    },
    //菜单切换
    menu:function (open){
      this.enterOutlineIf = false;
      this.exitOutlineIf = false;
      this.registerIf = false;
      this.getSpaceIf = false;
      this.queryShareIf = false;

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
          this.getMySpaceRel();
          this.menu("getSpaceIf");
          break
        case "queryShare":
          this.linkShareList();
          this.menu("queryShareIf");
          break
      }
    },
    //切换空间
    switchSpaceOnSelect:function (row) {
      this.isOverlay = true;
      let self = this;
      axios.post('/user/space/switchSpace', {
        targetSpaceId: row.code
      }).then(function (response) {
        self.isOverlay = false;
        self.showToast(response.data.result ? "success" : "error",response.data.message);
        if(response.data.result){
          location.reload();
        }
      }).catch(function (error) {
        self.isOverlay = false;
        console.log(error);
      });
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
      this.showDialog("warning","确定退出登录吗?",function () {
        //退出登录后移除本地存储的数据
        localStorage.removeItem(key().authorization);//移除token
        localStorage.removeItem(key().userName);
        localStorage.removeItem(key().userId);
        localStorage.removeItem(key().useSpaceRole);
        localStorage.removeItem(key().userAllSpaceRole);
        gws.methods.wsDisconnect();//断开socket
        location.reload();
      });
    },
    //获取用户信息
    getUserInfo:function (){
      let self = this;
      axios.get('/user/info/getUserInfo').then(function (res){
        if(res.data.result){//获得信息成功
          //登录后存储一堆数据到本地
          localStorage.setItem(key().userName,res.data.data.name);
          localStorage.setItem(key().userId,res.data.data.id);
          self.spaceOptions = [];
          if(res.data.data.spaces != null && res.data.data.spaces.length > 0){
            //找出正在使用的空间设置到缓存
            for (let i = 0; i < res.data.data.spaces.length; i++) {
              let tp = res.data.data.spaces[i];
              if(tp.state == 'USE'){
                localStorage.setItem(key().useSpaceRole,tp.role);
                self.spaceInfo.role = tp.role;
                self.spaceOptions.push({name:`${tp.title}(正在使用)`,code:tp.spaceId,role:tp.role,roleName:tp.role == 'WRITE' ? '读写':'只读'});
              }else{//切换空间只能切到未使用的空间中去
                self.spaceOptions.push({name:tp.title,code:tp.spaceId,role:tp.role,roleName:tp.role == 'WRITE' ? '读写':'只读'});
              }
            }
            self.userInfo.email = res.data.data.email;
            localStorage.setItem(key().userAllSpaceRole,JSON.stringify(res.data.data.spaces));
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
.div-center{
  width: 100%;height: 100%;display: flex;justify-content: center;align-items: Center;
}
</style>