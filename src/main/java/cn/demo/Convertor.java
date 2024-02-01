package cn.demo;

public class Convertor {
    public static void main(String[] args) {
        for(int i=1;i<121;i++) {
            String hex = toHex(i);
            System.out.println(hex);
        }
    }

    static String toHex(int dec){
        String hex=Integer.toHexString(dec);
        if(dec<16){
            hex="0"+hex;
        }
        return hex;
    }
}
