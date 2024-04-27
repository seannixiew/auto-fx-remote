package cn.handler.da;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.utils.*;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class ModuleCalSynSignalDa implements EventHandler {


    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;
    ToggleButton btDownload=rfTestController.btDownload;
    DbfClient dbfClient=rfTestController.dbfClient;


    private static final CountDownLatch[] countDownLatchs0=new CountDownLatch[32];
    private static final CountDownLatch[] countDownLatchs1=new CountDownLatch[32];

    static {
        for(int i=0;i<32;i++){
            countDownLatchs0[i]=new CountDownLatch(4);
        }

        for(int i=0;i<32;i++){
            countDownLatchs1[i]=new CountDownLatch(4);
        }
    }

    //实现串行下载fpga程序
    private static CountDownLatch cd0=new CountDownLatch(1);
    private static CountDownLatch cd1=new CountDownLatch(1);
    private static CountDownLatch cd2=new CountDownLatch(1);


    Process process0;
    Process process1;
    Process process2;
    Process process3;
    public BufferedWriter processOutput0;
    public BufferedWriter processOutput1;
    public BufferedWriter processOutput2;
    public BufferedWriter processOutput3;
    BufferedReader processInput0;
    BufferedReader processInput1;
    BufferedReader processInput2;
    BufferedReader processInput3;
    BufferedReader processError0;
    BufferedReader processError1;
    BufferedReader processError2;
    BufferedReader processError3;
    Thread reader0;
    Thread reader1;
    Thread reader2;
    Thread reader3;
    Thread writerAndTester0;
    Thread writerAndTester1;
    Thread writerAndTester2;
    Thread writerAndTester3;
    Thread sync;

    boolean programming0;
    boolean programming1;
    boolean programming2;
    boolean programming3;

    boolean readerRunning0=true;
    boolean readerRunning1=true;
    boolean readerRunning2=true;
    boolean readerRunning3=true;

    static int readCounter0=0;
    static int readCounter1=0;
    static int readCounter2=0;
    static int readCounter3=0;

    Properties properties =new Properties();

    public ModuleCalSynSignalDa(){

        for(int i=0;i<32;i++){
            countDownLatchs0[i]=new CountDownLatch(4);
        }

        for(int i=0;i<32;i++){
            countDownLatchs1[i]=new CountDownLatch(4);
        }

        cd0=new CountDownLatch(1);
        cd1=new CountDownLatch(1);
        cd2=new CountDownLatch(1);

        readCounter0=0;
        readCounter1=0;
        readCounter2=0;
        readCounter3=0;

    }

    @Override
    public void handle(Event event) {
        System.out.println("执行（整机）DA校正同步信号测试...");
        Platform.runLater(() -> {
            taLogs.appendText("开始执行（整机）DA校正同步信号测试...\n");
        });

        try {
            properties.load(new FileInputStream("src/main/resources/configs/vivado.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
            String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

            // 启动Vivado进程0
            ProcessBuilder processBuilder0 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder0.redirectErrorStream(true);
            process0 = processBuilder0.start();
            ThreadAndProcessPools.addProcess(process0);

            // 启动Vivado进程1
            ProcessBuilder processBuilder1 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder1.redirectErrorStream(true);
            process1 = processBuilder1.start();
            ThreadAndProcessPools.addProcess(process1);

            // 启动Vivado进程2
            ProcessBuilder processBuilder2 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder2.redirectErrorStream(true);
            process2 = processBuilder2.start();
            ThreadAndProcessPools.addProcess(process2);

            // 启动Vivado进程3（合路板）
            ProcessBuilder processBuilder3 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder3.redirectErrorStream(true);
            process3 = processBuilder3.start();
            ThreadAndProcessPools.addProcess(process3);

            Thread.sleep(3000);

        } catch (Exception e) {
        }

        reader0=new Thread(()->{
            try {
//                reader0 = Thread.currentThread();
                processInput0 = new BufferedReader(new InputStreamReader(process0.getInputStream(), "UTF-8"));
                processError0 = new BufferedReader(new InputStreamReader(process0.getInputStream()));

                while (readerRunning0) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput0,"reader0",0);
                    String error = readErrorFromProcess(processError0);
                    if(echo!=null && !echo.contains("read pending...")) {
                        String log = DateFormat.FORLOGSHORT.format(new Date()) + echo + "\n";
                        if (log.toUpperCase().contains("ERROR")) {
                            VivadoErrorCounts.setReadError(log);
                        }
                        Platform.runLater(() -> {
                            taLogs.appendText(log);
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date()) + error + "\n");
                        });
                    }
                    String s = echo;
                    if (programming0) {
                        if (s.contains("End of startup status: HIGH")) {
                            programming0 = false;
                            LockSupport.unpark(writerAndTester0);
                            System.out.println("process0下载完毕，唤醒测试Thread0...");
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
        reader0.start();
        ThreadAndProcessPools.addThread(reader0);

        reader1=new Thread(()->{
            try {
//                reader1 = Thread.currentThread();
                processInput1 = new BufferedReader(new InputStreamReader(process1.getInputStream(), "UTF-8"));
                processError1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));

                while (readerRunning1) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput1,"reader1",1);
                    String error = readErrorFromProcess(processError1);
                    if(echo!=null && !echo.contains("read pending...")) {
                        String log = DateFormat.FORLOGSHORT.format(new Date()) + echo + "\n";
                        if (log.toUpperCase().contains("ERROR")) {
                            VivadoErrorCounts.setReadError(log);
                        }
                        Platform.runLater(() -> {
                            taLogs.appendText(log);
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date()) + error + "\n");
                        });
                    }
                    String s = echo;
                    if (programming1) {
                        if (s.contains("End of startup status: HIGH")) {
                            programming1 = false;
                            LockSupport.unpark(writerAndTester1);
                            System.out.println("process1下载完毕，唤醒测试Thread1...");
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
        reader1.start();
        ThreadAndProcessPools.addThread(reader1);

        reader2=new Thread(()->{
            try {
//                reader2 = Thread.currentThread();
                processInput2 = new BufferedReader(new InputStreamReader(process2.getInputStream(), "UTF-8"));
                processError2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));

                while (readerRunning2) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput2,"reader2",2);
                    String error = readErrorFromProcess(processError2);
                    if(echo!=null && !echo.contains("read pending...")) {
                        String log = DateFormat.FORLOGSHORT.format(new Date()) + echo + "\n";
                        if (log.toUpperCase().contains("ERROR")) {
                            VivadoErrorCounts.setReadError(log);
                        }
                        Platform.runLater(() -> {
                            taLogs.appendText(log);
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date()) + error + "\n");
                        });
                    }
                    String s = echo;
                    if (programming2) {
                        if (s.contains("End of startup status: HIGH")) {
                            programming2 = false;
                            LockSupport.unpark(writerAndTester2);
                            System.out.println("process2下载完毕，唤醒测试Thread2...");
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
        reader2.start();
        ThreadAndProcessPools.addThread(reader2);

        reader3=new Thread(()->{
            try {
//                reader3 = Thread.currentThread();
                processInput3 = new BufferedReader(new InputStreamReader(process3.getInputStream(), "UTF-8"));
                processError3 = new BufferedReader(new InputStreamReader(process3.getInputStream()));

                while (readerRunning3) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput3,"reader3",3);
                    String error = readErrorFromProcess(processError3);
                    if(echo!=null && !echo.contains("read pending...")) {
                        String log = DateFormat.FORLOGSHORT.format(new Date()) + echo + "\n";
                        if (log.toUpperCase().contains("ERROR")) {
                            VivadoErrorCounts.setReadError(log);
                        }
                        Platform.runLater(() -> {
                            taLogs.appendText(log);
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date()) + error + "\n");
                        });
                    }
                    String s = echo;
                    if (programming3) {
                        if (s.contains("End of startup status: HIGH")) {
                            programming3 = false;
                            LockSupport.unpark(writerAndTester3);
                            System.out.println("process3下载完毕，唤醒测试Thread3...");
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
        reader3.start();
        ThreadAndProcessPools.addThread(reader3);

        processOutput0 = new BufferedWriter(new OutputStreamWriter(process0.getOutputStream()));
        System.out.println("shell0进程状态：" + process0.isAlive());
        processOutput1 = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream()));
        System.out.println("shell1进程状态：" + process1.isAlive());
        processOutput2 = new BufferedWriter(new OutputStreamWriter(process2.getOutputStream()));
        System.out.println("shell2进程状态：" + process2.isAlive());
        processOutput3 = new BufferedWriter(new OutputStreamWriter(process3.getOutputStream()));
        System.out.println("shell3进程状态：" + process3.isAlive());

        writerAndTester0=new Thread(()->{
//            writerAndTester0=Thread.currentThread();
            try {
                cd0.await();
                System.out.println("process0初始操作...");
                writeToProcess(processOutput0, 0,"open_hw" + "\n");
                writeToProcess(processOutput0, 0,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput0, "open_hw_target" + "\n");
                writeToProcess(processOutput0, 0, "open_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum0") + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum1") + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum2") + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum3") + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput0, 0,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput0, 0,     "set_property PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath0") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"set_property FULL_PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath0") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,    "set_property PROGRAM.FILE "+properties.getProperty("ModuleCalSynSignalDa.programPath0")+" [get_hw_devices xc7vx690t_0]" + "\n");

                if (btDownload.isSelected()){
                    System.out.println("process0下载...");
                    writeToProcess(processOutput0, 0,"program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    programming0 = true;
                    LockSupport.park();
                }
                cd1.countDown();
                Thread.sleep(10000);
                System.out.println("process0下载后设置...");
                writeToProcess(processOutput0, 0,"refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"mcu_spi_top_2_inst/ila_spi_line_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1248/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]" + "\n");
                Thread.sleep(1000);

                //设置采数下降沿触发
                writeToProcess(processOutput0,0,"set_property TRIGGER_COMPARE_VALUE eq1'bF [get_hw_probes trans_source_inst/pps -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"startgroup"+"\n");
                writeToProcess(processOutput0,0,"set_property CONTROL.DATA_DEPTH 4096 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                writeToProcess(processOutput0,0,"set_property CONTROL.TRIGGER_POSITION 4095 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                writeToProcess(processOutput0,0,"endgroup"+"\n");
                writeToProcess(processOutput0,0,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");

                for(int datadelay=0;datadelay<32;datadelay++){
                    System.out.println("process0到达检查点一，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点一被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput0,0,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    System.out.println("process0到达检查点二，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点二，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    Thread.sleep(3000); //触发余量
                    writeToProcess(processOutput0,0,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput0,0,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput0,0, "write_hw_ila_data -csv_file {"+properties.getProperty("ModuleCalSynSignalDa.samplePath")
                            +"process0"+"_"+datadelay+".csv} hw_ila_data_"+properties.getProperty("ModuleCalSynSignalDa.ilaNumDA1") + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    File file=new File(properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process0"+"_"+datadelay+".csv");
                    while (true){
                        Thread.sleep(1);
                        if (file.exists()){
                            System.out.println("已生成文件"+properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process0"+"_"+datadelay+".csv");
                            break;
                        }
                    }
                }
                readerRunning0=false;
                System.out.println("关闭process0的vivado...");
                SystemUtils.killProcessTree(process0);
            }catch (Exception e){}
        });
        writerAndTester0.start();
        ThreadAndProcessPools.addThread(writerAndTester0);

        writerAndTester1=new Thread(()->{
//            writerAndTester1=Thread.currentThread();
            try {
                cd1.await();
                System.out.println("process1初始操作...");
                writeToProcess(processOutput1, 1,"open_hw" + "\n");
                writeToProcess(processOutput1, 1,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput1, "open_hw_target" + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum0") + "\n");
                writeToProcess(processOutput1, 1, "open_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum1") + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum2") + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum3") + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput1, 1,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput1, 1,     "set_property PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath1") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"set_property FULL_PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath1") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,    "set_property PROGRAM.FILE "+properties.getProperty("ModuleCalSynSignalDa.programPath1")+" [get_hw_devices xc7vx690t_0]" + "\n");

                if (btDownload.isSelected()){
                    System.out.println("process1下载...");
                    writeToProcess(processOutput1, 1,"program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    programming1 = true;
                    LockSupport.park();
                }
                cd2.countDown();
                Thread.sleep(10000);
                System.out.println("process1下载后设置...");
                writeToProcess(processOutput1, 1,"refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"mcu_spi_top_2_inst/ila_spi_line_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1248/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]" + "\n");
                Thread.sleep(1000);

                //设置采数下降沿触发
                writeToProcess(processOutput1,1,"set_property TRIGGER_COMPARE_VALUE eq1'bF [get_hw_probes trans_source_inst/pps -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"startgroup"+"\n");
                writeToProcess(processOutput1,1,"set_property CONTROL.DATA_DEPTH 4096 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                writeToProcess(processOutput1,1,"set_property CONTROL.TRIGGER_POSITION 4095 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                writeToProcess(processOutput1,1,"endgroup"+"\n");
                writeToProcess(processOutput1,1,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");

                for(int datadelay=0;datadelay<32;datadelay++){
                    System.out.println("process1到达检查点一，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点一被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput1,1,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    System.out.println("process1到达检查点二，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点二，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    Thread.sleep(3000); //触发余量
                    writeToProcess(processOutput1,1,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput1,1,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput1,1, "write_hw_ila_data -csv_file {"+properties.getProperty("ModuleCalSynSignalDa.samplePath")
                            +"process1"+"_"+datadelay+".csv} hw_ila_data_"+properties.getProperty("ModuleCalSynSignalDa.ilaNumDA2") + "\n");
//
                    File file=new File(properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process1"+"_"+datadelay+".csv");
                    while (true){
                        Thread.sleep(1);
                        if (file.exists()){
                            System.out.println("已生成文件"+properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process1"+"_"+datadelay+".csv");
                            break;
                        }
                    }
                }
                readerRunning1=false;
                System.out.println("关闭process1的vivado...");
                SystemUtils.killProcessTree(process1);
            }catch (Exception e){}
        });
        writerAndTester1.start();
        ThreadAndProcessPools.addThread(writerAndTester1);

        writerAndTester2=new Thread(()->{
//            writerAndTester2=Thread.currentThread();
            try {
                cd2.await();
                System.out.println("process2初始操作...");
                writeToProcess(processOutput2, 2,"open_hw" + "\n");
                writeToProcess(processOutput2, 2,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput2, "open_hw_target" + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum0") + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum1") + "\n");
                writeToProcess(processOutput2, 2, "open_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum2") + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum3") + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput2, 2,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput2, 2,     "set_property PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath2") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"set_property FULL_PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath2") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,    "set_property PROGRAM.FILE "+properties.getProperty("ModuleCalSynSignalDa.programPath2")+" [get_hw_devices xc7vx690t_0]" + "\n");

                if (btDownload.isSelected()){
                    System.out.println("process2下载...");
                    writeToProcess(processOutput2, 2,"program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    programming2 = true;
                    LockSupport.park();
                }
                Thread.sleep(10000);
                System.out.println("process2下载后设置...");
                writeToProcess(processOutput2, 2,"refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"mcu_spi_top_2_inst/ila_spi_line_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1248/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]" + "\n");
                Thread.sleep(1000);

                //设置采数下降沿触发
                writeToProcess(processOutput2,2,"set_property TRIGGER_COMPARE_VALUE eq1'bF [get_hw_probes trans_source_inst/pps -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"startgroup"+"\n");
                writeToProcess(processOutput2,2,"set_property CONTROL.DATA_DEPTH 4096 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                writeToProcess(processOutput2,2,"set_property CONTROL.TRIGGER_POSITION 4095 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                writeToProcess(processOutput2,2,"endgroup"+"\n");
                writeToProcess(processOutput2,2,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");

                for(int datadelay=0;datadelay<32;datadelay++){
                    System.out.println("process2到达检查点一，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点一被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput2,2,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    System.out.println("process2到达检查点二，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点二，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    Thread.sleep(3000); //触发余量
                    writeToProcess(processOutput2,2,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput2,2,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput2,2, "write_hw_ila_data -csv_file {"+properties.getProperty("ModuleCalSynSignalDa.samplePath")
                            +"process2"+"_"+datadelay+".csv} hw_ila_data_"+properties.getProperty("ModuleCalSynSignalDa.ilaNumDA3") + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    File file=new File(properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process2"+"_"+datadelay+".csv");
                    while (true){
                        Thread.sleep(1);
                        if (file.exists()){
                            System.out.println("已生成文件"+properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process2"+"_"+datadelay+".csv");
                            break;
                        }
                    }
                }
                readerRunning2=false;
                System.out.println("关闭process2的vivado...");
                SystemUtils.killProcessTree(process2);
            }catch (Exception e){}
        });
        writerAndTester2.start();
        ThreadAndProcessPools.addThread(writerAndTester2);

        writerAndTester3=new Thread(()->{
//            writerAndTester3=Thread.currentThread();
            try {
                System.out.println("process3初始操作...");
                writeToProcess(processOutput3, 3,"open_hw" + "\n");
                writeToProcess(processOutput3, 3,"connect_hw_server" + "\n");
                Thread.sleep(1000);
                writeToProcess(processOutput3, 3,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum0") + "\n");
                writeToProcess(processOutput3, 3,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum1") + "\n");
                writeToProcess(processOutput3, 3,"close_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum2") + "\n");
                writeToProcess(processOutput3, 3, "open_hw_target "+properties.getProperty("ModuleCalSynSignalDa.boxNum3") + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput3, 3,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput3, 3,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                Thread.sleep(10000);
                writeToProcess(processOutput3, 3,     "set_property PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath3") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput3, 3,"set_property FULL_PROBES.FILE "+properties.getProperty("ModuleCalSynSignalDa.probesPath3") +" [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput3, 3,    "set_property PROGRAM.FILE "+properties.getProperty("ModuleCalSynSignalDa.programPath3")+" [get_hw_devices xc7vx690t_0]" + "\n");

                if (btDownload.isSelected()){
                    System.out.println("process3下载...");
                    writeToProcess(processOutput3, 3,"program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    programming3 = true;
                    LockSupport.park();
                }
                cd0.countDown();
                Thread.sleep(10000);
                System.out.println("process3下载后设置...");
                writeToProcess(processOutput3, 3,"refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_Calib_result_test/your_instance_name\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/beam_out_duc_inst/uut_yd_duc\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/uut_54fxt\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/uut_bsfp\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/uut_fxt_ad/ila_jzAD_ddc_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"dut_539_para/uut_para539\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"dut_539_para/uut_para_self\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_8 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"inst_top_gth/uut_ila_gth539\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_9 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"inst_top_gth/uut_ila_gth54\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_10 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1169/uut_yb_duc\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_11 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_source_inst/ila_calibsource_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_12 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_13 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_adda/ila_addata_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_15 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_gth_link\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_14 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/nolabel_line79/ila_jzAD_ddc_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_16 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/uut_ila_FenLu_YB\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_17 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/uut_ila_yd_out\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_18 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/uut_yb_ila\"}]]" + "\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_19 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_ila_539add\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/CNTVALUEIN_syn_o1 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/CNTVALUEIN_syn_o2 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/CNTVALUEIN_syn_o3 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/CNTVALUEOUT_syn_o1 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/CNTVALUEOUT_syn_o2 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/CNTVALUEOUT_syn_o3 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/LD_syn_o1 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/LD_syn_o2 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes trans_Calib_test_inst/LD_syn_o3 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE 0 [get_hw_probes trans_Calib_test_inst/LD_syn_o1 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_Calib_test_inst/LD_syn_o1} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE 1 [get_hw_probes trans_Calib_test_inst/LD_syn_o1 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_Calib_test_inst/LD_syn_o1} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE 0 [get_hw_probes trans_Calib_test_inst/LD_syn_o2 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_Calib_test_inst/LD_syn_o2} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE 1 [get_hw_probes trans_Calib_test_inst/LD_syn_o2 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_Calib_test_inst/LD_syn_o2} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE 0 [get_hw_probes trans_Calib_test_inst/LD_syn_o3 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_Calib_test_inst/LD_syn_o3} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "set_property OUTPUT_VALUE 1 [get_hw_probes trans_Calib_test_inst/LD_syn_o3 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_Calib_test_inst/LD_syn_o3} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");

                //设置触发条件
                writeToProcess(processOutput3, 3,"set_property TRIGGER_COMPARE_VALUE eq1'bF [get_hw_probes trans_Calib_test_inst/pps -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]]" + "\n");
                writeToProcess(processOutput3, 3,"startgroup" + "\n");
                writeToProcess(processOutput3, 3,"set_property CONTROL.DATA_DEPTH 4096 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]" + "\n");
                writeToProcess(processOutput3, 3,"set_property CONTROL.TRIGGER_POSITION 4095 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]" + "\n");
                writeToProcess(processOutput3, 3,"endgroup" + "\n");
                writeToProcess(processOutput3, 3,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]" + "\n");

                for(int datadelay=0;datadelay<32;datadelay++){
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE "+datadelay+" [get_hw_probes trans_Calib_test_inst/CNTVALUEIN_syn_o1 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_Calib_test_inst/CNTVALUEIN_syn_o1} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE "+datadelay+" [get_hw_probes trans_Calib_test_inst/CNTVALUEIN_syn_o2 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_Calib_test_inst/CNTVALUEIN_syn_o2} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE "+datadelay+" [get_hw_probes trans_Calib_test_inst/CNTVALUEIN_syn_o3 -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_Calib_test_inst/CNTVALUEIN_syn_o3} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/vio_delay_transCalib_inst\"}]]" + "\n");

                    System.out.println("process3到达检查点一，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点一被唤起...");  //所有AD及合路线程采数完成，并且合路线程完成delay设置=>同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput3,3,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]"+"\n");
                    System.out.println("process3到达检查点二，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点二，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    Thread.sleep(3000); //触发余量
                    writeToProcess(processOutput3,3,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput3,3,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]"+"\n");
                    writeToProcess(processOutput3,3, "write_hw_ila_data -csv_file {"+properties.getProperty("ModuleCalSynSignalDa.samplePath")
                            +"process3"+"_"+datadelay+".csv} hw_ila_data_"+properties.getProperty("ModuleCalSynSignalDa.ilaNumHL") + "\n");
                    File file=new File(properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process3"+"_"+datadelay+".csv");
                    while (true){
                        Thread.sleep(1);
                        if (file.exists()){
                            System.out.println("已生成文件"+properties.getProperty("ModuleCalSynSignalDa.samplePath")+"process3"+"_"+datadelay+".csv");
                            break;
                        }
                    }
                }
                readerRunning3=false;
                System.out.println("关闭process3的vivado...");
                SystemUtils.killProcessTree(process3);
            }catch (Exception e){}
        });
        writerAndTester3.start();
        ThreadAndProcessPools.addThread(writerAndTester3);


        sync=new Thread(()->{
//            sync=Thread.currentThread();
            try {
                for(int i=0;i<32;i++){
                    System.out.println("sync线程进入waiting，等待三个线程到达检查点一...循环编号："+i);
                    countDownLatchs0[i].await();
                    System.out.println("四个线程已到达检查点一，设置KL14，再依次唤起...循环编号："+i);
                    //设置KL14，pps切外
                    dbfClient.dbfWrite("EB900E1119AAAAAAAAAAAA");
                    Thread.sleep(500);
                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                    LockSupport.unpark(writerAndTester3);

                    System.out.println("syn线程进入waiting，等待三个线程到达检查点二，循环编号："+i);
                    countDownLatchs1[i].await();
                    System.out.println("四个线程已到达检查点二，设置KL5，KL14，再依次唤起...循环编号："+i);
                    //设置KL5
                    dbfClient.dbfWrite("EB9005111055AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111066AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111077AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111044AAAAAAAAAA");
                    Thread.sleep(500);
                    //设置KL14,pps切内
                    dbfClient.dbfWrite("EB900E111955AAAAAAAAAA");
                    Thread.sleep(500);
                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                    LockSupport.unpark(writerAndTester3);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        sync.start();
        ThreadAndProcessPools.addThread(sync);

        try {
            writerAndTester0.join();
            writerAndTester1.join();
            writerAndTester2.join();
            writerAndTester3.join();
            sync.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private  void writeToProcess(BufferedWriter processOutput,int senderNum, String command) throws IOException, InterruptedException {
        processOutput.write(command);
        processOutput.flush();
        String s= DateFormat.FORLOGSHORT.format(new Date())+"sender"+senderNum+"："+command;
        Platform.runLater(()->{
            taLogs.appendText(s);
        });
        System.out.println(s);
        Thread.sleep(1000);
    }

    private static String readFromProcess(BufferedReader processInput,String readerName,int counterNum) throws IOException {

        String s="read pending...";
        switch (counterNum){
            case 0:
                readCounter0++;
                if(processInput.ready()) {
                    char[] bt = new char[1024];
                    processInput.read(bt);
                    s = String.valueOf(bt);
                    s = s.replaceAll("\\u0000", " ");
                    readCounter0=0;
                }
                System.out.println(readerName+": "+s+" "+readCounter0);
                break;
            case 1:
                readCounter1++;
                if(processInput.ready()) {
                    char[] bt = new char[1024];
                    processInput.read(bt);
                    s = String.valueOf(bt);
                    s = s.replaceAll("\\u0000", " ");
                    readCounter1=0;
                }
                System.out.println(readerName+": "+s+" "+readCounter1);
                break;
            case 2:
                readCounter2++;
                if(processInput.ready()) {
                    char[] bt = new char[1024];
                    processInput.read(bt);
                    s = String.valueOf(bt);
                    s = s.replaceAll("\\u0000", " ");
                    readCounter2=0;
                }
                System.out.println(readerName+": "+s+" "+readCounter2);
                break;
            case 3:
                readCounter3++;
                if(processInput.ready()) {
                    char[] bt = new char[1024];
                    processInput.read(bt);
                    s = String.valueOf(bt);
                    s = s.replaceAll("\\u0000", " ");
                    readCounter3=0;
                }
                System.out.println(readerName+": "+s+" "+readCounter3);
                break;
        }
        return s;
    }

    private static String readErrorFromProcess(BufferedReader processInput) throws IOException {

        String s="read pending...";
        if(processInput.ready()) {
            char[] bt = new char[1024];
            processInput.read(bt);
            s = String.valueOf(bt);
            s = s.replaceAll("\\u0000", " ");
        }

        return s;
    }
}
