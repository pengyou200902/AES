import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class AESCipher {
    public static byte[] gbkText;
    public static byte[] initKey;
    public static int length = 0;
    public static int keyLength = 0;
    public static int cur = 0;
    public static int col = 0;
    public final static int row = 4;

    public AESCipher(String plainText, int keyLength) {
        this.keyLength = keyLength;
        this.col = this.keyLength / this.row;
        this.initKey = getInitKey(keyLength);
        System.out.printf("明文字符数: %d\n", plainText.length());
        this.gbkText = plainText.getBytes();
        this.length = gbkText.length;
        System.out.printf("默认是%s编码, 对应字节数: %d\n", System.getProperty("file.encoding"), this.length);
//        try {
//            this.gbkText = plainText.getBytes("GBK");
//            this.length = gbkText.length;
//            System.out.printf("GBK编码字节数: %d\n", this.length);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//
//        }
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
//                .append((byte) ((b >> 0) & 0x1));
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

    public static byte[] getInitKey(int keyLength) {
        byte[] initKey = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keyLength);
            SecretKey key = keyGenerator.generateKey();
            initKey = key.getEncoded();
            String s = bytesToHexString(initKey);
            System.out.println(s.getBytes().length);
            System.out.printf("十六进制密钥长度: %d, 二进制密钥长度则为: %d, 产生密钥是: %s\n", s.length(), s.length() * 4, s);
//            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < initKey.length; i++) {
//                sb.append(byteToBit(initKey[i]));
                System.out.println(byteToBit(initKey[i]));
            }
            System.out.println();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return initKey;
    }

//    public byte[] getGroupBytes(int begin, int end) {
//        byte[] needByte;
//        if(begin > end) {
//            System.out.println("begin must not > end!");
//            return null;
//        }
//        else if(begin <= end && end < gbkText.length && begin > -1) {
//            needByte = new byte[end - begin + 1];
//            for (int i = begin; i <= end; i++) {
//                needByte[i] = gbkText[begin + i];
//            }
//            return needByte;
//        }
//        else {
//            System.out.println("getGbkByte() ERROR!");
//            return null;
//        }
//    }

    public byte[][] nextGroupBytes() { // 按列分组
        int remain = length - cur;
        byte[][] subBytes = new byte[row][col];
        if (remain >= keyLength) {
            for (int j = 0; j < col; j++) {
                for (int i = 0; i < row; i++) {
                    subBytes[j][i] = gbkText[cur++];
                }
            }
        } else if (remain > 0) {

            for (int j = 0; j < col && remain > 0; j++) {
                for (int i = 0; i < row && remain > 0; i++, remain--) {
                    subBytes[j][i] = gbkText[cur++];
                }
            }
        }
        return subBytes;
    }

    public static byte substituteByte(byte b) {
        int high_4 = (b >> 4) & 0b00001111;
        int low_4 = b & 0b00001111;
        b = (byte) (AESParam.sbox[high_4][low_4]);
        return b;
    }

    public byte[][] shiftRows(byte[][] bytes) {
        if (bytes == null) return null;
        else if (bytes.length == 0) return bytes;
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
            return bytes;
        } else {
            System.out.println("Unknown Error!");
            return null;
        }
    }

// TO DO mix columns


    public static void main(String[] args) {
        AESCipher aesCipher = new AESCipher("我是你爸爸Tom。", 128);
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
