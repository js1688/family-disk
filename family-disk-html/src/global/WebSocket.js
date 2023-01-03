import SockJS from  'sockjs-client/dist/sockjs.min.js';
import Stomp from "stompjs";
import {key} from "@/global/KeyGlobal";
export default {
    data:function(){
        return {
            stompClient:null
        }
    },
    methods: {
        //创建socket连接
        wsConnection:function(){
            if(this.stompClient == null){
                //建立连接对象
                let token = localStorage.getItem(key().authorization);
                let socket = new SockJS(key().baseURL + 'gateway/stomp?Authorization=' + (token == null ? '' : token));
                //获取STOMP子协议的客户端对象
                this.stompClient = Stomp.over(socket);
                // 向服务器发起websocket连接
                this.stompClient.connect({},(frame) => {
                    console.log("ws建立连接成功")
                }, (err) => {
                    this.wsReConnection();
                });
            }
        },
        //重连
        wsReConnection:function (){
            console.log("ws开始重连");
            this.wsDisconnect();
            this.stompClient = null;
            this.wsConnection();
        },
        //断开socket连接
        wsDisconnect:function(){
            if(this.stompClient != null){
                this.stompClient.disconnect(()=>{
                    this.stompClient = null;
                    console.log("ws断开连接");
                });
            }
        },
        //订阅一个主题
        wsSubscribe:function (topic,callback){
            if(this.stompClient == null){
                return null;
            }
            return this.stompClient.subscribe(topic, (msg) => { // 订阅服务端提供的某个topic
                callback(msg);
            });
        },
        //取消订阅一个主题
        wsUnsubscribe: function (id){
            if(this.stompClient == null){
                return null;
            }
            return this.stompClient.unsubscribe(id);
        }
    }
};