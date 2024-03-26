package cn.utils;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.lang.reflect.Field;


public class SystemUtils {

    /**
     * 杀死指定进程数，即包括process进程的所有子进程
     *
     * @param process
     */
    public static void killProcessTree(Process process) {
        try {
            if (process != null && process.isAlive()) {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(process);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                int ret = kernel.GetProcessId(handle);
                Long PID = Long.valueOf(ret);
                String cmd = "cmd /c taskkill /PID " + PID + " /F /T ";
                Runtime rt = Runtime.getRuntime();
                Process killPrcess = rt.exec(cmd);
                killPrcess.waitFor();
                killPrcess.destroyForcibly();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}