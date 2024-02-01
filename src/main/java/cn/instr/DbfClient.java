package cn.instr;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class DbfClient {

    Socket socket = null;

    public String connect(String ip, long port){
        try {
            System.out.println("connecting...");
//            socket = new Socket("127.0.0.1", 12001);
            socket = new Socket("192.168.1.114", 12001);
            System.out.println("dbf client connection success");
            return "dbf client connection success";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "dbf connection fail";
    }



    public String dbfWrite(String cmdStr){

        String echo="";

//        cmdStr="EB900711121155AAAAAAAA";

        byte[] byteArray=HexStr2Bytes(cmdStr);


        try {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            os.write(byteArray);

            byte[] buffer = new byte[248];
            int length=is.read(buffer);
            if(length!=-1){
                echo=bytes2HexStr(buffer);
                System.out.println(echo);
            }
//            os.close();
//            is.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return echo;
    }


    public byte[] HexStr2Bytes(String hexString){

        // 确保字符串长度为偶数
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        // 字节数组
        int length = hexString.length() / 2;
        byte[] byteArray = new byte[length];

        // 遍历字符串并将每两个字符转换为一个字节
        for (int i = 0; i < length; i++) {
            int index = i * 2;
            int highNibble = Character.digit(hexString.charAt(index), 16);
            int lowNibble = Character.digit(hexString.charAt(index + 1), 16);

            // 将高四位左移4位，然后与低四位进行或运算
            byteArray[i] = (byte) ((highNibble << 4) | lowNibble);
        }

        // 打印字节数组
        System.out.print("Hex String: " + hexString + "\nByte Array (Big Endian): ");
        for (byte b : byteArray) {
            System.out.print(String.format("%02X ", b));
        }
        System.out.println("\n"+"==============above write================");

        return byteArray;
    }

    private  String bytes2HexStr(byte[] byteArray) {
        StringBuilder result = new StringBuilder();
        for (byte b : byteArray) {
            result.append(String.format("%02X ", b));
        }
        return result.toString();
    }

    /**
     * 测试代码
     * @param args
     */
    public static void main(String[] args) {
        DbfClient dbfClient=new DbfClient();
        dbfClient.connect("",0);
        dbfClient.dbfWrite("");
    }
}
