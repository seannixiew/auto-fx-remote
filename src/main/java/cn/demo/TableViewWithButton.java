package cn.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TableViewWithButton extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<CountForTable> tableView = new TableView<>();

        // Set up the data model
        /** 3个Item，会画出3行。 */
        tableView.getItems().addAll(
                new CountForTable(1),new CountForTable(2),new CountForTable(3)
        );

//        tableView.setTableMenuButtonVisible(false);
        TableColumn<CountForTable, Integer> buttonColumn = new TableColumn<>("Button Column");
        TableColumn<CountForTable, Integer> textFieldColumn = new TableColumn<>("TextField Column");

        buttonColumn.setCellFactory(new Callback<TableColumn<CountForTable, Integer>, TableCell<CountForTable, Integer>>() {
            @Override
            public TableCell<CountForTable, Integer> call(TableColumn<CountForTable, Integer> param) {
                return new TableCell<CountForTable, Integer>() {
                    private final Button button = new Button("Click me");

                    {
                        button.setOnAction(event -> {
                            // Handle button click event
                            System.out.println("Button clicked for: " + getTableView().getItems().get(getIndex()));
                        });
                    }

                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            switch (getIndex()){
                                case 0:
                                    setGraphic(button);
                                    break;
                                case 1:
                                    setGraphic(new Button("1"));
                                    break;
                                case 2:
                                    setGraphic(new Button("2"));
                                    break;
                            }
                        }
                    }
                };
            }
        });

        textFieldColumn.setCellFactory(new Callback<TableColumn<CountForTable, Integer>, TableCell<CountForTable, Integer>>() {
            @Override
            public TableCell<CountForTable, Integer> call(TableColumn<CountForTable, Integer> param) {
                return new TableCell<CountForTable, Integer>() {
                    private final TextField textField = new TextField();

                    {
                        textField.setOnAction(event -> {
                            // Handle text field action event
                            System.out.println("TextField value changed to: " + textField.getText());
                        });
                    }

                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(textField);
                        }
                    }
                };
            }
        });


        // Add columns to the table
        tableView.getColumns().addAll(buttonColumn, textFieldColumn);

        HBox root = new HBox(tableView);
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("TableView with Components Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    class CountForTable{
        public Integer count;

        public CountForTable(Integer i){
            this.count=i;
        }

        public Integer getCount() {
            return count;
        }
    }
}
