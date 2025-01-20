package com.demo.lesson10.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class PkceTest {

    public static void main(String[] args) throws Exception{
        // 明文
        String code_verifier = "linmoo";
        // 摘要算法
        String code_challenge_method = "SHA-256";
        // 密文
        byte[] bytes = code_verifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest md = MessageDigest.getInstance(code_challenge_method);
        byte[] digest = md.digest(bytes);
        String code_challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        System.out.println(code_challenge);
    }

}
