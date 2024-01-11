package cn.dispatcher;

import cn.controllers.RfTestController;
import cn.handler.tx.PowerInit;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.model.TestItemModel;
import cn.utils.CommonUtils;
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

    MatrixClient matrix0= rfTestController.matrix0;
    MatrixClient matrix1= rfTestController.matrix1;


    public void testHandlerDispatcher( Event event){

        if(tsA.isSelected() && tsB.isSelected()){ //遍历操作两个矩阵
            System.out.println("遍历操作两个矩阵");


        }else if(tsA.isSelected() && !tsB.isSelected()){ //遍历操作单个矩阵X
            if(matrix0==null || offeredChannelsA==null || offeredChannelsA.size()==0){
                CommonUtils.warningDialog("矩阵配置异常","矩阵未连接或未输入通道！");
                return;
            }
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
                    //固定等待，执行下一个测试项
                }
            }


        }else if(!tsA.isSelected() && tsB.isSelected()){ //遍历操作单个矩阵Y
            System.out.println("遍历操作单个矩阵Y");


        }else {//不操作任何矩阵
            System.out.println("不操作任何矩阵");

        }

    }
}
