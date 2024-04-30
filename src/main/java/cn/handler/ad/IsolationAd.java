package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.model.InstruType;
import cn.utils.*;
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
    List<String> offeredChannels0=rfTestController.popupControllerA.offeredChannels0;


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

    boolean readerRunning0=true;

    boolean programming;
    static int readCounter=0;
    Properties propertiesConfig = new Properties();
    Properties properties=new Properties();

    public IsolationAd(){
        readCounter=0;
    }


    @Override
    public void handle(Event event) {

        try {
            propertiesConfig.load(new FileInputStream("src/main/resources/configs/config.properties"));
            properties.load(new FileInputStream("src/main/resources/configs/vivado.properties"));
        }catch (Exception e){}



            System.out.println("执行AD隔离度测试...");
            Platform.runLater(() -> {
                taLogs.appendText("开始执行AD隔离度测试...\n");
            });
            try {
                String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
                String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

                // 启动Vivado进程
                ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
                processBuilder.redirectErrorStream(true);
                process = processBuilder.start();
                ThreadAndProcessPools.addProcess(process);

                Thread.sleep(3000);

            } catch (Exception e) {}

            reader=new Thread(() -> {
                try {
//                    reader = Thread.currentThread();
                    processInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    processError = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while (readerRunning0) {
                        Thread.sleep(1000);
                        String echo = readFromProcess(processInput);
                        String error = readErrorFromProcess(processError);
                        if(echo != null && !echo.contains("read pending...")) {
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
                        if (programming) {
                            if (s.contains("End of startup status: HIGH")) {
                                programming = false;
                                LockSupport.unpark(writerAndTester);
                                System.out.println("下载完毕，唤醒测试Thread...");
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            reader.start();
            ThreadAndProcessPools.addThread(reader);

            processOutput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            System.out.println("当前shell进程状态：" + process.isAlive());


        writerAndTester=new Thread(() -> {
//                writerAndTester = Thread.currentThread();

                try {
                    writeToProcess(processOutput, "open_hw" + "\n");
                    writeToProcess(processOutput, "connect_hw_server" + "\n");
                    Thread.sleep(3000);
                    writeToProcess(processOutput, "open_hw_target" + "\n");
                    writeToProcess(processOutput, "current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n");
                    Thread.sleep(5000);
                    writeToProcess(processOutput,      "set_property PROBES.FILE "+properties.getProperty("IsolationAd.probesPath")+" [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property FULL_PROBES.FILE "+properties.getProperty("IsolationAd.probesPath")+" [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput,     "set_property PROGRAM.FILE "+properties.getProperty("IsolationAd.programPath")+" [get_hw_devices xc7vx690t_0]" + "\n");
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
                        String power = propertiesConfig.getProperty("vsgPower");
                        instru0.writeCmd("SOURce1:POWer:POWer " + power + "dBm");//信号源初始功率
                        instru0.writeCmd(":OUTPut1 ON");


                        for (String channel : offeredChannels0) {
                            System.out.println("矩阵当前开启通道" + channel);
                            List<String> cmdChannel = Arrays.asList(channel);
                            matrix0.channelSwitch(cmdChannel);
                            Thread.sleep(100);

                            //采数
                            writeToProcess(processOutput, "run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}] -trigger_now" + "\n");
                            writeToProcess(processOutput, "wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");
                            writeToProcess(processOutput, "upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");

                            System.out.println("开始存文件...");
                            writeToProcess(processOutput, "write_hw_ila_data -csv_file {"+properties.getProperty("IsolationAd.samplePath")+"ch_"
                                    + channel + ".csv} hw_ila_data_3" + "\n");
                            File file = new File(properties.getProperty("IsolationAd.samplePath")+"ch_" + channel + ".csv");
                            while (true) {
                                Thread.sleep(1);
                                if (file.exists()) {
                                    System.out.println("已生成文件" + properties.getProperty("IsolationAd.samplePath")+"ch_" + channel + ".csv");
                                    break;
                                }
                            }
                        }
                        instru0.writeCmd(":OUTPut1 OFF");

                    }else if(InstruType.E8267D.equals(instru0.instruType)){
                        instru0.writeCmd(":OUTPut OFF");
                        instru0.writeCmd(":FREQuency 132.5MHz");
                        String power = propertiesConfig.getProperty("vsgPower");
                        instru0.writeCmd("SOURce:POWer:LEVel " + power);  //keysight
                        instru0.writeCmd(":OUTPut ON"); //keysight
                        instru0.writeCmd(":OUTPut:MOD:STAT OFF");// keysight


                        for (String channel : offeredChannels0) {
                            System.out.println("矩阵当前开启通道" + channel);
                            List<String> cmdChannel = Arrays.asList(channel);
                            matrix0.channelSwitch(cmdChannel);
                            Thread.sleep(100);

                            //采数
                            writeToProcess(processOutput, "run_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}] -trigger_now" + "\n");
                            writeToProcess(processOutput, "wait_on_hw_ila [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");
                            writeToProcess(processOutput, "upload_hw_ila_data [get_hw_ilas -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/ila_addata_inst\"}]" + "\n");

                            System.out.println("开始存文件...");
                            writeToProcess(processOutput, "write_hw_ila_data -csv_file {"+properties.getProperty("IsolationAd.samplePath")+"ch_"
                                    + channel + ".csv} hw_ila_data_3" + "\n");
                            File file = new File(properties.getProperty("IsolationAd.samplePath")+"ch_" + channel + ".csv");
                            while (true) {
                                Thread.sleep(1);
                                if (file.exists()) {
                                    System.out.println("已生成文件" + properties.getProperty("IsolationAd.samplePath")+"ch_" + channel + ".csv");
                                    break;
                                }
                            }
                        }
                        instru0.writeCmd(":OUTPut OFF");
                    }
                    readerRunning0=false;
                    System.out.println("关闭process的vivado...");
                    SystemUtils.killProcessTree(process);

                } catch (Exception e) {
                }
            });

        writerAndTester.start();
        ThreadAndProcessPools.addThread(writerAndTester);

        try {
            writerAndTester.join();
        }catch (Exception e){
            e.printStackTrace();
        }


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
