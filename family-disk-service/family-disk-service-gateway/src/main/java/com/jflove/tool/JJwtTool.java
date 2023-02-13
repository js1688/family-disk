package com.jflove.tool;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * @author tanjun
 * @date 2022/12/8 11:28
 * @describe jjwt token 工具类
 */
@Component
@Getter
public class JJwtTool {

    @Value("${spring.security.jjwt.issuer}")
    private String issuer;//token 发布者
    @Value("${spring.security.jjwt.ttlMinute}")
    private Integer ttlMinute; //有效时长,分钟

    private String secretKey; //token 签名key

    @PostConstruct
    public void initSecretKey(){
        secretKey = createSecretKey(issuer);
    }

    /**
     * 生成token
     * @param id   用户id
     * @param name 用户名称
     * @return
     */
    public String createJwt(String id, String name){
        return createJwt(id,name,null,null);
    }

    /**
     * 生成token
     * @param id   用户id
     * @param name 用户名称
     * @param map  token负载信息
     * @param expire 到期时间戳 到毫秒
     * @return token
     */
    public String createJwt(String id, String name, Map<String, Object> map,Long expire) {
        // 创建 JwtBuilder 设置基本的信息
        JwtBuilder jwtBuilder = Jwts.builder().setId(id).setSubject(name) //token使用方id和名称
                .setIssuedAt(new Date()) //token发布时间
                .setIssuer(issuer) //token 发布者
                .signWith(SignatureAlgorithm.HS256, secretKey);//接口调用方不需要解密token,故选择 hs256 对称加密方式
        // 设置token 负载信息
        if(map != null){
            map.forEach(jwtBuilder::claim);
        }
        // 设置失效时间
        long exp = 0;
        if(expire != null){
            exp = expire.longValue();
        }else {
            exp = DateUtil.offset(new Date(), DateField.MINUTE, ttlMinute).getTime();
        }
        jwtBuilder.setExpiration(new Date(exp));
        // 生成token
        return jwtBuilder.compact();
    }

    /**
     * 解析token 获取Jws<Claims> ,如token验证不通过,则会抛出对应的异常
     * @param token jwt生成的token
     * @return Jws<Claims>
     */
    public Jws<Claims> parseJwt(String token) throws SecurityException{
        try{
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        }catch (Exception e){
            String errMsg = e instanceof SignatureException ? "token签名验证不通过" :
                                e instanceof ExpiredJwtException ? "token已过期" :
                                        e instanceof MalformedJwtException ? "token 格式不正确" :
                                                e instanceof IllegalArgumentException ? "非法参数异常" : "token校验异常";
            throw new SecurityException(errMsg);
        }
    }

    /**
     * 将制定字符串Hs256加密成秘钥
     * @param str
     * @return
     * @throws Exception
     */
    public String createSecretKey(String str){
        byte [] encodedKey = Base64.getEncoder().encode(issuer.getBytes(Charset.defaultCharset()));
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, SignatureAlgorithm.HS256.getValue());
        return new String(key.getEncoded(), Charset.defaultCharset());
    }
}
