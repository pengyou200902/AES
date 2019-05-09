import java.io.UnsupportedEncodingException;
import java.util.Base64;

//wwwasdhkdakfdvbs,mvdkvjlkdsvsklwje ?abcdefghijkl //48
// abcdefg。
public class testCipher {
    public static Base64.Decoder decoder = Base64.getDecoder();
    public static Base64.Encoder encoder = Base64.getEncoder();

    public static AESCipher menu() {
        String plainText = AESCipher.plaintextInput();
        return new AESCipher(plainText, 128); // 本实例针对128位
    }

    public static void test() throws UnsupportedEncodingException {
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
        AESCipher.extendKey(key);
//        测试extendKey--------------------
        AESCipher aesCipher = new AESCipher(plain, key);
        byte[][] state = aesCipher.nextGroupBytes(plain, 0);
        aesCipher.addRoundKey(state, key, 0);
        aesCipher.subBytes(state, false);
        aesCipher.shiftRows(state, false);
        aesCipher.mixColumns(state, false);

    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        //test
//        test();
        //test
//        System.out.println(AESCipher.GF28multiple(2,212));
//        System.out.println(AESCipher.GF28multiple(2,-44));

        AESCipher aesCipher = menu();
//        System.out.println(new String(aesCipher.textBytes));
        System.out.println(encoder.encodeToString(aesCipher.textBytes));
//        System.out.println(encoder.encodeToString(aesCipher.textBytes).length());
//
        System.out.println("encrypt");
        aesCipher.encrypt();
//        String cipherText = new String(aesCipher.cipherBytes, "UTF-8");
        String cipherText = encoder.encodeToString(aesCipher.cipherBytes);
//        String cipherText = new String(aesCipher.cipherBytes);
        System.out.println("加密后：");
        System.out.println(cipherText);
        System.out.println(cipherText.length());
//        System.out.printf("加密后为: %s\nlength=%d\n", cipherText, cipherText.length());
        System.out.println(new String(decoder.decode(cipherText)));
//
        System.out.println("dddecrypt");
        aesCipher.decrypt();

//        String plainText = new String(aesCipher.decipherBytes, "UTF-8");
        String plainText = encoder.encodeToString(aesCipher.decipherBytes);
//        String plainText = new String(aesCipher.decipherBytes);
        System.out.println("解密后：");
        System.out.println(plainText);
        System.out.println(plainText.length());
//        System.out.printf("解密后为: %s\nlength=%d", plainText, plainText.length());
        System.out.println(new String(decoder.decode(plainText)));


//        测试mixColumns
//        byte[][] a = {
//                {(byte) 0xd4},
//                {(byte) 0xbf},
//                {(byte) 0x5d},
//                {(byte) 0x30}
//        };
//        int[][] a = {
//                {0xd4},
//                {0xbf},
//                {0x5d},
//                {0x30}
//        };
//        a = AESCipher.mixColumns(a, false);
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 1; j++) {
////                System.out.printf("%02x  ", AESCipher.byteToInt(a[i][j]));
//                System.out.printf("%02x  ", a[i][j]);
//            }
//            System.out.println();
//        }
//        System.out.printf("%2x\n", AESCipher.GF28multiple(1,0xd4) ^AESCipher.GF28multiple(1,0xbf)
//                                ^ AESCipher.GF28multiple(2,0x5d) ^ AESCipher.GF28multiple(3,0x30));
//        System.out.printf("%2x\n", AESCipher.GF28multiple(3,0xd4) ^AESCipher.GF28multiple(1,0xbf)
//                                ^ AESCipher.GF28multiple(1,0x5d) ^ AESCipher.GF28multiple(2,0x30));
//        System.out.printf("%02x  ", AESCipher.GF28multiple(1,0xd4));
//        System.out.printf("%02x  ", AESCipher.GF28multiple(1,0xbf));
//        System.out.printf("%02x  ", AESCipher.GF28multiple(2,0x5d));
//        System.out.printf("%02x  ", AESCipher.GF28multiple(3,0x30));

//        byte[] a = {1,2,3,4};
//        byte[] b = {5,6,7,8};
//        System.out.printf("%d\n",(((1^5)<<24) + ((2^6)<<16) + ((3^7)<<8) + 4^8));
//        System.out.println((1^5)<<24);
//        System.out.println((2^6)<<16);
//        System.out.println((3^7)<<8);
//        System.out.println(4^8);
//        System.out.printf("%d\n",((1<<24)+(2<<16)+(3<<8)+4)^((5<<24)+(6<<16)+(7<<8)+8));
//        byte a = (byte) 129;
//        byte b = (byte) 34;
//        byte c = (byte) (a^b);
//        System.out.println(a);
//        System.out.println(AESCipher.byteToInt(a));
//        System.out.println(a^b);
//        System.out.println((byte)(a^b));
//        System.out.println(c);
//        System.out.println(AESCipher.byteToInt(a)^AESCipher.byteToInt(b));
//        System.out.println(AESCipher.byteToInt((byte)(-14^15)));


//        int count = 0;
//        while(true) {
//            byte[][] a = aesCipher.nextGroupBytes();
//            System.out.printf("count =%d\n", count);
//            if (a == null) break;
//            for (int j = 0; j < 4; j++) { // store the ciphered bytes
//                for (int i = 0; i < 4; i++) {
//                    pT[count++] = a[i][j];
//                }
//            }
//        }
//        System.out.println(new String(pT));
//        System.out.println(AESCipher.bytesToHexString(pT).length());
//        System.out.println(AESCipher.bytesToHexString(pT));


//        测试用代码
//        byte[][] a = new byte[4][4];
//        System.out.println(a[0][3]);
//        AESCipher aesCipher = new AESCipher("我是你爸爸Tom。", 128);
//        for (int j = 0; j < 4; j++) {//初始密钥按列输出
//            for (int i = 0; i < row; i++) {
//                System.out.printf("%4x ", byteToInt(key[i][j]));
//            }
//            System.out.println();
//        }
//        System.out.println(0b1111 ^ 0b100011);// = 101100 = System.out.println(0b101100);
//        System.out.println(GF28multiple(2,0xc9) ^ GF28multiple(3,0x7a) ^ GF28multiple(1,0x63) ^ GF28multiple(1,0xb0));
//        System.out.println(0xd4);
//        System.out.println(GF28multiple(1,0xc9) ^ GF28multiple(2,0x7a) ^ GF28multiple(3,0x63) ^ GF28multiple(1,0xb0));
//        System.out.println(0x28);
//        System.out.println(GF28multiple(1,0xc9) ^ GF28multiple(1,0x7a) ^ GF28multiple(2,0x63) ^ GF28multiple(3,0xb0));
//        System.out.println(0xbe);
//        System.out.println(GF28multiple(3,0xc9) ^ GF28multiple(1,0x7a) ^ GF28multiple(1,0x63) ^ GF28multiple(2,0xb0));
//        System.out.println(0x22);
//        System.out.println(GF28multiple(2,0xc9) ^ GF28multiple(3,0x6e) ^ GF28multiple(1,0x46) ^ GF28multiple(1,0xa6));
//        System.out.println(0xdb);
//        byte[] t= {};
//        System.out.println(t.length);

//        final Base64.Decoder decoder = Base64.getDecoder();
//        final Base64.Encoder encoder = Base64.getEncoder();
//        final String text = "字a我 ";
//        final byte[] textByte = text.getBytes("UTF-8");
//        byte[] bytes = new byte[16];
//        for (int i = 0; i < 16; i++) {
//            if (i < textByte.length)    bytes[i] = textByte[i];
//            else                        bytes[i] = "a".getBytes()[0];
//        }
////编码
//
//        String encodedText = new String(encoder.encode(textByte));
//        System.out.println(encodedText);
//        System.out.println(encodedText.length());
//        System.out.println(textByte.length);
//        System.out.println(encodedText.getBytes().length);
//        System.out.println(encoder.encodeToString(textByte));
//
////解码
//        System.out.println(new String(decoder.decode(encodedText), "UTF-8"));
//        System.out.println(encoder.encodeToString(bytes));
//        System.out.println(new String(decoder.decode(encoder.encodeToString(bytes)), "UTF-8"));
    }
}
