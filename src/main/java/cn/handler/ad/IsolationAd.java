package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.model.InstruType;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.LockSupport;

public class IsolationAd implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    TextArea aChannelsTypeIn=rfTestController.popupControllerA.taChannels;


    InstrumentClient instru0=rfTestController.instru0;  //信号源
    MatrixClient matrix0= rfTestController.matrix0;  //矩阵X
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;
    ToggleButton btDownload=rfTestController.btDownload;

    Process process;
    public BufferedWriter processOutput;
    BufferedReader processInput;
    BufferedReader processError;
    Thread reader;
    public Thread writerAndTester;

    boolean programming;
    //    boolean sampling;
    static int readCounter=0;
    Properties properties = new Properties();


    @Override
    public void handle(Event event) {

        try {
            properties.load(new FileInputStream("src/main/resources/configs/config.properties"));
        }catch (Exception e){}



            System.out.println("执行AD隔离度测试...");
            Platform.runLater(() -> {
                taLogs.appendText("开始执行AD隔离度测试...");
            });
            try {
                String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
                String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

                // 启动Vivado进程
                ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
                processBuilder.redirectErrorStream(true);
                process = processBuilder.start();

            } catch (Exception e) {}

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
                    }

                } catch (Exception e) {}
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
                    Thread.sleep(5000);
                    writeToProcess(processOutput, "set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0325/AD_impl_pps_r_added/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/0325/AD_impl_pps_r_added/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/0325/AD_impl_pps_r_added/TOP_test2.bit} [get_hw_devices xc7vx690t_0]" + "\n");
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
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_4 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"Inst_freq_change_syn/Inst_ila_freq_change_syn\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_5 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"TCTM_INST/ila_tctm_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "display_hw_ila_data [ get_hw_ila_data hw_ila_data_6 -of_objects [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"rec_Calib_inst/ila_rec_calib_syntest_inst\"}]]" + "\n");
                    Thread.sleep(1000);

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

                    writeToProcess(processOutput, "set_property OUTPUT_VALUE_RADIX UNSIGNED [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 0 [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {ADandDA_inst/damodeset} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");

                    //配置信号源
                    if (InstruType.SMW200A.equals(instru0.instruType)) {
                        instru0.writeCmd(":OUTPut1 OFF");
                        instru0.writeCmd("SOURce:POWer:MODE CW");
                        instru0.writeCmd("SOURce1:FREQuency:CW 132.5MHz");
                        String power = properties.getProperty("vsgPower");
                        instru0.writeCmd("SOURce1:POWer:POWer " + power + "dBm");//信号源初始功率
                        instru0.writeCmd(":OUTPut1 ON");

                        String s = aChannelsTypeIn.getText().trim();
                        String[] channels = s.split("\\s+");
                        for (String channel : channels) {
                            System.out.println("矩阵当前开启通道" + channel);
                            List<String> cmdChannel = Arrays.asList(channel);
                            matrix0.channelSwitch(cmdChannel);
                            Thread.sleep(100);

                            //采数
                            writeToProcess(processOutput, "run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}] -trigger_now" + "\n");
                            writeToProcess(processOutput, "wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");
                            writeToProcess(processOutput, "upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");

                            System.out.println("开始存文件...");
                            writeToProcess(processOutput, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\caishu\\0326\\ch_"
                                    + channel + ".csv} hw_ila_data_3" + "\n");
                            File file = new File("E:\\wx\\2_projects\\caishu\\0326\\ch_" + channel + ".csv");
                            while (true) {
                                if (file.exists()) {
                                    System.out.println("已生成文件" + "E:\\wx\\2_projects\\caishu\\0326\\ch_" + channel + ".csv");
                                    break;
                                }
                            }
                        }
                        instru0.writeCmd(":OUTPut1 OFF");

                    }else if(InstruType.E8267D.equals(instru0.instruType)){
                        instru0.writeCmd(":OUTPut OFF");
                        instru0.writeCmd(":FREQuency 132.5MHz");
                        String power = properties.getProperty("vsgPower");
                        instru0.writeCmd("SOURce:POWer:LEVel " + power);  //keysight
                        instru0.writeCmd(":OUTPut ON"); //keysight
                        instru0.writeCmd(":OUTPut:MOD:STAT OFF");// keysight

                        String s = aChannelsTypeIn.getText().trim();
                        String[] channels = s.split("\\s+");
                        for (String channel : channels) {
                            System.out.println("矩阵当前开启通道" + channel);
                            List<String> cmdChannel = Arrays.asList(channel);
                            matrix0.channelSwitch(cmdChannel);
                            Thread.sleep(100);

                            //采数
                            writeToProcess(processOutput, "run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}] -trigger_now" + "\n");
                            writeToProcess(processOutput, "wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");
                            writeToProcess(processOutput, "upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");

                            System.out.println("开始存文件...");
                            writeToProcess(processOutput, "write_hw_ila_data -csv_file {E:\\wx\\2_projects\\caishu\\0326\\ch_"
                                    + channel + ".csv} hw_ila_data_3" + "\n");
                            File file = new File("E:\\wx\\2_projects\\caishu\\0326\\ch_" + channel + ".csv");
                            while (true) {
                                if (file.exists()) {
                                    System.out.println("已生成文件" + "E:\\wx\\2_projects\\caishu\\0326\\ch_" + channel + ".csv");
                                    break;
                                }
                            }
                        }
                        instru0.writeCmd(":OUTPut OFF");
                    }
                    System.out.println("测试结束");

                } catch (Exception e) {
                }
            }).start();


    }


    private  void writeToProcess(BufferedWriter processOutput, String command) throws IOException, InterruptedException {
        processOutput.write(command);
        processOutput.flush();
        String s= DateFormat.FORLOGSHORT.format(new Date())+"send："+command;
        Platform.runLater(()->{
            taLogs.appendText(s);
        });
        System.out.println(s);
        Thread.sleep(1000);
    }

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
