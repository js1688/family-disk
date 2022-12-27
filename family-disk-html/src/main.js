import { createApp } from 'vue';
import App from './App.vue';
import 'vant/lib/index.css';
import router from "./router/index";
import axios from 'axios';
import kg from './global/KeyGlobal';

//全局配置axios的请求根路径
axios.defaults.baseURL = 'http://api.jflove.cn/';

//全局拦截设置头部
axios.interceptors.request.use(config => {//声明请求拦截器
    let token = localStorage.getItem(kg.data().authorization);
    if(token != null){//如果本地保存了token,则在头部传送token
        config.headers.Authorization = token;
    }
    let useSpaceId = localStorage.getItem(kg.data().useSpaceId);
    if(useSpaceId != null){//如果本地保存了正在使用的空间id,则在头部传送使用中空间id
        config.headers.common[kg.data().useSpaceId] = useSpaceId;
    }
    return config;//一定要返回
})


const app = createApp(App);
app.use(router);
app.mount('#app');