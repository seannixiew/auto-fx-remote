package cn.controllers.outline;

import cn.controllers.ChartsController;
import cn.controllers.PropertiesController;
import cn.controllers.RfTestController;
import cn.controllers.root.RootController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class ViewController extends RootController {

    @FXML
    Pane containerPane;
    @FXML
    Pane rfTestPane;
    @FXML
    Pane chartsPane;
    @FXML
    Pane monitorPane;
    @FXML
    Pane propertiesPane;
    @FXML
    Pane otherClientPane;


    @FXML
    PropertiesController propertiesPaneController;

    @FXML
    RfTestController rfTestPaneController;

    @FXML
    ChartsController chartsPaneController;

    public RfTestController getRfTestPaneController() {
        return rfTestPaneController;
    }

    public PropertiesController getPropertiesPaneController() {
        return propertiesPaneController;
    }

    public ChartsController getChartsPaneController() {
        return chartsPaneController;
    }

    public void showRfTestPane(){
        rfTestPane.setVisible(true);
        chartsPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);
        otherClientPane.setVisible(false);

    }

    public void showChartsPane(){
        chartsPane.setVisible(true);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);
        otherClientPane.setVisible(false);


    }

    public void showMonitorPane(){
        monitorPane.setVisible(true);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        propertiesPane.setVisible(false);
        otherClientPane.setVisible(false);

    }

    public void showPropertiesPane(){
        propertiesPane.setVisible(true);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        otherClientPane.setVisible(false);

    }

    public void showOtherClientPane(){
        otherClientPane.setVisible(true);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);

    }

    public void initialize() throws IOException {



    }
}
