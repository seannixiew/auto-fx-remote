package cn.handler.da;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
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

public class ModuleFreqSwitchSynSignalDa implements EventHandler {


    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;
    ToggleButton btDownload=rfTestController.btDownload;
    DbfClient dbfClient=rfTestController.dbfClient;


    private static final CountDownLatch[][] countDownLatchs0=new CountDownLatch[32][3];
    private static final CountDownLatch[][] countDownLatchs1=new CountDownLatch[32][3];
    private static final CountDownLatch[] countDownLatchs2=new CountDownLatch[32];

    static {
        for(int i=0;i<32;i++){
            for(int j=0;j<3;j++) {
                countDownLatchs0[i][j] = new CountDownLatch(4);
            }
        }

        for(int i=0;i<32;i++){
            for(int j=0;j<3;j++) {
                countDownLatchs1[i][j] = new CountDownLatch(4);
            }
        }

        for(int i=0;i<32;i++){
            countDownLatchs2[i] = new CountDownLatch(4);
        }
    }

    //实现串行下载fpga程序
    private static final CountDownLatch cd0=new CountDownLatch(1);
    private static final CountDownLatch cd1=new CountDownLatch(1);
    private static final CountDownLatch cd2=new CountDownLatch(1);

    private static final CountDownLatch cdFirstSendDbf=new CountDownLatch(4);    //控制初次发地检指令时机



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

    static int readCounter0=0;
    static int readCounter1=0;
    static int readCounter2=0;
    static int readCounter3=0;

    @Override
    public void handle(Event event) {
        System.out.println("执行（整机）AD校正同步信号测试...");
        Platform.runLater(() -> {
            taLogs.appendText("开始执行（整机）AD校正同步信号测试...");
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
            // 启动Vivado进程3（合路板）
            ProcessBuilder processBuilder3 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
            processBuilder3.redirectErrorStream(true);
            process3 = processBuilder3.start();

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
        new Thread(()->{
            try {
                reader3 = Thread.currentThread();
                processInput3 = new BufferedReader(new InputStreamReader(process3.getInputStream(), "UTF-8"));
                processError3 = new BufferedReader(new InputStreamReader(process3.getInputStream()));

                while (true) {
                    Thread.sleep(1000);
                    String echo = readFromProcess(processInput3,"reader3",3);
                    String error = readErrorFromProcess(processError3);
                    Platform.runLater(() -> {
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+echo + "\n");
                        taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+error + "\n");
                    });
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
        }).start();

        processOutput0 = new BufferedWriter(new OutputStreamWriter(process0.getOutputStream()));
        System.out.println("shell0进程状态：" + process0.isAlive());
        processOutput1 = new BufferedWriter(new OutputStreamWriter(process1.getOutputStream()));
        System.out.println("shell1进程状态：" + process1.isAlive());
        processOutput2 = new BufferedWriter(new OutputStreamWriter(process2.getOutputStream()));
        System.out.println("shell2进程状态：" + process2.isAlive());
        processOutput3 = new BufferedWriter(new OutputStreamWriter(process3.getOutputStream()));
        System.out.println("shell3进程状态：" + process3.isAlive());

        new Thread(()->{
            writerAndTester0=Thread.currentThread();
            try {
                cd0.await();
                System.out.println("process0初始操作...");
                writeToProcess(processOutput0, 0,"open_hw" + "\n");
                writeToProcess(processOutput0, 0,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput0, "open_hw_target" + "\n");
                writeToProcess(processOutput0, 0,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                writeToProcess(processOutput0, 0,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/000018eaffd701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput0, 0,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput0, 0,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA1/DA_top2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA1/DA_top2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput0, 0,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA1/DA_top2.bit} [get_hw_devices xc7vx690t_0]" + "\n");

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
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"mcu_spi_top_2_inst/ila_spi_line_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1258/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput0, 0,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]" + "\n");
                cdFirstSendDbf.countDown();
                Thread.sleep(1000);
                LockSupport.park();
                System.out.println("process0设置vio_delay_ld及上升沿触发...");

                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE 0 [get_hw_probes Inst_freq_change_syn_DA1/LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA1/LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE 1 [get_hw_probes Inst_freq_change_syn_DA1/LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA1/LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property TRIGGER_COMPARE_VALUE eq1'bR [get_hw_probes Inst_freq_change_syn_DA1/IO_syn_iddr -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                writeToProcess(processOutput0,0,"set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes Inst_freq_change_syn_DA1/CNTVALUEOUT -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes Inst_freq_change_syn_DA1/CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                
                for(int datadelay=0;datadelay<32;datadelay++){
                    writeToProcess(processOutput0,0,"set_property OUTPUT_VALUE "+datadelay+" [get_hw_probes Inst_freq_change_syn_DA1/CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");
                    writeToProcess(processOutput0,0,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA1/CNTVALUEIN} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_vio_syn_DA1\"}]]"+"\n");

                    //第一个小循环
                    System.out.println("process0到达检查点一#0，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点一#0被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput0,0,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    System.out.println("process0到达检查点二#0，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput0,0,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    writeToProcess(processOutput0,0,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    writeToProcess(processOutput0,0, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process0"+"_"+datadelay+"#0"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    File file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process0"+"_"+datadelay+"#0"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process0"+"_"+datadelay+"#0"+".csv");
                            break;
                        }
                    }
                    //第二个小循环
                    System.out.println("process0到达检查点一#1，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点一#1被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput0,0,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    System.out.println("process0到达检查点二#1，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput0,0,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    writeToProcess(processOutput0,0,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    writeToProcess(processOutput0,0, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process0"+"_"+datadelay+"#1"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process0"+"_"+datadelay+"#1"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process0"+"_"+datadelay+"#1"+".csv");
                            break;
                        }
                    }
                    //第三个小循环
                    System.out.println("process0到达检查点一#2，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点一#0被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput0,0,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    System.out.println("process0到达检查点二#2，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput0,0,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    writeToProcess(processOutput0,0,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA1/Inst_ila_freq_change_syn_DA1\"}]"+"\n");
                    writeToProcess(processOutput0,0, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process0"+"_"+datadelay+"#2"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process0"+"_"+datadelay+"#2"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process0"+"_"+datadelay+"#2"+".csv");
                            break;
                        }
                    }
                    //小循环结束
                    System.out.println("process0到达检查点三，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs2[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process0通过检查点三，被唤起...执行下个循环");

                }
            }catch (Exception e){}
        }).start();
        new Thread(()->{
            writerAndTester1=Thread.currentThread();
            try {
                cd1.await();
                System.out.println("process1初始操作...");
                writeToProcess(processOutput1, 1,"open_hw" + "\n");
                writeToProcess(processOutput1, 1,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput1, "open_hw_target" + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput1, 1,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                writeToProcess(processOutput1, 1,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/000018eaffd701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput1, 1,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput1, 1,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA2/DA_top2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA2/DA_top2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput1, 1,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA2/DA_top2.bit} [get_hw_devices xc7vx690t_0]" + "\n");

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
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"mcu_spi_top_2_inst/ila_spi_line_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1258/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput1, 1,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]" + "\n");
                cdFirstSendDbf.countDown();
                Thread.sleep(1000);
                LockSupport.park();
                System.out.println("process1设置vio_delay_ld及上升沿触发...");

                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE 0 [get_hw_probes Inst_freq_change_syn_DA2/LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA2/LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE 1 [get_hw_probes Inst_freq_change_syn_DA2/LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA2/LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property TRIGGER_COMPARE_VALUE eq1'bR [get_hw_probes Inst_freq_change_syn_DA2/IO_syn_iddr -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                writeToProcess(processOutput1,1,"set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes Inst_freq_change_syn_DA2/CNTVALUEOUT -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes Inst_freq_change_syn_DA2/CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                
                for(int datadelay=0;datadelay<32;datadelay++){
                    writeToProcess(processOutput1,1,"set_property OUTPUT_VALUE "+datadelay+" [get_hw_probes Inst_freq_change_syn_DA2/CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");
                    writeToProcess(processOutput1,1,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA2/CNTVALUEIN} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_vio_syn_DA2\"}]]"+"\n");

                    //第一个小循环
                    System.out.println("process1到达检查点一#0，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点一#0被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput1,1,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    System.out.println("process1到达检查点二#0，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点二#0，被合路板唤起...");

                    writeToProcess(processOutput1,1,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    writeToProcess(processOutput1,1,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    writeToProcess(processOutput1,1, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process1"+"_"+datadelay+"#0"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    File file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process1"+"_"+datadelay+"#0"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process1"+"_"+datadelay+"#0"+".csv");
                            break;
                        }
                    }
                    //第二个小循环
                    System.out.println("process1到达检查点一#1，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点一#1被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput1,1,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    System.out.println("process1到达检查点二#1，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点二#1，被合路板唤起...");

                    writeToProcess(processOutput1,1,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    writeToProcess(processOutput1,1,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    writeToProcess(processOutput1,1, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process1"+"_"+datadelay+"#1"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process1"+"_"+datadelay+"#1"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process1"+"_"+datadelay+"#1"+".csv");
                            break;
                        }
                    }
                    //第三个小循环
                    System.out.println("process1到达检查点一#2，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点一#0被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput1,1,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    System.out.println("process1到达检查点二#2，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput1,1,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    writeToProcess(processOutput1,1,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA2/Inst_ila_freq_change_syn_DA2\"}]"+"\n");
                    writeToProcess(processOutput1,1, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process1"+"_"+datadelay+"#2"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process1"+"_"+datadelay+"#2"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process1"+"_"+datadelay+"#2"+".csv");
                            break;
                        }
                    }
                    //小循环结束
                    System.out.println("process1到达检查点三，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs2[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process1通过检查点三，被唤起...执行下个循环");

                }
            }catch (Exception e){}
        }).start();
        new Thread(()->{
            writerAndTester2=Thread.currentThread();
            try {
                cd2.await();
                System.out.println("process2初始操作...");
                writeToProcess(processOutput2, 2,"open_hw" + "\n");
                writeToProcess(processOutput2, 2,"connect_hw_server" + "\n");
                Thread.sleep(1000);
//                writeToProcess(processOutput2, "open_hw_target" + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput2, 2,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                writeToProcess(processOutput2, 2,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/000018eaffd701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput2, 2,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                writeToProcess(processOutput2, 2,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA3/DA_top2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA3/DA_top2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput2, 2,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0315/impl_freq_syn_added_DA3/DA_top2.bit} [get_hw_devices xc7vx690t_0]" + "\n");

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
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"mcu_spi_top_2_inst/ila_spi_line_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1258/ila_tctm_inst\"}]]" + "\n");
                writeToProcess(processOutput2, 2,"display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_source_inst/ILA_trans_source_syntest_inst\"}]]" + "\n");

                cdFirstSendDbf.countDown();
                Thread.sleep(1000);
                LockSupport.park();
                System.out.println("process2设置vio_delay_ld及上升沿触发...");

                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE 0 [get_hw_probes Inst_freq_change_syn_DA3/LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");
                writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA3/LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE 1 [get_hw_probes Inst_freq_change_syn_DA3/LD -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");
                writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA3/LD} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property TRIGGER_COMPARE_VALUE eq1'bR [get_hw_probes Inst_freq_change_syn_DA3/IO_syn_iddr -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property CONTROL.TRIGGER_POSITION 100 [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                writeToProcess(processOutput2,2,"set_property INPUT_VALUE_RADIX UNSIGNED [get_hw_probes Inst_freq_change_syn_DA3/CNTVALUEOUT -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");
                writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes Inst_freq_change_syn_DA3/CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");

                for(int datadelay=0;datadelay<32;datadelay++){
                    writeToProcess(processOutput2,2,"set_property OUTPUT_VALUE "+datadelay+" [get_hw_probes Inst_freq_change_syn_DA3/CNTVALUEIN -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");
                    writeToProcess(processOutput2,2,"commit_hw_vio [get_hw_probes {Inst_freq_change_syn_DA3/CNTVALUEIN} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_vio_syn_DA3\"}]]"+"\n");

                    //第一个小循环
                    System.out.println("process2到达检查点一#0，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点一#0被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput2,2,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    System.out.println("process2到达检查点二#0，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput2,2,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    writeToProcess(processOutput2,2,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    writeToProcess(processOutput2,2, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process2"+"_"+datadelay+"#0"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    File file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process2"+"_"+datadelay+"#0"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process2"+"_"+datadelay+"#0"+".csv");
                            break;
                        }
                    }
                    //第二个小循环
                    System.out.println("process2到达检查点一#1，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点一#1被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput2,2,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    System.out.println("process2到达检查点二#1，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput2,2,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    writeToProcess(processOutput2,2,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    writeToProcess(processOutput2,2, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process2"+"_"+datadelay+"#1"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process2"+"_"+datadelay+"#1"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process2"+"_"+datadelay+"#1"+".csv");
                            break;
                        }
                    }
                    //第三个小循环
                    System.out.println("process2到达检查点一#2，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点一#0被唤起...");  //所有AD线程采数完成，并且合路线程完成delay设置，并且同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput2,2,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    System.out.println("process2到达检查点二#2，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点二，被合路板唤起...");

                    writeToProcess(processOutput2,2,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    writeToProcess(processOutput2,2,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn_DA3/Inst_ila_freq_change_syn_DA3\"}]"+"\n");
                    writeToProcess(processOutput2,2, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process2"+"_"+datadelay+"#2"+".csv} hw_ila_data_4" + "\n");
//                    writeToProcess(processOutput0,"start_gui");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process2"+"_"+datadelay+"#2"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process2"+"_"+datadelay+"#2"+".csv");
                            break;
                        }
                    }
                    //小循环结束
                    System.out.println("process2到达检查点三，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs2[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process2通过检查点三，被唤起...执行下个循环");

                }
            }catch (Exception e){}
        }).start();
        new Thread(()->{
            writerAndTester3=Thread.currentThread();
            try {
                System.out.println("process3初始操作...");
                writeToProcess(processOutput3, 3,"open_hw" + "\n");
                writeToProcess(processOutput3, 3,"connect_hw_server" + "\n");
                Thread.sleep(1000);
                writeToProcess(processOutput3, 3,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658006c001}" + "\n");
                writeToProcess(processOutput3, 3,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/80806580073c01}" + "\n");
                writeToProcess(processOutput3, 3,"close_hw_target {localhost:3121/xilinx_tcf/Xilinx/8080658007c701}" + "\n");
                writeToProcess(processOutput3, 3,"open_hw_target {localhost:3121/xilinx_tcf/Xilinx/000018eaffd701}" + "\n");
                Thread.sleep(10000);
                writeToProcess(processOutput3, 3,"current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput3, 3,"refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                Thread.sleep(10000);
                writeToProcess(processOutput3, 3,"set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/HL_impl_freq_syn_added/TOP_HL_SELF.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput3, 3,"set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0315/HL_impl_freq_syn_added/TOP_HL_SELF.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                writeToProcess(processOutput3, 3,"set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0315/HL_impl_freq_syn_added/TOP_HL_SELF.bit} [get_hw_devices xc7vx690t_0]" + "\n");

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
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_Calib_result_test/your_instance_name\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_rec_freq_change_syn/Inst_ila_rec_freq_change_syn\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/beam_out_duc_inst/uut_yd_duc\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/uut_54fxt\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/uut_bsfp\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"UUT_HL_YD/uut_fxt_ad/ila_jzAD_ddc_inst\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_8 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"dut_539_para/uut_para539\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_9 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"dut_539_para/uut_para_self\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_10 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"inst_top_gth/uut_ila_gth539\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_11 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"inst_top_gth/uut_ila_gth54\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_12 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"nolabel_line1178/uut_yb_duc\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_13 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_source_inst/ila_calibsource_inst\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_14 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"trans_Calib_test_inst/ILA_trans_Calib_syntest_inst\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_15 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_adda/ila_addata_inst\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_16 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_gth_link\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_17 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/nolabel_line79/ila_jzAD_ddc_inst\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_18 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/uut_ila_FenLu_YB\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_19 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/uut_ila_yd_out\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_20 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_helu_yb/uut_yb_ila\"}]]"+"\n");
                writeToProcess(processOutput3,3, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_21 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_ila_539add\"}]]"+"\n");
                cdFirstSendDbf.countDown();
                Thread.sleep(1000);
                LockSupport.park();
                System.out.println("process3设置vio_delay_ld及上升沿触发...");
                writeToProcess(processOutput3,3, "set_property TRIGGER_COMPARE_VALUE eq1'bR [get_hw_probes Inst_trans_freq_change_syn/IO_syn -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]]"+"\n");
                writeToProcess(processOutput3,3, "commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");


                for(int datadelay=0;datadelay<32;datadelay++){
                    //第一次小循环
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE 0 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");

                    System.out.println("process3到达检查点一#0，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点一#0被唤起...");  //所有AD及合路线程采数完成，并且合路线程完成delay设置=>同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput3,3,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    System.out.println("process3到达检查点二#0，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][0].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点二#0，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    writeToProcess(processOutput3,3,"set_property OUTPUT_VALUE 1 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");
                    writeToProcess(processOutput3,3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");
                    Thread.sleep(3000); //触发余量
                    //唤醒DA
                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);

                    writeToProcess(processOutput3,3,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    writeToProcess(processOutput3,3,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    writeToProcess(processOutput3,3, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process3"+"_"+datadelay+"#0"+".csv} hw_ila_data_3" + "\n");
                    File file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process3"+"_"+datadelay+"#0"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process3"+"_"+datadelay+"#0"+".csv");
                            break;
                        }
                    }
                    //第二次小循环
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE 0 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");

                    System.out.println("process3到达检查点一#1，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点一#1被唤起...");  //所有AD及合路线程采数完成，并且合路线程完成delay设置=>同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput3,3,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    System.out.println("process3到达检查点二#1，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][1].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点二#1，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    writeToProcess(processOutput3,3,"set_property OUTPUT_VALUE 1 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");
                    writeToProcess(processOutput3,3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");
                    Thread.sleep(3000); //触发余量
                    //唤醒DA
                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);

                    writeToProcess(processOutput3,3,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    writeToProcess(processOutput3,3,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    writeToProcess(processOutput3,3, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process3"+"_"+datadelay+"#1"+".csv} hw_ila_data_3" + "\n");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process3"+"_"+datadelay+"#1"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process3"+"_"+datadelay+"#1"+".csv");
                            break;
                        }
                    }
                    //第三次小循环
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE 0 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");

                    System.out.println("process3到达检查点一#2，等待同步...当前datadelay："+datadelay);
                    countDownLatchs0[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点一#2被唤起...");  //所有AD及合路线程采数完成，并且合路线程完成delay设置=>同步线程pps切外，并唤醒此

                    //开始等待触发
                    writeToProcess(processOutput3,3,"run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    System.out.println("process3到达检查点二#2，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs1[datadelay][2].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点二#2，被唤起..."); //所有线程等待触发，同步线程激活触发条件，并唤醒此
                    writeToProcess(processOutput3,3,"set_property OUTPUT_VALUE 1 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");
                    writeToProcess(processOutput3,3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]"+"\n");
                    Thread.sleep(3000); //触发余量
                    //唤醒DA
                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);

                    writeToProcess(processOutput3,3,"wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    writeToProcess(processOutput3,3,"upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_trans_freq_change_syn/Inst_ila_trans_freq_change_syn\"}]"+"\n");
                    writeToProcess(processOutput3,3, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"
                            +"process3"+"_"+datadelay+"#2"+".csv} hw_ila_data_3" + "\n");
                    file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process3"+"_"+datadelay+"#2"+".csv");
                    while (true){
                        if (file.exists()){
                            System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\0311\\0caishu\\"+"process3"+"_"+datadelay+"#2"+".csv");
                            break;
                        }
                    }
                    //小循环结束
                    writeToProcess(processOutput3, 3,"set_property OUTPUT_VALUE 0 [get_hw_probes trans_freq_change_flag -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");
                    writeToProcess(processOutput3, 3,"commit_hw_vio [get_hw_probes {trans_freq_change_flag} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_flag\"}]]" + "\n");
                    System.out.println("process3到达检查点三，等待同步触发...当前datadelay："+datadelay);
                    countDownLatchs2[datadelay].countDown();
                    LockSupport.park();
                    System.out.println("process3通过检查点三，被唤起...执行下个循环"); 
                }
            }catch (Exception e){}
        }).start();


        new Thread(()->{
            sync=Thread.currentThread();
            try {
                //4块板子下完程序（执行完display xxx），执行此处
                cdFirstSendDbf.await();
                //设置KL14，pps切外
                dbfClient.dbfWrite("EB900E1119AAAAAAAAAAAA");
                //设置KL5
                dbfClient.dbfWrite("EB9005111055AAAAAAAAAA");
                Thread.sleep(500);
                dbfClient.dbfWrite("EB9005111066AAAAAAAAAA");
                Thread.sleep(500);
                dbfClient.dbfWrite("EB9005111077AAAAAAAAAA");
                Thread.sleep(500);
                dbfClient.dbfWrite("EB9005111044AAAAAAAAAA");
                Thread.sleep(500);

                LockSupport.unpark(writerAndTester0);
                LockSupport.unpark(writerAndTester1);
                LockSupport.unpark(writerAndTester2);
                LockSupport.unpark(writerAndTester3);

                for(int i=0;i<32;i++){
                    //第一个小循环
                    System.out.println("sync线程进入waiting，等待四个线程到达检查点一#0...循环编号："+i);
                    countDownLatchs0[i][0].await();
                    System.out.println("四个线程已到达检查点一#0，依次唤起...循环编号："+i);

                    //KL14 pps切内，3个小循环只发第一次
                    dbfClient.dbfWrite("EB900E111955AAAAAAAAAA");
                    Thread.sleep(500);

                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                    LockSupport.unpark(writerAndTester3);

                    System.out.println("syn线程进入waiting，等待三个线程到达检查点二#0，循环编号："+i);
                    countDownLatchs1[i][0].await();
                    System.out.println("四个线程已到达检查点二，单独唤起合路板，再由合路板唤起DA板...循环编号："+i);
                    LockSupport.unpark(writerAndTester3);
                    //第二个小循环
                    System.out.println("sync线程进入waiting，等待四个线程到达检查点一#1...循环编号："+i);
                    countDownLatchs0[i][1].await();
                    System.out.println("四个线程已到达检查点一#1，依次唤起...循环编号："+i);

                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                    LockSupport.unpark(writerAndTester3);

                    System.out.println("syn线程进入waiting，等待三个线程到达检查点二#1，循环编号："+i);
                    countDownLatchs1[i][1].await();
                    System.out.println("四个线程已到达检查点二，单独唤起合路板，再由合路板唤起DA板...循环编号："+i);
                    LockSupport.unpark(writerAndTester3);
                    //第三个小循环
                    System.out.println("sync线程进入waiting，等待四个线程到达检查点一#2...循环编号："+i);
                    countDownLatchs0[i][2].await();
                    System.out.println("四个线程已到达检查点一#2，依次唤起...循环编号："+i);

                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                    LockSupport.unpark(writerAndTester3);

                    System.out.println("syn线程进入waiting，等待三个线程到达检查点二#2，循环编号："+i);
                    countDownLatchs1[i][2].await();
                    System.out.println("四个线程已到达检查点二，单独唤起合路板，再由合路板唤起DA板...循环编号："+i);
                    LockSupport.unpark(writerAndTester3);
                    //小循环结束

                    System.out.println("syn线程进入waiting，等待三个线程到达检查点三，循环编号："+i);
                    countDownLatchs2[i].await();
                    System.out.println("四个线程已到达检查点三，pps切外，复位，全部唤起...循环编号："+i);
                    
                    //设置KL14,pps切外
                    dbfClient.dbfWrite("EB900E1119AAAAAAAAAAAA");
                    Thread.sleep(200);
                    //设置KL5
                    dbfClient.dbfWrite("EB9005111055AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111066AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111077AAAAAAAAAA");
                    Thread.sleep(500);
                    dbfClient.dbfWrite("EB9005111044AAAAAAAAAA");
                    Thread.sleep(500);

                    LockSupport.unpark(writerAndTester0);
                    LockSupport.unpark(writerAndTester1);
                    LockSupport.unpark(writerAndTester2);
                    LockSupport.unpark(writerAndTester3);
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