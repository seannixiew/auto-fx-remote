package cn.controllers;

import cn.controllers.root.RootController;
import cn.instr.PowerSupplyClient;
import cn.model.PowerSupplyModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

import java.util.Timer;
import java.util.TimerTask;


public class MonitorController extends RootController {

    @FXML
    Pane root;

    @FXML
    Accordion accorPane;

    @FXML
    TableView<PowerSupplyModel> tv0;
    @FXML
    TableColumn tcIp;
    @FXML
    TableColumn tcPort;
    @FXML
    TableColumn tcStart;
    @FXML
    TableColumn<PowerSupplyModel,String> tcVoltage;
    @FXML
    TableColumn<PowerSupplyModel,String> tcCurrent;
    @FXML
    TableColumn tcOn;
    @FXML
    TableColumn tcOff;
    @FXML
    TextArea taScpiReader;

    boolean[] monitorings={false,false,false};

    PowerSupplyClient[] powerSupplyClient=new PowerSupplyClient[3];

    TextField[] tfIpArray=new TextField[3];
    TextField[] tfPortArray=new TextField[3];
    Button[] btStartArray=new Button[3];
    PowerSupplyModel[] data=new PowerSupplyModel[3];
    {
        for(int i=0;i<3;i++){
            data[i]=new PowerSupplyModel("-1","-1");
        }
    }


    public  void initialize(){

        Timer[] timers=new Timer[3];
        TimerTask[] timerTasks=new TimerTask[3];

        tcIp.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setGraphic(null);
                        }else {
                            setGraphic(tfIpArray[getIndex()]=new TextField());
                        }

                    }
                };
            }
        });

        tcPort.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setGraphic(null);
                        }else {
                            setGraphic(tfPortArray[getIndex()]=new TextField());
                        }

                    }
                };
            }
        });

        tcStart.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setGraphic(null);
                        }else {
                            setGraphic(btStartArray[getIndex()]=new Button("Monitor"));

                            btStartArray[getIndex()].setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {

                                    monitorings[getIndex()]=!monitorings[getIndex()];

                                    if(monitorings[getIndex()]) {

                                        powerSupplyClient[getIndex()] = new PowerSupplyClient();
                                        String ip = tfIpArray[getIndex()].getText().trim();
                                        int port = Integer.parseInt(tfPortArray[getIndex()].getText());

                                        if (!powerSupplyClient[getIndex()].connect(ip, port)) {
                                            System.out.println("电源连接失败...");
                                            Platform.runLater(() -> {
                                                taScpiReader.appendText("电源连接失败...");
                                            });
                                            return;
                                        }

                                        Platform.runLater(() -> {
                                            taScpiReader.appendText(powerSupplyClient[getIndex()].writeAndRead("*IDN?"));
                                        });

                                        timers[getIndex()] = new Timer(true);
                                        timerTasks[getIndex()] =new TimerTask() {
                                            @Override
                                            public void run() {
                                                String current = powerSupplyClient[getIndex()].writeAndRead("MEASure:CURRent?");
                                                String voltage = powerSupplyClient[getIndex()].writeAndRead("MEASure:VOLTage?");
                                                System.out.println("voltage:" + voltage.trim() + "; current:" + current.trim());
                                                data[getIndex()].setVoltage(voltage);
                                                data[getIndex()].setCurrent(current);
                                            }
                                        };
                                        timers[getIndex()].schedule(timerTasks[getIndex()], 1000, 500);
                                        btStartArray[getIndex()].setStyle("-fx-background-color: green");
                                        btStartArray[getIndex()].setText("停      止");
                                    }else {
                                        if(timerTasks[getIndex()]!=null) {
                                            timerTasks[getIndex()].cancel();
                                        }
                                        if(timers[getIndex()]!=null) {
                                            timers[getIndex()].purge();
                                            timers[getIndex()].cancel();
                                        }
                                        btStartArray[getIndex()].setStyle(null);
                                        btStartArray[getIndex()].setText("Monitor");
                                    }

                                }
                            });
                        }

                    }
                };
            }
        });

        tcOn.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setGraphic(null);
                        }else {
                            setGraphic(new Button("ON"));
                        }

                    }
                };
            }
        });

        tcOff.setCellFactory(new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(TableColumn param) {
                return new TableCell(){
                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setGraphic(null);
                        }else {
                            setGraphic(new Button("OFF"));
                        }

                    }
                };
            }
        });


        tcVoltage.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PowerSupplyModel, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<PowerSupplyModel, String> param) {
                return param.getValue().voltageProperty();
            }
        });

        tcCurrent.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<PowerSupplyModel, String>, ObservableValue<String>>() { //ObservableValue是Property的基类
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<PowerSupplyModel, String> param) {
                return param.getValue().currentProperty();
            }
        });

        tv0.getItems().addAll(data[0],data[1],data[2]);

//new Thread(()->{
//    try {
//
//        Thread.sleep(10000);
//        data[0].setVoltage("999");
//        Thread.sleep(3000);
//        data[0].setVoltage("777");
//    }catch (Exception e){}
//}).start();

    }
}
