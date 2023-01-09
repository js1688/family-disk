export function key(){
    return {
        authorization:'Authorization',
        useSpaceId:'USE_SPACE_ID',
        userName:'USER_NAME',
        userEmail:'USER_EMAIL',
        userId:'USER_ID',
        useSpaceRole:'USE_SPACE_ROLE',
        baseURL:'http://127.0.0.1:8800/'
    };
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

/**
 * 是否存在空间id
 * @returns {boolean}
 */
export function isSpace(){
    let useSpaceId = localStorage.getItem(key().useSpaceId);
    if(useSpaceId != null){//如果本地保存了正在使用的空间id,则在头部传送使用中空间id
        return true;
    }
    return false;
}