package org.hcmc.hcplayground.utility;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * AES加密类
 */
public class AesAlgorithm {

    /**
    这个常量用于生成AES加密及解密的IV参数
    字符串常量，长度必须16位
    该常量一旦定义便不能再次改动
    否则因此而生成的加密数据都不能被解密
    */
    private static final String AES_IV_VALUE = "$*HCPlayground#&";
    /**
    加密时使用的混肴数据
    */
    private static final String AES_KEY_SALT = "0123456789abcdef";
    /**
    AES加密算法名称
    */
    private static final String AES_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    /**
    用于AES加密及解密的IV参数
    */
    private static final IvParameterSpec iv = new IvParameterSpec(AES_IV_VALUE.getBytes(StandardCharsets.UTF_8));

    public AesAlgorithm() {

    }

    private static SecretKeySpec getSecretKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(AES_ALGORITHM);
        KeySpec keySpec = new PBEKeySpec(key.toCharArray(), AES_KEY_SALT.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);

        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }


    public static String Encrypt(String key, String plainText) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        SecretKeySpec secretKeySpec = getSecretKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
    }

    public static String Decrypt(String key, String cipherText) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec secretKeySpec = getSecretKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
    }
}
