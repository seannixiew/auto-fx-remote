package cn;

import cn.controllers.PropertiesController;
import cn.controllers.RfTestController;
import cn.controllers.VivadoClientController;
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
import javafx.stage.StageStyle;
//import jfxtras.styles.jmetro.JMetro;
//import jfxtras.styles.jmetro.Style;

import java.io.File;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.getIcons().add(new Image("imgs/window.jpg"));
        primaryStage.setTitle("L载荷-桌面快测 V2.0");
        FXMLLoader fxmlLoader=new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/view.fxml"));
        Pane root=fxmlLoader.load();
        Scene scene=new Scene(root,1200,680);
/*
//        可行但不优雅
//        ViewController controller = fxmlLoader.getController();
//        controller.getPropertiesPaneController().bt0.setOnAction(event -> {
//            new Thread(()->{
//                FileChooser fileChooser=new FileChooser();
//                FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("CSV Files","*.csv","XLS Files","*.xls","XLSX Files","*.xlsx");
//                fileChooser.getExtensionFilters().add(extensionFilter);
//                Platform.runLater(()->{
//                    File file=fileChooser.showOpenDialog(primaryStage);
//                    System.out.println(file);
//                });
//            }).start();
//        });
*/
        //better
        PropertiesController propertiesController =(PropertiesController) ControllersManager.CONTROLLERS.get(PropertiesController.class.getSimpleName());
        propertiesController.setPrimaryStageAndBoundAction(primaryStage);

        //前提是对应fxml此时已加载完成
        //★ 用于在controller中使用primaryStage（由于该方法在main调用，所以顺序是：生成controller（执行完initialize）---main调用获取primayStage，所以不能在initialize中使用primaryStage）
        VivadoClientController vivadoClientController =(VivadoClientController) ControllersManager.CONTROLLERS.get(VivadoClientController.class.getSimpleName());
        vivadoClientController.setPrimaryStage(primaryStage);


//        new JMetro(Style.LIGHT).setScene(scene);  //暂时无法灵活调整细节
        scene.getStylesheets().add("css/beauty.css");
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
