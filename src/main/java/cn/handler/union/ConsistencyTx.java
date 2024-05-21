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



public class ConsistencyTx implements EventHandler {

    RfTestController rfTestController =(RfTestController) ControllersManager.CONTROLLERS.get(RfTestController.class.getSimpleName());

    DbfClient dbfClient=rfTestController.dbfClient;
    InstrumentClient instru2=rfTestController.instru2;
    ComboBox<String> cbLoop=rfTestController.cbLoop;

    @Override
    public void handle(Event event) {

        System.out.println("执行DBF+RF-发射一致性测试...");
        int selectedIndex = cbLoop.getSelectionModel().getSelectedIndex();
        System.out.println("loop index：" + selectedIndex);

        if(selectedIndex==0){
            Thread t=new Thread(()->{
                //测试逻辑...
            });
            t.start();
            ThreadAndProcessPools.addThread(t);

            try {
                t.join();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if (selectedIndex == 5) {
            Thread t = new Thread(() -> {
                HashMap<String, Double> map = new HashMap<>();
                //补偿矢网和信号源功率delta
                // TODO: 2024/1/19 校准补偿
                map.put("218MHz", -0.97);
                map.put("221.5MHz", -0.84);
                map.put("225MHz", -0.67);

                String fileName = "";

                XSSFSheet sourceSheet = null;
                try {
                    XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream("E:\\wx\\1_auto-test\\testDocuments\\0117\\功率标定_forRead.xlsx"));
                    sourceSheet = xssfWorkbook.getSheetAt(0);
                } catch (Exception excelE) {
                    excelE.printStackTrace();
                }

                int rowCount = sourceSheet.getPhysicalNumberOfRows();
                XSSFRow row = null;
                XSSFCell cell = null;
                double delta = 0;

                for (int i = 1; i < rowCount; i++) {
                    row = sourceSheet.getRow(i);
                    cell = row.getCell(0);
                    cell.setCellType(CellType.STRING);
                    int currChannel = Integer.parseInt(cell.getStringCellValue().trim());

                    cell = row.getCell(1);
                    cell.setCellType(CellType.STRING);
                    String freq = cell.getStringCellValue().trim();
                    //若有多余字符，excel或代码处理

                    cell = row.getCell(2);
                    cell.setCellType(CellType.STRING);
                    double vnaPower = Double.parseDouble(cell.getStringCellValue().trim());
                    delta = map.get(freq);
                    vnaPower = vnaPower + delta;

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
                        System.out.println("表格功率读取异常");
                        break;
                    }

                    try {
//                    instru0.writeCmd("*RST");
                        instru2.writeCmd(":SYSTEM:DISPLAY:UPDATE ON");
                        instru2.writeCmd(":INITiate:CONTinuous:ALL OFF");
                        //                    instru0.writeCmd("SENSe1:FREQuency:STARt 0.14 GHz; STOP 0.3 GHz");
                        instru2.writeCmd("SENSe1:FREQuency:STARt 0.21 GHz; STOP 0.24 GHz");
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
                        instru2.writeCmd("INITiate1:IMMediate; *WAI");
//                        Thread.sleep(3000); //sweep time 400ms count 10

                        instru2.writeCmd("CALCulate1:PARameter:SELect 'Trc1'");
                        instru2.writeCmd("CALCulate1:MARKER1 ON");
                        instru2.writeCmd("CALCulate1:MARKER2 ON");
                        instru2.writeCmd("CALCulate1:MARKER3 ON");
                        instru2.writeCmd("CALCulate1:MARKer1:X 0.218 GHz");
                        instru2.writeCmd("CALCulate1:MARKer2:X 0.2215 GHz");
                        instru2.writeCmd("CALCulate1:MARKer3:X 0.225 GHz");

                        //                    instru0.writeCmd("CALCulate:MARKer:COUPled:TYPE channel"); //不支持
                        instru2.writeCmd("CALC:MARK:COUP ON");

                        // TODO: 2024/1/19 改路径
                        //导出.dat
                        Thread.sleep(200);
                        String pathPre = "MMEMory:STORe:TRACe 'Trc1','C:\\20240119\\consisTx\\";
                        String pathPost = "',UNFORMatted,LOGPhase";
                        fileName = currChannel + "_" + freq + ".dat";
                        String trace = pathPre + fileName + pathPost;

                        instru2.writeCmd(trace);
                        instru2.writeCmd("*OPC?");
                        instru2.readResult();

                        //自动截图
                        String mmemPath = "MMEM:NAME 'C:\\20240119\\consisTx\\";
                        String mmemFormat = ".bmp'";  //单引号
                        String mmemName = mmemPath + currChannel + "_" + freq + mmemFormat;
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
