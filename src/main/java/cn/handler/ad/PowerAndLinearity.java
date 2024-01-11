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

import java.io.*;
import java.util.Date;
import java.util.concurrent.locks.LockSupport;

public class PowerAndLinearity implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    InstrumentClient instru0=rfTestController.instru0;  //信号源
    InstrumentClient instru2=rfTestController.instru1;  //网分
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;

    Process process;
    Thread reader;
    Thread writer;
    BufferedWriter processOutput;
    Thread currThread;

    boolean programming;
    boolean sampling;



    @Override
    public void handle(Event event) {
//        if(!(instru0.isConnected && instru2.isConnected)){
//            System.out.println("请检查仪表连接！");
//            return;
//        }
        if (true || InstruType.SMW200A.equals(instru0.instruType) && InstruType.ZNB.equals(instru2.instruType)) {
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
                    BufferedReader processInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    BufferedReader processError = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while (true) {
                        Thread.sleep(1000);
                        String echo = readFromProcess(processInput);
                        String error = readFromProcess(processError);
                        Platform.runLater(() -> {
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+echo + "\n");
                            taLogs.appendText(DateFormat.FORLOGSHORT.format(new Date())+error + "\n");
                        });
                        String s = echo;
                        if (programming) {
                            if (s.contains("End of startup status: HIGH")) {
                                programming = false;
                                LockSupport.unpark(writer);
                            }
                        }
                        if (sampling) {
                            if (s.contains("ILA Waveform data saved to file")) {
                                sampling = false;
                                LockSupport.unpark(writer);
                            }
                        }
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
                writer = Thread.currentThread();
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
                    writeToProcess(processOutput, "program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                    //display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~"ADandDA_inst/ila_addata_inst"}]]

                    //...
                    programming = true;
                    LockSupport.park();
                    writeToProcess(processOutput, "refresh_hw_device [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");

                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_1 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/AD_SPI_inst/ila_spi_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_2 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/DA_SPI_INST/ila_daspi_INST\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_3 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"TCTM_INST/ila_tctm_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_Calib_inst/ila_rec_calib_simp_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_beam_gth/dut_54/ila_54beam_inst\"}]]" + "\n");
                    Thread.sleep(1000);
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_7 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_beam_gth/ila_dds_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_8 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"uut_beam_gth/uut_para539/uut_ila_5\"}]]" + "\n");
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
                    writeToProcess(processOutput, "run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}] -trigger_now" + "\n");
                    writeToProcess(processOutput, "wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]]\n" + "\n");
                    sampling = true;
                    LockSupport.park();

                    writeToProcess(processOutput, "write_hw_ila_data -csv_file {E:\\wx\\1_auto-test\\about vivado\\bit_file_from_yangjiashuo\\0111\\iladata6.csv} hw_ila_data_3" + "\n");
                    Thread.sleep(2000); //存文件时间

                    instru0.writeCmd("*RST;*CLS");
                    instru0.writeCmd("*WAI");
                    instru0.writeCmd("SOURce1:FREQuency:MODE CW");

                    for(ValueCollection.FreqAndPower set :ValueCollection.vsgList){
                        String freq=set.getFreq();
                        String power=set.getPower();
                        instru0.writeCmd("SOURce1:FREQuency:CW "+freq);
                        instru0.writeCmd("SOURce1:POWer:POWer " + power);
                        Thread.sleep(500);
                        //采数
                        sampling=true;
                        LockSupport.park();
                        writeToProcess(processOutput, "write_hw_ila_data -csv_file {E:\\wx\\1_auto-test\\about vivado\\bit_file_from_yangjiashuo\\0111\\"
                                +freq+"_"+power+"_"+"iladata.csv} hw_ila_data_3" + "\n");
                    }


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
        Thread.sleep(500);
    }

    private static String readFromProcess(BufferedReader processInput) throws IOException {
        char[] bt = new char[1024];
        processInput.read(bt);
        String s=String.valueOf(bt);
        s=s.replaceAll("\\u0000"," ");
        System.out.println(s);
        return s;
    }
}
