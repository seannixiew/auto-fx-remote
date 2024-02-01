package cn.handler.union;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.model.InstruKind;
import cn.utils.ControllersManager;
import cn.utils.ExcelUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

public class PowerTx implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());
    DbfClient dbfClient=rfTestController.dbfClient;
    InstrumentClient instru0=rfTestController.instru0; //信号源
    InstrumentClient instru3=rfTestController.instru3; //功率计


    @Override
    public void handle(Event event) {

        new Thread(()->{

            Map<String,Integer> map=new HashMap<>(); // 频率，offset列号
            map.put("218MHz",2);
            map.put("221.5MHz",3);
            map.put("225MHz",4);

            System.out.println("执行DBF+RF-发射功率扫描...");
//            instru0.writeCmd("*RST;*CLS");  //不可复位。双通道，影响时钟。
//            instru0.writeCmd("*WAI");
            instru0.writeCmd("SOURce1:FREQuency:MODE CW");

            instru3.writeCmd("*RST;*CLS");
            instru3.writeCmd("*WAI");
            instru3.writeCmd("SENSe:FREQuency 1.521 GHz");
            instru3.writeCmd("UNIT:POWER DBM ");

            for(int channel=1;channel<121;channel++) {
                //调试中断用...
//                if(channel==8){
//                    System.out.println("通道2测试完成，中断中...");
//                }

                try {
//                    String dbfCmd = "BB00010000FF";
                    String dbfCmd = "BB00"+toHex(channel)+"0000FF";
                    System.out.println("DA通道为："+channel +" --- dbf切换指令为："+dbfCmd);
                    String dbfAns=dbfClient.dbfWrite(dbfCmd);   //地检切通道
                    System.out.println("DBF ans：" + dbfAns);
                    Thread.sleep(1000);    //通道切换等待1s
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                for (int i = 0; i < 3; i++) {
                    String freq="";
                    if(i==0) {
                        freq = "218MHz";
                        continue;
                    }
                    if(i==1){
                        freq="221.5MHz";
                    }
                    if(i==2){
                        freq="225MHz";
                        continue;
                    }
                    System.out.println(freq);
                    HSSFSheet sheet = null;
                    try {
                        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream("E:\\wx\\1_auto-test\\testDocuments\\0117\\offset表.xls")); //已包含1根公共线
                        sheet = hssfWorkbook.getSheetAt(0);
                    } catch (Exception excelE) {
                        excelE.printStackTrace();
                    }

                    double offset = 0;
                    offset = -sheet.getRow(channel).getCell(map.get(freq)).getNumericCellValue(); //负号
                    if (!(offset > 55)) { //如果offset无效
                        offset = 56.5;
                        System.out.println("offset 无效！");
                    }
                    System.out.println("offset:" + offset);

                    // TODO: 2023/12/8 信号源初始化确定
                    double sourcePower = -15; //信号源初始化值设定
//单音标定，功率计，单通道
                    try {
                        //对应33dBm功率，大概输入-32.39dBm左右
                        instru0.writeCmd("SOURce1:FREQuency:CW " + freq);
                        instru0.writeCmd("SOURce1:POWer:POWer " + sourcePower);//信号源初始功率

                        instru0.writeCmd(":OUTPut1 ON");
                        instru3.writeCmd("SENSE:CORRECTION:OFFSET " + offset); // 设置offset
                        instru3.writeCmd("SENSE:CORRECTION:OFFSET:State ON");
                        instru3.writeCmd("SENSE:AVERAGE:COUNT 16384");
                        instru3.writeCmd("INITiate:CONTinuous 1");
                        instru3.writeCmd("*OPC?");
                        instru3.readResult();
                        Thread.sleep(2000);  //功率计设置后，读数前，固定等待
                        instru3.writeCmd(":INIT:IMM");
                        instru3.writeCmd("FETCH?");
                        //保留两位小数
                        String resultTmp = instru3.readResult();
                        double valueD2 = new BigDecimal(Double.parseDouble(resultTmp)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                        double target = 34.1;//定标到 34 dbm（回退1dB）
                        while (target - valueD2 > 0.1 || valueD2 - target > 0.1) {
                            sourcePower = (target - valueD2) / 2 + sourcePower;  //以(1/2*Δ)的速度逼近target，沿±方向

                            // TODO: 2023/12/8 门限注意修改
                            if (sourcePower > 0) {//保护门限暂定-29dBm(-32dBm+3)
                                System.out.println("信号源功率过高异常！");
                                sourcePower = -40; //
                                instru0.writeCmd("SOURce1:POWer:POWer -40");
                                break;
                            }
                            instru0.writeCmd("SOURce1:POWer:POWer " + sourcePower);
                            instru0.writeCmd("*OPC?");
                            instru0.readResult();

                            // TODO: 2023/12/8 缩短时间
                            Thread.sleep(1000); //信号源功率设置后，功率计读取前，再加上TR响应时间，固定等待1000 ms
                            instru3.writeCmd(":INIT:IMM");
                            instru3.writeCmd("FETCH?");
                            valueD2 = new BigDecimal(Double.parseDouble(instru3.readResult())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                        }
                        //入excel，打印结果
                        System.out.println("sourcePower:" + sourcePower);
                        System.out.println("meterPower:" + valueD2);
                        ExcelUtils.writeVals2Cell(Arrays.asList(channel + "", freq, sourcePower + "", valueD2 + "", offset + ""), "E:\\wx\\1_auto-test\\testDocuments\\0117\\功率标定.xlsx");

                        instru0.writeCmd(":OUTPut1 OFF");
                        System.out.println("通道"+channel+"："+"频率"+freq+"：测试完成。");
                        Thread.sleep(1000);


                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

            System.out.println("DBF+RF-发射功率扫描：测试完成。");
        }).start();
    }

    private String toHex(int dec){
        String hex=Integer.toHexString(dec);
        if(dec<16){
            hex="0"+hex;
        }
        return hex;
    }
}
