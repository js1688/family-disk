import SparkMD5 from "spark-md5";

let data = {
    shareToken:'SHARE_TOKEN',
    authorization:'Authorization',
    userName:'USER_NAME',
    userId:'USER_ID',
    useSpaceRole:'USE_SPACE_ROLE',
    userAllSpaceRole:'USER_ALL_SPACE_ROLE',
    // 公网
    baseURL:'https://api.jflove.cn/',
    // 局域网
    lanURL:'https://192.168.3.20/'
};

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