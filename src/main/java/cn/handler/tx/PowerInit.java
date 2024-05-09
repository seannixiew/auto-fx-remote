package cn.handler.tx;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.model.InstruType;
import cn.utils.ControllersManager;
import cn.utils.ThreadAndProcessPools;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;


//继承Handler，以便someControl.setOnAction(new PowerInit()); 复用事件处理逻辑，同一个事件处理方便绑定多个控件。（将事件处理逻辑封装为普通方法进行调用亦可）
public class PowerInit implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    ComboBox<String> cbLoop=rfTestController.cbLoop; //mode 0：单测

    InstrumentClient instru0=rfTestController.instru0;
    InstrumentClient instru3=rfTestController.instru3;
    DbfClient dbfClient=rfTestController.dbfClient;

    TextArea taResults=rfTestController.taResults;
    TextArea taLogs=rfTestController.taLogs;

    @Override
    public void handle(Event event) {
        Thread t=new Thread(()->{
            System.out.println("执行RF功率标定测试...");
            int selectedIndex = cbLoop.getSelectionModel().getSelectedIndex();
            System.out.println("loop index："+selectedIndex);
//            if(!(instru0.isConnected && instru3.isConnected)){
//                System.out.println("请检查仪表连接！");
//                return;
//            }
//            if(InstruType.SMW200A.equals(instru0.instruType) && InstruType.NRP.equals(instru3.instruType)){
//
//            }
            if(selectedIndex==0){
                Platform.runLater(()->{
                    taLogs.appendText("循环模式：0 --> 单通道测试\n");
                });
                //测试逻辑...
            }


        });
        t.start();
        ThreadAndProcessPools.addThread(t);

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
