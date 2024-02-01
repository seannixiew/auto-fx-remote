package cn.controllers;

import cn.controllers.root.RootController;
import cn.demo.ChartsDataGenerateDemo;
import cn.model.ChartsModel;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;


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

    @FXML
    void onStartPlot(){

//        ChartsDataGenerateDemo.test(); //临时方法，测试用
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        ChartsModel.chartsActive=true;





    }

    @FXML
    void onEndPlot(){

    }

    @FXML
    void initialize(){



    }

}
