package cn.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;


public class ExcelUtils {

    public static void generateExcel(List<List <String>> list, String savePath) {
        // excel文档对象
        HSSFWorkbook excel = new HSSFWorkbook();
        // sheet对象
        HSSFSheet sheet = excel.createSheet("sheet1");
        sheet.setDefaultRowHeightInPoints(12);// 设置缺省列高
        sheet.setDefaultColumnWidth(40);// 设置缺省列宽

        // 在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        HSSFRow row0 = sheet.createRow(0);
        // 创建单元格（excel的单元格，参数为列索引，可以是0～255之间的任何一个
        HSSFCell cell0 = row0.createCell(0);
        HSSFCell cell1 = row0.createCell(1);
        cell0.setCellValue("测试项");
        cell1.setCellValue("值");
        for(int i=0;i<list.size();i++){
            HSSFRow rowi=sheet.createRow(i+1);
            rowi.createCell(0).setCellValue(list.get(i).get(0));
            rowi.createCell(1).setCellValue(list.get(i).get(1));
        }


        // 输出Excel文件
        try {
            FileOutputStream fos = new FileOutputStream(new File(savePath));
            excel.write(fos);
            excel.close();
            fos.close();
            System.out.println("生成excel文档成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("生成excel文档失败");
        }
    }

    public static int writeVal2Cell(String val1,String val2, String channel, String filePath){
        try {
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new FileInputStream(filePath));
            HSSFSheet sheet = hssfWorkbook.getSheetAt(0); //sheet 0
            int writeRowNum = sheet.getLastRowNum()+1;

            FileOutputStream out=new FileOutputStream(filePath);
            HSSFRow r=sheet.createRow(writeRowNum);
            r.createCell(0).setCellValue(channel);
            r.createCell(1).setCellValue(val1);
            r.createCell(2).setCellValue(val2);
            // TODO: 2023/11/23 流处理分析
            out.flush();
            hssfWorkbook.write(out);
            out.close();
            System.out.println("结果插入成功！");
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public static int writeVals2Cell(List<String> vals,  String filePath){
        try {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(filePath));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0); //sheet 0
            int writeRowNum = sheet.getLastRowNum()+1;

            FileOutputStream out=new FileOutputStream(filePath);
            XSSFRow r=sheet.createRow(writeRowNum);
            for(int i=0;i<vals.size();i++) {
                r.createCell(i).setCellValue(vals.get(i));
            }
            // TODO: 2023/11/23 流处理分析
            out.flush();
            xssfWorkbook.write(out);
            out.close();
            System.out.println("结果插入成功！");
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }


    public static int writeVals2Cell2(List<String> vals,  String filePath){
        try {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(filePath));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0); //sheet 0
//            int writeRowNum = sheet.getLastRowNum()+1;

            FileOutputStream out=new FileOutputStream(filePath);
            for(int i=0;i<vals.size();i++){
                XSSFRow r=sheet.createRow(i);
                XSSFCell cell=r.createCell(0);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(vals.get(i));

            }


            // TODO: 2023/11/23 流处理分析
            out.flush();
            xssfWorkbook.write(out);
            out.close();
            System.out.println("结果插入成功！");
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    public int typeDistinguish(Cell cell){
        CellType cellType=cell.getCellType();
        return 0;
    }
}
