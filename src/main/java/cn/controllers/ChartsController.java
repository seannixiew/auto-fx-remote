package cn.controllers;

import cn.controllers.root.RootController;
import cn.demo.ChartsDataGenerateDemo;
import cn.model.ChartsModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.List;


public class ChartsController extends RootController {


    @FXML
    LineChart<Number,Number> lineChart;
    @FXML
    NumberAxis xLine;
    @FXML
    NumberAxis yLine;

    @FXML
    PieChart pieChart;

    @FXML
    Button btStartPlot;
    @FXML
    Button btEndPlot;

    ChartsModel.DataTask dataTask;
    XYChart.Series<Number,Number> xy1;
    XYChart.Series<Number,Number> xy2;

    int normal;
    int corner;
    int abnormal;
    int untested;

    @FXML
    void onStartPlot(){

        xy1.setName("小信号平坦度");  //此处为UI线程，setName()不可放入新建线程内
        xy2.setName("大信号平坦度");

        ChartsModel.chartsActive = true;

        if (dataTask.isRunning() == false) { //isRunning()不可放入新建线程内
            dataTask.start();
        }
    }

    @FXML
    void onEndPlot(){
        dataTask.cancel();
        dataTask.reset();
    }

    @FXML
    void initialize(){

/** 模拟专用代码 */
        Thread thread=new Thread(()-> {
            new ChartsDataGenerateDemo().test();
        });
        thread.setDaemon(true);
        thread.start();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("正常", 0),
                new PieChart.Data("临界", 0),
                new PieChart.Data("异常", 0),
                new PieChart.Data("待测", 100));
        pieChart.setData(pieChartData);
        pieChart.setClockwise(true);
        pieChart.setAnimated(false);
        pieChart.setLabelsVisible(true);

        xy1=new XYChart.Series<>();
        xy2=new XYChart.Series<>();

        lineChart.getData().add(xy1);
        lineChart.getData().add(xy2);

        dataTask=new ChartsModel.DataTask();
        dataTask.setDelay(Duration.seconds(0));
        dataTask.setPeriod(Duration.seconds(1));

        dataTask.valueProperty().addListener(new ChangeListener<Integer>() { //监听的是结果画图集合的大小
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                if(newValue!=null && newValue<25) { //oldValue始终是null？
                    if(ChartsModel.lineValues.size()!=0) {

                        System.out.println("测试结果有" + newValue + "组");
                        List dataGroups = ChartsModel.lineValues.getLast();
                        System.out.println(dataGroups);
                        int ch=((Number) dataGroups.get(0)).intValue();
                        double val1=((Number) dataGroups.get(1)).doubleValue();
                        double val2=((Number) dataGroups.get(2)).doubleValue();
                        XYChart.Data<Number, Number> data1 = new XYChart.Data<>(ch,val1);
                        XYChart.Data<Number, Number> data2 = new XYChart.Data<>(ch, val2);

                        xy1.getData().add(data1);
                        xy2.getData().add(data2);

                        // TODO: 2024/2/2 判据定义
                        if (ch > 20) {
                            xLine.setLowerBound(ch - 20);
                            xLine.setUpperBound((ch));
                        }

                        if(val1<3.5 && val1>-3.5){
                            normal++;
                        }else if(val1>3.5 || val1<-3.5){
                            corner++;
                        }else {
                            abnormal++;
                        }

                        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                                new PieChart.Data("正常", normal),
                                new PieChart.Data("临界", corner),
                                new PieChart.Data("异常", abnormal),
                                new PieChart.Data("待测", 120-ch));
                        pieChart.setData(pieChartData);
                    }
                }
            }
        });


    }

}
