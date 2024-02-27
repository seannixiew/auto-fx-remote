package cn.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TableViewData extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建一个 TableView 和按钮
        TableView<Person> tableView = new TableView<>();
        Button updateButton = new Button("Update");

        // 创建表格列
        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());

        TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

        // 将表格列添加到 TableView 中
        tableView.getColumns().addAll(firstNameColumn, lastNameColumn);

        // 创建一个 ObservableList 以保存表格数据
        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("John", "Doe"),
                new Person("Jane", "Smith"),
                new Person("Bob", "Johnson")
        );

        // 将数据设置到 TableView 中
        tableView.setItems(data);

        // 按钮点击事件：更新 TableView 中的数据
        updateButton.setOnAction(event -> {
            // 修改数据
            data.get(0).setFirstName("Updated John");
            data.get(1).setLastName("Updated Smith");
        });

        // 创建布局并将 TableView 和按钮添加到布局中
        VBox root = new VBox(tableView, updateButton);

        // 创建场景并将布局添加到场景中
        Scene scene = new Scene(root, 300, 200);

        // 设置场景并显示主舞台
        primaryStage.setScene(scene);
        primaryStage.setTitle("TableView Update Example");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 定义 Person 类，表示表格的数据模型
    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;

        public Person(String firstName, String lastName) {
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public SimpleStringProperty firstNameProperty() {
            return firstName;
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }
    }
}
