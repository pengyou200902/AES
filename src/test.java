import java.util.Base64;

public class test {
    //    public static Base64.Decoder decoder = Base64.getDecoder();
    public static Base64.Encoder encoder = Base64.getEncoder();

    public static void test() {
        byte[] plain = {(byte) 0x32, (byte) 0x43, (byte) 0xf6, (byte) 0xa8,
                (byte) 0x88, (byte) 0x5a, (byte) 0x30,
                (byte) 0x8d, (byte) 0x31, (byte) 0x31,
                (byte) 0x98, (byte) 0xa2, (byte) 0xe0,
                (byte) 0x37, (byte) 0x07, (byte) 0x34
        };
        System.out.println("输入（未补a）：" + encoder.encodeToString(plain));

//        测试extendKey--------------------
        byte[][] initKey = {    //已经按列排
                {(byte) 0x2b, (byte) 0x28, (byte) 0xab, (byte) 0x09},
                {(byte) 0x7e, (byte) 0xae, (byte) 0xf7, (byte) 0xcf},
                {(byte) 0x15, (byte) 0xd2, (byte) 0x15, (byte) 0x4f},
                {(byte) 0x16, (byte) 0xa6, (byte) 0x88, (byte) 0x3c}
        };
        byte[][] key = new byte[4][44];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                key[i][j] = initKey[i][j];
            }
        }
//        AESCipher aesCipher = new AESCipher(plain, key);
//        byte[][] state = AESCipher.nextGroupBytes(plain, 4, 4, 0);
        byte[] cipherBytes = AESCipher.encrypt(plain, key, 16, false);
        byte[] decipherBytes = AESCipher.decrypt(cipherBytes, key, 16);
        System.out.println("原文：\t\t" + new String(plain));
        System.out.println("原文Base64：\t" + encoder.encodeToString(plain));
        System.out.println("密文：\t\t" + new String(cipherBytes));
        System.out.println("密文Base64：\t" + encoder.encodeToString(cipherBytes));
        System.out.println("解密：\t\t" + new String(decipherBytes));
        System.out.println("解密Base64：\t" + encoder.encodeToString(decipherBytes));

    }

    public static void main(String[] args) {
        test();
//        System.out.println("\0".getBytes()[0]);
//        System.out.println("\0".getBytes().length);
    }

}
