package cn.demo;

import cn.model.ChartsModel;

import java.util.Arrays;
import java.util.Random;

/**
 * 用于生成折线图数据的代码
 */
public class ChartsDataGenerateDemo {

    public  void test() {

        new Thread(()->{

            try {
                System.out.println("模拟ing...");
                Thread.sleep(1000);

                Random random=new Random();
                while (true){
                    ChartsModel.lineValues.clear();
                    for(int ch=1;ch<31;ch++) {
                        double val1 = random.nextDouble() * 8-4;
                        double val2 = random.nextDouble() * 4 - 2;
                        Thread.sleep(1000);
//                        System.out.println("通道" + ch + "测试完毕");

                        /**
                         * 模板代码
                         */
                        if (ChartsModel.chartsActive) {
//                            System.out.println("图表数据写入内存...");
                            ChartsModel.lineValues.add(Arrays.asList(ch, val1, val2));
                        }
                    }
               }
            }catch (Exception e){}
        }).start();
    }
}
