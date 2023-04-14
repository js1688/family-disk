
import axios from "axios";

/**
 * 文件上传工具类,支持超大文件上传
 */

let uploadList = {};//map,文件md5值当key,value,包含信息

/**
 * 获取列表
 * @returns
 */
export function getUploadList(){
    return uploadList;
}


/**
 * 执行上传,按队列顺序上传
 * 每次取队列中的第一条上传
 * @returns {Promise<void>}
 */
let isUpload = false;
async function queueSyncUpload(){
    if(isUpload){//已激活上传,不需要重复激活
        return;
    }
    isUpload = true;//设置正在上传
    let no1 = uploadList[Object.keys(uploadList)[0]];//每次取第一条开始执行上传

    //开始上传分片,直至所有分片上传完成

    let upload = async function (i){
        if(!uploadList[no1.md5]){//上传可能已经取消了,不要上传了
            return;
        }

        let start = i * no1.sliceInfo.sliceSize;
        let end = start + no1.sliceInfo.sliceSize;
        let chunk = no1.file.slice(start,end);
        let form = new FormData();
        form.append('originalFileName', no1.file.name);
        form.append('f', chunk,no1.md5 + "-" + i);
        form.append('s', no1.source);
        form.append('m', no1.file.type);
        form.append('n', no1.sliceInfo.sliceNum);
        form.append('start', start);
        form.append('end', end);
        form.append('fileMd5', no1.md5);
        form.append('totalLength', no1.sliceInfo.totalSize);

        //todo 如果单个分片上传时发生失败,这里考虑重试,后面再说
        let response = await axios.post("/file/slice/addFile", form, {
            header:{
                'Content-Type': 'multipart/form-data'
            }
        });
        const { data, headers } = response;

        //更新进度
        no1.progress++;

        if(i < no1.sliceInfo.sliceNum - 1 && response.status == 200){//还有文件分片,继续请求
            await upload(i+1);
        }
    }

    //开始上传
    await upload(0);

    isUpload = false;

    if(uploadList[no1.md5]){
        no1.callback(no1);
        //清除上传记录
        delete uploadList[no1.md5];
    }

    //判断队列中是否还有任务,有则重新激活
    if(Object.keys(uploadList).length != 0){
        queueSyncUpload().then(function (e){});
    }
}

/**
 * 添加一个文件的上传,支持大文件
 * @param sliceInfo
 * @param file
 * @param md5
 * @param source
 * @param callback 文件上传成功后的回调
 * @returns {Promise<{msg: string, state: boolean}>}
 * @constructor
 */
export async function FileUpload(sliceInfo,file,md5,source,callback){
    if(uploadList[md5]){
        return {state:false,msg:`文件[${file.name}]正在上传,请不要重复上传`};
    }
    uploadList[md5] = {sliceInfo:sliceInfo,file:file,md5:md5,progress:0,source:source,callback:callback};
    queueSyncUpload().then(function (e){});//激活上传
    return {state:true,msg:`文件[${file.name}]加入上传列表成功.`};
}

/**
 * 取消上传
 * @param fileMd5
 * @returns {Promise<{msg: string, state: boolean}>}
 * @constructor
 */
export async function StopFileUpload(fileMd5){
    let map = uploadList[fileMd5];
    if(map){
        delete uploadList[fileMd5];
        return {state:true,msg:`文件[${map.file.name}]已取消上传`};
    }else{
        return {state:false,msg:"文件不存在"};
    }
}