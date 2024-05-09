package cn.handler.tx;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.utils.ControllersManager;
import cn.utils.ThreadAndProcessPools;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class ConsistencyTxRf implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    ComboBox<String> cbLoop=rfTestController.cbLoop;

    InstrumentClient instru2=rfTestController.instru2;
    DbfClient dbfClient=rfTestController.dbfClient;

    TextArea taResults=rfTestController.taResults;
    TextArea taLogs=rfTestController.taLogs;


    @Override
    public void handle(Event event) {
        Thread t=new Thread(()->{
            System.out.println("执行RF发射幅相测试...");
            int selectedIndex = cbLoop.getSelectionModel().getSelectedIndex();
            System.out.println("loop index："+selectedIndex);

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
