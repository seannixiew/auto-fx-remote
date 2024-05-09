package cn.controllers.popup;

import cn.controllers.RfTestController;
import cn.controllers.root.RootController;
import cn.instr.MatrixClient;
import cn.utils.CommonUtils;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;


public class PopupControllerA extends RootController {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    MatrixClient matrix0=rfTestController.matrix0;
    public List<String> offeredChannels0;
    TextArea taLogs=rfTestController.taLogs;

    @FXML
    public TextField tfIP;
    @FXML
    public TextArea taChannels;
    @FXML
    public Button btConnection;
    @FXML
    public Button btInputAll;
    @FXML
    public Button btConfirm;

    @FXML
    public Button btSwitchOn;
    @FXML
    public Button btReset;
    @FXML
    public TextField tfEndpoints;


    public void onConnection(){
        new Thread(()-> {
            long port = 3000; //不必支持修改端口，可以使用配置文件
            String ip = tfIP.getText().trim();
            if (ip.isEmpty()){
                System.out.println("ip为空！");
                CommonUtils.warningDialog("ip异常","ip地址为空！");
                return;
            }
            if (matrix0.connect(ip, port) == 1) {
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"Matrix0 已连接" + "\n");
                });
            } else {
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"超时！Matrix0" + "\n");
                });
            }
        }).start();
    }
    public void onInputAll(){
        Platform.runLater(()->{
            taChannels.setText( "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18\n" +
                                "19 20 21 22 23 24 25 26 27 28 29 30 31 32 33\n" +
                                "34 35 36 37 38 39 40 41 42 43 44 45 46 47 48\n" +
                                "49 50 51 52 53 54 55 56 57 58 59 60 61 62 63\n" +
                                "64 65 66 67 68 69 70 71 72 73 74 75 76 77 78\n" +
                                "79 80 81 82 83 84 85 86 87 88 89 90 91 92 93\n" +
                                "94 95 96 97 98 99 100 101 102 103 104 105 106\n" +
                                "107 108 109 110 111 112 113 114 115 116 117\n" +
                                "118 119 120 121 122 123 124 125 126 127 128");
        });
    }
    public void onConfirm(){
        String text=taChannels.getText().trim();
        String[] channels=text.split("\\s+");
        offeredChannels0=new ArrayList<>();
        for(String channel:channels){
            channel="A"+channel.trim();
            offeredChannels0.add(channel);
        }
        Platform.runLater(()->{
            taLogs.appendText(DateFormat.FORLOG.format(new Date())+"矩阵0即将遍历操作的通道为："+ Arrays.toString(channels)+"\n");
            System.out.println("矩阵0即将遍历操作的通道为："+text);
        });
    }
    public void onSwitchOn(){
        String text=taChannels.getText().trim();
        String[] channels=text.split("\\s+");
        List<String> cmdChannels=new ArrayList<>();
        for(String channel:channels){
            channel="A"+channel.trim();
            cmdChannels.add(channel);
        }
        String echo=matrix0.channelSwitch(cmdChannels);
        Platform.runLater(()->{
            taLogs.appendText(DateFormat.FORLOG.format(new Date())+echo+"\n");
        });
    }
    public void onReset(){
        String echo = matrix0.rst();

        Platform.runLater(()->{
            taLogs.appendText(DateFormat.FORLOG.format(new Date())+echo+"\n");
        });
    }

    public void initialize(){
        tfEndpoints.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null){
                if( newValue.trim().length()!=0){
                    String[] ends=newValue.trim().split("\\s+");
                    if(ends!=null && ends.length==2){
                        String pattern = "^[1-9]\\d*$";
                        Pattern regex = Pattern.compile(pattern);
                        if(regex.matcher(ends[0].trim()).matches() && regex.matcher(ends[1].trim()).matches()){
                            int start=Integer.parseInt(ends[0].trim());
                            int stop=Integer.parseInt(ends[1].trim());
                            StringBuilder sb=new StringBuilder();
                            while (start<stop+1){
                                sb.append(start+" ");
                                start++;
                            }
                            taChannels.setText(sb.toString());
                        }
                    }
                }else {
                    taChannels.setText("");
                }
            }
        });
    }


}