package cn.model;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ValueCollection {

    static Properties properties=new Properties();
    public static Double initPower=0.0;

    static {
        try {
            properties.load(new FileInputStream("src/main/resources/configs/config.properties"));
            String power=properties.getProperty("vsgPower");
            initPower= Double.parseDouble(power);
        }catch (Exception e){}

    }


    public static List<FreqAndPower> vsgList= Arrays.asList(

            new FreqAndPower("105MHz",(initPower)+""),
            new FreqAndPower("132.5MHz",(initPower)+""),
            new FreqAndPower("160MHz",(initPower)+""),
            new FreqAndPower("105MHz",(initPower-6)+""),
            new FreqAndPower("132.5MHz",(initPower-6)+""),
            new FreqAndPower("160MHz",(initPower-6)+""),
            new FreqAndPower("105MHz",(initPower-30)+""),
            new FreqAndPower("132.5MHz",(initPower-30)+""),
            new FreqAndPower("160MHz",(initPower-30)+""),
            new FreqAndPower("105MHz",(initPower-60)+""),
            new FreqAndPower("132.5MHz",(initPower-60)+""),
            new FreqAndPower("160MHz",(initPower-60)+"")

    );

    public static class FreqAndPower{
        public String freq;
        public String power;
        public String fileName;

        public FreqAndPower(String freq, String power) {
            this.freq = freq;
            this.power = power;
        }

        public FreqAndPower(String freq, String power, String fileName) {
            this.freq = freq;
            this.power = power;
            this.fileName = fileName;
        }

        public String getFreq() {
            return freq;
        }

        public String getPower() {
            return power;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
