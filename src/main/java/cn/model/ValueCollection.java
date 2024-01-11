package cn.model;

import java.util.Arrays;
import java.util.List;

public class ValueCollection {

    public static List<FreqAndPower> vsgList= Arrays.asList(
            new FreqAndPower("100MHz","5"),
            new FreqAndPower("120MHz","6")
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
