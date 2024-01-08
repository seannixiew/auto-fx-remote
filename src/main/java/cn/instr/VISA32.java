package cn.instr;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.LongByReference;

public interface VISA32 extends Library {


    // TODO: 2024/1/7 绝对路径打包时关注
    //32位JDK8对应32位visa32.dll
    public VISA32 INSTANCE = (VISA32) Native.loadLibrary(
            "D:\\IDEA_Projects\\auto-fx\\src\\main\\resources\\dll\\visa32.dll", VISA32.class);

    public static final long VI_NULL = 0;
    public static final long VI_SUCCESS = 0;
    public static final int VI_ATTR_TERMCHAR_EN = (int) (0x3FFF0038);

    public int viOpenDefaultRM(LongByReference session);

    public int viOpen(NativeLong viSession, String rsrcName,
                      NativeLong accessMode, NativeLong timeout,
                      LongByReference session);

    public int viClose(NativeLong vi);

    public int viScanf(NativeLong vi, String readFmt, Object... args);

    public int viPrintf(NativeLong vi, String writeFmt, Object... args);

    NativeLong viSetAttribute(NativeLong vi, NativeLong attrName, NativeLong attrValue);
}
