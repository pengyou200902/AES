import java.util.Scanner;

public class testCipher {

    public static AESCipher menu() {
        Scanner sc = new Scanner(System.in);
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
        sc.close();
        return new AESCipher(plainText, 128); // 本实例针对128位
    }

    public static void main(String[] args) {
        AESCipher aesCipher = menu();
//        Scanner sc = new Scanner(System.in);
//        System.out.print("请输入待加密的明文：");
//        String plainText;
//        boolean legal = false;
//        do{
//            System.out.print(">>> ");
//            plainText = sc.nextLine();
//            if(plainText == null || plainText.length() <= 0) {
//                legal = false;
//                System.out.println("明文输入有误！请检查并重新输入！");
//            }
//            else legal = true;
//        }while(!legal);
//        AESCipher aesCipher = new AESCipher(plainText, 128); // 本实例针对128位

//        测试用代码
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
        /* check substitute
        byte[] t = aesCipher.getGbkBytes(0, 0);
        System.out.println(t.length);
        System.out.println(bytesToHexString(t));
        System.out.println(bytesToHexString(new byte[]{substituteByte(t[0])}));
        */


//        System.out.println(aesCipher.getPlainText());
    }
}
