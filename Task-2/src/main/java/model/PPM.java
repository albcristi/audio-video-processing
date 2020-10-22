package model;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.*;

@Getter
@Setter
public class PPM {
    private String fileName;
    private String format;
    private Integer maxValue;
    private Integer height;  // lines
    private Integer width;  // columns

    // matrixes for RGB
    private int[][] r;
    private int[][] g;
    private int[][] b;

    // matrixes for YUV
    private double[][] y;
    private double[][] u;
    private double[][] v;

    public PPM(){}
    public PPM(String fileName){
        this.fileName = fileName;
        this.readPPM();
        this.fromRGBtoYUV(); //needs transformation in
        // order to have also the YUV format stored
    }

    private PPM(PPM img){
        this.fileName = img.fileName;
        this.format = img.format;
        this.maxValue = img.maxValue;
        this.width = img.width;
        this.height = img.height;
        this.r = new int[height][width];
        this.g = new int[height][width];
        this.b = new int[height][width];
        this.y = img.y;
        this.u = img.u;
        this.v = img.v;
    }

    private void readPPM(){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            this.format = reader.readLine(); // will be P3, but anyway
            reader.readLine(); // get rid of the comment from the second line
            String dimensionsLine = reader.readLine();
            this.width = Integer.parseInt(dimensionsLine.split(" ")[0]);
            this.height = Integer.parseInt(dimensionsLine.split(" ")[1]);
            this.maxValue = Integer.parseInt(reader.readLine());
            // time to define the dimensions for the RGB and YUV matrixes
            this.r = new int[height][width];
            this.g = new int[height][width];
            this.b = new int[height][width];
            this.y = new double[height][width];
            this.u = new double[height][width];
            this.v = new double[height][width];
            //now it's time to construct the RGB matrixes from the input file
            int line, column;
            line=column=0;
            while (true){
                if(column == this.width){
                    // since our file contains one element per line
                    // when the column value is equal to the width
                    // it means that we are moving to a next row in
                    // the RGB matrixes and the column value needs
                    // to be initialised with value 0
                    column = 0;
                    line++;
                }
                if(line == this.height) // case when are done reading
                    break;             // data from the file
                r[line][column] = Integer.parseInt(reader.readLine());
                g[line][column] = Integer.parseInt(reader.readLine());
                b[line][column] = Integer.parseInt(reader.readLine());
                column++;
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fromRGBtoYUV() {
        for (int line = 0; line < this.height; line++)
            for (int column = 0; column < this.width; column++) {
                /*
                Y =   0.299*R + 0.587*G + 0.114*B
                U = 128 – 0.1687*R – 0.3312*G + 0.5*B
                V = 128 + 0.5*R – 0.4186*G – 0.0813*B
                 */
                y[line][column] = 0.299 * r[line][column] + 0.587 * g[line][column] + 0.114 * b[line][column];
                u[line][column] = 128 - 0.168736 * r[line][column] - 0.331264 * g[line][column] + 0.5 * b[line][column];
                v[line][column] = 128 + 0.5 * r[line][column] - 0.418688 * g[line][column] - 0.081312 * b[line][column];
            }
    }

    public PPM fromYUVtoRGB(){
        PPM newPPM = new PPM(this);
        for (int line = 0; line < height; line++)
            for (int column = 0; column < width; column++) {
                Double rValue = y[line][column] + 1.402 * (v[line][column] - 128);
                Double gValue = y[line][column] - 0.344136 * (u[line][column] - 128) - 0.714136 * (v[line][column] - 128);
                Double bValue = y[line][column] + 1.772 * (u[line][column] - 128);
                if (rValue > 255)
                    rValue = 255.0;
                if (rValue < 0)
                    rValue = 0.0;
                if (gValue > 255)
                    gValue = 255.0;
                if (gValue < 0)
                    gValue = 0.0;
                if (bValue > 255)
                    bValue = 255.0;
                if (bValue < 0)
                    bValue = 0.0;
                newPPM.r[line][column] = rValue.intValue();
                newPPM.g[line][column] = gValue.intValue();
                newPPM.b[line][column] = bValue.intValue();
            }
        return newPPM;
    }

    @SneakyThrows
    public void writeImageToFile(){
        FileWriter fileWriter = new FileWriter(fileName);
        PrintWriter printWriter = new PrintWriter(fileWriter);

        printWriter.println(format);
        printWriter.println(width+" "+height);
        printWriter.println(maxValue);
        for (int line = 0; line < height; line++) {
            for (int column = 0; column < width; column++) {
                printWriter.println(this.r[line][column]);
                printWriter.println(this.g[line][column]);
                printWriter.println(this.b[line][column]);
            }
        }

        printWriter.close();
    }
}
