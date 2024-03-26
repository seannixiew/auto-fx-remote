package cn.instr;

import cn.model.InstruKind;
import cn.model.InstruType;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;

public class InstrumentClient {


    int instruKind;
    public String instruType;
    public boolean isConnected=false;

    LongByReference defaultSession;
    LongByReference vipSession;
//    String ip="";
//    public String getIp() {
//        return ip;
//    }

    public InstrumentClient(){}

    public InstrumentClient(int instruKind){
        this.instruKind=instruKind;
    }

    public void setInstruType(String instruType) {
        this.instruType = instruType;
    }

    public boolean open(String deviceType, String ip) {
        VISA32 visa32 = VISA32.INSTANCE;

        defaultSession = new LongByReference(0);
        int result = visa32.viOpenDefaultRM(defaultSession);
        if (result != VISA32.VI_SUCCESS) {
            return false;
        }

        vipSession = new LongByReference(0);
        String cmd = "TCPIP0::<ip>::inst0::INSTR".replace("<ip>", ip);
//        String cmd = "TCPIP0::<ip>::5025::SOCKET".replace("<ip>", ip);
        NativeLong a = new NativeLong(defaultSession.getValue());
        NativeLong b = new NativeLong(0);
        result = visa32.viOpen(a, cmd, b, b, vipSession);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("open fail:"+result);
            return false;
        }
        //连接成功才会设置
        isConnected=true;
        instruType=deviceType;
        return true;
    }

    public boolean openUsb(String deviceType, String address) {
        VISA32 visa32 = VISA32.INSTANCE;

        defaultSession = new LongByReference(0);
        int result = visa32.viOpenDefaultRM(defaultSession);
        if (result != VISA32.VI_SUCCESS) {
            return false;
        }

        vipSession = new LongByReference(0);
        String cmd = address;
        NativeLong a = new NativeLong(defaultSession.getValue());
        NativeLong b = new NativeLong(0);
        result = visa32.viOpen(a, cmd, b, b, vipSession);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("open fail:"+result);
            return false;
        }
        //连接成功才会设置
        isConnected=true;
        instruType=deviceType;
        return true;
    }


    public boolean openHislip(String deviceType, String ip) {
        VISA32 visa32 = VISA32.INSTANCE;

        defaultSession = new LongByReference(0);
        int result = visa32.viOpenDefaultRM(defaultSession);
        if (result != VISA32.VI_SUCCESS) {
            return false;
        }

        vipSession = new LongByReference(0);
        String cmd = "TCPIP0::<ip>::hislip1::INSTR".replace("<ip>", ip);
//        String cmd = "TCPIP0::<ip>::5025::SOCKET".replace("<ip>", ip);
        NativeLong a = new NativeLong(defaultSession.getValue());
        NativeLong b = new NativeLong(0);
        result = visa32.viOpen(a, cmd, b, b, vipSession);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("open fail:"+result);
            return false;
        }
        //连接成功才会设置
        isConnected=true;
        instruType=deviceType;
        return true;
    }

    //NRP专用（未使用）
    public boolean openSocket(String deviceType, String ip) {
        VISA32 visa32 = VISA32.INSTANCE;

        defaultSession = new LongByReference(0);
        int result = visa32.viOpenDefaultRM(defaultSession);
        if (result != VISA32.VI_SUCCESS) {
            return false;
        }

        vipSession = new LongByReference(0);
//        String cmd = "TCPIP0::<ip>::inst0::INSTR".replace("<ip>", ip);
        String cmd = "TCPIP0::<ip>::5025::SOCKET".replace("<ip>", ip); //nrp是4002
        NativeLong a = new NativeLong(defaultSession.getValue());

        //开启socket终止符
        VISA32.INSTANCE.viSetAttribute(a,new NativeLong(VISA32.VI_ATTR_TERMCHAR_EN),new NativeLong(1) );

        NativeLong b = new NativeLong(0);
        result = visa32.viOpen(a, cmd, b, b, vipSession);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("open fail:"+result);
            return false;
        }
        isConnected=true;
        instruType=deviceType;
        return true;
    }

    /**
     * 关闭设备.
     *
     * @return 成功返回true，失败返回false
     */
    public boolean close() {

        NativeLong a = new NativeLong(vipSession.getValue());
        int result = VISA32.INSTANCE.viClose(a);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println(result);
            return false;
        }

        NativeLong b = new NativeLong(defaultSession.getValue());
        result = VISA32.INSTANCE.viClose(b);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println(result);
            return false;
        }
        isConnected=false;
        return true;
    }

    public boolean writeCmd(String cmdStr) {
        NativeLong a = new NativeLong(vipSession.getValue());
        int result = VISA32.INSTANCE.viPrintf(a, "%s\n", cmdStr);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("write fail:"+result);
            return false;
        }
        return true;
    }

    public String readResult() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(1024);

       // VISA32.INSTANCE.viSetAttribute(a,new NativeLong(VISA32.VI_ATTR_TERMCHAR_EN),new NativeLong(1) );

        int result = VISA32.INSTANCE.viScanf(a, "%t", mem);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("read fail:"+result);
            return null;
        }
        return mem.getString(0);
    }

    public String readResultSocket() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(1024);

         VISA32.INSTANCE.viSetAttribute(a,new NativeLong(VISA32.VI_ATTR_TERMCHAR_EN),new NativeLong(1) );

        int result = VISA32.INSTANCE.viScanf(a, "%t", mem);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("read fail:"+result);
            return null;
        }
        return mem.getString(0);
    }

    public byte[] readImg() {
        NativeLong a = new NativeLong(vipSession.getValue());
        Memory mem = new Memory(10240);

         VISA32.INSTANCE.viSetAttribute(a,new NativeLong(VISA32.VI_ATTR_TERMCHAR_EN),new NativeLong(1) );

        int result = VISA32.INSTANCE.viScanf(a, "%t", mem);
        if (result != VISA32.VI_SUCCESS) {
            System.out.println("read fail:"+result);
            return null;
        }
        return mem.getByteArray(0,10240);
    }



}
