package cn.instr;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MatrixClient {


    public Socket socket = null;

    public int connect(String ip, long port){
        try {
            System.out.println("connecting...");
            socket = new Socket(ip, 3000);
            System.out.println("connection success");
            return 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public String channelSwitch(List<String> channels){

        String echo="matrix respond error！";
        String cmd="SETROUTE";

        for(String ch:channels){
            cmd=cmd+" "+ch;
        }
        cmd=cmd+"\n";
//        cmd="RST"+"\n";
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

    public String channelSwitchOff(List<String> channels){

        String echo="matrix respond error！";
        String cmd="CLOSEROUTE";

        for(String ch:channels){
            cmd=cmd+" "+ch;
        }
        cmd=cmd+"\n";
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

    public String rst(){

        String echo="matrix respond error！";
        String cmd="RST"+"\n";

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

        String cmd=null;
        Socket socket = null;
        try {
            System.out.println("connecting...");
//            socket = new Socket("192.168.1.102", 5025);
            socket = new Socket("192.168.1.123", 3000);
            System.out.println("connection success");

            cmd="11 10 W0 AA AA AA AA AA\n";
            OutputStream os = socket.getOutputStream();
            InputStream is=socket.getInputStream();
//            os.write("SETROUTE A2\n".getBytes(StandardCharsets.UTF_8));
//            os.write("CLOSEROUTE A2\n".getBytes(StandardCharsets.UTF_8));
            os.write(cmd.getBytes(StandardCharsets.UTF_8));
//os.flush();   // maybe significant

            byte[] buffer = new byte[1024];
            int length=is.read(buffer);
            if(length!=-1){
                String data=new String(buffer,0,length);
                System.out.println(data);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();

                    System.out.println("socket closed");
                } catch (Exception e) {

                }
            }
        }
    }

}