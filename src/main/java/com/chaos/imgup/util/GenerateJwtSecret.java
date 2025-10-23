package com.chaos.imgup.util;

import java.security.SecureRandom;
import java.util.Base64;

public class GenerateJwtSecret {
    public static void main(String[] args) {
        // 生成32字节的随机密钥（256位）
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        // 编码为Base64字符串
        String base64Secret = Base64.getEncoder().encodeToString(key);
        System.out.println("Base64编码的密钥：" + base64Secret);
    }
}