package cn.controllers;

import cn.controllers.root.RootController;
import cn.handler.ad.PowerAndLinearityAd;
import cn.utils.ControllersManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;
import java.util.Properties;

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

    Properties properties=new Properties();

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
            System.out.println(files3);
        });
    }

    @FXML
    public void onDownload0(){

    }
    @FXML
    public void onDownload1(){

    }
    @FXML
    public void onDownload2(){

    }
    @FXML
    public void onDownload3(){

    }

    @FXML
    public void onTclWrite(){
        try {
            writeToProcess(processOutput, tfTcl.getText().trim()+"\n");
//            writeToProcess(processOutput, "set_property board_part xilinx.com:zc702:part0:1.4 [current_project]\n");
        }catch (Exception e){}

    }

    @FXML
    public void onGetRunningShell(){
        // TODO: 2024/1/14 增加分支从其他handlerInstance中获取实例

        RfTestController rfTestController=(RfTestController)ControllersManager.CONTROLLERS.get("RfTestController");
        PowerAndLinearityAd handlerInstance = (PowerAndLinearityAd) rfTestController.mainTestDispatcher.HandlerInstance;
        processOutput=handlerInstance.processOutput;
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
                    BufferedReader processError = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while (true){
                        Thread.sleep(3000);
                        readFromProcess(processInput);
                        readFromProcess(processError);
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
        process.destroy();
        System.out.println("当前shell进程状态："+process.isAlive());
    }




    private static void writeToProcess(BufferedWriter processOutput, String command) throws IOException {
        processOutput.write(command);
        processOutput.flush();
    }

    private static void readFromProcess(BufferedReader processInput) throws IOException {
        // TODO: 2024/1/14 ready() 
        char[] bt = new char[1024];
//        do {
        processInput.read(bt);
        // 直接打印char[] bt会出现\u0000 即
        String s=String.valueOf(bt);
        s=s.replaceAll("\\u0000"," ");
        System.out.println(s);
//        } while (processInput.ready());

    }

    public void initialize(){
    }
}
