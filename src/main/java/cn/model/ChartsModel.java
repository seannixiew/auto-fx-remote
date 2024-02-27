package cn.model;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.util.*;



public class ChartsModel {

    //事件修改，用于激活结果集合存储
    public static boolean chartsActive=false;

    // TODO: 2024/2/2 改成堆栈，同时不要监听大小
    public static LinkedList<List<Number>> lineValues=new LinkedList<>(); //[ch1,val1,val2],[ch2,val1,val2]...


    //异步任务，返回结果集合的大小给dataTask的value属性
    public static class DataTask extends ScheduledService<Integer>{
        @Override
        protected Task<Integer> createTask() {
            Task<Integer> task=new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    return lineValues.size();
                }
            };
            return task;
        }
    }


}
