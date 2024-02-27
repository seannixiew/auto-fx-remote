package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.InstrumentClient;
import cn.model.InstruType;
import cn.model.ValueCollection;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

import java.io.*;
import java.util.Date;
import java.util.concurrent.locks.LockSupport;

public class PowerAndLinearity implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    InstrumentClient instru0=rfTestController.instru0;  //信号源
    InstrumentClient instru2=rfTestController.instru2;  //网分
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;
    ToggleButton btDownload=rfTestController.btDownload;

    Process process;
    public BufferedWriter processOutput;
    BufferedReader processInput;
    BufferedReader processError;
    Thread reader;
    Thread writerAndTester;


    boolean programming;
//    boolean sampling;
    static int readCounter=0;




    @Override
    public void handle(Event event) {
//        if(!(instru0.isConnected && instru2.isConnected)){
//            System.out.println("请检查仪表连接！");
//            return;
//        }
        if (true || InstruType.SMW200A.equals(instru0.instruType) && InstruType.ZNB.equals(instru2.instruType)) {  // TODO: 2024/1/12 被旁路
            System.out.println("执行ad线性度测试...");
            Platform.runLater(() -> {
                taLogs.appendText("开始执行ad线性度测试...");
            });
            try {
                String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
                String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

                // 启动Vivado进程
                ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
//            ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath);
                processBuilder.redirectErrorStream(true);
                process = processBuilder.start();

            } catch (Exception e) {
            }

            new Thread(() -> {
                try {
                    reader = Thread.currentThread();
                    processInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    processError = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while (true) {
                        Thread.sleep(1000);
                        String echo = readFromProcess(processInput);
                        String error = readErrorFromProcess(processError);
                        Platform.runLater(() -> {
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+echo + "\n");
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+error + "\n");
                        });
                        String s = echo;
                        if (programming) {
                            if (s.contains("End of startup status: HIGH")) {
                                programming = false;
                                LockSupport.unpark(writerAndTester);
                                System.out.println("下载完毕，唤醒测试Thread...");
                            }
                        }
//                        if (sampling) {
//                            System.out.println("采数中回读："+s);
//                            if (s.contains("ILA Waveform data saved to file")) {
//                                sampling = false;
//                                LockSupport.unpark(writerAndTester);
//                                System.out.println("采数完毕，唤醒测试Thread...");
//                                System.out.println("sampling="+sampling);
//                            }
//                        }
                        //存文件无回显，固定等待或独立起线程
//                        if (saving) {
//                            if (s.contains("E:/wx/1_auto-test/about vivado")) {
//                                saving = false;
//                                LockSupport.unpark(writer);
//                            }
//                        }
                    }

                } catch (Exception e) {
                }
            }).start();

            processOutput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            System.out.println("当前shell进程状态：" + process.isAlive());

            new Thread(() -> {
                writerAndTester = Thread.currentThread();
                try {
                    writeToProcess(processOutput, "open_hw" + "\n");
                    writeToProcess(processOutput, "connect_hw_server" + "\n");
                    Thread.sleep(3000);
                    writeToProcess(processOutput, "open_hw_target" + "\n");
                    writeToProcess(processOutput, "current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                    writeToProcess(processOutput, "set_property PROBES.FILE {E:/wx/1_auto-test/about vivado/bit_file_from_yangjiashuo/0111/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property FULL_PROBES.FILE {E:/wx/1_auto-test/about vivado/bit_file_from_yangjiashuo/0111/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property PROGRAM.FILE {E:/wx/1_auto-test/about vivado/bit_file_from_yangjiashuo/0111/TOP_test2.bit} [get_hw_devices xc7vx690t_0]" + "\n");
                    if(btDownload.isSelected()) {
                        writeToProcess(processOutput, "program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                        //display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~"ADandDA_inst/ila_addata_inst"}]]

                        //考虑：在reader线程中增加readerWait标志，if(readerWait){Locksupport.park();}
                        // 测试线程下载程序时，readerWait设置为true，下载完毕后，将readerWait设置为false，然后LockSupport.unpark(readerThread)
                        programming = true;
                        LockSupport.park();
                    }
                    Thread.sleep(10000);
                    writeToProcess(processOutput, "refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                    Thread.sleep(10000);

                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"TCTM_INST/ila_tctm_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_Calib_inst/ila_rec_calib_simp_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_beam_gth/dut_54/ila_54beam_inst\"}]]" + "\n");
                    Thread.sleep(1000);
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_beam_gth/ila_dds_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_8 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_beam_gth/uut_para539/uut_ila_5\"}]]" + "\n");

                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 0 [get_hw_probes pps_t_ctrl -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {pps_t_ctrl} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 1 [get_hw_probes pps_t_ctrl -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {pps_t_ctrl} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 1 [get_hw_probes rst_in_t -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {rst_in_t} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 0 [get_hw_probes rst_in_t -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {rst_in_t} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 1 [get_hw_probes rst_in_t -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {rst_in_t} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 1 [get_hw_probes pps_t -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {pps_t} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 0 [get_hw_probes pps_t -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {pps_t} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"vio_rstctl_inst\"}]]" + "\n");

//                    sampling = true;
//                    LockSupport.park();
//                    Thread.sleep(7000);
//                    writeToProcess(processOutput, "write_hw_ila_data -csv_file {E:\\wx\\1_auto-test\\about vivado\\bit_file_from_yangjiashuo\\0111\\firstSample.csv} hw_ila_data_3" + "\n");
//                    Thread.sleep(3000); //存文件时间

//                    instru0.writeCmd("*RST");
//                    instru0.writeCmd("*CLS");
//                    instru0.writeCmd("*WAI");
//                    instru0.writeCmd("SOURce1:FREQuency:MODE CW");

                    System.out.println("待遍历set："+ValueCollection.vsgList);

                    for(ValueCollection.FreqAndPower set :ValueCollection.vsgList){
                        String freq=set.getFreq();
                        String power=set.getPower();
                        /***********************************************************************************************/
                        instru0.writeCmd("SOURce1:FREQuency:CW "+freq);   //R&S
//                        instru0.writeCmd(":FREQuency "+freq);   //keysight
                        System.out.println("当前频点："+freq);
                        instru0.writeCmd("SOURce1:POWer:POWer " + power);  //R&S
//                        instru0.writeCmd("SOURce:POWer:LEVel " + power);  //keysight
                        System.out.println("当前功率："+power);
                        instru0.writeCmd(":OUTPut1 ON"); //R&S
//                        instru0.writeCmd(":OUTPut ON"); //keysight
                        Thread.sleep(500);
                        /***********************************************************************************************/
                        //采数
                        writeToProcess(processOutput, "run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}] -trigger_now" + "\n");
                        writeToProcess(processOutput, "wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");
//                        writeToProcess(processOutput, "display_hw_ila_data [upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                        writeToProcess(processOutput, "upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");

//                        sampling=true;

                        // TODO: 2024/1/12 还是要增加标志位，时间长
//                        System.out.println("等待采数...");
//                        LockSupport.park();
//                        Thread.sleep(7000);
                        System.out.println("开始存文件...");
                        writeToProcess(processOutput, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\L payload\\vivado files\\AD_impl_Calib_syn_test\\"
                                +freq+"_"+power+".csv} hw_ila_data_3" + "\n");
                        File file=new File("E:\\wx\\2_projects\\L payload\\vivado files\\AD_impl_Calib_syn_test\\"+freq+"_"+power+".csv");
                        while (true){
                            if (file.exists()){
                                System.out.println("已生成文件"+"E:\\wx\\2_projects\\L payload\\vivado files\\AD_impl_Calib_syn_test\\\\"+freq+"_"+power+".csv");
                                break;
                            }
                        }
                    }
                    instru0.writeCmd(":OUTPut1 OFF");
                    System.out.println("测试结束");

                } catch (Exception e) {
                }

            }).start();
        }
    }

    private  void writeToProcess(BufferedWriter processOutput, String command) throws IOException, InterruptedException {
        processOutput.write(command);
        processOutput.flush();
        String s=DateFormat.FORLOGSHORT.format(new Date())+"send："+command;
        Platform.runLater(()->{
            taLogs.appendText(s);
        });
        System.out.println(s);
        Thread.sleep(1000);
    }

    /** 说明：AD线性度测试可用。 */
//    private static String readFromProcess(BufferedReader processInput) throws IOException {
//        char[] bt = new char[1024];
//        processInput.read(bt);
//        String s=String.valueOf(bt);
//        s=s.replaceAll("\\u0000"," ");
//        System.out.println(s);
//        return s;
//    }

    private static String readFromProcess(BufferedReader processInput) throws IOException {

        String s="read pending...";
        readCounter++;
        if(processInput.ready()) {
            char[] bt = new char[1024];
            processInput.read(bt);
            s = String.valueOf(bt);
            s = s.replaceAll("\\u0000", " ");
            readCounter=0;
        }
        System.out.println(s+" "+readCounter);
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
