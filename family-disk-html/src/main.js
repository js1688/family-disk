import { createApp } from 'vue';
import App from './App.vue';
import 'vant/lib/index.css';
import router from "./router/index";
import axios from 'axios';
import {isSpace, isToken, key, keyPut} from '@/global/KeyGlobal';
import gws from "@/global/WebSocket";

axios.defaults.withCredentials = true
//全局配置axios的请求根路径
axios.defaults.baseURL = key().baseURL;

//请求拦截设置头部
axios.interceptors.request.use(config => {//声明请求拦截器
    if(isToken()){//如果本地保存了token,则在头部传送token
        config.headers[key().authorization] = localStorage.getItem(key().authorization);
    }
    if(isSpace()){//如果本地保存了正在使用的空间id,则在头部传送使用中空间id
        config.headers[key().useSpaceId] = localStorage.getItem(key().useSpaceId);
    }
    return config;//一定要返回
});
//响应拦截器
axios.interceptors.response.use(response => {
    if(response.data.result == false && response.data.message == 'token已过期'){
        //token失效了,清空token存储,空间id存储
        localStorage.removeItem(key().authorization);
        localStorage.removeItem(key().useSpaceId);
        gws.methods.wsDisconnect();//断开socket
    }
    return response;
}, error => {
    return error;
});


//如果有token,则自动发起连接websocket
if(isToken()){
    gws.methods.wsConnection(null);
}



//检查网络环境,是否可以切换至内网
//切换到内网时,会碰到网页端是https,内网地址是http,无法访问过去,需要在服务端启动nginx将ip默认的443端口做一下转发,可以使用openssl工具生成一个本地证书试试
console.log("尝试切换到内网环境:"+key().lanURL);
axios.options(key().lanURL).then(function (res2){
    if(res2.request.status == 200 || res2.request.status == 404){
        keyPut("baseURL",key().lanURL);
        axios.defaults.baseURL = key().baseURL;//重新设置
        setTimeout(function (){
            if(isToken()){
                gws.methods.wsReConnection();
            }
        },3000);//三秒后使用新地址重连一下websocket
        console.log("内网环境切换成功");
    }
}).catch(function (err) {
});

const app = createApp(App);
app.use(router);
app.mount('#app');
