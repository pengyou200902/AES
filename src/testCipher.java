/**
 * @Author = Friende
 * @Date = 2019/05/12
 * @github = pengyou200902
 */
//以下用例
//wwwasdhkdakfdvbs,mvdkvjlkdsvsklwje ?abcdefghijkl //48
//wwwasdhkdakfdvbsgmvdkvjlkdsvsklwjexxabcdefghijkl //48
// abcdefg我吃了个柠檬。

import java.util.Base64;
public class testCipher {
    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();

    public static AESCipher getInstance() {
        String plainText = AESCipher.plaintextInput();
        return new AESCipher(plainText, 128); // 本实例针对128位
    }

    public static void goInstance(AESCipher aesCipher) {
//        System.out.println("encrypt");
        aesCipher.encrypt();
        String cipherText = encoder.encodeToString(aesCipher.cipherBytes);
//        System.out.println("dddecrypt");
        aesCipher.decrypt();

        System.out.println("原文Base64：\n" + encoder.encodeToString(aesCipher.textBytes));
        System.out.printf("加密后为: \n%s\tlength=%d\n", cipherText, cipherText.length());
        System.out.println("加密后Base64：" + new String(decoder.decode(cipherText)));

        String plainText = encoder.encodeToString(aesCipher.decipherBytes);
        System.out.printf("解密后为: \n%s\tlength=%d\n", plainText, plainText.length());
        System.out.println("解密出原文：" + new String(decoder.decode(plainText)));
    }

    public static void goStatic() {
        String plainText = AESCipher.plaintextInput();
        byte[] textBytes = plainText.getBytes();
        byte[][] key = AESCipher.initKey(128);
        // 加密过程中会对未扩展的key进行扩展
        byte[] cipherBytes = AESCipher.encrypt(textBytes, key, 16, false);
        byte[] decipherBytes = AESCipher.decrypt(cipherBytes, key, 16);

        System.out.println("原文Base64：\n" + encoder.encodeToString(textBytes));

        String cipherText = encoder.encodeToString(cipherBytes);
        System.out.printf("加密后为: \n%s\tlength=%d\n", cipherText, cipherText.length());
        System.out.println("加密后Base64：" + new String(decoder.decode(cipherText)));

        String decipherText = encoder.encodeToString(decipherBytes);
        System.out.printf("解密后为: \n%s\tlength=%d\n", decipherText, decipherText.length());
        System.out.println("解密出原文：" + new String(decoder.decode(decipherText)));
    }


    public static void main(String[] args) {
        AESCipher aesCipher = getInstance();
        goInstance(aesCipher);
    }
}
