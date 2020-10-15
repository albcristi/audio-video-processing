import lombok.SneakyThrows;
import model.Block;
import model.BlockOperations;
import model.PPM;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        PPM p = new PPM("./sources/nt-P3.ppm");
        int width, height;
        String format;
        width = p.getWidth();
        height = p.getHeight();
        format = p.getFormat();
        List<Block> yBlocks = BlockOperations.toBlocks(p, p.getY(), "Y");
        List<Block> uBlocks = BlockOperations.toBlocks(p, p.getU(), "U");
        List<Block> vBlocks = BlockOperations.toBlocks(p, p.getV(), "V");

        List<Block> decompressedU = BlockOperations.decompressBlocks(uBlocks);
        List<Block> decompressedV = BlockOperations.decompressBlocks(vBlocks);
        double[][] y = BlockOperations.reconstructMatrix(yBlocks);
        double[][] u = BlockOperations.reconstructMatrix(decompressedU);
        double[][] v = BlockOperations.reconstructMatrix(decompressedV);
        PPM decodedImage = new PPM();
        decodedImage.setFileName("./sources/decoded-image.ppm");
        decodedImage.setFormat(format);
        decodedImage.setMaxValue(p.getMaxValue());
        decodedImage.setWidth(width);
        decodedImage.setHeight(height);
        decodedImage.setY(y);
        decodedImage.setU(u);
        decodedImage.setV(v);
        decodedImage = decodedImage.fromYUVtoRGB();
        decodedImage.writeImageToFile();
    }

    @SneakyThrows
    private static void generateRandomImg(){
        FileWriter fileWriter = new FileWriter("./sources/myimg.ppm");
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println("P3");
        printWriter.println("# my random image");
        printWriter.println(64+" "+64);
        printWriter.println(255);
        for (int line = 0; line < 600; line++) {
            for (int column = 0; column < 800; column++) {
                printWriter.println(new Random().nextInt(255)+1);
                printWriter.println(new Random().nextInt(255)+1);
                printWriter.println(new Random().nextInt(255)+1);
            }
        }

        printWriter.close();
    }
}
