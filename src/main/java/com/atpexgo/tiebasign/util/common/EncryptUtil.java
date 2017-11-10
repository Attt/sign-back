package com.atpexgo.tiebasign.util.common;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class EncryptUtil {

    private static final String KEY = "ATPEXGO!!";

    /**
     * AES加密->BASE64加密
     *
     * @param content 需要加密的内容
     * @return
     */
    public static String encrypt(String content) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(KEY.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("ISO-8859-1");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            String encrypted =  new String(cipher.doFinal(byteContent),"ISO-8859-1");// 加密
            return new String(Base64.getEncoder().encode(encrypted.getBytes("ISO-8859-1")),"ISO-8859-1");
        } catch (Exception e) {
            log.error("加密出错!", e);
        }
        return null;
    }


    /**
     * BASE64解密->AES解密
     *
     * @param content 待解密内容
     * @return
     */
    public static String decrypt(String content) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(content.getBytes("ISO-8859-1"));
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(KEY.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(encrypted);
            return new String(result,"ISO-8859-1"); // 加密
        } catch (Exception e) {
            log.error("解密出错！", e);
        }
        return null;
    }


    /**
     * 字母表右移（熵减）
     * @param str
     * @return
     */
    public static String rightShift(String str) {
        char[] S = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            char temp = S[i];
            if(temp == 90)
                temp = 64;
            S[i] = (char) (temp + 1);
        }
        return String.copyValueOf(S).toUpperCase();
    }

    /**
     * 字母表左移（熵减）
     * @param str
     * @return
     */
    public static String leftShift(String str){
        char[] S = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {
            char temp = S[i];
            if(temp == 65)
                temp = 91;
            S[i] = (char) (temp - 1);
        }
        return String.copyValueOf(S).toUpperCase();
    }

}
