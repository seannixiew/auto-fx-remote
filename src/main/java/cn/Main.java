package cn;

import cn.controllers.outline.ViewController;
import cn.utils.ControllersManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image("imgs/window.jpg"));
        primaryStage.setTitle("L载荷-桌面快测 V2.0");
        FXMLLoader fxmlLoader=new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/view.fxml"));
        Pane root=fxmlLoader.load();
        Scene scene=new Scene(root,1200,680);

        ViewController controller = fxmlLoader.getController();
        controller.getPropertiesPaneController().bt0.setOnAction(event -> {
            new Thread(()->{
                FileChooser fileChooser=new FileChooser();
                FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("CSV Files","*.csv","XLS Files","*.xls","XLSX Files","*.xlsx");
                fileChooser.getExtensionFilters().add(extensionFilter);
                Platform.runLater(()->{
                    File file=fileChooser.showOpenDialog(primaryStage);
                    System.out.println(file);
                });
            }).start();
        });



        primaryStage.setScene(scene);
        primaryStage.show();
    }


    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("init...");
    }

    @Override
    public void stop() throws Exception {
        System.out.println("stop...");
        super.stop();
    }
}
