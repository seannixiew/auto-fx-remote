package cn.handler.union;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.utils.ControllersManager;
import cn.utils.ThreadAndProcessPools;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.HashMap;

public class ConsistencyRx implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    DbfClient dbfClient=rfTestController.dbfClient;
    InstrumentClient instru2=rfTestController.instru2;

    @Override
    public void handle(Event event) {

        System.out.println("执行DBF+RF-接收一致性测试...");

        Thread t = new Thread(() -> {

            for (int i = 1; i < 121; i++) {
                int currChannel = i;
                double vnaPower = -5; //随需求调整

                String fileName = "";
                try {
                    String dbfCmd = "BB00" + toHex(currChannel) + "0000FF";
                    System.out.println("DA通道为：" + currChannel + " --- dbf切换指令为：" + dbfCmd);
                    String dbfAns = dbfClient.dbfWrite(dbfCmd);   //地检切通道
                    System.out.println("DBF ans：" + dbfAns);
                    Thread.sleep(1000); //通道切换等待1s
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                if (vnaPower > 5) {
                    vnaPower = -10;
                    System.out.println("矢网功率设置异常，恢复默认值。");
                    break;
                }

                try {
//                    instru0.writeCmd("*RST");
                    instru2.writeCmd(":SYSTEM:DISPLAY:UPDATE ON");
                    instru2.writeCmd(":INITiate:CONTinuous:ALL ON");
//                    instru2.writeCmd(":INITiate:CONTinuous:ALL OFF");  // single step 1
                    instru2.writeCmd("SENSe1:FREQuency:STARt 0.105 GHz; STOP 0.165 GHz");
                    instru2.writeCmd("SOUR:POW " + vnaPower);
                    instru2.writeCmd("SENSe1:BANDwidth:RESolution 1KHz");
                    instru2.writeCmd("SWEep:STEP 500 kHz");
                    //提升扫描稳定性
                    //                    instru0.writeCmd(":SENSE:SWEEP:COUNT:ALL 10");
                    //                    instru0.writeCmd(":SENSE1:SWEEP:TIME 400 ms");

                    instru2.writeCmd("CALCulate1:PARameter:SDEFine 'Trc2', 'S21'");
                    instru2.writeCmd("CALCulate1:FORMat PHASe");
                    instru2.writeCmd("DISPlay:WINDow2:STATe ON");
                    instru2.writeCmd("DISPlay:WINDow2:TRACe2:FEED 'Trc2'");

                    Thread.sleep(200);
//                    instru2.writeCmd("INITiate1:IMMediate; *WAI");  // single step 2
//                        Thread.sleep(3000); //sweep time 400ms count 10

                    instru2.writeCmd("CALCulate1:PARameter:SELect 'Trc1'");
                    instru2.writeCmd("CALCulate1:MARKER1 ON");
                    instru2.writeCmd("CALCulate1:MARKER2 ON");
                    instru2.writeCmd("CALCulate1:MARKER3 ON");
                    instru2.writeCmd("CALCulate1:MARKER4 ON");
                    instru2.writeCmd("CALCulate1:MARKER5 ON");
                    instru2.writeCmd("CALCulate1:MARKER6 ON");
                    instru2.writeCmd("CALCulate1:MARKER7 ON");
                    instru2.writeCmd("CALCulate1:MARKER8 ON");
                    instru2.writeCmd("CALCulate1:MARKer1:X 0.105 GHz");
                    instru2.writeCmd("CALCulate1:MARKer2:X 0.120 GHz");
                    instru2.writeCmd("CALCulate1:MARKer3:X 0.1325 GHz");
                    instru2.writeCmd("CALCulate1:MARKer4:X 0.135 GHz");
                    instru2.writeCmd("CALCulate1:MARKer5:X 0.1385 GHz");
                    instru2.writeCmd("CALCulate1:MARKer6:X 0.142 GHz");
                    instru2.writeCmd("CALCulate1:MARKer7:X 0.150 GHz");
                    instru2.writeCmd("CALCulate1:MARKer8:X 0.160 GHz");

                    //                    instru0.writeCmd("CALCulate:MARKer:COUPled:TYPE channel"); //不支持
                    instru2.writeCmd("CALC:MARK:COUP ON");

                    // TODO: 2024/1/19 改路径
                    //导出.dat
                    Thread.sleep(200);
// single step 3
//                    String pathPre = "MMEMory:STORe:TRACe 'Trc1','C:\\20240521\\Rx\\";
//                    String pathPost = "',UNFORMatted,LOGPhase";
//                    fileName = currChannel + ".dat";
//                    String trace = pathPre + fileName + pathPost;

                    String pathPre = "MMEM:STOR:TRAC:CHAN 1,'C:\\20240521\\Rx\\";
                    String pathPost = "',UNFORMatted,LOGPhase";
                    fileName = currChannel + ".dat";
                    String trace = pathPre + fileName + pathPost;

                    instru2.writeCmd(trace);
                    instru2.writeCmd("*OPC?");
                    instru2.readResult();

                    //自动截图
                    String mmemPath = "MMEM:NAME 'C:\\20240521\\Rx\\";
                    String mmemFormat = ".bmp'";  //单引号
                    String mmemName = mmemPath + currChannel + mmemFormat;
                    instru2.writeCmd("HCOP:DEV:LANG BMP");
                    instru2.writeCmd(mmemName);
                    instru2.writeCmd("HCOP:IMM");
                    instru2.writeCmd("*OPC?");
                    instru2.readResult();

                    System.out.println("row：" + i + " TEST END.");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        t.start();
        ThreadAndProcessPools.addThread(t);

        try {
            t.join();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String toHex(int dec){
        String hex=Integer.toHexString(dec);
        if(dec<16){
            hex="0"+hex;
        }
        return hex;
    }
}
