import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class AESCipher {
    public static byte[] Text;  // 明文字节数组
    public static byte[] cipherBytes; // 明文补0后加密产生的密文
    public static byte[][] key;   //密钥
    public static int length = 0;   //明文length，即字符数
    public static int keyLength = 0;    //密钥的bit位数
    public static int keyCol = 44;  //  本例实现的是128位密钥的，故经过轮密钥加扩展后一共44列
    public static int cur = 0;  //取明文的字节分组时所用的下标
    public static int col = 0;  //每组的列数不是final，因为密钥长度不同，列也会不同，但其实本例只实现了128位密钥的，故col其实是4
    public static boolean extendKey = false;
    public final static int row = 4;    //但是行数是确定4行
    private static Scanner sc = new Scanner(System.in);

    public AESCipher(String plainText, int keyLen) {
        cur = 0;
        keyLength = keyLen;
        col = keyLength / 8 / row;
        extendKey = false;
        initKey(keyLen);
        System.out.printf("明文字符数: %d\n", plainText.length());
        Text = plainText.getBytes();
        length = Text.length;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), length);
    }

    public static String plaintextInput() {
        System.out.print("请输入待加密的明文：");
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
        col = keyLength / 8 / row;
        extendKey = false;
        initKey(keyLength);
        String plainText = plaintextInput();
        Text = plainText.getBytes();
        System.out.printf("明文字符数: %d\n", plainText.length());
        length = Text.length;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), length);
        cipherBytes = null;
    }

    public static int byteToInt(byte a) {
        int x = a >= 0 ? a : a + 256;
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


    public byte[][] nextGroupBytes() { // 获取按列分组的字节组，本实例其实规模是4*4的byte数组
        int remain = length - 1 - cur;  // cur是下标，从0开始，length是长度
        byte[][] subBytes;
        if (remain >= keyLength) {  //剩下的待加密内容长度足够，即 >= 分组长度（密钥长度）
            subBytes = new byte[row][col];
            for (int j = 0; j < col; j++) {
                for (int i = 0; i < row; i++) {
                    subBytes[i][j] = Text[cur++];
                }
            }
        } else if (remain > 0) {    //剩下的待加密内容 < 所需的分组长度（密钥长度），采取了补0
            subBytes = new byte[row][col];
            for (int j = 0; j < col; j++) {
                for (int i = 0; i < row; i++) {
                    if (cur < length) {  //此处应该等价于if(remain > 0)
                        subBytes[i][j] = Text[cur++];
                    } else {
                        subBytes[i][j] = (byte) 0;
                    }
                }
            }
            return subBytes;
        }
        return null; //明文取完了，就返回null
    }

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

    public void shiftRows(byte[][] bytes) { //行移位，数组为引用传递，故使用void
        if (bytes == null) return;
        else if (bytes.length == 0) return;
        else if (bytes.length > 1) {
            int r = bytes.length;
            int c = bytes[0].length;
            for (int i = 1; i < r; i++) {
                leftLoopMove(bytes[i], i);
            }
        } else {
            System.out.println("Unknown Error!");
        }
    }

    // GF(2^8)乘法
    public static int GF28multiple(int a, int b) {
        if (a == 0 || b == 0) return 0;
        if (a == 1) return b;
        if (b == 1) return a;
        int count = 0;
        int result = 0;

        do {
            result ^= ((a * (b & 0b1)) << count);
            count++;
            b >>= 1;
        } while (b != 0);
        if ((result >> 7) == 1) {
            result ^= 0b00011011;
        }
        return (result & 0b11111111);
    }


    public byte[][] mixColumns(byte[][] bytes, boolean reverse) { //列混淆,参数是待混淆的
        int set_col = bytes[0].length;
        byte[][] mixedBytes;
        final int[][] mixCol;

        if (set_col >= 4) {  //判断一下col有效与否
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
//                    mixedBytes[i][j] ^= GF28multiple(mixCol[i][k], byteToInt(bytes[k][j])); // 参与运算不应该这样转int
                    mixedBytes[i][j] ^= GF28multiple(mixCol[i][k], bytes[k][j]);
                }
            }
        }
        return mixedBytes;
    }

    public void extendKey() {
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
                    w_j_1[count] = (byte) (substituteByte(key[i][j - 1], false) ^ AESParam.Rcon[round]);
                    w_j[count] = (byte) (w_j_4[count] ^ w_j_1[count]);
                    key[count][j] = w_j[count]; //填入总密钥
                }
                // T函数内容结束
            }
        } // end for / 结束轮密钥加
        extendKey = true;
    }

    public void addRoundKey(byte[][] bytes, int round) {
        for (int j = round * 4; j < round + 4; j++) {
            for (int i = 0; i < row; i++) {
                bytes[i][j] ^= key[i][j];
            }
        }
    }

    public void cipher() {
        if (cipherBytes != null) {
            System.out.println("要再次加密请先调用resetAll()！");
            return;
        }
        byte[][] state;
        int count = 0;
        int round = 0;
        int keyByteLength = keyLength / 8;
        cipherBytes = new byte[Text.length / keyByteLength + Text.length % keyByteLength];
        // 先来了10轮的密钥扩展
        if (!extendKey) extendKey();
        while (true) { // encryption
            round = 0;
            state = nextGroupBytes();
            if (state == null) break;
            addRoundKey(state, round);
            for (round = 1; round <= 9; round++) { // round为轮数，前9轮有mixColumns
                for (int i = 0; i < row; i++) {
                    for (int j = 0; j < col; j++) {
                        state[i][j] = substituteByte(state[i][j], false);
                    }
                } // end substituteByte
                shiftRows(state);   // end shiftRows
                state = mixColumns(state, false); // end mixColumns
                addRoundKey(state, round); // end addRoundKey
            } // end 9 rounds
            // 10th round here, without mixColumns
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    state[i][j] = substituteByte(state[i][j], false);
                }
            } // end substituteByte
            shiftRows(state);   // end shiftRows
            addRoundKey(state, round); // end addRoundKey, end 10th round
            for (int i = 0; i < row; i++) { // store the ciphered bytes
                for (int j = 0; j < col; j++) {
                    cipherBytes[count++] = state[i][j];
                }
            } // end store
        } // end encryption
    }



}
