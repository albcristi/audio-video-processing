import lombok.SneakyThrows;
import model.*;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        PPM p = new PPM("./sources/nt-P3.ppm");
        Encoder encoder = new Encoder(p);
        Decoder decoder = new Decoder(encoder);
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
