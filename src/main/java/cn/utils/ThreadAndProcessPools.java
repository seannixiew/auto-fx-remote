package cn.utils;

import cn.controllers.RfTestController;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

public class ThreadAndProcessPools {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    TextArea taLogs=rfTestController.taLogs;

    static List<Thread> theRoundThreadList=new ArrayList<>(); //vivado reader & writer & sync threads
    static List<Process> theRoundProcessList=new ArrayList<>();

    public static boolean clearProcessAndThread(){

        try {
            for (Thread t : theRoundThreadList) {
                if (t != null && t.isAlive()) {
                    t.stop();
                    System.out.println("clearing 线程: " + t);
                }
            }

            theRoundThreadList.clear();

            for (Process p : theRoundProcessList) {
                SystemUtils.killProcessTree(p);
            }

            theRoundProcessList.clear();

            Thread.sleep(1000);
            System.out.println("reader & writer 线程池 及 vivado 进程池：大概已清理。");

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void addThread(Thread t){
        theRoundThreadList.add(t);
    }

    public static void addProcess(Process p){
        theRoundProcessList.add(p);
    }
}
