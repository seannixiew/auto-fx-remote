package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.utils.ControllersManager;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;

import java.util.List;

public class ConsistecnyAd implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    InstrumentClient instru2=rfTestController.instru2;  //网分
    TextArea taLogs=rfTestController.taLogs;
    TextArea taResults=rfTestController.taResults;

    MatrixClient matrix0=rfTestController.matrix0;

    public List<String> offeredChannelsA;

    @Override
    public void handle(Event event) {

        System.out.println("执行ad一致性测试...");
        Platform.runLater(() -> {
            taLogs.appendText("开始执行ad线性度测试...");
        });

    }
}
