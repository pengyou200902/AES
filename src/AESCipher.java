import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AESCipher {
    public static byte[] textBytes;  // 明文字节数组
    public static byte[] cipherBytes; // 明文补0后加密产生的密文字
    public static byte[] decipherBytes; // 密文解密后
    public static byte[][] key;   //密钥
    public static int length = 0;   //明文即字节数，未补0
    public static int keyLength = 0;    //密钥的bit位数
    public static int cipherLength = 0;    //明文补0后加密产生的密文字节数，也是补0后明文的字节数
    public static int keyCol = 44;  //  本例实现的是128位密钥的，故经过轮密钥加扩展后一共44列
    public static int cur = 0;  //取明文的字节分组时所用的下标
    public static int col = 4;  //每组的列数不是final，因为密钥长度不同，列也会不同，但其实本例只实现了128位密钥的，故col其实是4
    public static boolean extendKey = false;
    public static byte a;

    static {
        try {
            a = "a".getBytes(System.getProperty("file.encoding"))[0];
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public final static int row = 4;    //但是行数是确定4行

    private static Scanner sc = new Scanner(System.in);

    public AESCipher(String plainText, int keyLen) {
        cur = 0;
        keyLength = keyLen;
        int groupLength = keyLength / 8;
        col = groupLength / row;
//        System.out.printf(" col = %d\n", col);
        extendKey = false;
        initKey(keyLen);
        System.out.printf("明文字符数: %d\n", plainText.length());
        textBytes = plainText.getBytes();
//        textBytes = Base64.getEncoder().encode(plainText.getBytes());
        length = textBytes.length;
        if (length % groupLength != 0) cipherLength = (length / groupLength + 1) * groupLength;
        else cipherLength = length / groupLength * groupLength;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), length);
//        System.out.printf("默认是%s编码, 对应base64字节数: %d\n", System.getProperty("file.encoding"), length);
        System.out.printf("补a后长度: %d字节\n", cipherLength);

    }

    public AESCipher(byte[] plain, byte[][] key) {
        extendKey = true;
        cur = 0;
        keyLength = 128;
        int groupLength = keyLength / 8;
        col = groupLength / row;
        textBytes = plain;
        length = textBytes.length;
        if (length % groupLength != 0) cipherLength = (length / groupLength + 1) * groupLength;
        else cipherLength = length / groupLength * groupLength;
        System.out.printf("明文字节数: %d\n", length);
        System.out.printf("补'a'后长度: %d字节\n", cipherLength);
    }

    public static String plaintextInput() {
        System.out.println("请输入待加密的明文：");
        String plainText;
        boolean legal = false;
        do {
            System.out.print(">>> ");
            plainText = sc.nextLine();
            if (plainText == null || plainText.length() <= 0) {
                legal = false;
                System.out.println("明文输入有误！请检查并重新输入！");
            } else legal = true;
        } while (!legal);
        return plainText;
    }

    public void resetAll() {
        cur = 0;
        System.out.println("\n请输入keyLength: 128");
        keyLength = 128;    // 本例仅实现128位密钥
        int groupLength = keyLength / 8;
        col = groupLength / row;
        extendKey = false;
        initKey(keyLength);
        String plainText = plaintextInput();
        System.out.printf("明文字符数: %d\n", plainText.length());
        textBytes = plainText.getBytes();
        length = textBytes.length;
        if (length % groupLength != 0) cipherLength = (length / groupLength + 1) * groupLength;
        else cipherLength = length / groupLength * groupLength;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), length);
        System.out.printf("补0后长度: %d\n", cipherLength);
        cipherBytes = null;
    }

    public static void out(byte[][] bytes, int x, int y) {
        x = (x <= bytes.length && x > -1) ? x : bytes.length;
        y = (y <= bytes[0].length && y > -1) ? y : bytes[0].length;

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                System.out.printf("%02x  ", bytes[i][j]);
            }
            System.out.println();
        }
    }

    public static int byteToInt(byte a) {
        int x = (a >= 0 ? a : a + 256);
        return x;
    }

    public static StringBuffer byteToBit(byte b) {
        return new StringBuffer()
                .append((byte) ((b >> 7) & 0b1))
                .append((byte) ((b >> 6) & 0b1))
                .append((byte) ((b >> 5) & 0b1))
                .append((byte) ((b >> 4) & 0b1))
                .append((byte) ((b >> 3) & 0b1))
                .append((byte) ((b >> 2) & 0b1))
                .append((byte) ((b >> 1) & 0b1))
                .append((byte) (b & 0b1));
    }


    public static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String strHex = Integer.toHexString(bytes[i]);
            if (strHex.length() > 3) {
                sb.append(strHex.substring(6));
            } else {
                if (strHex.length() < 2) {
                    sb.append("0" + strHex);
                } else {
                    sb.append(strHex);
                }
            }
        }
        return sb.toString();
    }

    public static void initKey(int keyLength) { //初始化首次密钥
        key = new byte[row][keyCol];    //初始密钥4*4，扩展新增40列，得44列
        byte[] initKey = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keyLength);
            SecretKey generateKey = keyGenerator.generateKey();
            initKey = generateKey.getEncoded();
            String s = bytesToHexString(initKey);
//            测试用输出
            System.out.println(initKey.length);
            System.out.printf("十六进制密钥长度: %d, 二进制密钥长度则为: %d, 产生密钥是: %s\n", s.length(), s.length() * 4, s);
            System.out.println("二进制     十六进制");
            for (int i = 0; i < initKey.length; i++) {
                System.out.printf("%s     %2x\n", byteToBit(initKey[i]), byteToInt(initKey[i]));
            }System.out.println();
            int count = 0;
            for (int j = 0; j < 4; j++) {   //将初始密钥写入key数组
                for (int i = 0; i < row; i++) {
                    key[i][j] = initKey[count++];
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void leftLoopMove(byte[] bytes, int round) { // 数组为引用传递，故使用void
        if (round > 0) {
            int c = bytes.length;
            byte[] b = bytes.clone();
            for (int j = round, count = 0; count < c; count++, j = (j + 1) % c) {
                bytes[count] = b[j]; //左循环移位
            }
        } else if (round == 0) return;
        else {
            System.out.println("Error round Num !!!");
            return;
        }
    }


    public static byte[][] nextGroupBytes(byte[] bytes, int begin) { // 获取按列分组的字节组，本实例其实规模是4*4的byte数组
        int maxLen = bytes.length;
        if (begin >= maxLen) return null;

        byte[][] subBytes = new byte[row][col];

        for (int j = 0; j < col; j++) {
            for (int i = 0; i < row; i++) {
                if (begin < maxLen) subBytes[i][j] = bytes[begin++];
                else subBytes[i][j] = a; //补a
            }
        }
//        System.out.printf("cur=%d, end next\n", cur);
        cur = begin;
        return subBytes;
    }

//    public static byte[][] nextGroupBytes(byte[] in) { // 获取按列分组的字节组，本实例其实规模是4*4的byte数组
//        byte[][] subBytes = new byte[row][col];
//
//        for (int j = 0; j < col; j++) {
//            for (int i = 0; i < row; i++) {
//                if (cur < maxLen) subBytes[i][j] = bytes[cur++];
//                else subBytes[i][j] = a; //补a
//            }
//        }
//        return in;
//    }

    public static byte substituteByte(byte b, boolean reverse) { //字节替换
        int low_4 = b & 0b1111;
        int high_4 = (b >> 4) & 0b1111;
        final int[][] sbox;

        if (reverse) {  //逆操作
            sbox = AESParam._sbox;
        } else sbox = AESParam.sbox;

        b = (byte) (sbox[high_4][low_4]);
        return b;
    }

    public static void subBytes(byte[][] bytes, boolean reverse) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                bytes[i][j] = substituteByte(bytes[i][j], reverse);
            }
        }
//        System.out.println("字节代换后：");
//        out(bytes, -1, -1);
    }

    public void shiftRows(byte[][] bytes, boolean reverse) { //行移位，数组为引用传递，故使用void
        if (bytes == null) return;
        else if (bytes.length == 0) return;

        else if (bytes.length > 1) {
            int r = bytes.length;
            if (!reverse) {
                for (int i = 1; i < r; i++) {
                    leftLoopMove(bytes[i], i);
                }
            } else {
                for (int i = 1; i < r; i++) {
                    leftLoopMove(bytes[i], r - i);
                }
            }
//            System.out.println("行移位后：");
//            out(bytes, row, col);
        } else {
            System.out.println("Unknown Error!");
        }
    }

    // GF(2^8)乘法
    public static int GF28multiple(int a, int b) {
        int aa = a, bb = b;
        int result = 0;
        if (a == 0 || b == 0) result = 0;
        else if (a == 1) result = b;
        else if (b == 1) result = a;
        else {
//            int count = 0;
//            int bit = 0;
//        a &= 0b11111111;
//        b &= 0b11111111;
//        result ^= (a * bit);
//        b ^= bit;
//        System.out.println("gf28 while");
            for (int i = 0; i < 8; ++i) {
                if ((a & 1) == 1) {
                    result ^= b;
                }
                int flag = (b & 0b100000000);
                b <<= 1;
                if (flag == 1) {
                    b ^= 0x1B; /* x^8 + x^4 + x^3 + x + 1 */
                }
                a >>= 1;
            }

//        while (b != 0) {
//            System.out.printf("b=%d\n", b);


//            --------------------------
//            result <<= 1;
//            if ((result & 0b100000000) == 1) {
//                result ^= 0b100011011;
//            }
//            result ^= ( (a * (b & 1)) << count);
//            b >>= 1;
//            count++;

//            --------------------------
//            bit = b & 0b1;
//            if(bit == 0) {
//                result <<= 1;
//                if (((result >> 8) & 0b1) == 1) {
//                    result ^= 0b100011011;
//                }
//                b >>= 1;
//                count++;
//            }
//            else {  // 就是bit == 1
//                result ^= (a << count);
//                b ^= bit;
//            }
//            result &= 0b11111111;
//        }
        }
        result &= 0b11111111;
        System.out.printf("%02x X %02x = %02x\n", aa, bb, result);
        return result;
    }


    public static byte[][] mixColumns(byte[][] bytes, boolean reverse) { //列混淆,参数是待混淆的
        int set_col = bytes[0].length;
        byte[][] mixedBytes;
        final int[][] mixCol;

        if (set_col > 0) {  //判断一下col有效与否
            mixedBytes = new byte[row][set_col];
        } else {
            System.out.println("Error col number !!!");
            return null;
        }
        if (reverse) {  //逆操作
            mixCol = AESParam._mixCol;
        } else mixCol = AESParam.mixCol;
        //矩阵GF(2^8)乘法
//        StringBuffer sb ;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < set_col; j++) {
//                mixedBytes[i][j] = (byte) (GF28multiple(mixCol[i][0], byteToInt(bytes[0][i]))
//                                        ^ GF28multiple(mixCol[i][1], byteToInt(bytes[1][i]))
//                                        ^ GF28multiple(mixCol[i][2], byteToInt(bytes[2][i]))
//                                        ^ GF28multiple(mixCol[i][3], byteToInt(bytes[3][i])));
//                sb = new StringBuffer();
//                mixedBytes[i][j] = (byte) (GF28multiple(mixCol[i][0],byteToInt( bytes[0][j]))
//                                        ^ GF28multiple(mixCol[i][1], byteToInt(bytes[1][j]))
//                                        ^ GF28multiple(mixCol[i][2], byteToInt(bytes[2][j]))
//                                        ^ GF28multiple(mixCol[i][3], byteToInt(bytes[3][j])));
                for (int k = 0; k < mixCol[0].length; k++) {
//                    mixedBytes[i][j] = (byte) (mixedBytes[i][j] ^ GF28multiple(mixCol[i][k], byteToInt(bytes[k][j]))); // 参与运算应该这样转int ????
//                    mixedBytes[i][j] ^= GF28multiple(mixCol[i][k], bytes[k][j]);
                    mixedBytes[i][j] ^= GFMul(mixCol[i][k], bytes[k][j]); // 这就对了。。。
//                    int a = GF28multiple(mixCol[i][k], byteToInt(bytes[k][j]));
//                    sb.append(a).append('^');
//                    mixedBytes[i][j] ^= a; // 参与运算应该这样转int ????
                }
//                System.out.println(sb.append('=').append(mixedBytes[i][j]));
            }
        }
//        System.out.println("列混淆后：");
//        out(mixedBytes, -1, -1);
        return mixedBytes;
    }

    public static void extendKey(byte[][] key) {
        int round = 0;
        byte[] w_j_1 = new byte[row];
        byte[] w_j_4 = new byte[row];
        byte[] w_j = new byte[row];
        for (int j = 4; j < keyCol; j++) {
            if (j % 4 != 0) {
                for (int i = 0; i < row; i++) {
                    w_j_4[i] = key[i][j - 4];
                    w_j_1[i] = key[i][j - 1];
                    w_j[i] = (byte) (w_j_4[i] ^ w_j_1[i]);
                    key[i][j] = w_j[i]; //填入总密钥
                }
            } else { // j是4的倍数
                // T 函数内容
                round = j / 4;
                for (int i = 1, count = 0; count < row; i = (i + 1) % row, count++) {
                    w_j_4[count] = key[count][j - 4];    // 这样一来此处不变
                    // 而下方 w_j_1 即代表 w[j-1] 就是左循环移位1次并且字节代换后，而且与轮常量异或后的了
                    w_j_1[count] = substituteByte(key[i][j - 1], false);
                    w_j_1[count] = (byte) (w_j_1[count] ^ AESParam.Rcon[round][count]);
                    w_j[count] = (byte) (w_j_4[count] ^ w_j_1[count]);
                    key[count][j] = w_j[count]; //填入总密钥
                }
                // T函数内容结束
            }
        } // end for / 结束轮密钥加
        extendKey = true;
        //输出全密钥
        System.out.println("------------extendKey-------------");
        out(key, -1, -1);
    }

    public static void addRoundKey(byte[][] bytes, byte[][] key, int round) {
//        System.out.printf("w[%d] ---- w[%d]\n", round * 4, round * 4 + 4 - 1);
        for (int j = 0, k = round * 4; k < round * 4 + 4; j++, k++) {
            for (int i = 0; i < row; i++) {
                bytes[i][j] = (byte) (bytes[i][j] ^ key[i][k]);
            }
        }
        //输出结果
//        System.out.printf("第 %d 轮密钥加结果：\n", round);
//        out(bytes, -1, -1);
    }

    public void encrypt() {
        if (cipherBytes != null) {
            System.out.println("要再次加密请先调用resetAll()！");
            return;
        }
        byte[][] state;
        int count = 0;
        int round = 0;

        cipherBytes = new byte[cipherLength];
        // 先来了10轮的密钥扩展
        if (!extendKey) extendKey(key);
        while (true) { // encryption
            round = 0;
            state = nextGroupBytes(textBytes, cur);
            if (state == null) break;

            addRoundKey(state, key, round);
            for (round = 1; round <= 10; round++) { // round为轮数，前9轮有mixColumns
//                System.out.printf("第 %d 轮\n", round);
                subBytes(state, false); // end substituteByte
//                System.out.println("end substituteByte");
                shiftRows(state, false);   // end shiftRows
//                System.out.println("end shiftRows");
                if (round < 10) {
                    state = mixColumns(state, false); // end mixColumns
//                    System.out.println("end mixColumns");
                }
                addRoundKey(state, key, round); // end addRoundKey
//                System.out.println("end addRoundKey");
            } // end 10 rounds
            for (int j = 0; j < col; j++) { // store the ciphered bytes
                for (int i = 0; i < row; i++) {
                    cipherBytes[count++] = state[i][j];
                }
            } // end store
//            System.out.printf("count=%d, end store\n", count);
        } // end encryption
//        System.out.println("end encryption");
    }

    public void decrypt() {
        int round = 0;
        int count = 0;
        byte[][] state;
        cur = 0;
        decipherBytes = new byte[length];
        while (count < length) { // decryption
            round = 0;
            state = nextGroupBytes(cipherBytes, cur);
            if (state == null) break;
            addRoundKey(state, key, round);
//            shiftRows(state, true); //书上
//            subBytes(state, true);  //书上
//            for (round = 9; round > 0; round--) {  //书上
//                addRoundKey(state, key, round);  //书上
//                mixColumns(state, true);    //书上
//                shiftRows(state, true); //书上
//                subBytes(state, true);  //书上
//            }   //书上
//            addRoundKey(state, key, 10); //书上

            for (round = 10; round > 0; round--) {
//            for (round = 1; round <= 10; round++) {
                shiftRows(state, true);
                subBytes(state, true);  // end De-substituteByte
//                System.out.println("end de-substituteByte");
                addRoundKey(state, key, round);  // end De-addRoundKey
//                System.out.println("end De-addRoundKey");
                if (round > 1) {
//                if (round < 10) {
                    mixColumns(state, true); // end De-mixColumns
//                    System.out.println("end De-mixColumns");
                }
            }
//            subBytes(state, true);  // end De-substituteByte
////                System.out.println("end de-substituteByte");
//            shiftRows(state, true);
//            addRoundKey(state, key, round);  // end De-addRoundKey
            for (int j = 0; j < col && count < length; j++) { // store the deciphered bytes
                for (int i = 0; i < row && count < length; i++) {
                    decipherBytes[count++] = state[i][j];
                }
            } // end store
        }
    }

    public static int GFMul2(int s) {
        int result = s << 1;
        int a7 = result & 0x00000100;

        if (a7 != 0) {
            result = result & 0x000000ff;
            result = result ^ 0x1b;
        }

        return result;
    }

    public static int GFMul3(int s) {
        return GFMul2(s) ^ s;
    }

    public static int GFMul4(int s) {
        return GFMul2(GFMul2(s));
    }

    public static int GFMul8(int s) {
        return GFMul2(GFMul4(s));
    }

    public static int GFMul9(int s) {
        return GFMul8(s) ^ s;
    }

    public static int GFMul11(int s) {
        return GFMul9(s) ^ GFMul2(s);
    }

    public static int GFMul12(int s) {
        return GFMul8(s) ^ GFMul4(s);
    }

    public static int GFMul13(int s) {
        return GFMul12(s) ^ s;
    }

    public static int GFMul14(int s) {
        return GFMul12(s) ^ GFMul2(s);
    }

    /**
     * GF上的二元运算
     */
    public static int GFMul(int n, int s) {
        int result = 0;

        if (n == 1)
            result = s;
        else if (n == 2)
            result = GFMul2(s);
        else if (n == 3)
            result = GFMul3(s);
        else if (n == 0x9)
            result = GFMul9(s);
        else if (n == 0xb)//11
            result = GFMul11(s);
        else if (n == 0xd)//13
            result = GFMul13(s);
        else if (n == 0xe)//14
            result = GFMul14(s);

        return result;
    }
    
}
