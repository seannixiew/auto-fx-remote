package cn.utils;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxtUtils {

    public static void main(String[] args) throws Exception{

        File file=new File("E:\\wx\\2_projects\\L payload\\组网星\\小卫星测试\\2024-05-13\\1-06-000.dat");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        List<String> targetFreq= Arrays.asList(
                "2.100000000000000E+008",
                "2.150000000000000E+008",
                "2.180000000000000E+008",
                "2.215000000000000E+008",
                "2.250000000000000E+008",
                "2.300000000000000E+008",
                "2.350000000000000E+008",
                "2.400000000000000E+008",

                "1.050000000000000E+008",
                "1.200000000000000E+008",
                "1.325000000000000E+008",
                "1.350000000000000E+008",
                "1.385000000000000E+008",
                "1.420000000000000E+008",
                "1.500000000000000E+008",
                "1.600000000000000E+008"
        );

        String targetFile="E:\\wx\\2_projects\\L payload\\组网星\\小卫星测试\\1.xlsx";
        try {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
            xssfWorkbook.createSheet();
            xssfWorkbook.write(new FileOutputStream(targetFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String lineText=null;
        while ((lineText=bufferedReader.readLine())!=null){
            String[] tokens=lineText.split(",");
            if(targetFreq.contains(tokens[0])){
                ExcelUtils.writeVals2Cell(Arrays.asList(tokens[0],tokens[1],tokens[2]),targetFile);
            }
        }
    }
}
