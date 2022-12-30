import { createApp } from 'vue';
import App from './App.vue';
import 'vant/lib/index.css';
import router from "./router/index";
import axios from 'axios';
import {isSpace, isToken, key} from '@/global/KeyGlobal';
import gws from "@/global/WebSocket";


axios.defaults.withCredentials = true
//全局配置axios的请求根路径
axios.defaults.baseURL = key().baseURL;

//请求拦截设置头部
axios.interceptors.request.use(config => {//声明请求拦截器
    if(isToken()){//如果本地保存了token,则在头部传送token
        config.headers.Authorization = localStorage.getItem(key().authorization);
    }
    if(isSpace()){//如果本地保存了正在使用的空间id,则在头部传送使用中空间id
        config.headers[key().useSpaceId] = localStorage.getItem(key().useSpaceId);
    }
    return config;//一定要返回
});
//响应拦截器
axios.interceptors.response.use(response => {
    if(response.data.result == false && response.data.message == 'token已过期'){
        //token失效了,清空token存储
        localStorage.removeItem(key().authorization);
        gws.methods.wsDisconnect();//断开socket
    }
    return response;
}, error => {
    return error;
});

//如果有token,则自动发起连接websocket
if(isToken()){
    gws.methods.wsConnection();
}

const app = createApp(App);
app.use(router);
app.mount('#app');
