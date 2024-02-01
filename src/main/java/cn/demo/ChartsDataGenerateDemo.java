package cn.demo;

import cn.model.ChartsModel;

import java.util.Arrays;
import java.util.Random;

public class ChartsDataGenerateDemo {

    public  void test() {

        new Thread(()->{

            try {
                System.out.println("testing...");
                Thread.sleep(3000);

                Random random=new Random();
                for(int ch=1;ch<121;ch++) {
                    double val1=random.nextDouble()*10;
                    double val2= random.nextDouble()*5-5;
                    Thread.sleep(1000);
                    System.out.println("通道"+ch+"测试完毕");

                    if (ChartsModel.chartsActive) {
                        System.out.println("图表数据写入内存...");
                        ChartsModel.lineValues.put(ch, Arrays.asList(val1,val2));
                    }

               }
            }catch (Exception e){}
        }).start();
    }
}
