package cn.demo;

import cn.instr.InstrumentClient;

public class VSA89600 {
    public static void main(String[] args) throws Exception {

        InstrumentClient instrumentClient=new InstrumentClient();
        instrumentClient.openHislip("","192.168.1.137");
        Thread.sleep(1000);
        instrumentClient.writeCmd("*IDN?");
        String res=instrumentClient.readResult();
        System.out.println(res);
        instrumentClient.writeCmd(":FREQuency:CENTer?");
        res=instrumentClient.readResult();
        System.out.println(res);

    }
}
