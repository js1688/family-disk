import SockJS from  'sockjs-client/dist/sockjs.min.js';
import Stomp from "stompjs";
import kg from "@/global/KeyGlobal";
export default {
    data:function(){
        return {
            socket:null,
            stompClient:null
        }
    },
    methods: {
        wsConnection:function(){
            //建立连接对象
            this.socket = new SockJS('http://127.0.0.1:8800/gateway/stomp');
            //获取STOMP子协议的客户端对象
            this.stompClient = Stomp.over(this.socket);
            // 向服务器发起websocket连接
            this.stompClient.connect({},(frame) => {
                console.log("ws建立连接成功")
            }, (err) => {
                // 连接发生错误时的处理函数
                console.log(err);
            });
        },
        wsDisconnect:function(){

        },
        wsSubscribe:function (){
            let topic = "/user/";
            topic += localStorage.getItem(kg.data().userId) + "-" + localStorage.getItem(kg.data().useSpaceId);
            topic += "/add/file/result";
            this.stompClient.subscribe(topic, (msg) => { // 订阅服务端提供的某个topic
                console.log(msg.body);  // msg.body存放的是服务端发送给我们的信息
            });
        }
    }
};