/**
 * 下载文件工具类,支持超大文件下载
 */
import streamSaver from 'streamsaver'

import axios from "axios";


//todo 修改插件中的默认值改为自己本地（发布到线上记得改为线上地址）
streamSaver.mitm = 'http://localhost:5173/ss/mitm.html?version=2.0.0';

let data = {
    fileMap:{}//map,文件md5值当key,value,包含信息
};

/**
 * 开始下载一个完整的文件
 * 必须先使用文件探测方法得到探测结果,再开始下载
 * 每次下载的分片大小由服务端决定,下载文件都是同步下载分片,不使用异步多线程下载
 * @param soi 探测信息结果
 */
export async function FileDownload(soi){
    if(soi.state){
        let fileStream = streamSaver.createWriteStream(soi.data.name,{size:soi.data.total});
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
            //将二进制流写入
            await new Blob([data]).stream().pipeTo(fileStream,{preventClose:true});//设置写入后不要自动关闭文件流
            //更新下载进度
            soi.data.progress++;

            if(end < soi.data.total-1 && response.status == 206){//还有文件分片,继续请求
                await sliceDownload(start+contentLength);
            }
        }

        if(data.fileMap[soi.data.fileMd5]){
            return {state:false,msg:"文件正在下载,请不要重复下载"};
        }

        await sliceDownload(0);//开始下载
        await fileStream.getWriter().close();//文件下载完成了,主动关闭写入文件流
        return {state:true,msg:"下载完成"};
    }else{
        return soi;
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
    let contentLength = headers["content-length"] * 1;
    let total = contentRange.substring(contentRange.indexOf("/") + 1) * 1;
    let sliceNum = Math.ceil(total / contentLength);
    return {state:true,
        data:{
            total:total, //文件总大小
            sliceNum:sliceNum, //文件将会被分成多少片下载
            fileMd5:fileMd5, //文件md5值
            name:name,//文件名称
            source:source,//文件来源
            progress:0,//文件已下载多少片
        }
    }
}