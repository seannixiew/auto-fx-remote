package cn.dispatcher;

import cn.controllers.RfTestController;
import cn.handler.tx.PowerInit;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.model.TestItemModel;
import cn.utils.ControllersManager;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import org.controlsfx.control.ToggleSwitch;

import java.lang.reflect.Method;
import java.util.List;

public class MainTestDispatcher {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    ObservableList<TreeItem<TestItemModel>> selectedItems=rfTestController.selectedItems;
    ToggleSwitch tsA=rfTestController.tsA;
    ToggleSwitch tsB=rfTestController.tsB;
    TextArea taLogs=rfTestController.taLogs;
    List<String> offeredChannelsA=rfTestController.offeredChannelsA;
    List<String> offeredChannelsB=rfTestController.offeredChannelsB;

    public void testHandlerDispatcher( Event event){

        if(tsA.isSelected() && tsB.isSelected()){ //遍历操作两个矩阵
            System.out.println("遍历操作两个矩阵");

        }else if(tsA.isSelected() && !tsB.isSelected()){ //遍历操作单个矩阵X
            System.out.println("遍历操作单个矩阵X");
            for (String currChannel:offeredChannelsA) {   //注意：currChanel通道号前带“A”
                System.out.println("当前测试矩阵通道为"+currChannel);
                for(TreeItem<TestItemModel> item:selectedItems){

                    TestItemModel testItem=item.getValue();
                    try {
                        Class handlerClass=Class.forName(testItem.getHandlerName());
                        handlerClass.getMethod("handle",Event.class).invoke(handlerClass.newInstance(),event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //统一，反射调用测试项进行测试；逐个；期间等待固定时间；
                //在每个测试方法中，识别仪表型号，根据不同型号执行不同流程
            }
        }else if(!tsA.isSelected() && tsB.isSelected()){ //遍历操作单个矩阵Y
            System.out.println("遍历操作单个矩阵Y");

        }else {//不操作任何矩阵
            System.out.println("不操作任何矩阵");

        }

    }
}
