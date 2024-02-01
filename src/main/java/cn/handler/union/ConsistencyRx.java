package cn.handler.union;

import javafx.event.Event;
import javafx.event.EventHandler;

public class ConsistencyRx implements EventHandler {



    @Override
    public void handle(Event event) {
        System.out.println("执行DBF+RF-接收一致性测试...");
        new Thread(()->{

        }).start();

    }
}
