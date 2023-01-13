import SockJS from  'sockjs-client/dist/sockjs.min.js';
import Stomp from "stompjs";
import {key} from "@/global/KeyGlobal";
export default {
    methods: {
        //创建socket连接
        wsConnection:function(callback){
            if(this.stompClient == null){
                //建立连接对象
                let token = localStorage.getItem(key().authorization);
                let socket = new SockJS(key().baseURL + 'gateway/stomp?Authorization=' + (token == null ? '' : token));
                //获取STOMP子协议的客户端对象
                this.stompClient = Stomp.over(socket);
                // 向服务器发起websocket连接
                this.stompClient.connect({},(frame) => {
                    console.log("ws建立连接成功");
                    if(callback){
                        callback();
                    }
                }, (err) => {
                    setTimeout(() => {
                        this.wsReConnection();
                    }, 3000);//3秒后重试
                });
            }
        },
        //重连
        wsReConnection:function (){
            console.log("ws开始重连");
            //取消所有的订阅
            let callbacksTemp = {};
            for (const key in this.callbacks) {
                callbacksTemp[key] = this.callbacks[key];
                this.wsUnsubscribe(key);
            }
            this.wsDisconnect();
            let self = this;
            this.wsConnection(function (){
                //重新订阅
                for (const key in callbacksTemp) {
                    self.wsSubscribe(callbacksTemp[key].topic,callbacksTemp[key].callback);
                }
            });
        },
        //断开socket连接
        wsDisconnect:function(){
            if(this.stompClient != null){
                this.stompClient.disconnect(()=>{
                    console.log("ws断开连接");
                });
                this.stompClient = null;
            }
        },
        //订阅一个主题
        wsSubscribe:function (topic,callback){
            if(this.stompClient == null){
                return null;
            }
            let ret = this.stompClient.subscribe(topic, (msg) => { // 订阅服务端提供的某个topic
                callback(msg);
            });
            if(!this.callbacks){
                this.callbacks = {};
            }
            this.callbacks[ret.id]={"topic":topic,"callback":callback};
            return ret;
        },
        //取消订阅一个主题
        wsUnsubscribe: function (id){
            if(this.stompClient == null){
                return null;
            }
            delete this.callbacks[id];
            return this.stompClient.unsubscribe(id);
        }
    }
};