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
        TableView<Person> tableView = new TableView<>();
//        tableView.setTableMenuButtonVisible(false);
        TableColumn<Person, Void> buttonColumn = new TableColumn<>("Button Column");
        TableColumn<Person, String> textFieldColumn = new TableColumn<>("TextField Column");

        buttonColumn.setCellFactory(createButtonCellFactory());
        textFieldColumn.setCellFactory(createTextFieldCellFactory());

        // Set up the data model
        tableView.getItems().addAll(
                new Person("John", "Doe"),
                new Person("Jane", "Smith"),
                new Person("Bob", "Johnson")
        );

        // Add columns to the table
        tableView.getColumns().addAll(buttonColumn, textFieldColumn);

        HBox root = new HBox(tableView);
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("TableView with Components Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Callback<TableColumn<Person, Void>, TableCell<Person, Void>> createButtonCellFactory() {
        return new Callback<TableColumn<Person, Void>, TableCell<Person, Void>>() {
            @Override
            public TableCell<Person, Void> call(TableColumn<Person, Void> param) {
                return new TableCell<Person, Void>() {
                    private final Button button = new Button("Click me");

                    {
                        button.setOnAction(event -> {
                            // Handle button click event
                            System.out.println("Button clicked for: " + getTableView().getItems().get(getIndex()));
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(button);
                        }
                    }
                };
            }
        };
    }

    private Callback<TableColumn<Person, String>, TableCell<Person, String>> createTextFieldCellFactory() {
        return new Callback<TableColumn<Person, String>, TableCell<Person, String>>() {
            @Override
            public TableCell<Person, String> call(TableColumn<Person, String> param) {
                return new TableCell<Person, String>() {
                    private final TextField textField = new TextField();

                    {
                        textField.setOnAction(event -> {
                            // Handle text field action event
                            System.out.println("TextField value changed to: " + textField.getText());
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            textField.setText(item);
                            setGraphic(textField);
                        }
                    }
                };
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }

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

        public SimpleStringProperty firstNameProperty() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public String getLastName() {
            return lastName.get();
        }

        public SimpleStringProperty lastNameProperty() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }
    }
}
