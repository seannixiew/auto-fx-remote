package cn.instr;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PowerSupplyClient {

    public Socket socket = null;

    public boolean connect(String ip, int port){
        try {
            System.out.println("connecting...");
            socket = new Socket(ip, port); //1790
            System.out.println("connection success");
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public String writeAndRead(String cmd){

        String echo="default";
        try {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            os.write(cmd.getBytes(StandardCharsets.UTF_8));

            byte[] buffer = new byte[1024];
            int length=is.read(buffer);
            if(length!=-1){
                echo=new String(buffer,0,length);
                System.out.println(echo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return echo;
    }

    public static void main(String[] args) {
        PowerSupplyClient ps=new PowerSupplyClient();
        boolean c=ps.connect("192.168.1.19",0);
        System.out.println(c);
        String w=ps.writeAndRead("SYSTem:ERRor?");
        System.out.println(w);
    }
}
