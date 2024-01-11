package cn.controllers;

import cn.controllers.root.RootController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.*;

public class VivadoClientController  extends RootController {

    @FXML
    TextField tfTcl;

    Process process;

    Runnable reader;

    BufferedWriter processOutput;



    @FXML
    public void onTclWrite(){
        try {
            writeToProcess(processOutput, tfTcl.getText().trim()+"\n");
//            writeToProcess(processOutput, "set_property board_part xilinx.com:zc702:part0:1.4 [current_project]\n");
        }catch (Exception e){}

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
        char[] bt = new char[1024];
//        do {
        processInput.read(bt);
        // 直接打印char[] bt会出现\u0000 即
        String s=String.valueOf(bt);
        s=s.replaceAll("\\u0000"," ");
        System.out.println(s);
//        } while (processInput.ready());

    }
}
