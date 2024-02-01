package cn.controllers;

import cn.controllers.root.RootController;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

public class PropertiesController extends RootController {

    @FXML
    Pane root;

    public Button bt0=new Button("浏览");

    public Button bt1=new Button("浏览");

    public Button bt2=new Button("浏览");

    public Button bt3=new Button("浏览");

    public Button bt4=new Button("浏览");


    @FXML
    public void initialize(){

        ListView<Integer> listView=new ListView<>(); //泛型省略，则为Object?

        listView.getItems().addAll(0,1,2,3,4,5);

        listView.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> param) {
                return new PropertyListCell();
            }
        });

        root.getChildren().add(listView);

        AnchorPane.setLeftAnchor(listView,0.0);
        AnchorPane.setRightAnchor(listView,0.0);
        AnchorPane.setTopAnchor(listView,0.0);
        AnchorPane.setBottomAnchor(listView,0.0);
    }

    private  class PropertyListCell extends ListCell<Integer>{

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setGraphic(generateHBox(item));
            }
        }

        private HBox generateHBox(int item){
            HBox hbox=null;

            switch (item){
                case 0:
                    Label lbColor=new Label("可选，说明文档见C:/documents/configs.txt");
                    lbColor.setStyle("-fx-background-color: #1E90FF;");
                    AnchorPane apColor=new AnchorPane(lbColor);
                    hbox=new HBox(apColor);
                    AnchorPane.setLeftAnchor(lbColor,0.0);
                    AnchorPane.setRightAnchor(lbColor,0.0);
                    AnchorPane.setTopAnchor(lbColor,0.0);
                    AnchorPane.setBottomAnchor(lbColor,0.0);
                    HBox.setHgrow(apColor, Priority.ALWAYS);
                    break;
                case 1:
                    Label lb=new Label("结果保存路径");
                    TextField tf=new TextField();
                    hbox=new HBox(lb,tf,bt0);
                    HBox.setHgrow(tf, Priority.ALWAYS);
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER);
                    break;
                case 2:
                    Label lb1=new Label("输出偏置文件");
                    TextField tf1=new TextField();
                    hbox=new HBox(lb1,tf1,bt1);
                    HBox.setHgrow(tf1, Priority.ALWAYS);
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER);
                    break;
                case 3:
                    Label lb2=new Label("阈值判定规则");
                    TextField tf2=new TextField();
                    hbox=new HBox(lb2,tf2,bt2);
                    HBox.setHgrow(tf2, Priority.ALWAYS);
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER);
                    break;
                case 4:
                    Label lb3=new Label("日志保存路径");
                    TextField tf3=new TextField();
                    hbox=new HBox(lb3,tf3,bt3);
                    HBox.setHgrow(tf3, Priority.ALWAYS);
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER);
                    break;
                case 5:
                    Label lb4=new Label("IP及端口配置");
                    TextField tf4=new TextField();
                    hbox=new HBox(lb4,tf4,bt4);
                    HBox.setHgrow(tf4, Priority.ALWAYS);
                    hbox.setSpacing(10.0);
                    hbox.setAlignment(Pos.CENTER);
                    break;

            }

            return hbox;
        }
    }


}
