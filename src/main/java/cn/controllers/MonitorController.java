package cn.controllers;

import cn.controllers.root.RootController;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MonitorController extends RootController {

    @FXML
    Pane root;

    @FXML
    Accordion accorPane;




    public  void initialize(){
        accorPane.prefHeightProperty().bind(root.heightProperty());
    }
}
