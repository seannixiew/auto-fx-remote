package cn.demo;

import cn.instr.InstrumentClient;
import cn.instr.VISA32;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;

public class DeviceUsbConnect {

    public static void main(String[] args) {
        InstrumentClient instrumentClient = new InstrumentClient();
        instrumentClient.openUsb("示波器","USB0::0x0957::0x17A8::MY54310455::0::INSTR");
        instrumentClient.writeCmd("*IDN?");
        String s=instrumentClient.readResult();
        System.out.println(s);

        for(int i=0;i<99;i++) {
            try {
                //示波器截图
                instrumentClient.writeCmd(":RUN");
                Thread.sleep(2000); //must
                instrumentClient.writeCmd(":STOP");
                Thread.sleep(100);
                instrumentClient.writeCmd(":SAVE:IMAGe:FORMat BMP");
                String savCmd = ":SAVE:IMAGe 'mm'";
                instrumentClient.writeCmd(savCmd);
                instrumentClient.writeCmd("*OPC?"); //使用OPC会报错query interrupted
                Thread.sleep(1000);
                instrumentClient.readResult();
//                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }
    }
}
