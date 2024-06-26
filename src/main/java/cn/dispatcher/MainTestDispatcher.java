package cn.dispatcher;

import cn.controllers.RfTestController;
import cn.controllers.outline.ViewController;
import cn.handler.tx.PowerInit;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.model.TestItemModel;
import cn.utils.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.ToggleSwitch;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class MainTestDispatcher {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    ViewController viewController=(ViewController) ControllersManager.CONTROLLERS.get(ViewController.class.getSimpleName());
    TextArea taConsole=viewController.taConsole;
    TitledPane tpConsole=viewController.tpConsole;

    ObservableList<TreeItem<TestItemModel>> selectedItems=rfTestController.selectedItems;
    TextArea taLogs=rfTestController.taLogs;

    public Object HandlerInstance;


    public void testHandlerDispatcher( Event event){

        new Thread(()-> {

            try {
                String time = DateFormat.FORFILENAME.format(new Date());
                PrintStream txtPrintStream = new PrintStream("E:\\testLogs\\log-" + time + ".txt");
                PrintStream console = System.out;
                System.setOut(new PrintStream(new TeeOutputStream(console, txtPrintStream)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            VivadoErrorCounts.readErrorProperty.addListener((observable, oldValue, newValue) -> {
                if(newValue!=null && newValue!="") {
                    Platform.runLater(() -> {
                        taConsole.appendText(newValue);
                        tpConsole.setStyle("-fx-border-color: red");
                    });
                }
            });

            for (TreeItem<TestItemModel> item : selectedItems) {

                try {
                    //执行每个测试项前清理线程池和进程池
                    ThreadAndProcessPools.clearProcessAndThread();
                }catch (Exception e){
                    e.printStackTrace();
                }
                TestItemModel testItem = item.getValue();
                try {
                    Class handlerClass = Class.forName(testItem.getHandlerName());
                    HandlerInstance = handlerClass.newInstance();
                    handlerClass.getMethod("handle", Event.class).invoke(HandlerInstance, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(testItem.getName()+"---测试完成！");
                Platform.runLater(()->{
                    taLogs.appendText(testItem.getName()+"---测试完成！\n");
                });
            }

            System.out.println("所有测试项完毕。");
            Platform.runLater(()->{
                taLogs.appendText("所有测试项完毕。\n");
            });

        }).start();

    }
}
