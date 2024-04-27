package cn.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VivadoErrorCounts {

    public static StringProperty readErrorProperty=new SimpleStringProperty();

    public static void setReadError(String s){
        readErrorProperty.setValue(s); ;  //切忌new！！！
    }

}
