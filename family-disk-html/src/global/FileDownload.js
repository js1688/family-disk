/**
 * 下载文件工具类,支持超大文件下载
 *   beforeCreate() {
 *     //因为使用 streamSaver 下载 会生成 300*150 大小的 iframe 窗口,属性是隐藏的, 它会造成 body高度增加, 所以在页面创建之前,给body 添加忽略隐藏属性的高度
 *     document.querySelector('body').setAttribute('style', 'overflow:hidden;')
 *   },
 *   created() {
 *       //如果想监听下载进度的变化,改变页面,可以通过定时任务的方式监听变化,改变data对象中的变量达到改变页面的目的
 *       let self = this;
 *       setInterval(function (){
 *         let list = getDownloadList();
 *         self.downloadList = [];
 *         for (const listKey in list) {
 *           let v = list[listKey];
 *           v.data['scale'] = v.data.progress == 0 ? 0 : Math.floor(v.data.progress/v.data.sliceNum*100);
 *           self.downloadList.push(v);
 *         }
 *       }, 500);
 *   },
 */
import streamSaver from 'streamsaver'

import axios from "axios";


streamSaver.mitm = window.location.protocol + '//' + window.location.host + '/ss/mitm.html?version=2.0.0';
let downloadList = {};//map,文件md5值当key,value,包含信息

/**
 * 获取列表
 * @returns
 */
export function getDownloadList(){
    return downloadList;
}

/**
 * 请求获得一个文件的二进制完整流,适合小文件下载
 * 它不会加入到文件下载列表中
 * 如果文件超过了10mb,就会拒绝下载
 * @param soi 探测信息结果
 */
export async function FileDoownloadSmall(soi){
    if(soi.state){
        let size = Math.floor(soi.data.total/1024/1024);
        if(size > 10){
            return {state:false,msg:`文件[${soi.data.name}]太大,请使用大文件下载方式`};
        }
        let bytes = new Uint8Array(soi.data.total);
        let sliceDownload = async function (s){
            let response = await axios.post('/file/slice/getFile', {
                fileMd5: soi.data.fileMd5,
                name: soi.data.name,
                source:soi.data.source
            },{
                responseType:"arraybuffer",
                headers:{
                    Range:"bytes="+s+"-"
                }
            });

            const { data, headers } = response;
            let contentRange = headers["content-range"];
            let end = contentRange.substring(contentRange.indexOf("-") + 1,contentRange.indexOf("/")) * 1;
            let contentLength = headers["content-length"] * 1;
            let start = contentRange.substring(6,contentRange.indexOf("-")) * 1;

            if(!soi.data.sliceNum){//第一次下载,分析需要下载多少次
                soi.data.sliceNum = Math.ceil(soi.data.total / contentLength);
            }

            //将分片二进制字节复制到总字节缓存中
            let retArr = new Uint8Array(data);
            for (let i = 0; i < retArr.byteLength; i++) {
                bytes[start + i] = retArr[i];
            }

            //更新下载进度
            soi.data.progress++;

            if(end < soi.data.total-1 && response.status == 206){//还有文件分片,继续请求
                await sliceDownload(start+contentLength);
            }
        }

        await sliceDownload(0);//开始下载

        return {state:true,msg:`文件[${soi.data.name}]下载完成`,bytes:bytes};

    }else{
        return soi;
    }
}

/**
 * 执行下载,按队列顺序同步下载
 * 每次取列表中的第一条开始下载
 * @constructor
 */
let isDownload = false;
async function queueSyncDownload(){
    if(isDownload){//已激活下载,不需要重复激活
        return;
    }
    isDownload = true;//设置正在下载
    let soi = downloadList[Object.keys(downloadList)[0]];//每次取第一条开始执行下载
    let sliceDownload = async function (s){
        if(!downloadList[soi.data.fileMd5]){//下载可能已经取消了,不要下载了
            return;
        }

        let response = await axios.post('/file/slice/getFile', {
            fileMd5: soi.data.fileMd5,
            name: soi.data.name,
            source:soi.data.source
        },{
            responseType:"arraybuffer",
            headers:{
                Range:"bytes="+s+"-"
            }
        });

        const { data, headers } = response;
        let contentRange = headers["content-range"];
        let end = contentRange.substring(contentRange.indexOf("-") + 1,contentRange.indexOf("/")) * 1;
        let contentLength = headers["content-length"] * 1;
        let start = contentRange.substring(6,contentRange.indexOf("-")) * 1;

        if(!soi.data.sliceNum){//第一次下载,分析需要下载多少次
            soi.data.sliceNum = Math.ceil(soi.data.total / contentLength);
        }



        //将二进制流写入
        await new Blob([data]).stream().pipeTo(soi.fileStream,{preventClose:true});//设置写入后不要自动关闭文件流
        //更新下载进度
        soi.data.progress++;

        if(end < soi.data.total-1 && response.status == 206){//还有文件分片,继续请求
            await sliceDownload(start+contentLength);
        }
    }

    await sliceDownload(0);//开始下载

    if(!downloadList[soi.data.fileMd5]){//下载可能已经取消了,不要下载了
        isDownload = false;
        return;
    }

    await soi.fileStream.getWriter().close();//文件下载完成了,主动关闭写入文件流

    //清除下载记录
    delete downloadList[soi.data.fileMd5];

    isDownload = false;

    //判断队列中是否还有任务,有则重新激活
    if(Object.keys(downloadList).length != 0){
        queueSyncDownload().then(function (e){});
    }
}

/**
 * 开始下载一个完整的文件,支持下载超大文件
 * 必须先使用文件探测方法得到探测结果,再开始下载
 * 每次下载的分片大小由服务端决定,下载文件都是同步下载分片,不使用异步多线程下载
 * @param soi 探测信息结果
 */
export async function FileDownload(soi){
    if(soi.state){
        if(downloadList[soi.data.fileMd5]){
            return {state:false,msg:`文件[${soi.data.name}]正在下载,请不要重复下载`};
        }

        let fileStream = streamSaver.createWriteStream(soi.data.name,{size:soi.data.total});
        soi['fileStream'] = fileStream;
        downloadList[soi.data.fileMd5] = soi;
        queueSyncDownload().then(function (e){});
        return {state:true,msg:`文件[${soi.data.name}]加入下载列表成功.`};
    }else{
        return soi;
    }
}



/**
 * 停止文件的下载
 * @param fileMd5
 * @constructor
 */
export async function StopFileDownload(fileMd5){
    let map = downloadList[fileMd5];
    if(map){
        await map.fileStream.getWriter().close();
        delete downloadList[fileMd5];
        return {state:true,msg:`文件[${map.data.name}]已取消下载`};
    }else{
        return {state:false,msg:"文件不存在"};
    }
}

/**
 * 下载前的试探,试探文件有多大
 * @param fileMd5 文件md5值
 * @param name 文件名称
 * @param source 文件来源
 */
export async function FileSoundOut(fileMd5,name,source){
    let response = await axios.post('/file/slice/getFile', {
        fileMd5: fileMd5,
        name: name,
        source:source
    },{
        responseType:"arraybuffer",
        headers:{
            Range:"bytes=0-1"
        }
    });
    const { data, headers } = response;
    try {
        //有错误
        let msg = JSON.parse(data);
        return {state:false,msg:msg.message};
    }catch (e){}
    //试探正常
    let contentRange = headers["content-range"];
    let total = contentRange.substring(contentRange.indexOf("/") + 1) * 1;
    return {state:true,
        data:{
            total:total, //文件总大小
            sliceNum:0, //文件将会被分成多少片下载
            fileMd5:fileMd5, //文件md5值
            name:name,//文件名称
            source:source,//文件来源
            progress:0,//文件已下载多少片
        }
    }
}