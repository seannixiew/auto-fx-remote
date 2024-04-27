package cn.controllers;

import cn.controllers.popup.PopupControllerA;
import cn.controllers.popup.PopupControllerB;
import cn.controllers.root.RootController;
import cn.dispatcher.MainTestDispatcher;
import cn.handler.ad.DaForTest;
import cn.instr.DbfClient;
import cn.model.InstruKind;
import cn.model.InstruType;
import cn.model.TestItemModel;
import cn.model.TestItems;
import cn.utils.CommonUtils;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.utils.DateFormat;
import cn.utils.ThreadAndProcessPools;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.poi.ss.usermodel.RichTextString;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.ToggleSwitch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class RfTestController extends RootController {

    @FXML
    public Pane vb0;
    @FXML
    public Pane ap0;

    public ObservableList<TreeItem<TestItemModel>> selectedItems;

    @FXML
    public ListView<String> listView;
    @FXML
    public TitledPane tp0;
    @FXML
    public TitledPane tp1;
    @FXML
    public TitledPane tp2;
    @FXML
    public ComboBox cbVSG;
    @FXML
    public  ComboBox cbSA;
    @FXML
    public ComboBox cbVNA;
    @FXML
    public ComboBox cbPowerMeter;
    @FXML
    public ComboBox cbOsc;
    @FXML
    public ToggleSwitch tsMatrix0;
    @FXML
    public ToggleSwitch tsMatrix1;
    @FXML
    public TextArea taResults;
    @FXML
    public TextArea taLogs;
    @FXML
    public Button btConnection0;
    @FXML
    public Button btConnection1;
    @FXML
    public Button btConnection2;
    @FXML
    public Button btConnection3;
    @FXML
    public TextField tf0;
    @FXML
    public TextField tf1;
    @FXML
    public TextField tf2;
    @FXML
    public TextField tf3;
    @FXML
    public TextField tf4;
    @FXML
    public Button btStart;
    @FXML
    public Button btEnd;
    @FXML
    public Button btCustom;
    @FXML
    public ToggleButton btDownload;
    @FXML
    public Button btGroundTest;
    @FXML
    public Label lbErrors;

    @FXML
    public ComboBox<String> cbLoop;

    public PopupControllerA popupControllerA;
    public PopupControllerB popupControllerB;


    public InstrumentClient instru0=new InstrumentClient(InstruKind.vectorSignalGenerator);
    public InstrumentClient instru1=new InstrumentClient(InstruKind.signalAnalyzer);
    public InstrumentClient instru2=new InstrumentClient(InstruKind.vna);
    public InstrumentClient instru3=new InstrumentClient(InstruKind.powerMetre);
    public InstrumentClient instru4=new InstrumentClient(InstruKind.Oscilloscope);
    public MatrixClient matrix0=new MatrixClient();
    public MatrixClient matrix1=new MatrixClient();
    public DbfClient dbfClient=new DbfClient();

    public List<String> offeredChannels0;
    public List<String> offeredChannels1;

    public MainTestDispatcher mainTestDispatcher;

    boolean ifMaxHeightTp2=false;
    double lastHeightTp0;
    double lastHeightTp1;
    double lastHeightTp2;

    @FXML
    void onMouClic(){

        if(!ifMaxHeightTp2) {
            ifMaxHeightTp2=true;
            lastHeightTp0=tp0.getHeight();
            lastHeightTp1=tp1.getHeight();
            lastHeightTp2=tp2.getHeight();
            Platform.runLater(()->{
                tp0.setMaxHeight(0);
                tp1.setMaxHeight(0);
                tp2.setPrefHeight(lastHeightTp0 + lastHeightTp1 +lastHeightTp2 );
                tp2.setStyle("-fx-border-color: #FF7744 ");
            });
        }else {
            ifMaxHeightTp2=false;
            tp0.setMaxHeight(9999);
            tp1.setMaxHeight(9999);
            Platform.runLater(()->{
                tp0.setPrefHeight(lastHeightTp0);
                tp1.setPrefHeight(lastHeightTp1);
                tp2.setPrefHeight(lastHeightTp2);
                tp2.setStyle(null);
            });
        }
    }

    @FXML
    void onActionBtStart(Event event){
//        if(listView.getItems().size()==0 || (instru0.isConnected || instru1.isConnected || instru2.isConnected || instru3.isConnected)==false){
//            Platform.runLater(()->{
//                taLogs.appendText(DateFormat.FORLOG.format(new Date())+"未配置测试项，或者未连接任何测试仪器！\n");
//                System.out.println("未配置测试项，或者未连接任何测试仪器！");
//            });
//            return;
//        }
        System.out.println("测试启动...");

        mainTestDispatcher=new MainTestDispatcher();
        mainTestDispatcher.testHandlerDispatcher(event);
    }


    @FXML
    void onActionBtEnd(){
        //注意！多个测试项同时运行时，一次只终止一个测试项
        boolean res=ThreadAndProcessPools.clearProcessAndThread();
        Platform.runLater(()->{
            taLogs.appendText("reader & writer 线程池 及 vivado 进程池 清空状态："+res+"\n");
        });
    }

    @FXML
    void onActionBtCustom(){
        // TODO: 2024/2/27 临时方案，必须处理
        if(true){
            LockSupport.unpark(DaForTest.writerAndTester);
        }
    }


    @FXML
    void onActionBtGroundTest(){
        new Thread(()->{
            String res=dbfClient.connect("",0);
            taLogs.appendText(res);
        }).start();
    }

    @FXML
    void onActionBtConnection0(){
        new Thread(()->{
            String ip=tf0.getText().trim();
            if (ip.isEmpty()){
                System.out.println("ip为空！");
                CommonUtils.warningDialog("ip异常","ip地址为空！");
                return;
            }
            String cbValue=(String) cbVSG.getValue();
            if(cbValue==null){
                CommonUtils.warningDialog("仪表型号异常！","请选择型号！");
                return;
            }

            boolean isOpen=instru0.open(cbValue,ip);
            if(!isOpen){
                System.out.println("connecting failed.");
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"信号源连接失败...\n");
                });
                return;
            }

            System.out.println("是否连接："+instru0.isConnected+"，标识仪表型号为："+instru0.instruType+"，仪表实例为："+instru0);
            instru0.writeCmd("*IDN?");
            String s = instru0.readResult();
            System.out.println(s);
            Platform.runLater(()->{
                taLogs.appendText(DateFormat.FORLOG.format(new Date())+s);
            });
        }).start();
    }


    @FXML
    void onActionBtConnection1(){
        new Thread(()->{
            String ip=tf1.getText().trim();
            if (ip.isEmpty()){
                System.out.println("ip为空！");
                CommonUtils.warningDialog("ip异常","ip地址为空！");
                return;
            }
            boolean isOpen=instru1.open("",ip);
            if(!isOpen){
                System.out.println("connecting failed.");
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"频谱连接失败...\n");
                });
                return;
            }
            instru1.writeCmd("*IDN?");
            String s = instru1.readResult();
            System.out.println(s);
            Platform.runLater(()->{
                taLogs.appendText(DateFormat.FORLOG.format(new Date())+s);
            });
        }).start();
    }

    @FXML
    void onActionBtConnection2(){
        new Thread(()->{
            String ip=tf2.getText().trim();
            if (ip.isEmpty()){
                System.out.println("ip为空！");
                CommonUtils.warningDialog("ip异常","ip地址为空！");
                return;
            }
            String cbValue=(String) cbVNA.getValue();
            if(cbValue==null){
                CommonUtils.warningDialog("仪表型号异常！","请选择型号！");
                return;
            }

            boolean isOpen=instru2.open(cbValue,ip);
            if(!isOpen){
                System.out.println("connecting failed.");
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"矢网连接失败...\n");
                });
                return;
            }
            System.out.println("是否连接："+instru2.isConnected+"，标识仪表型号为："+instru2.instruType+"，仪表实例为："+instru2);
            instru2.writeCmd("*IDN?");
            String s = instru2.readResult();
            System.out.println(s);
            Platform.runLater(()->{
                taLogs.appendText(DateFormat.FORLOG.format(new Date())+s);
            });
        }).start();
    }

    @FXML
    void onActionBtConnection3(){
        new Thread(()->{
            String ip=tf3.getText().trim();
            if (ip.isEmpty()){
                System.out.println("ip为空！");
                CommonUtils.warningDialog("ip异常","ip地址为空！");
                return;
            }
            boolean isOpen=instru3.open("",ip);
            if(!isOpen){
                System.out.println("connecting failed.");
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"功率计连接失败...\n");
                });
                return;
            }
            instru3.writeCmd("*IDN?");
            String s = instru3.readResult();
            System.out.println(s);
            Platform.runLater(()->{
                taLogs.appendText(DateFormat.FORLOG.format(new Date())+s);
            });
        }).start();
    }

    @FXML
    void onActionBtConnection4(){
        new Thread(()->{

            if(cbOsc.getValue().toString().equals(InstruType.MSOX3014A)){
                //专门适配InfiniiVision MSO-X 3014A
                tf4.setEditable(false);
                tf4.setText("USB0::0x0957::0x17A8::MY54310455::0::INSTR");

                String ip=tf4.getText().trim();
                boolean isOpen= instru4.openUsb("",ip);
                if(!isOpen){
                    System.out.println("connecting failed.");
                    Platform.runLater(()->{
                        taLogs.appendText(DateFormat.FORLOG.format(new Date())+"示波器连接失败...\n");
                    });
                    return;
                }
                instru4.writeCmd("*IDN?");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
                String s = instru4.readResult();
                System.out.println(s);
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+s);
                });
            }else {
                String ip=tf4.getText().trim();
                if (ip.isEmpty()){
                    System.out.println("ip为空！");
                    CommonUtils.warningDialog("ip异常","ip地址为空！");
                    return;
                }
                boolean isOpen=instru4.open("",ip);
                if(!isOpen){
                    System.out.println("connecting failed.");
                    Platform.runLater(()->{
                        taLogs.appendText(DateFormat.FORLOG.format(new Date())+"示波器连接失败...\n");
                    });
                    return;
                }
                instru4.writeCmd("*IDN?");
                String s = instru4.readResult();
                System.out.println(s);
                Platform.runLater(()->{
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+s);
                });
            }
        }).start();
    }


    public void initialize() throws Exception{

        CheckBoxTreeItem<TestItemModel> node0 = new CheckBoxTreeItem<TestItemModel>(TestItems.tx);
        node0.setExpanded(true);
        node0.getChildren().addAll(
                new CheckBoxTreeItem<TestItemModel>(TestItems.txPowerInit),
                new CheckBoxTreeItem<TestItemModel>(TestItems.txEvm),
                new CheckBoxTreeItem<TestItemModel>(TestItems.txNpr),
                new CheckBoxTreeItem<TestItemModel>(TestItems.txFlatness),
                new CheckBoxTreeItem<TestItemModel>(TestItems.txSuppresionOutIf),
                new CheckBoxTreeItem<TestItemModel>(TestItems.txNoiseOutBand),
                new CheckBoxTreeItem<TestItemModel>(TestItems.txConsisAmongChannels));

        CheckBoxTreeItem<TestItemModel> node1 = new CheckBoxTreeItem<TestItemModel>(TestItems.rx);
        node1.setExpanded(true);
        node1.getChildren().addAll(
                new CheckBoxTreeItem<TestItemModel>(TestItems.rxP1dB),
                new CheckBoxTreeItem<TestItemModel>(TestItems.rxGainAndHarmo),
                new CheckBoxTreeItem<TestItemModel>(TestItems.rxSuppresionOutIf),
                new CheckBoxTreeItem<TestItemModel>(TestItems.rx3rdIntercept),
                new CheckBoxTreeItem<TestItemModel>(TestItems.rxFlatness),
                new CheckBoxTreeItem<TestItemModel>(TestItems.rxConsisAmongChannels));

        CheckBoxTreeItem<TestItemModel> node2 = new CheckBoxTreeItem<TestItemModel>(TestItems.ad);
        node2.setExpanded(true);
        node2.getChildren().addAll(
                new CheckBoxTreeItem<TestItemModel>(TestItems.ad40FuncAndPerformance),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adTestDAFuncAndPerformance),
                new CheckBoxTreeItem<TestItemModel>(TestItems.ad40Isolation),
                new CheckBoxTreeItem<TestItemModel>(TestItems.ad40Consistency),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adInModuleSequenceStability),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adInModuleCalSynSignal),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adInModuleConsistency),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adInModulePpsSyn),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adInModuleFreqSwitchSynSignal));
        CheckBoxTreeItem<TestItemModel> node3 = new CheckBoxTreeItem<TestItemModel>(TestItems.da);
        node3.setExpanded(true);
        node3.getChildren().addAll(
                new CheckBoxTreeItem<TestItemModel>(TestItems.da40FuncAndPerformanceAndIso),
                new CheckBoxTreeItem<TestItemModel>(TestItems.daTestADFuncAndPerformance),
                new CheckBoxTreeItem<TestItemModel>(TestItems.da40Power),
                new CheckBoxTreeItem<TestItemModel>(TestItems.da40Consistency),
                new CheckBoxTreeItem<TestItemModel>(TestItems.daInModuleCalSynSignal),
                new CheckBoxTreeItem<TestItemModel>(TestItems.daInModuleFreqSwitchSynSignal));

        CheckBoxTreeItem<TestItemModel> node4 = new CheckBoxTreeItem<TestItemModel>(TestItems.dbf);
        node4.setExpanded(true);
        node4.getChildren().addAll(
                new CheckBoxTreeItem<TestItemModel>(TestItems.daAndRf),
                new CheckBoxTreeItem<TestItemModel>(TestItems.adAndRf),
                new CheckBoxTreeItem<TestItemModel>(TestItems.dbfAndRfTxPower),
                new CheckBoxTreeItem<TestItemModel>(TestItems.dbfAndRfTxConsistency),
                new CheckBoxTreeItem<TestItemModel>(TestItems.dbfAndRfRxConsistency));

        CheckBoxTreeItem<TestItemModel> virualNode = new CheckBoxTreeItem<TestItemModel>();
        virualNode.getChildren().addAll(node0,node1,node2,node3,node4);
        final CheckTreeView<TestItemModel> checkTreeView = new CheckTreeView<>(virualNode);
        checkTreeView.setShowRoot(false);
        AnchorPane.setLeftAnchor(checkTreeView,0.0);
        AnchorPane.setRightAnchor(checkTreeView,0.0);
        AnchorPane.setTopAnchor(checkTreeView,0.0);
        AnchorPane.setBottomAnchor(checkTreeView,0.0);


        // and listen to the relevant events (e.g. when the checked items change).
        checkTreeView.getCheckModel().getCheckedItems().addListener(new ListChangeListener<TreeItem<TestItemModel>>() {
            public void onChanged(ListChangeListener.Change<? extends TreeItem<TestItemModel>> c) {
                ObservableList<TreeItem<TestItemModel>> fullSelected=checkTreeView.getCheckModel().getCheckedItems();
                selectedItems=FXCollections.observableArrayList();
                ObservableList<String> tmp=FXCollections.observableArrayList();
                for(TreeItem<TestItemModel> treeItem:fullSelected){
                    if(treeItem.getValue().getId()<0){ //或者用父节点==null判定
                        continue;
                    }
                    selectedItems.add(treeItem);
                    tmp.add(treeItem.getParent().getValue()+"："+treeItem.getValue());
                }
                listView.setItems(tmp);
                System.out.println(selectedItems);
            }
        });
        ap0.getChildren().add(checkTreeView);


        cbVSG.setItems(FXCollections.observableArrayList(InstruType.SMW200A,InstruType.E8267D));
        cbSA.setItems(FXCollections.observableArrayList(InstruType.FSW,InstruType.N9040B,InstruType.FSU));
        cbVNA.setItems(FXCollections.observableArrayList(InstruType.ZNB));
        cbPowerMeter.setItems(FXCollections.observableArrayList(InstruType.NRP));
        cbOsc.setItems(FXCollections.observableArrayList(InstruType.MSOX3014A));
//        cbVSG.setStyle("-fx-background-color: red");
//        cbVSG.setMouseTransparent(true);    用于校验

        btDownload.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    btDownload.setStyle("-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");
                }else {
                    btDownload.setStyle(null);
                }
            }
        });


        FXMLLoader fxmlLoader0=new FXMLLoader();
        fxmlLoader0.setLocation(getClass().getResource("/fxml/popover0.fxml"));
        Pane popRoot0=fxmlLoader0.load();
        popupControllerA=fxmlLoader0.getController();

        PopOver popOver0=new PopOver();
        popOver0.setAutoHide(false);
        popOver0.setTitle("矩阵X配置");
        popOver0.setHeaderAlwaysVisible(true);
        popOver0.setCloseButtonEnabled(true);
        popOver0.setContentNode(popRoot0);

        tsMatrix0.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
                if(newVal==true) {
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"矩阵0 Involved！\n");
                    popOver0.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
                    popOver0.show(tsMatrix0);

                }
                if(newVal==false){
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"矩阵0 Excluded！\n");
                    popOver0.hide();
                }
            }
        });

//
//        popupControllerA.btConnection.setOnAction(event -> {
//            new Thread(()-> {
//                long port = 3000; //不必支持修改端口，可以使用配置文件
//                String ip = popupControllerA.tfIP.getText().trim();
//                if (ip.isEmpty()){
//                    System.out.println("ip为空！");
//                    CommonUtils.warningDialog("ip异常","ip地址为空！");
//                    return;
//                }
//                if (matrix0.connect(ip, port) == 1) {
//                    Platform.runLater(()->{
//                        taLogs.appendText(DateFormat.FORLOG.format(new Date())+"Matrix0 已连接" + "\n");
//                    });
//                } else {
//                    Platform.runLater(()->{
//                        taLogs.appendText(DateFormat.FORLOG.format(new Date())+"超时！Matrix0" + "\n");
//                    });
//                }
//            }).start();
//        });
//
//        popupControllerA.btInputAll.setOnAction(event -> {
//            Platform.runLater(()->{
//                popupControllerA.taChannels.setText("1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 " +
//                        "25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 " +
//                        "55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 " +
//                        "85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100 101 102 103 104 105 106 107 108 109 110 " +
//                        "111 112 113 114 115 116 117 118 119 120 121 122 123 124 125 126 127 128");
//            });
//        });
//
//        popupControllerA.btConfirm.setOnAction(event -> {
//            String text=popupControllerA.taChannels.getText().trim();
//            String[] channels=text.split("\\s+");
//            offeredChannels0=new ArrayList<>();
//            for(String channel:channels){
//                channel="A"+channel.trim();
//                offeredChannels0.add(channel);
//            }
//            Platform.runLater(()->{
//                taLogs.appendText(DateFormat.FORLOG.format(new Date())+"即将遍历操作的通道为："+text+"\n");
//                System.out.println("即将遍历操作的通道为："+text);
//            });
//        });


        FXMLLoader fxmlLoader1=new FXMLLoader();
        fxmlLoader1.setLocation(getClass().getResource("/fxml/popover1.fxml"));
        Pane popRoot1=fxmlLoader1.load();
        popupControllerB=fxmlLoader1.getController();

        PopOver popOver1=new PopOver();
        popOver1.setAutoHide(false);
        popOver1.setTitle("矩阵Y配置");
        popOver1.setHeaderAlwaysVisible(true);
        popOver1.setCloseButtonEnabled(true);
        popOver1.setContentNode(popRoot1);

        tsMatrix1.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
                if(newVal==true) {
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"矩阵1 Involved！\n");
                    popOver1.setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
                    popOver1.show(tsMatrix1);

                }
                if(newVal==false){
                    taLogs.appendText(DateFormat.FORLOG.format(new Date())+"矩阵1 Excluded！\n");
                    popOver1.hide();
                }
            }
        });
//
//
//        popupControllerB.btConnection.setOnAction(event -> {
//            new Thread(()-> {
//                long port = 3000; //不必支持修改端口，可以使用配置文件
//                String ip = popupControllerB.tfIP.getText().trim();
//                if (ip.isEmpty()){
//                    System.out.println("ip为空！");
//                    CommonUtils.warningDialog("ip异常","ip地址为空！");
//                    return;
//                }
//                if (matrix1.connect(ip, port) == 1) {
//                    Platform.runLater(()->{
//                        taLogs.appendText(DateFormat.FORLOG.format(new Date())+"Matrix1 已连接" + "\n");
//                    });
//                } else {
//                    Platform.runLater(()->{
//                        taLogs.appendText(DateFormat.FORLOG.format(new Date())+"超时！Matrix1" + "\n");
//                    });
//                }
//            }).start();
//        });
//
//        popupControllerB.btInputAll.setOnAction(event -> {
//            Platform.runLater(()->{
//                popupControllerB.taChannels.setText("1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 " +
//                        "25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 " +
//                        "55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 " +
//                        "85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100 101 102 103 104 105 106 107 108 109 110 " +
//                        "111 112 113 114 115 116 117 118 119 120 121 122 123 124 125 126 127 128");
//            });
//        });
//
//        popupControllerB.btConfirm.setOnAction(event -> {
//            String text=popupControllerB.taChannels.getText().trim();
//            String[] channels=text.split("\\s+");
//            offeredChannels1=new ArrayList<>();
//            for(String channel:channels){
//                channel="A"+channel.trim();
//                offeredChannels1.add(channel);
//            }
//            Platform.runLater(()->{
//                taLogs.appendText(DateFormat.FORLOG.format(new Date())+"即将遍历操作的通道为："+text+"\n");
//                System.out.println("即将遍历操作的通道为："+text);
//            });
//        });

        cbLoop.getItems().addAll("遍历模式：0","遍历模式：1","遍历模式：2","遍历模式：3","遍历模式：4","遍历模式：5");
        cbLoop.getSelectionModel().select(0);

    }

}
