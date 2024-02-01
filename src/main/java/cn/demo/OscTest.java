package cn.demo;

import cn.instr.InstrumentClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class OscTest {

    static InstrumentClient instrumentClient=new InstrumentClient();


    public static void main(String[] args) {

        boolean opened=instrumentClient.openSocket("","192.168.1.160");
        System.out.println(opened);

        instrumentClient.writeCmd("*IDN?");
        System.out.println(instrumentClient.readResultSocket());

        instrumentClient.writeCmd(":DISPlay:DATA? PNG");
        byte[] rawImg=instrumentClient.readImg();
        System.out.println(rawImg.toString());

        // 将字节数组转换为BufferedImage
        BufferedImage image = byteArrayToImage(rawImg);
        System.out.println(image);

        // 保存为BMP文件
        saveAsBMP(image, "E:\\wx\\1_auto-test\\about vivado\\bit_file_from_yangjiashuo\\0111\\");


    }


    private static BufferedImage byteArrayToImage(byte[] byteArray) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            return ImageIO.read(bis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveAsBMP(BufferedImage image, String outputPath) {
        try {
            File outputBMP = new File(outputPath);
            ImageIO.write(image, "bmp", outputBMP);
            System.out.println("BMP saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
