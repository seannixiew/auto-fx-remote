package cn.demo;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class TableViewWithButton extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 创建表格
        TableView<Person> tableView = new TableView<>();


        tableView.setStyle("-fx-table-header-visible: false;");
        // 创建表格列
        TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
        TableColumn<Person, Void> actionColumn = new TableColumn<>("Action");

        // 设置列与数据模型的关联
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());

        // 添加列到表格
        tableView.getColumns().addAll(firstNameColumn, actionColumn);

        // 设置 "Action" 列的单元格工厂
        actionColumn.setCellFactory(column -> new TableCell<Person, Void>() {
            final Button button = new Button("Click Me");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    // 在单元格中添加按钮，并设置按钮的事件处理器
                    setGraphic(button);
                    button.setOnAction(event -> {
                        // 处理按钮点击事件
                        System.out.println("Button clicked for " + getTableRow().getItem());
                    });
                }
            }
        });

        // 添加数据到表格
        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("John", "Doe"),
                new Person("Jane", "Doe"),
                new Person("Jim", "Smith")
        );
        tableView.setItems(data);

        // 创建场景
        Scene scene = new Scene(tableView, 400, 200);

        // 设置舞台标题
        primaryStage.setTitle("TableView with Button Example");

        // 设置舞台场景
        primaryStage.setScene(scene);

        // 显示舞台
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 数据模型类
    public static class Person {
        private final String firstName;
        private final String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        // 下面的方法是为了在表格列中使用 JavaFX 属性绑定
        public StringProperty firstNameProperty() {
            return new SimpleStringProperty(firstName);
        }
    }
}
