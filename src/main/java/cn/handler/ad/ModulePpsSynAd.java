package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

import java.io.*;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class ModulePpsSynAd implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;
    ToggleButton btDownload=rfTestController.btDownload;
    DbfClient dbfClient=rfTestController.dbfClient;
    InstrumentClient instru4=rfTestController.instru4;


    private static final CountDownLatch[] countDownLatchs0=new CountDownLatch[96];

    static {
        for(int i=0;i<96;i++){
            countDownLatchs0[i]=new CountDownLatch(3);
        }
    }

    //实现串行下载fpga程序
    private static final CountDownLatch cd0=new CountDownLatch(1);
    private static final CountDownLatch cd1=new CountDownLatch(1);

    Process process0;
    Process process1;
    Process process2;

    public BufferedWriter processOutput0;
    public BufferedWriter processOutput1;
    public BufferedWriter processOutput2;

    BufferedReader processInput0;
    BufferedReader processInput1;
    BufferedReader processInput2;

    BufferedReader processError0;
    BufferedReader processError1;
    BufferedReader processError2;

    Thread reader0;
    Thread reader1;
    Thread reader2;

    Thread writerAndTester0;
    Thread writerAndTester1;
    Thread writerAndTester2;
    Thread sync;

    boolean programming0;
    boolean programming1;
    boolean programming2;


    static int readCounter0=0;
    static int readCounter1=0;
    static int readCounter2=0;

    @Override
    public void handle(Event event) {
        System.out.println("执行（整机）PPS板间同步信号测试...");
        Platform.runLater(() -> {
            taLogs.appendText("开始执行（整机）PPS板间同步信号测试...\n");
        });

        try {
            String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
            String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

            // 启动Vivado进程0
            ProcessBuilder processBuilder0 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder0.redirectErrorStream(true);
            process0 = processBuilder0.start();
            // 启动Vivado进程1
            ProcessBuilder processBuilder1 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder1.redirectErrorStream(true);
            process1 = processBuilder1.start();
            // 启动Vivado进程2
            ProcessBuilder processBuilder2 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder2.redirectErrorStream(true);
            process2 = processBuilder2.start();


        } catch (Exception e) {
        }

        new Thread(()->{
            try {
                reader0 = Thread.currentThread();
                processInput0 = new BufferedReader(new InputStreamReader(process0.getInputStream(), "UTF-8"));
                processError0 = new BufferedReader(new InputStreamReader(process0.getInputStream()));

                while (true) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput0,"reader0",0);
                    String error = readErrorFromProcess(processError0);
                    Platform.runLater(() -> {
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+echo + "\n");
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+error + "\n");
                    });
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
        }).start();
        new Thread(()->{
            try {
                reader1 = Thread.currentThread();
                processInput1 = new BufferedReader(new InputStreamReader(process1.getInputStream(), "UTF-8"));
                processError1 = new BufferedReader(new InputStreamReader(process1.getInputStream()));

                while (true) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput1,"reader1",1);
                    String error = readErrorFromProcess(processError1);
                    Platform.runLater(() -> {
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+echo + "\n");
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+error + "\n");
                    });
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
        }).start();
        new Thread(()->{
            try {
                reader2 = Thread.currentThread();
                processInput2 = new BufferedReader(new InputStreamReader(process2.getInputStream(), "UTF-8"));
                processError2 = new BufferedReader(new InputStreamReader(process2.getInputStream()));

                while (true) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput2,"reader2",2);
                    String error = readErrorFromProcess(processError2);
                    Platform.runLater(() -> {
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+echo + "\n");
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+error + "\n");
                    });
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
        }).start();

        processOutput0 = new BufferedWriter(new OutputStreamWriter(process0.getOutputStream()));
        System.out.println("shell0进程状态：" + process0.isAlive());
        processOutput1 = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream()));
        System.out.println("shell1进程状态：" + process1.isAlive());
        processOutput2 = new BufferedWriter(new OutputStreamWriter(process2.getOutputStream()));
        System.out.println("shell2进程状态：" + process2.isAlive());

        new Thread(()->{
            writerAndTester0=Thread.currentThread();
            try {
                System.out.println("process0初始操作...");
                writeToProcess(processOutput0, 0,"open_hw" + "\n");
                writeToProcess(processOutput0, 0,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput0, "open_hw_target" + "\n");
                writeToProcess(processOutput0, 0,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput0, 0,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput0, 0,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.bit} [get_hw_devices xc7vx690t_0]" + "\n");

                if (btDownload.isSelected()){
                    System.out.println("process0下载...");
                    writeToProcess(processOutput0, 0,"program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    programming0 = true;
                    LockSupport.park();
                }
                cd0.countDown();
                Thread.sleep(10000);
                System.out.println("process0下载后设置...");
                writeToProcess(processOutput0, 0,"refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn/Inst_ila_freq_change_syn\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"TCTM_INST/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_Calib_inst/ila_rec_calib_syntest_inst\"}]]" + "\n");
                Thread.sleep(1000);

                //damodeset设置和pps_delay_ld设置
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE 41 [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {ADandDA_inst/damodeset} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes CNTVALUEOUT -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE 0 [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE 1 [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");

                for(int i=0;i<96;i++){
                    int delay=0;
                    if(i<32){
                        delay=i;
                    }else {
                        delay=10;
                    }
                    writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE "+delay+" [get_hw_probes CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                    writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {CNTVALUEIN} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");

                    System.out.println("process0到达检查点一，等待同步...当前datadelay："+delay);
                    countDownLatchs0[i].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点一被唤起...");  //所有AD线程配置完delay，同步线程pps切内-截图-pps切外-FPGA复位，并唤醒此
                }
            }catch (Exception e){}
        }).start();
        new Thread(()->{
            writerAndTester1=Thread.currentThread();
            try {
                cd0.await();
                System.out.println("process1初始操作...");
                writeToProcess(processOutput1, 1,"open_hw" + "\n");
                writeToProcess(processOutput1, 1,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput1, "open_hw_target" + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput1, 1,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput1, 1,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput1, 1,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.bit} [get_hw_devices xc7vx690t_0]" + "\n");

                if (btDownload.isSelected()){
                    System.out.println("process1下载...");
                    writeToProcess(processOutput1, 1,"program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    programming1 = true;
                    LockSupport.park();
                }
                cd1.countDown();
                Thread.sleep(10000);
                System.out.println("process1下载后设置...");
                writeToProcess(processOutput1, 1,"refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                Thread.sleep(10000);

                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn/Inst_ila_freq_change_syn\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"TCTM_INST/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_Calib_inst/ila_rec_calib_syntest_inst\"}]]" + "\n");
                Thread.sleep(1000);

                //damodeset设置和pps_delay_ld设置
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE 41 [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {ADandDA_inst/damodeset} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes CNTVALUEOUT -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE 0 [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE 1 [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");

                for(int i=0;i<96;i++){
                    int delay=0;
                    if(i<32){
                        delay=10;
                    }else if(i<64){
                        delay=i;
                    }else if(i<96){
                        delay=10;
                    }
                    writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE "+delay+" [get_hw_probes CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                    writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {CNTVALUEIN} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");

                    System.out.println("process1到达检查点一，等待同步...当前datadelay："+delay);
                    countDownLatchs0[i].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点一被唤起...");  //所有AD线程配置完delay，同步线程pps切内-截图-pps切外-FPGA复位，并唤醒此
                }
            }catch (Exception e){}
        }).start();
        new Thread(()->{
            writerAndTester2=Thread.currentThread();
            try {
                cd1.await();
                System.out.println("process2初始操作...");
                writeToProcess(processOutput2, 2,"open_hw" + "\n");
                writeToProcess(processOutput2, 2,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput2, "open_hw_target" + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput2, 2,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput2, 2,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput2, 2,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0311/AD_impl_testDA_ila/TOP_test2.bit} [get_hw_devices xc7vx690t_0]" + "\n");

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
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn/Inst_ila_freq_change_syn\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"TCTM_INST/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_Calib_inst/ila_rec_calib_syntest_inst\"}]]" + "\n");
                Thread.sleep(1000);

                //damodeset设置和pps_delay_ld设置
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE 41 [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {ADandDA_inst/damodeset} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes CNTVALUEOUT -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE 0 [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE 1 [get_hw_probes LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");

                for(int i=0;i<96;i++){
                    int delay=0;
                    if(i<64){
                        delay=10;
                    }else {
                        delay=i;
                    }
                    writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE "+delay+" [get_hw_probes CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");
                    writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {CNTVALUEIN} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_pps_inst\"}]]"+"\n");

                    System.out.println("process2到达检查点一，等待同步...当前datadelay："+delay);
                    countDownLatchs0[i].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点一被唤起...");  //所有AD线程配置完delay，同步线程pps切内-截图-pps切外-FPGA复位，并唤醒此
                }
            }catch (Exception e){}
        }).start();

        new Thread(()->{
            sync=Thread.currentThread();
            try {

                //设置KL14，pps切外
                dbfClient.dbfWrite("EB900E1119AAAAAAAAAAAA");
                Thread.sleep(500);

                for(int i=0;i<96;i++){
                    System.out.println("sync线程进入waiting，等待三个线程到达检查点一...循环编号："+i);
                    countDownLatchs0[i].await();
                    System.out.println("四个线程已到达检查点一，设置KL14，再依次唤起...循环编号："+i);

                    //设置KL5
                    dbfClient.dbfWrite("EB9005111011AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111022AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111033AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111044AAAAAAAAAA");
                    Thread.sleep(500);

                    //设置KL14,pps切内
                    dbfClient.dbfWrite("EB900E111955AAAAAAAAAA");

                    Thread.sleep(500);

                    //示波器截图
                    instru4.writeCmd(":RUN");
                    Thread.sleep(1000); //must
                    instru4.writeCmd(":STOP");
                    Thread.sleep(1000);//must
                    instru4.writeCmd(":SAVE:IMAGe:FORMat BMP");
                    String savCmd=":SAVE:IMAGe '"+i+"'";  //同名自动覆盖
                    instru4.writeCmd(savCmd);
                    instru4.writeCmd("*WAI"); //使用*OPC?回读前需要等待否则会报错query interrupted


                    //设置KL14，pps切外
                    dbfClient.dbfWrite("EB900E1119AAAAAAAAAAAA");
                    Thread.sleep(500);


                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
