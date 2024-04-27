package cn.controllers;

import cn.controllers.root.RootController;
import cn.dispatcher.MainTestDispatcher;
import cn.handler.ad.PowerAndLinearityAd;
import cn.handler.base.BaseHandler;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import cn.utils.SystemUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.LockSupport;

public class VivadoClientController  extends RootController {

    @FXML
    TextField tfTcl;

    Process process;

    Runnable reader;

    BufferedWriter processOutput;

    Stage primaryStage;

    List<File> files0;
    List<File> files1;
    List<File> files2;
    List<File> files3;

    @FXML
    TextField tf0;
    @FXML
    TextField tf1;
    @FXML
    TextField tf2;
    @FXML
    TextField tf3;

    @FXML
    TextArea taReader;

    @FXML
    ComboBox<String> cbPickShell;
    @FXML
    Button btGetShell;

    boolean readerRunning=true;

    Properties properties=new Properties();

    static int readCounter0=0;
    static int readCounter1=0;
    static int readCounter2=0;
    static int readCounter3=0;

    //★ 用于在controller中使用primaryStage（由于该方法在main调用，所以顺序是：生成controller（执行完initialize）---main调用获取primayStage---调用事件函数使用primaryStage。注意不能在initialize中使用primaryStage）
    public void setPrimaryStage(Stage primaryStage){
        this.primaryStage=primaryStage;
    }


    @FXML
    public void onBrowse0(){
        FileChooser fileChooser=new FileChooser();
        FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("program files","*.bit","*.ltx");
        fileChooser.getExtensionFilters().add(extensionFilter);
        Platform.runLater(()->{
            files0=fileChooser.showOpenMultipleDialog(primaryStage);
            if(files0!=null) {
                tf0.appendText(files0.toString());
            }
            System.out.println(files0);
        });
    }
    @FXML
    public void onBrowse1(){
        FileChooser fileChooser=new FileChooser();
        FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("program files","*.bit","*.ltx");
        fileChooser.getExtensionFilters().add(extensionFilter);
        Platform.runLater(()->{
            files1=fileChooser.showOpenMultipleDialog(primaryStage);
            if(files1!=null) {
                tf1.appendText(files1.toString());
            }
            System.out.println(files1);
        });
    }
    @FXML
    public void onBrowse2(){
        FileChooser fileChooser=new FileChooser();
        FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("program files","*.bit","*.ltx");
        fileChooser.getExtensionFilters().add(extensionFilter);
        Platform.runLater(()->{
            files2=fileChooser.showOpenMultipleDialog(primaryStage);
            if(files2!=null) {
                tf2.appendText(files2.toString());
            }
            System.out.println(files2);
        });
    }
    @FXML
    public void onBrowse3(){
        FileChooser fileChooser=new FileChooser();
        FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("program files","*.bit","*.ltx");
        fileChooser.getExtensionFilters().add(extensionFilter);
        Platform.runLater(()->{
            files3=fileChooser.showOpenMultipleDialog(primaryStage);
            if(files3!=null) {
                tf3.appendText(files3.toString());
            }
            System.out.println(files3);
        });
    }

    /** 对应property中下载器0 */
    @FXML
    public void onDownload0() throws IOException {
        if (files0 == null) {
            return;
        }
        String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
        String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

        // 启动Vivado进程
        ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
        processBuilder.redirectErrorStream(true);
        Process downloadProcess = processBuilder.start();

        new Thread(() -> {
            try {
                BufferedReader processInput = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream(), "UTF-8"));
//                BufferedReader processError = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()));
                int timeCounter=0;
                while (true) {
                    Thread.sleep(1000);
                    timeCounter++;
                    String echo = readFromProcess(processInput, "downloadReader0", 0);
//                    String error = readErrorFromProcess(processError,0);

                    if (echo.contains("End of startup status: HIGH")) {
                        System.out.println("fpga0下载完毕...关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                    if (echo.contains("End of startup status: LOW") || echo.contains("ERROR") || timeCounter>300) {
                        System.out.println("fpga0下载失败。关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }).start();

        BufferedWriter processOutput = new BufferedWriter(new OutputStreamWriter(downloadProcess.getOutputStream()));

        String boxNum = properties.getProperty("vivadoClient.boxNum0");
        String probesPath;
        String programPath;
        if (files0.get(0).toString().contains(".ltx")) {
            probesPath = files0.get(0).toString();
            programPath = files0.get(1).toString();
        } else {
            probesPath = files0.get(1).toString();
            programPath = files0.get(0).toString();
        }

        new Thread(() -> {
            if (downloadProcess.isAlive()) {
                try {
                    System.out.println("fpga0开始下载...");
                    writeToProcess(processOutput, 0, "open_hw" + "\n");
                    writeToProcess(processOutput, 0, "connect_hw_server" + "\n");
                    Thread.sleep(1000);
                    writeToProcess(processOutput, 0, "open_hw_target " + boxNum + "\n");
                    Thread.sleep(10000);
                    writeToProcess(processOutput, 0, "current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 0, "refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                    writeToProcess(processOutput, 0, "set_property PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 0, "set_property FULL_PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 0, "set_property PROGRAM.FILE {" + programPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 0, "program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                } catch (Exception e) {
                }
            }
        }).start();
    }

    /** 对应property中下载器1 */
    @FXML
    public void onDownload1() throws IOException {
        if (files1 == null) {
            return;
        }
        String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
        String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

        // 启动Vivado进程
        ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
        processBuilder.redirectErrorStream(true);
        Process downloadProcess = processBuilder.start();

        new Thread(() -> {
            try {
                BufferedReader processInput = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream(), "UTF-8"));
//                BufferedReader processError = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()));
                int timeCounter=0;
                while (true) {
                    Thread.sleep(1000);
                    timeCounter++;
                    String echo = readFromProcess(processInput, "downloadReader1", 1);
//                    String error = readErrorFromProcess(processError,1);

                    if (echo.contains("End of startup status: HIGH")) {
                        System.out.println("fpga1下载完毕...关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                    if (echo.contains("End of startup status: LOW") || echo.contains("ERROR") || timeCounter>300) {
                        System.out.println("fpga1下载失败。关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }).start();

        BufferedWriter processOutput = new BufferedWriter(new OutputStreamWriter(downloadProcess.getOutputStream()));

        String boxNum = properties.getProperty("vivadoClient.boxNum1");
        String probesPath;
        String programPath;
        if (files1.get(0).toString().contains(".ltx")) {
            probesPath = files1.get(0).toString();
            programPath = files1.get(1).toString();
        } else {
            probesPath = files1.get(1).toString();
            programPath = files1.get(0).toString();
        }

        new Thread(() -> {
            if (downloadProcess.isAlive()) {
                try {
                    System.out.println("fpga1开始下载...");
                    writeToProcess(processOutput, 1, "open_hw" + "\n");
                    writeToProcess(processOutput, 1, "connect_hw_server" + "\n");
                    Thread.sleep(1000);
                    writeToProcess(processOutput, 1, "open_hw_target " + boxNum + "\n");
                    Thread.sleep(10000);
                    writeToProcess(processOutput, 1, "current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 1, "refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                    writeToProcess(processOutput, 1, "set_property PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 1, "set_property FULL_PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 1, "set_property PROGRAM.FILE {" + programPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 1, "program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                } catch (Exception e) {
                }
            }
        }).start();
    }

    /** 对应property中下载器2 */
    @FXML
    public void onDownload2() throws IOException {
        if (files2 == null) {
            return;
        }
        String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
        String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

        // 启动Vivado进程
        ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
        processBuilder.redirectErrorStream(true);
        Process downloadProcess = processBuilder.start();

        new Thread(() -> {
            try {
                BufferedReader processInput = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream(), "UTF-8"));
//                BufferedReader processError = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream()));
                int timeCounter=0;
                while (true) {
                    Thread.sleep(1000);
                    timeCounter++;
                    String echo = readFromProcess(processInput, "downloadReader2", 2);
//                    String error = readErrorFromProcess(processError,2);

                    if (echo.contains("End of startup status: HIGH")) {
                        System.out.println("fpga2下载完毕...关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                    if (echo.contains("End of startup status: LOW") || echo.contains("ERROR") || timeCounter>300) {
                        System.out.println("fpga2下载失败。关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }).start();

        BufferedWriter processOutput = new BufferedWriter(new OutputStreamWriter(downloadProcess.getOutputStream()));

        String boxNum = properties.getProperty("vivadoClient.boxNum2");
        String probesPath;
        String programPath;
        if (files2.get(0).toString().contains(".ltx")) {
            probesPath = files2.get(0).toString();
            programPath = files2.get(1).toString();
        } else {
            probesPath = files2.get(1).toString();
            programPath = files2.get(0).toString();
        }

        new Thread(() -> {
            if (downloadProcess.isAlive()) {
                try {
                    System.out.println("fpga2开始下载...");
                    writeToProcess(processOutput, 2, "open_hw" + "\n");
                    writeToProcess(processOutput, 2, "connect_hw_server" + "\n");
                    Thread.sleep(1000);
                    writeToProcess(processOutput, 2, "open_hw_target " + boxNum + "\n");
                    Thread.sleep(10000);
                    writeToProcess(processOutput, 2, "current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 2, "refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                    writeToProcess(processOutput, 2, "set_property PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 2, "set_property FULL_PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 2, "set_property PROGRAM.FILE {" + programPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 2, "program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                } catch (Exception e) {
                }
            }
        }).start();
    }

    /** 对应property中下载器3 */
    @FXML
    public void onDownload3() throws IOException {
        if (files3 == null) {
            return;
        }
        String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";
        String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

        // 启动Vivado进程
        ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
        processBuilder.redirectErrorStream(true);
        Process downloadProcess = processBuilder.start();

        new Thread(() -> {
            try {
                BufferedReader processInput = new BufferedReader(new InputStreamReader(downloadProcess.getInputStream(), "UTF-8"));
//                BufferedReader processError = new BufferedReader(new InputStreamReader(downloadProcess.getErrorStream()));
                int timeCounter=0;
                while (true) {
                    Thread.sleep(1000);
                    timeCounter++;
                    String echo = readFromProcess(processInput, "downloadReader3", 3);
//                    String error = readErrorFromProcess(processError,3);

                    if (echo.contains("End of startup status: HIGH")) {
                        System.out.println("fpga3下载完毕...关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }

                    if (echo.contains("End of startup status: LOW") || echo.contains("ERROR") || timeCounter>300) {
                        System.out.println("fpga3下载失败。关闭vivado。");
                        SystemUtils.killProcessTree(downloadProcess);
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }).start();

        BufferedWriter processOutput = new BufferedWriter(new OutputStreamWriter(downloadProcess.getOutputStream()));

        String boxNum = properties.getProperty("vivadoClient.boxNum3");
        String probesPath;
        String programPath;
        if (files3.get(0).toString().contains(".ltx")) {
            probesPath = files3.get(0).toString();
            programPath = files3.get(1).toString();
        } else {
            probesPath = files3.get(1).toString();
            programPath = files3.get(0).toString();
        }

        new Thread(() -> {
            if (downloadProcess.isAlive()) {
                try {
                    System.out.println("fpga3开始下载...");
                    writeToProcess(processOutput, 3, "open_hw" + "\n");
                    writeToProcess(processOutput, 3, "connect_hw_server" + "\n");
                    Thread.sleep(1000);
                    writeToProcess(processOutput, 3, "open_hw_target " + boxNum + "\n");
                    Thread.sleep(10000);
                    writeToProcess(processOutput, 3, "current_hw_device [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 3, "refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7vx690t_0] 0]" + "\n"); // 6~7s
                    writeToProcess(processOutput, 3, "set_property PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 3, "set_property FULL_PROBES.FILE {" + probesPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 3, "set_property PROGRAM.FILE {" + programPath + "} [get_hw_devices xc7vx690t_0]" + "\n");
                    writeToProcess(processOutput, 3, "program_hw_devices [get_hw_devices xc7vx690t_0]" + "\n");
                } catch (Exception e) {
                }
            }
        }).start();
    }

    @FXML
    public void onTclWrite(){
        try {
            writeToProcess(processOutput, 0,tfTcl.getText().trim()+"\n");
//            writeToProcess(processOutput, "set_property board_part xilinx.com:zc702:part0:1.4 [current_project]\n");
        }catch (Exception e){}

    }

    @FXML
    public void onGetShell(){
        RfTestController rfTestController=(RfTestController)ControllersManager.CONTROLLERS.get("RfTestController");
        MainTestDispatcher mainTestDispatcher= rfTestController.mainTestDispatcher;
        if(mainTestDispatcher!=null){
            BaseHandler  handlerInstance = (BaseHandler) mainTestDispatcher.HandlerInstance;

            String shellNum=cbPickShell.getValue();
            if(shellNum!=null && shellNum!="") {
                int num=Integer.parseInt(shellNum.charAt(8)+"");
                switch (num) {
                    case 0:
                        processOutput = handlerInstance.processOutput0;
                        if(processOutput!=null) {
                            Platform.runLater(() -> {
                                taReader.appendText("已获取processOutput0...\n");
                            });
                        }
                        break;
                    case 1:
                        processOutput = handlerInstance.processOutput1;
                        if(processOutput!=null) {
                            Platform.runLater(() -> {
                                taReader.appendText("已获取processOutput1...\n");
                            });
                        }
                        break;
                    case 2:
                        processOutput = handlerInstance.processOutput2;
                        if(processOutput!=null) {
                            Platform.runLater(() -> {
                                taReader.appendText("已获取processOutput2...\n");
                            });
                        }
                        break;
                    case 3:
                        processOutput = handlerInstance.processOutput3;
                        if(processOutput!=null) {
                            Platform.runLater(() -> {
                                taReader.appendText("已获取processOutput3...\n");
                            });
                        }
                        break;
                }
            }
        }

    }


    @FXML
    public void onNewShell(){
        try {
            String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";

            // Tcl脚本路径
//            String tclScriptPath = "D:\\vivado_projects\\open.tcl";
            String tclScriptPath = "D:\\vivado_projects\\blank.tcl";

            // 启动Vivado进程
            ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
//            ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        }catch (Exception e){}

        reader=new Runnable() {
            @Override
            public void run() {
                try {

                    BufferedReader processInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
//                    BufferedReader processError = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while (readerRunning){
                        Thread.sleep(1000);
                        readFromProcess(processInput,"tclReader",0);
//                        readErrorFromProcess(processError,4);
                    }

                }catch (Exception e){}
            }
        };
        new Thread(reader).start();

        processOutput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        System.out.println("当前shell进程状态："+process.isAlive());
    }

    @FXML
    public void onKillShell(){
        readerRunning=false;
        SystemUtils.killProcessTree(process);
//        process.destroyForcibly(); // 只是关闭cmd窗口，而没有关闭vivado进程
        System.out.println("当前shell进程状态："+process.isAlive());

    }




    private  void writeToProcess(BufferedWriter processOutput,int senderNum, String command) throws IOException, InterruptedException {
        processOutput.write(command);
        processOutput.flush();
        String s= DateFormat.FORLOGSHORT.format(new Date())+"sender"+senderNum+"："+command;

        System.out.println(s);
        Thread.sleep(1000);
    }


//    private static void readFromProcess(BufferedReader processInput) throws IOException {
//        // TODO: 2024/1/14 ready()
//        char[] bt = new char[1024];
////        do {
//        processInput.read(bt);
//        // 直接打印char[] bt会出现\u0000 即
//        String s=String.valueOf(bt);
//        s=s.replaceAll("\\u0000"," ");
//        System.out.println(s);
////        } while (processInput.ready());
//
//    }

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

    private static String readErrorFromProcess(BufferedReader processInput,int num) throws IOException {

        String s="";
        if(processInput.ready()) {
            char[] bt = new char[1024];
            processInput.read(bt);
            s = String.valueOf(bt);
            s = s.replaceAll("\\u0000", " ");
            System.out.println("err"+num+": "+s);
        }

        return s;
    }

    public void initialize(){

        try {
            properties.load(new FileInputStream("src/main/resources/configs/vivado.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        cbPickShell.getItems().addAll("Running 0","Running 1","Running 2","Running 3");
    }
}
