/**
 * @Author = Friende
 * @Date = 2019/05/12
 * @github = pengyou200902
 */
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AESCipher {
    public byte[] textBytes;  // 明文字节数组
    public byte[] cipherBytes; // 明文补0后加密产生的加密文字
    public byte[] decipherBytes; // 密文解密后
    public byte[][] key;   //密钥
    public int length = 0;   //明文即字节数，未补0
    public int keyLength = 0;    //密钥的bit位数
    public int cipherLength = 0;    //明文补0后加密产生的密文字节数，也是补0后明文的字节数
    public int cur = 0;  //取明文的字节分组时所用的下标
    public static int col = 4;  //每组的列数不是final，因为密钥长度不同，列也会不同，但其实本例只实现了128位密钥的，故col其实是4
    public static int keyCol = 44;  //  本例实现的是128位密钥的，故经过轮密钥加扩展后一共44列
    public final static int row = 4;    //但是行数是确定4行
    public static final byte byte_0 = '\0';
    public boolean extendKey = false;
    private static Scanner sc = new Scanner(System.in);

    public AESCipher(String plainText, int keyLen) {
        initMyself(plainText, keyLen);
    }

//    public AESCipher(byte[] plain, byte[][] extendedKey) {
//        key = extendedKey;
//        extendKey = true;
//        cur = 0;
//        keyLength = 128;
//        int groupLength = keyLength / 8;
//        col = groupLength / row;
//        textBytes = plain;
//        length = textBytes.length;
//        if (length % groupLength != 0) cipherLength = (length / groupLength + 1) * groupLength;
//        else cipherLength = length / groupLength * groupLength;
//        System.out.printf("明文字节数: %d\n", length);
//        System.out.printf("补'\\0'后长度: %d字节\n", cipherLength);
//    }

    public void initMyself(String plainText, int keyLen) {
        cur = 0;
        extendKey = false;
        keyLength = keyLen;
        int groupLength = keyLength / 8;
        col = groupLength / row;
        key = initKey(keyLen);
        System.out.printf("明文字符数: %d\n", plainText.length());
        textBytes = plainText.getBytes();
        length = textBytes.length;
        if (length % groupLength != 0) cipherLength = (length / groupLength + 1) * groupLength;
        else cipherLength = length / groupLength * groupLength;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), length);
        System.out.printf("补'\\0'后长度: %d字节\n", cipherLength);
    }

    public static String plaintextInput() {
        System.out.println("\n请输入待加密的明文：（由于补'\\0'的原因，原文Base64和解密后的内容的Base64不一定相同）");
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
        int keyLen = 128;
        String plainText = plaintextInput();
        initMyself(plainText, keyLen);
    }

    public static void out(byte[][] bytes, int beginRow, int beginCol, int nRow, int nCol) {
        nRow = (nRow <= bytes.length && nRow > -1) ? nRow : bytes.length;
        nCol = (nCol <= bytes[0].length && nCol > -1) ? nCol : bytes[0].length;

        for (int i = beginRow; i < beginRow + nRow; i++) {
            for (int j = beginCol; j < beginCol + nCol; j++) {
                System.out.printf("%02x  ", bytes[i][j]);
            }
            System.out.println();
        }
    }

    public static int byteToInt(byte a) {
        return a & 0xff;
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

    public static byte[][] initKey(int keyLength) { //初始化首次密钥
        try {
            byte[][] key = new byte[row][keyCol];    //初始密钥4*4，扩展新增40列，得44列
            byte[] initKey = null;
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
            return key;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("initKey ERROR !");
        return null;
    }

    public static byte[][] initKey(byte[] firstKey) { //初始化首次密钥
        byte[][] key = new byte[row][keyCol];    //初始密钥4*4，扩展新增40列，得44列
        int count = 0;
        for (int j = 0; j < 4; j++) {   //将初始密钥写入key数组
            for (int i = 0; i < row; i++) {
                key[i][j] = firstKey[count++];
            }
        }
        return key;
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


    public static byte[][] nextGroupBytes(byte[] bytes, int row, int col, int begin) {
        // 获取按列分组的字节组，本实例其实规模是4*4的byte数组
        int maxLen = bytes.length;
        if (begin >= maxLen) return null;

        byte[][] subBytes = new byte[row][col];

        for (int j = 0; j < col; j++) {
            for (int i = 0; i < row; i++) {
                if (begin < maxLen) subBytes[i][j] = bytes[begin++];
                else subBytes[i][j] = byte_0; //补'\0'
            }
        }
//        System.out.printf("cur=%d, end next\n", cur);
//        cur = begin;
        return subBytes;
    }

    public static byte substituteByte(byte b, boolean reverse) { //字节替换
        int low_4 = b & 0b1111;
        int high_4 = (b >> 4) & 0b1111;
        final int[][] sbox;

        if (reverse) {  //逆操作
            sbox = AESParam._sbox;
        } else sbox = AESParam.sbox;

        b = (byte) (sbox[high_4][low_4] & 0xff);
        return b;
    }

    public static void subBytes(byte[][] bytes, boolean reverse) {
        int row = bytes.length;
        int col = bytes[0].length;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                bytes[i][j] = substituteByte(bytes[i][j], reverse);
            }
        }
        System.out.println("字节代换后：");
        out(bytes, 0, 0, -1, -1);
    }

    public static void shiftRows(byte[][] bytes, boolean reverse) { //行移位，数组为引用传递，故使用void
        if (bytes == null) return;
        else if (bytes.length == 0) return;

        else if (bytes.length > 1) {
            int r = bytes.length;
            System.out.println("行移位前：");
            out(bytes, 0, 0, -1, -1);
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
        System.out.println("行移位后：");
        out(bytes, 0, 0, -1, -1);
    }

    // GF(2^8)乘法
    public static int GF28multiple(int a, int b) {
        int aa = a, bb = b;
        int result = 0;
        if (a == 0 || b == 0) result = 0;
        else if (a == 1) result = b;
        else if (b == 1) result = a;
        else {
            for (int i = 0; i < 8; ++i) {
                if ((a & 1) == 1) {
                    result ^= b;
                }
                int flag = (b & 0x1ff);
                b <<= 1;
                if (flag == 1) {
                    b ^= 0x1B; /* x^8 + x^4 + x^3 + x + 1 */
                }
                a >>= 1;
            }
        }
        result &= 0xff;
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
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < set_col; j++) {
                for (int k = 0; k < mixCol[0].length; k++) {
                    mixedBytes[i][j] ^= GF28multiple(mixCol[i][k], bytes[k][j] & 0xff); // 这就对了
                }
            }
        }
        System.out.println("列混淆后：");
        out(mixedBytes, 0, 0, -1, -1);
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
//                    w_j[i] = (byte) (w_j_4[i] ^ w_j_1[i]);
                    w_j[i] = (byte) ((w_j_4[i] ^ w_j_1[i]) & 0xff);
                    key[i][j] = w_j[i]; //填入总密钥
                }
            } else { // j是4的倍数
                // T 函数内容
                round = j / 4;
                for (int i = 1, count = 0; count < row; i = (i + 1) % row, count++) {
                    w_j_4[count] = key[count][j - 4];    // 这样一来此处不变
                    // 而下方 w_j_1 即代表 w[j-1] 就是左循环移位1次并且字节代换后，而且与轮常量异或后的了
                    w_j_1[count] = substituteByte(key[i][j - 1], false);
                    w_j_1[count] = (byte) ((byteToInt(w_j_1[count]) ^ AESParam.Rcon[round][count]) & 0xff);
                    w_j[count] = (byte) ((w_j_4[count] ^ w_j_1[count]) & 0xff);
                    key[count][j] = w_j[count]; //填入总密钥
                }
                // T函数内容结束
            }
        } // end for / 结束轮密钥加
//        extendKey = true;
        //输出全密钥
        System.out.println("------------extendKey-------------");
        out(key, 0, 0, -1, -1);
    }

    public static void addRoundKey(byte[][] bytes, byte[][] key, int round) {
        System.out.printf("w[%d] ------------- w[%d]\n", round * 4, round * 4 + 4 - 1);
        out(key, 0, round * 4, 4, 4);
        for (int j = 0, k = round * 4; k < round * 4 + 4; j++, k++) {
            for (int i = 0; i < 4; i++) {
                bytes[i][j] = (byte) ((bytes[i][j] ^ key[i][k]) & 0xff);
            }
        }
        //输出结果
        System.out.printf("第 %d 轮密钥加结果：\n", round);
        out(bytes, 0, 0, -1, -1);
    }

    public static byte[] encrypt(byte[] textBytes, byte[][] key, int groupLength, boolean extendKey) { //groupLength明文分组的字节数
        System.out.println("----------------------encrypt----------------------");
        int length = textBytes.length;
        int cipherLength = 0;
        if (length % groupLength != 0) cipherLength = (length / groupLength + 1) * groupLength;
        else cipherLength = length / groupLength * groupLength;
        byte[] cipherBytes = new byte[cipherLength];
        byte[][] state;
        int count = 0;
        int round = 0;
        int cur = 0;
        // 先来了10轮的密钥扩展
        if (!extendKey) extendKey(key);
//        extendKey(key);
        while (true) { // encryption
            round = 0;
            state = nextGroupBytes(textBytes, 4, 4, cur);
            cur += groupLength;
            if (state == null) break;
            System.out.printf("被加密内容：\n", round);
            out(state, 0, 0, 4, 4);
            addRoundKey(state, key, round);
            for (round = 1; round <= 10; round++) { // round为轮数，前9轮有mixColumns
                System.out.printf("第 %d 轮\n", round);
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
            for (int j = 0; j < 4; j++) { // store the ciphered bytes
                for (int i = 0; i < 4; i++) {
                    cipherBytes[count++] = state[i][j];
                }
            } // end store
//            System.out.printf("count=%d, end store\n", count);
        } // end encryption
        System.out.println("end encryption");
        return cipherBytes;
    }

    public static byte[] decrypt(byte[] cipherBytes, byte[][] key, int groupLength) { //groupLength明文分组的字节数
        System.out.println("----------------------decrypt----------------------");
        int round = 0;
        int count = 0;
        int cur = 0;
        int length = cipherBytes.length;
        byte[][] state;
        byte[] decipherBytes = new byte[length];
        while (count < length) { // decryption
            round = 10;
            state = nextGroupBytes(cipherBytes, 4, 4, cur);
            if (state == null) break;
            System.out.printf("需要解密内容：\n", round);
            out(state, 0, 0, 4, 4);
            cur += groupLength;
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

            for (round = 9; round > -1; round--) {
                System.out.printf("第 %d 轮\n", round);
                shiftRows(state, true);
                subBytes(state, true);  // end De-substituteByte
//                System.out.println("end de-substituteByte");
                addRoundKey(state, key, round);  // end De-addRoundKey
//                System.out.println("end De-addRoundKey");
                if (round > 0) {
                    state = mixColumns(state, true); // end De-mixColumns
//                    System.out.println("end De-mixColumns");
                }
            }
            for (int j = 0; j < col && count < length; j++) { // store the deciphered bytes
                for (int i = 0; i < row && count < length; i++) {
                    decipherBytes[count++] = state[i][j];
                }
            } // end store
        }
        return decipherBytes;
    }

    public void encrypt() {
        if (cipherBytes != null) {
            System.out.println("\n要再次加密请先调用resetAll()！");
            return;
        }
        cipherBytes = encrypt(textBytes, key, 16, extendKey);
    }

    public void decrypt() {
        if (cipherBytes == null) {
            System.out.println("\n请先加密再解密！");
            return;
        }
        decipherBytes = decrypt(cipherBytes, key, 16);
    }
}
