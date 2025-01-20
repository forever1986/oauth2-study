package com.demo.lesson06.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类
 */
public class JwtUtil {

    //有效期为
    public static final Long JWT_TTL = 60 * 60 * 1000L; // 60 * 60 *1000  一个小时
    //设置秘钥明文
    public static final String JWT_KEY = "moo";

    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 创建token
     */
    public static String createToken(String subject) {
        return getJwtBuilder(subject, null, getUUID());// 设置过期时间
    }

    /**
     * 创建token
     */
    public static String createToken(String subject, Long ttlMillis) {
        return getJwtBuilder(subject, ttlMillis, getUUID());// 设置过期时间
    }

    /**
     * 创建token
     */
    public static String createToken(String id, String subject, Long ttlMillis) {
        return getJwtBuilder(subject, ttlMillis, id);// 设置过期时间
    }

    /**
     * 解析token
     */
    public static String parseJWT(String token) throws Exception {
        SecretKey secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    private static String getJwtBuilder(String subject, Long ttlMillis, String uuid) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = generalKey();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        if(ttlMillis==null){
            ttlMillis=JwtUtil.JWT_TTL;
        }
        long expMillis = nowMillis + ttlMillis;
        Date expDate = new Date(expMillis);
        return Jwts.builder()
                .setId(uuid)            //唯一的ID
                .setSubject(subject)    // 主题  可以是JSON数据
                .setIssuer(JWT_KEY)       // 签发者
                .setIssuedAt(now)       // 签发时间
                .signWith(signatureAlgorithm, secretKey)    //使用HS256对称加密算法签名, 第二个参数为秘钥
                .setExpiration(expDate)
                .compact();
    }

    /**
     * 生成加密后的秘钥 secretKey
     */
    private static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }

}
