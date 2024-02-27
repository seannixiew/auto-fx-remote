package cn.model;

import javafx.beans.property.SimpleStringProperty;

public class PowerSupplyModel {

    SimpleStringProperty voltage;
    SimpleStringProperty current;

    public PowerSupplyModel(String voltage, String current) {
        this.voltage = new SimpleStringProperty(voltage);
        this.current = new SimpleStringProperty(current);
    }

    public String getVoltage() {
        return voltage.get();
    }

    public SimpleStringProperty voltageProperty() {
        return voltage;
    }

    public String getCurrent() {
        return current.get();
    }

    public SimpleStringProperty currentProperty() {
        return current;
    }

    public void setVoltage(String voltage) {
        this.voltage.set(voltage);
    }

    public void setCurrent(String current) {
        this.current.set(current);
    }
}
