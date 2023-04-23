/**
 * 独立的工具方法
 */


import JSZip from "jszip";

import heic2any from 'alexcorvi-heic2any';
import SparkMD5 from "spark-md5";

/**
 * heic格式图片转普通图片格式
 * @param blob
 * @returns {Promise<Blob | Blob[]>}
 */
export function HeicToCommon(blob){
    return heic2any({ blob: blob});
}

/**
 * livp格式的图片转成普通图片格式
 * @param blob 原始文件
 * @param blob
 * @returns {Promise<unknown>}
 * @constructor
 */
export function LivpToCommon(blob){
    //LIVP是苹果设备的实况图片,核心是压缩包,使用压缩包将它解压
    let jszip = new JSZip();
    let p = new Promise ((resolve,reject)=>{
        jszip.loadAsync(blob).then((zip) => { // 读取zip
            for (let key in zip.files) { // 循环判定是否有层级
                let file = zip.files[key];;
                //它里面会有2个文件,一个是图片,一个是视频,这里预览只取图片
                if (/.(JPG|JPEG|GIF|BMP|PNG)$/i.test(file.name.toUpperCase())) {
                    // uint8array,blob,arraybuffer,nodebuffer,string
                    file.async('blob').then(function (b){
                        resolve(b);
                    });
                }
            }
        });
    });
    return p;
}

/**
 * 从一个视频中获取第一帧图片的base64值
 * @param url
 * @returns {Promise<unknown>}
 * @constructor
 */
export function GetVideoCoverBase64(url){
    let video = document.createElement("video");
    video.setAttribute('crossOrigin', 'anonymous');//处理跨域
    video.setAttribute('src', url);
    video.setAttribute('width', '100%');
    video.setAttribute('height', '100%');
    video.currentTime = 0.1;//设置获取那一帧,跳转到视频的0.1秒即可
    let p = new Promise ((resolve,reject)=>{
        video.addEventListener('loadeddata', () => {
            let canvas = document.createElement("canvas");
            canvas.width = video.width;
            canvas.height = video.height;
            canvas.getContext("2d").drawImage(video, 0, 0, video.width, video.height); //绘制canvas
            let base64 = canvas.toDataURL('image/jpeg');//转换为base64
            resolve(base64);
            video.remove();//及时销毁对象
        });
        video.addEventListener('error', function(e) {
            reject(e);
            video.remove();//及时销毁对象
        })
    });
    return p;
}

/**
 * base64转blob
 * @param base64
 * @returns {Promise<module:buffer.Blob>}
 */
export async function Base64toBlob(base64){
    let arr = base64.split(",");
    let  mime = arr[0].match(/:(.*?);/)[1];
    let  str = window.atob(arr[1]);
    let  n = str.length;
    let  u8arr = new Uint8Array(n);
    while (n--) {
        u8arr[n] = str.charCodeAt(n);
    }
    return new Blob([u8arr], {type:mime});
}

/**
 * 格式化日期
 * @param date
 * @returns {string}
 */
export function FormatDate(date){
    return `${FormatDateDay(date)} 23:59:59`;
}

/**
 * 格式化日期,精确到天
 * @param dt
 * @returns {string}
 * @constructor
 */
export function FormatDateDay(dt){
    let year = dt.getFullYear();
    let month = (dt.getMonth() + 1).toString().padStart(2,'0');
    let date = dt.getDate().toString().padStart(2,'0');
    return year+'-'+month+'-'+date;
}

/**
 * 计算文件md5值
 * 分片数量为1,直接计算完整md5值,
 * 如果分片大于1,则将第一个分片和最后一个分片放入计算md5值,这不是真实的,不计算完整的是防止浏览器内存溢出
 * @param file 文件对象
 * @param sliceSize 文件分片,每片大小
 * @param sliceNum 文件分片数量
 * @returns {Promise<unknown>}
 */
export async function FileMd5(file,sliceSize,sliceNum){
    let spark = new SparkMD5.ArrayBuffer();
    if(sliceNum > 1){
        let sslice = await file.slice(0,sliceSize).arrayBuffer();
        spark.append(sslice);
        let eslice = await file.slice(file.size - sliceSize,file.size).arrayBuffer();
        spark.append(eslice);
        return spark.end();
    }else{
        let allslice = await file.slice(0,file.size).arrayBuffer();
        spark.append(allslice);
        return spark.end();
    }
}

/**
 * 从浏览器地址栏中取到指定参数的值
 * @param key
 * @returns {string|null}
 */
export function GetUrlParam(key){
    return decodeURIComponent((new RegExp('[?|&]'+key+'=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null;
}

/**
 * 计算文件对象分片信息,可分多少片
 * @param file
 * @constructor
 */
export function CountFileSliceInfo(file){
    let sliceSize = 1024 * 1024 * 3;//每片的大小
    if(file.size < sliceSize){
        sliceSize = file.size;
    }
    let sliceNum = Math.ceil(file.size/sliceSize);//分片数量
    return {sliceNum:sliceNum,sliceSize:sliceSize,totalSize:file.size};
}