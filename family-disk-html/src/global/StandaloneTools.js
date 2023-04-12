/**
 * 独立的工具方法
 */

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