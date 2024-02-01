package cn.controllers.outline;

import cn.controllers.ChartsController;
import cn.controllers.PropertiesController;
import cn.controllers.RfTestController;
import cn.controllers.root.RootController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
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
    Pane groundTestPane;
    @FXML
    Pane vivadoClientPane;

    //直接注入子Controller，特殊规则
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
    @FXML
    Button btRfTest;
    @FXML
    Button btCharts;
    @FXML
    Button btMonitor;
    @FXML
    Button btProperties;
    @FXML
    Button btGroundTest;
    @FXML
    Button btVivadoClient;

    public void showRfTestPane(){

        btRfTest.setStyle("-fx-background-color:#95CACA; -fx-underline: true;-fx-effect: innershadow(gaussian,  #666666, 5, 0.5, 2, 2) ");
        btCharts.setStyle(null);
        btMonitor.setStyle(null);
        btProperties.setStyle(null);
        btGroundTest.setStyle(null);
        btVivadoClient.setStyle(null);

        rfTestPane.setVisible(true);
        chartsPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);
        groundTestPane.setVisible(false);
        vivadoClientPane.setVisible(false);

    }

    public void showChartsPane(){

        btRfTest.setStyle(null);
        btCharts.setStyle("-fx-background-color:#95CACA;-fx-underline: true;-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");
        btMonitor.setStyle(null);
        btProperties.setStyle(null);
        btGroundTest.setStyle(null);
        btVivadoClient.setStyle(null);

        chartsPane.setVisible(true);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);
        groundTestPane.setVisible(false);
        vivadoClientPane.setVisible(false);


    }

    public void showMonitorPane(){

        btRfTest.setStyle(null);
        btCharts.setStyle(null);
        btMonitor.setStyle("-fx-background-color:#95CACA;-fx-underline: true;-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");
        btProperties.setStyle(null);
        btGroundTest.setStyle(null);
        btVivadoClient.setStyle(null);

        monitorPane.setVisible(true);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        propertiesPane.setVisible(false);
        groundTestPane.setVisible(false);
        vivadoClientPane.setVisible(false);

    }

    public void showPropertiesPane(){

        btRfTest.setStyle(null);
        btCharts.setStyle(null);
        btMonitor.setStyle(null);
        btProperties.setStyle("-fx-background-color:#95CACA;-fx-underline: true;-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");
        btGroundTest.setStyle(null);
        btVivadoClient.setStyle(null);

        propertiesPane.setVisible(true);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        groundTestPane.setVisible(false);
        vivadoClientPane.setVisible(false);

    }

    public void showGroundTestPane(){

        btRfTest.setStyle(null);
        btCharts.setStyle(null);
        btMonitor.setStyle(null);
        btProperties.setStyle(null);
        btGroundTest.setStyle("-fx-background-color:#95CACA;-fx-underline: true;-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");
        btVivadoClient.setStyle(null);

        groundTestPane.setVisible(true);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);
        vivadoClientPane.setVisible(false);

    }

    public void showVivadoClientPane(){

        btRfTest.setStyle(null);
        btCharts.setStyle(null);
        btMonitor.setStyle(null);
        btProperties.setStyle(null);
        btGroundTest.setStyle(null);
        btVivadoClient.setStyle("-fx-background-color:#95CACA;-fx-underline: true;-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");

        vivadoClientPane.setVisible(true);
        groundTestPane.setVisible(false);
        chartsPane.setVisible(false);
        rfTestPane.setVisible(false);
        monitorPane.setVisible(false);
        propertiesPane.setVisible(false);


    }

    public void initialize() throws IOException {

        btRfTest.setStyle("-fx-background-color:#95CACA;-fx-underline: true;-fx-effect: innershadow(gaussian, #666666, 5, 0.5, 2, 2)");

    }
}
