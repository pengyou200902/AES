import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class AESCipher {
    public static byte[] Text;
    public static byte[][] key;   //密钥
    public static int length = 0;   //明文length，即字符数
    public static int keyLength = 0;
    public static int keyCol = 44;  //  本例实现的是128位密钥的，故经过轮密钥加扩展后一共44列
    public static int cur = 0;  //取明文的字节分组时所用的下标
    public static int col = 0;  //每组的列数不是final，因为密钥长度不同，列也会不同，但其实本例只实现了128位密钥的，故col其实是4
    public final static int row = 4;    //但是行数是确定4行

    public AESCipher(String plainText, int keyLen) {
        keyLength = keyLen;
        col = keyLength / 8 / row;
        getInitKey(keyLen);
        System.out.printf("明文字符数: %d\n", plainText.length());
        Text = plainText.getBytes();
        length = Text.length;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), length);
    }

    public static int byteToInt(byte a) {
        int x = a >= 0 ? a : a + 256;
        return x;
    }

    public static StringBuffer byteToBit(byte b) {
        return new StringBuffer()
                .append((byte) ((b >> 7) & 0x1))
                .append((byte) ((b >> 6) & 0x1))
                .append((byte) ((b >> 5) & 0x1))
                .append((byte) ((b >> 4) & 0x1))
                .append((byte) ((b >> 3) & 0x1))
                .append((byte) ((b >> 2) & 0x1))
                .append((byte) ((b >> 1) & 0x1))
                .append((byte) (b & 0x1));
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

    public static void getInitKey(int keyLength) { //初始化首次密钥
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


//    public byte[] getGroupBytes(int begin, int end) {
//        byte[] needByte;
//        if(begin > end) {
//            System.out.println("begin must not > end!");
//            return null;
//        }
//        else if(begin <= end && end < Text.length && begin > -1) {
//            needByte = new byte[end - begin + 1];
//            for (int i = begin; i <= end; i++) {
//                needByte[i] = Text[begin + i];
//            }
//            return needByte;
//        }
//        else {
//            System.out.println("getGbkByte() ERROR!");
//            return null;
//        }
//    }

    public byte[][] nextGroupBytes() { // 获取按列分组的字节组
        int remain = length - 1 - cur;  // cur是下标，从0开始，length是长度
        byte[][] subBytes = new byte[row][col];
        if (remain >= keyLength) {  //剩下的待加密内容长度足够，即 >= 分组长度（密钥长度）
            for (int j = 0; j < col; j++) {
                for (int i = 0; i < row; i++) {
                    subBytes[i][j] = Text[cur++];
                }
            }
        } else if (remain > 0) {    //剩下的待加密内容 < 所需的分组长度（密钥长度），采取了补0
//            for (int j = 0; j < col && remain > 0; j++) { // 此部分未采取补0
//                for (int i = 0; i < row && remain > 0; i++, remain--) {
//                    subBytes[i][j] = Text[cur++];
//                }
//            }
            for (int j = 0; j < col; j++) {
                for (int i = 0; i < row; i++) {
                    if (cur < length) {  //此处应该等价于if(remain > 0)
                        subBytes[i][j] = Text[cur++];
                    } else {
                        subBytes[i][j] = (byte) 0;
                    }
                }
            }

        }
        return subBytes;
    }

    public static byte substituteByte(byte b, boolean reverse) { //字节替换
        int low_4 = b & 0b1111;
        int high_4 = (b >> 4) & 0b1111;
        int[][] sbox;

        if (reverse) {
            sbox = AESParam._sbox;
        } else sbox = AESParam.sbox;
        b = (byte) (AESParam.sbox[high_4][low_4]);
        return b;
    }

    public void shiftRows(byte[][] bytes) { //行移位，数组为引用传递，故使用void
        if (bytes == null) return;
        else if (bytes.length == 0) return;
        else if (bytes.length > 1) {
            int r = bytes.length;
            int c = bytes[0].length;
            for (int i = 1; i < r; i++) {
                byte[] b = bytes[i].clone();
                int count = 0;
                for (int j = i; count < c; count++, j = (j + 1) % c) {
                    bytes[i][count] = b[j]; //左循环移位
                }
            }
//            return bytes;
        } else {
            System.out.println("Unknown Error!");
//            return null;
        }
    }

    // GF(2^8)乘法
    public static int mod2multiple(int a, int b) {
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
        if (reverse) {
            mixCol = AESParam._mixCol;
        } else mixCol = AESParam.mixCol;
        //矩阵GF(2^8)乘法
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < bytes[0].length; j++) {
                for (int k = 0; k < AESParam.mixCol[0].length; k++) {
                    mixedBytes[i][j] ^= mod2multiple(mixCol[i][k], byteToInt(bytes[k][j]));
                }
            }
        }
        return mixedBytes;
    }

    public void addRoundKey() {
        for (int i = 4; i < keyCol; i++) {

        }

    }

    public void cipher() {

    }


    public static void main(String[] args) {

//        测试用代码
        AESCipher aesCipher = new AESCipher("我是你爸爸Tom。", 128);


//        for (int j = 0; j < 4; j++) {//初始密钥按列输出
//            for (int i = 0; i < row; i++) {
//                System.out.printf("%4x ", byteToInt(key[i][j]));
//            }
//            System.out.println();
//        }
//        System.out.println(0b1111 ^ 0b100011);// = 101100 = System.out.println(0b101100);
//        System.out.println(mod2multiple(2,0xc9) ^ mod2multiple(3,0x7a) ^ mod2multiple(1,0x63) ^ mod2multiple(1,0xb0));
//        System.out.println(0xd4);
//        System.out.println(mod2multiple(1,0xc9) ^ mod2multiple(2,0x7a) ^ mod2multiple(3,0x63) ^ mod2multiple(1,0xb0));
//        System.out.println(0x28);
//        System.out.println(mod2multiple(1,0xc9) ^ mod2multiple(1,0x7a) ^ mod2multiple(2,0x63) ^ mod2multiple(3,0xb0));
//        System.out.println(0xbe);
//        System.out.println(mod2multiple(3,0xc9) ^ mod2multiple(1,0x7a) ^ mod2multiple(1,0x63) ^ mod2multiple(2,0xb0));
//        System.out.println(0x22);
//        System.out.println(mod2multiple(2,0xc9) ^ mod2multiple(3,0x6e) ^ mod2multiple(1,0x46) ^ mod2multiple(1,0xa6));
//        System.out.println(0xdb);
//        byte[] t= {};
//        System.out.println(t.length);
        /* check substitute
        byte[] t = aesCipher.getGbkBytes(0, 0);
        System.out.println(t.length);
        System.out.println(bytesToHexString(t));
        System.out.println(bytesToHexString(new byte[]{substituteByte(t[0])}));
        */


//        System.out.println(aesCipher.getPlainText());
    }
}
