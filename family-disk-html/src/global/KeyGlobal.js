import SparkMD5 from "spark-md5";

let data = {
    shareToken:'SHARE_TOKEN',
    authorization:'Authorization',
    userName:'USER_NAME',
    userId:'USER_ID',
    useSpaceRole:'USE_SPACE_ROLE',
    userAllSpaceRole:'USER_ALL_SPACE_ROLE',
    // 公网
    baseURL:'//api.jflove.cn/',
    // 局域网
    lanURL:'//192.168.3.20/'
};

export function getUrlParam(key){
    return decodeURIComponent((new RegExp('[?|&]'+key+'=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null;
}

export function keyPut(k,v){
    data[k] = v;
}

export function key(){
    return data;
}

/**
 * 是否存在token
 * @returns {boolean}
 */
export function isToken(){
    let token = localStorage.getItem(key().authorization);
    if(token != null){//如果本地保存了token,则在头部传送token
        return true;
    }
    return false;
}

export function formatDate(date){
    return `${date.getFullYear()}-${(date.getMonth() + 1) < 10 ? '0' + (date.getMonth() + 1) : (date.getMonth() + 1)}-${date.getDate() < 10 ? '0' + date.getDate() : date.getDate()} 23:59:59`;
}

/**
 * 计算文件md5值
 * 分片数量为1,直接计算完整md5值,
 * 如果分片大于1,则将第一个分片和最后一个分片放入计算md5值,这不是真实的,不计算完整的是防止浏览器内存溢出
 * @param file
 * @param sliceSize
 * @param sliceNum
 * @returns {Promise<unknown>}
 */
export function fileMd5(file,sliceSize,sliceNum){
    return new Promise(function (resolve,reject){
        let spark = new SparkMD5.ArrayBuffer();
        if(sliceNum > 1){
            let reader = new FileReader();
            reader.readAsArrayBuffer(file.slice(0,sliceSize));
            reader.onload = (event) => {
                spark.append(event.target.result);
            };
            reader.onloadend = () =>{
                let reader2 = new FileReader();
                reader2.readAsArrayBuffer(file.slice(file.size - sliceSize,file.size));
                reader2.onload = (event) => {
                    spark.append(event.target.result);
                };
                reader2.onloadend = () =>{
                    resolve(spark.end());
                }
            };
        }else{
            let reader = new FileReader();
            reader.readAsArrayBuffer(file.slice(0,file.size));
            reader.onload = (event) => {
                spark.append(event.target.result);
            };
            reader.onloadend = () =>{
                resolve(spark.end());
            };
        }
    })
}