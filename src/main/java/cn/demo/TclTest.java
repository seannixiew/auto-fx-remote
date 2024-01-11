package cn.demo;

import java.io.*;

public class TclTest {

    public static void main(String[] args) {
        try {
            // Vivado的安装路径
            String vivadoPath = "D:\\Xilinx\\Vivado\\2018.3\\bin\\vivado.bat";

            // Tcl脚本路径
            String tclScriptPath = "D:\\vivado_projects\\open.tcl";

            // 启动Vivado进程
            ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath);
//            ProcessBuilder processBuilder = new ProcessBuilder(vivadoPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            new Thread(()->{
                try {

                    BufferedReader processInput = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    BufferedReader processError = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    while (true){
                        Thread.sleep(3000);
                        readFromProcess(processInput);
                        readFromProcess(processError);
                    }

                }catch (Exception e){}
            }).start();


//            processInput.close();
//            Thread.sleep(3000);
//            readFromProcess(processInput);
//            readFromProcess(processError);
            // 向Vivado发送Tcl命令
//            writeToProcess(processOutput, "open_project /path/to/your/project\n");
//            writeToProcess(processOutput, "source " + tclScriptPath + "\n");
            // 添加更多Tcl命令...
//            Thread.sleep(3000);
//            readFromProcess(processInput);
//            readFromProcess(processError);

            // 获取Vivado的输入流（用于向Vivado写入Tcl命令）
            BufferedWriter processOutput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writeToProcess(processOutput, "set_property board_part xilinx.com:zc702:part0:1.4 [current_project]\n");


            System.out.println("");
//
//            processInput.close();
//            processOutput.close();


            /**
             * good
             */
//            String tclScriptPath2 = "D:\\vivado_projects\\open.tcl";
//            // 启动Vivado进程
//            ProcessBuilder processBuilder2 = new ProcessBuilder(vivadoPath, "-mode", "tcl", "-source", tclScriptPath2);
//            processBuilder2.redirectErrorStream(true);
//            Process process2 = processBuilder2.start();
//            BufferedReader processInput2 = new BufferedReader(new InputStreamReader(process2.getInputStream(),"GBK"));
//            readFromProcess(processInput2);
//            processInput.close();


//            processInput = new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
//            readFromProcess(processInput);
//            processInput.close();


//            processOutput.close();
//            int exitCode = process.waitFor();
//            System.out.println("Vivado exited with code " + exitCode);

        } catch (Exception  e) {
            e.printStackTrace();
        }
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
