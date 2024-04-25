package cn.dispatcher;

import cn.controllers.RfTestController;
import cn.handler.tx.PowerInit;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.model.TestItemModel;
import cn.utils.CommonUtils;
import cn.utils.ControllersManager;
import cn.utils.DateFormat;
import cn.utils.TeeOutputStream;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.ToggleSwitch;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

public class MainTestDispatcher {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    ObservableList<TreeItem<TestItemModel>> selectedItems=rfTestController.selectedItems;
    TextArea taLogs=rfTestController.taLogs;

    ComboBox<String> cbLoop=rfTestController.cbLoop;

    public Object HandlerInstance;


    public void testHandlerDispatcher( Event event){

        new Thread(()-> {

            //此处仅打印
            char mode=cbLoop.getValue().charAt(5);
            System.out.println("遍历模式："+mode);

            try {
                String time = DateFormat.FORFILENAME.format(new Date());
                PrintStream txtPrintStream = new PrintStream("E:\\testLogs\\log-" + time + ".txt");
                PrintStream console = System.out;
                System.setOut(new PrintStream(new TeeOutputStream(console, txtPrintStream)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (TreeItem<TestItemModel> item : selectedItems) {

                TestItemModel testItem = item.getValue();
                try {
                    Class handlerClass = Class.forName(testItem.getHandlerName());
                    HandlerInstance = handlerClass.newInstance();
                    handlerClass.getMethod("handle", Event.class).invoke(HandlerInstance, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                System.out.println(testItem.getName()+"---测试完成！");
//                Platform.runLater(()->{
//                    taLogs.appendText(testItem.getName()+"---测试完成！");
//                });
            }

//            System.out.println("所有测试项完毕。");
//            Platform.runLater(()->{
//                taLogs.appendText("所有测试项完毕。");
//            });

        }).start();

    }
}
