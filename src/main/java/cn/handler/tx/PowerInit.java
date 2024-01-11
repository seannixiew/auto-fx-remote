package cn.handler.tx;

import cn.controllers.RfTestController;
import cn.instr.InstrumentClient;
import cn.model.InstruType;
import cn.utils.ControllersManager;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;



public class PowerInit implements EventHandler {
    //继承Handler，以便someControl.setOnAction(new PowerInit()); 复用事件处理逻辑，同一个事件处理方便绑定多个控件。（将事件处理逻辑封装为普通方法进行调用亦可）

    //非静态类非静态成员，只有实例化后才会被初始化
    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    InstrumentClient instru0=rfTestController.instru0;
    InstrumentClient instru1=rfTestController.instru1;
    TextArea taLogs=rfTestController.taLogs;

    @Override
    public void handle(Event event) {
        if(!(instru0.isConnected && instru1.isConnected)){
            System.out.println("请检查仪表连接！");
            return;
        }
        if(InstruType.SMW200A.equals(instru0.instruType) && InstruType.NRP.equals(instru1.instruType)){

        }
    }
}
