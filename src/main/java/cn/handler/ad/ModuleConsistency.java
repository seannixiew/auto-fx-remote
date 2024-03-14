package cn.handler.ad;

import cn.controllers.RfTestController;
import cn.instr.DbfClient;
import cn.instr.InstrumentClient;
import cn.instr.MatrixClient;
import cn.utils.ControllersManager;
import javafx.event.Event;
import javafx.event.EventHandler;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class ModuleConsistency implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    DbfClient dbfClient=rfTestController.dbfClient;
    InstrumentClient instru2=rfTestController.instru2;
    MatrixClient matrixClient0= rfTestController.matrix0;


    @Override
    public void handle(Event event) {
        System.out.println("执行AD整机通道间一致性...");

        new Thread(()->{
            String fileName="";

            XSSFSheet sourceSheet=null;
            try {
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream("E:\\wx\\2_projects\\L payload\\vivado files\\0311\\AD通道-开关矩阵对应关系.xlsx"));
                sourceSheet = xssfWorkbook.getSheetAt(0);
            } catch (Exception excelE) {
                excelE.printStackTrace();
            }

            XSSFRow row=null;
            XSSFCell cellB=null;
            XSSFCell cellC=null;

            for(int i=1;i<121;i++){
                row=sourceSheet.getRow(i);
                cellB=row.getCell(1);
                cellB.setCellType(CellType.STRING);
                int matrixChannel=Integer.parseInt(cellB.getStringCellValue().trim());

                cellC=row.getCell(2);
                cellC.setCellType(CellType.STRING);
                int adChannel=Integer.parseInt(cellC.getStringCellValue().trim());

                try {
                    String dbfCmd = "BB00"+toHex(adChannel)+"0000FF";
                    System.out.println("AD通道为："+adChannel +" --- dbf切换指令为："+dbfCmd);
                    String dbfAns=dbfClient.dbfWrite(dbfCmd);   //地检切通道
                    System.out.println("DBF ans：" + dbfAns);
                    Thread.sleep(1000); //通道切换等待1s
                }catch (Exception ee){
                    ee.printStackTrace();
                }

                try{
                    System.out.println("矩阵通道为："+matrixChannel);
                    List<String> cmdChannel= Arrays.asList(matrixChannel+"");
                    matrixClient0.channelSwitch(cmdChannel);
                    Thread.sleep(100);
                }catch (Exception e){}


                try{
//                    instru0.writeCmd("*RST");
                    instru2.writeCmd(":SYSTEM:DISPLAY:UPDATE ON");
                    instru2.writeCmd(":INITiate:CONTinuous:ALL OFF");
                    //                    instru0.writeCmd("SENSe1:FREQuency:STARt 0.14 GHz; STOP 0.3 GHz");
                    instru2.writeCmd("SENSe1:FREQuency:STARt 0.105 GHz; STOP 0.16 GHz");
                    instru2.writeCmd("SOUR:POW -6");
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
                    instru2.writeCmd("INITiate1:IMMediate; *WAI");
//                        Thread.sleep(3000); //sweep time 400ms count 10

                    instru2.writeCmd("CALCulate1:PARameter:SELect 'Trc1'");
                    instru2.writeCmd("CALCulate1:MARKER1 ON");
                    instru2.writeCmd("CALCulate1:MARKER2 ON");
                    instru2.writeCmd("CALCulate1:MARKER3 ON");
                    instru2.writeCmd("CALCulate1:MARKer1:X 0.135 GHz");
                    instru2.writeCmd("CALCulate1:MARKer2:X 0.1385 GHz");
                    instru2.writeCmd("CALCulate1:MARKer3:X 0.142 GHz");

                    //                    instru0.writeCmd("CALCulate:MARKer:COUPled:TYPE channel"); //不支持
                    instru2.writeCmd("CALC:MARK:COUP ON");

                    // TODO: 2024/1/19 改路径
                    //导出.dat
                    Thread.sleep(200);
                    String pathPre = "MMEMory:STORe:TRACe 'Trc1','C:\\3188\\20240314\\";
                    String pathPost = "',UNFORMatted,LOGPhase";
                    fileName = adChannel  + ".dat";
                    String trace = pathPre + fileName + pathPost;

                    instru2.writeCmd(trace);
                    instru2.writeCmd("*OPC?");
                    instru2.readResult();

                    //自动截图
                    String mmemPath = "MMEM:NAME 'C:\\3188\\20240314\\";
                    String mmemFormat = ".bmp'";  //单引号
                    String mmemName = mmemPath + adChannel + mmemFormat;
                    instru2.writeCmd("HCOP:DEV:LANG BMP");
                    instru2.writeCmd(mmemName);
                    instru2.writeCmd("HCOP:IMM");
                    instru2.writeCmd("*OPC?");
                    instru2.readResult();

                    System.out.println("row："+i+" TEST END.");
                } catch(Exception exception){
                    exception.printStackTrace();
                }


            }


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
