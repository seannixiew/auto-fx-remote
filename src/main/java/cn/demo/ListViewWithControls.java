package cn.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ListViewWithControls extends Application {

    @Override
    public void start(Stage primaryStage) {
        ListView<String> listView = new ListView<>();
        listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ButtonListCell();
            }
        });


        // 添加一些示例数据
        listView.getItems().addAll("Item 1", "Item 2", "Item 3");

        VBox root = new VBox(listView);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("ListView with Controls");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 自定义 ListCell，包含一个 Button
    private static class ButtonListCell extends ListCell<String> {
        private Button button;

        public ButtonListCell() {
            button = new Button("Click me");
            button.setOnAction(event -> {
                String item = getItem();
                System.out.println("Button clicked for item: " + item);
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(button);
            }
        }
    }
}
