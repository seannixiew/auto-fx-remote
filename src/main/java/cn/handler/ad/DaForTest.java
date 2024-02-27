package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.InstrumentClient;
import cn.model.InstruType;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.locks.LockSupport;

public class DaForTest implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    InstrumentClient instru0=rfTestController.instru0;  //信号源
    InstrumentClient instru1=rfTestController.instru1;  //频谱仪
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;
    ToggleButton btDownload=rfTestController.btDownload;

    Process process;
    public BufferedWriter processOutput;
    BufferedReader processInput;
    BufferedReader processError;
    Thread reader;
    public static Thread writerAndTester; //static 是临时方案


    boolean programming;
    //    boolean sampling;
    static int readCounter=0;

    Properties properties = new Properties();

    @Override
    public void handle(Event event) {

        try {
            properties.load(new FileInputStream("src/main/resources/configs/config.properties"));
        }catch (Exception e){}
//        System.out.println(properties.getProperty("vsgPower"));
//        System.out.println(properties.getProperty("adLineNo"));

        if (true || InstruType.SMW200A.equals(instru0.instruType) && InstruType.FSW.equals(instru1.instruType)) {  // TODO: 2024/1/12 当前被旁路
            System.out.println("执行测试DA测试...");
            Platform.runLater(() -> {
                taLogs.appendText("开始执行测试DA测试...");
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
                    writeToProcess(processOutput, "set_property PROBES.FILE {E:/wx/2_projects/L payload/vivado files/AD_impl_Calib_syn_test/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property FULL_PROBES.FILE {E:/wx/2_projects/L payload/vivado files/AD_impl_Calib_syn_test/TOP_test2.ltx} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, "set_property PROGRAM.FILE {E:/wx/2_projects/L payload/vivado files/AD_impl_Calib_syn_test/TOP_test2.bit} [get_hw_devices xc7vx690t_0]" + "\n");
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
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE 41 [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {ADandDA_inst/damodeset} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");

                    Platform.runLater(()->{
                        taResults.appendText("确认完毕后，点击Custom继续执行测试！\n");
                    });
                    LockSupport.park();

                    System.out.println("继续测试...");
                    String adLineNo=properties.getProperty("adLineNo");
                    writeToProcess(processOutput, "set_property OUTPUT_VALUE "+adLineNo+" [get_hw_probes ADandDA_inst/damodeset -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");
                    writeToProcess(processOutput, "commit_hw_vio [get_hw_probes {ADandDA_inst/damodeset} -of_objects [get_hw_vios -of_objects [get_hw_devices xc7vx690t_0] -filter {CELL_NAME=~\"ADandDA_inst/vio_damodeset_inst\"}]]" + "\n");

                    Platform.runLater(()->{
                        taResults.appendText("执行扫频测试...\n");
                    });
                    instru0.writeCmd("SOURce1:FREQuency:STARt 105 MHz");
                    instru0.writeCmd("SOURce1:FREQuency:STOP 160 MHz");
                    instru0.writeCmd("SOURce1:SWEep:FREQuency:STEP:LINear 0.1 MHz");
                    instru0.writeCmd("SOURce1:FREQuency:MODE SWEep");
                    String power=properties.getProperty("vsgPower");
                    instru0.writeCmd("SOURce1:POWer:POWer " + power+"dBm");//信号源初始功率
                    instru0.writeCmd(":OUTPut1 ON");

                    instru1.writeCmd("FREQ:CENT 135 MHz");
                    instru1.writeCmd(":FREQuency:SPAN 70 MHz");
                    instru1.writeCmd("BAND:AUTO ON");
                    instru1.writeCmd("BAND:VID:RAT:AUTO ON");
                    instru1.writeCmd("BAND:VIDeo:AUTO ON");
                    instru1.writeCmd("TRAC1:TYPE MAXHold");
                    Thread.sleep(7000);
                    String imgPath0=":MMEMory:STORe:SCReen 'D:\\3188\\20240227\\"+DateFormat.FORFILENAME.format(new Date())+".png'";
                    instru1.writeCmd(imgPath0);

                    Platform.runLater(()->{
                        taResults.appendText("执行扫功率测试...频点：105 MHz\n");
                    });
                    Double startPower=Double.parseDouble(power)-49;
                    Double endPower=Double.parseDouble(power);
                    instru0.writeCmd(":OUTPut1 OFF");
                    instru0.writeCmd("SOURce1:POWer:STARt "+startPower+"dBm");
                    instru0.writeCmd("SOURce1:POWer:STOP "+endPower+" dBm");
                    instru0.writeCmd(":SWEep:POWer:STEP 1");
                    instru0.writeCmd(":SWEep:POWer:DWELl 0.2");
                    instru0.writeCmd("SOURce:POWer:MODE SWEep");
                    instru0.writeCmd("SOURce1:FREQuency:CW 105MHz");
                    instru0.writeCmd(":OUTPut1 ON");

                    instru1.writeCmd("FREQ:CENT 105 MHz");
                    instru1.writeCmd(":FREQuency:SPAN 0 Hz");
                    instru1.writeCmd(":SWEep:TIME 10s");
                    instru1.writeCmd("BAND 100 KHz");
                    instru1.writeCmd("BAND:VIDeo 100 KHz");
                    Thread.sleep(12000);
                    String imgPath1=":MMEMory:STORe:SCReen 'D:\\3188\\20240227\\"+DateFormat.FORFILENAME.format(new Date())+".png'";
                    instru1.writeCmd(imgPath1);

                    Platform.runLater(()->{
                        taResults.appendText("执行扫功率测试...频点：132.5 MHz\n");
                    });
                    instru0.writeCmd(":OUTPut1 OFF");
                    instru0.writeCmd("SOURce1:POWer:STARt "+startPower+"dBm");
                    instru0.writeCmd("SOURce1:POWer:STOP "+endPower+" dBm");
                    instru0.writeCmd(":SWEep:POWer:STEP 1");
                    instru0.writeCmd(":SWEep:POWer:DWELl 0.2");
                    instru0.writeCmd("SOURce:POWer:MODE SWEep");
                    instru0.writeCmd("SOURce1:FREQuency:CW 132.5MHz");
                    instru0.writeCmd(":OUTPut1 ON");

                    instru1.writeCmd("FREQ:CENT 132.5 MHz");
                    instru1.writeCmd(":FREQuency:SPAN 0 Hz");
                    instru1.writeCmd(":SWEep:TIME 10s");
                    instru1.writeCmd("BAND 100 KHz");
                    instru1.writeCmd("BAND:VIDeo 100 KHz");
                    Thread.sleep(12000);
                    String imgPath2=":MMEMory:STORe:SCReen 'D:\\3188\\20240227\\"+DateFormat.FORFILENAME.format(new Date())+".png'";
                    instru1.writeCmd(imgPath2);

                    Platform.runLater(()->{
                        taResults.appendText("执行扫功率测试...频点：160 MHz\n");
                    });
                    instru0.writeCmd(":OUTPut1 OFF");
                    instru0.writeCmd("SOURce1:POWer:STARt "+startPower+"dBm");
                    instru0.writeCmd("SOURce1:POWer:STOP "+endPower+" dBm");
                    instru0.writeCmd(":SWEep:POWer:STEP 1");
                    instru0.writeCmd(":SWEep:POWer:DWELl 0.2");
                    instru0.writeCmd("SOURce:POWer:MODE SWEep");
                    instru0.writeCmd("SOURce1:FREQuency:CW 160MHz");
                    instru0.writeCmd(":OUTPut1 ON");

                    instru1.writeCmd("FREQ:CENT 160 MHz");
                    instru1.writeCmd(":FREQuency:SPAN 0 Hz");
                    instru1.writeCmd(":SWEep:TIME 10s");
                    instru1.writeCmd("BAND 100 KHz");
                    instru1.writeCmd("BAND:VIDeo 100 KHz");
                    Thread.sleep(12000);
                    String imgPath3=":MMEMory:STORe:SCReen 'D:\\3188\\20240227\\"+DateFormat.FORFILENAME.format(new Date())+".png'";
                    instru1.writeCmd(imgPath3);


                } catch (Exception e) {
                }
            }).start();


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
