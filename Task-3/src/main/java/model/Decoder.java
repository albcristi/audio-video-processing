package model;

import com.sun.tools.javac.util.Pair;

import javax.swing.text.Element;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Decoder {
    private Encoder encoder;
    private Integer originalHeight;
    private Integer getOriginalWidth;

    private double[][] Q = {
            {6, 4, 4, 6, 10, 16, 20, 24},
            {5, 5, 6, 8, 10, 23, 24, 22},
            {6, 5, 6, 10, 16, 23, 28, 22},
            {6, 7, 9, 12, 20, 35, 32, 25},
            {7, 9, 15, 22, 27, 44, 41, 31},
            {10, 14, 22, 26, 32, 42, 45, 37},
            {20, 26, 31, 35, 41, 48, 48, 40},
            {29, 37, 38, 39, 45, 40, 41, 40}
    };
    private List<Block> encodedY = new ArrayList<>();
    private List<Block> encodedU = new ArrayList<>();
    private List<Block> encodedV = new ArrayList<>();
    private Integer currentIndex = 0;

    public Decoder(Encoder encoder){
        this.encoder = encoder;
        getOriginalWidth = encoder.getImage().getWidth();
        originalHeight = encoder.getImage().getHeight();
        constructBlocks();

        /*encodedY = this.encoder.getEncodedY();
        encodedU = this.encoder.getEncodedU();
        encodedV = this.encoder.getEncodedV();*/
        // lab 2
        deQuantizationPhase(encodedY);
        deQuantizationPhase(encodedU);
        deQuantizationPhase(encodedV);

        inverseDCT(encodedY);
        inverseDCT(encodedU);
        inverseDCT(encodedV);

        addValue(encodedY);
        addValue(encodedU);
        addValue(encodedV);
        // lab 1
        changeGtoBlockMatrix(encodedY);
        changeGtoBlockMatrix(encodedU);
        changeGtoBlockMatrix(encodedV);

        double[][] y = BlockOperations.reconstructMatrix(encodedY);
        double[][] u = BlockOperations.reconstructMatrix(encodedU);
        double[][] v = BlockOperations.reconstructMatrix(encodedV);

        PPM newImage = new PPM();
        newImage.setFileName("./sources/decoded-image.ppm");
        PPM old = encoder.getImage();
        newImage.setFormat(old.getFormat());
        newImage.setY(y);
        newImage.setU(u);
        newImage.setV(v);
        newImage.setMaxValue(old.getMaxValue());
        newImage.setWidth(old.getWidth());
        newImage.setHeight(old.getHeight());
        newImage = newImage.fromYUVtoRGB();
        newImage.writeImageToFile();
    }

    // lab 3

    public void constructBlocks(){
        try{
            encodedY = new ArrayList<>();
            encodedU = new ArrayList<>();
            encodedV = new ArrayList<>();
            /*
            List<int[]> y = encoder.encZigY;
            List<int[]> u = encoder.encZigU;
            List<int[]> v = encoder.encZigV;
            for(int[] el: y){
                int[][] gMatrix;
                gMatrix = zigZagFormation(el);
                assert gMatrix.length ==8;
                assert gMatrix[7].length == 8;
                Block b = new Block(8, "Y");
                b.setGMatrix(gMatrix);
                b.setOriginalHeight(originalHeight);
                b.setOriginalWidth(getOriginalWidth);
                encodedY.add(b);
            }
            for(int[] el: u){
                int[][] gMatrix;
                gMatrix = zigZagFormation(el);
                Block b = new Block(8, "U");
                b.setGMatrix(gMatrix);
                b.setOriginalHeight(originalHeight);
                b.setOriginalWidth(getOriginalWidth);
                assert gMatrix.length ==8;
                assert gMatrix[7].length == 8;
                encodedU.add(b);
            }
            for(int[] el: v){
                int[][] gMatrix;
                gMatrix = zigZagFormation(el);
                Block b = new Block(8, "V");
                b.setGMatrix(gMatrix);
                b.setOriginalHeight(originalHeight);
                b.setOriginalWidth(getOriginalWidth);
                assert gMatrix.length ==8;
                assert gMatrix[7].length == 8;
                encodedV.add(b);
            }*/
            List<EntropyElement> entropyArray = readEntropyEncArray();
            //List<EntropyElement> entropyArray = encoder.entropy;
            while (currentIndex<entropyArray.size()){
                int[][] gMatrix;
                gMatrix = zigZagFormation(decodeEntropyArray(getEntropyArrayForBlock(entropyArray)));
                Block block = new Block(8,"Y");
                block.setGMatrix(gMatrix);
                block.setOriginalHeight(originalHeight);
                block.setOriginalWidth(getOriginalWidth);
                encodedY.add(block);


                gMatrix = zigZagFormation(decodeEntropyArray(getEntropyArrayForBlock(entropyArray)));
                block = new Block(8,"U");
                block.setGMatrix(gMatrix);
                block.setOriginalHeight(originalHeight);
                block.setOriginalWidth(getOriginalWidth);
                encodedU.add(block);


                gMatrix = zigZagFormation(decodeEntropyArray(getEntropyArrayForBlock(entropyArray)));
                block = new Block(8,"V");
                block.setGMatrix(gMatrix);
                block.setOriginalHeight(originalHeight);
                block.setOriginalWidth(getOriginalWidth);
                encodedV.add(block);

            }
        }
        catch (Exception e){
            //System.out.println(e.getMessage());
        }
    }

    public List<EntropyElement> readEntropyEncArray() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("./sources/entropy.txt"));
        List<EntropyElement> elements = new ArrayList<>();
        String line = "";
        List<String> entropyData = new ArrayList<>();
        while (true){
            line = reader.readLine();
            if(line == null || line.equals(""))
                break;
            entropyData = Arrays.asList(line.split(","));
            if(entropyData.size() == 2){
                elements.add(new EntropyElement(Integer.parseInt(entropyData.get(0)),
                        Integer.parseInt(entropyData.get(1))));
                continue;
            }
            elements.add(new EntropyElement(
                    Integer.parseInt(entropyData.get(0)),
                    Integer.parseInt(entropyData.get(1)),
                    Integer.parseInt(entropyData.get(2))
            ));
        }
        reader.close();
        return elements;
    }

    private List<EntropyElement> getEntropyArrayForBlock(List<EntropyElement> encArray){
        List<EntropyElement> entropyBlock = new ArrayList<>();
        Integer currentElement = currentIndex;
        while (true){
            entropyBlock.add(encArray.get(currentElement));
            currentElement++;
            if(encArray.get(currentElement).run_length==-1 || currentElement == encArray.size()-1){
                /*if(encArray.get(currentElement).size == 0) {
                    entropyBlock.add(encArray.get(currentElement));
                    currentIndex = currentElement + 1;
                    break;
                }*/
                currentIndex = currentElement;
                break;
            }
        }
        return entropyBlock;
    }

    private int[] decodeEntropyArray(List<EntropyElement> encArray){
        int[] array = new int[64];
        int currentInd = 0;
        //System.out.println(encArray);
        for(EntropyElement element: encArray){
            if(element.run_length==-1 && element.size!=0){
                array[currentInd] = element.amplitude;
                currentInd++;
                continue;
            }
            if(element.run_length==-2){
                while (currentInd<64){
                    array[currentInd] = 0;
                    currentInd++;
                }
                break;
            }
            int cont = 0;
            while (cont < element.run_length){
                array[currentInd] = 0;
                currentInd++;
                cont++;
            }
            array[currentInd] = element.amplitude;
            currentInd++;
        }
        //System.out.println(Arrays.toString(array));
        return array;
    }

    private int[][] zigZagFormation(int[] result){
        int[][] matrix = new int[8][8];
        // each block we will perform a zig zag parsing is an 8x8 block
        int currentLine = 0;
        int currentColumn = 0;
        int index = 0;

        // we parse in zig zag in 2 phases:
        //     - first half of the matrix
        //     - second half
        matrix[0][0]= result[0];
        while (true) {
            // move to the next column
            currentColumn += 1;
            index += 1;
            matrix[currentLine][currentColumn] = result[index];
            // we slide from on the matrix until column becomes 0
            // slide column-- and line++
            do {
                currentColumn -= 1;
                currentLine += 1;
                index += 1;
                matrix[currentLine][currentColumn] = result[index];
            } while (currentColumn != 0);
            if (currentLine == 7)
                break;
            // now go down with one line
            currentLine += 1;
            index += 1;
            matrix[currentLine][currentColumn] = result[index];
            // we climb on the matrix now
            // meaning line--, column++
            do {
                currentLine -= 1;
                currentColumn += 1;
                index += 1;
                matrix[currentLine][currentColumn] = result[index];
            }
            while (currentLine != 0);
        }
        return matrix;
    }


    // lab 1 & 2
    private void deQuantizationPhase(List<Block> blocks){
        for(Block block: blocks)
            _deQuantizationPhase(block);
    }

    private void _deQuantizationPhase(Block block){
        int[][] result = new int[8][8];
        for(int i=0; i<8; i++)
            for(int j=0; j<8; j++)
                result[i][j] = (int)(block.getGMatrix()[i][j]*Q[i][j]);
        block.setGMatrix(result);
    }

    private void inverseDCT(List<Block> blocks){
        for(Block block: blocks)
            _inverseDCT(block);
    }

    private void _inverseDCT(Block block){
        double constant = (double) 1/4;
        int[][] f = new int[8][8];
        for(int x=0; x<8; x++)
            for(int y=0; y<8; y++)
                f[x][y] = (int) (constant * sumOnU(block.getGMatrix(), x, y));
        block.setGMatrix(f);
    }

    private double sumOnU(int[][] matrix, int x, int y){
        double sum = 0.0;
        for(int u=0; u<8; u++)
            sum += sumOnV(matrix, x, y, u);
        return sum;
    }

    private double sumOnV(int[][] matrix, int x, int y, int u){
        double sum = 0.0;
        for(int v=0; v<8; v++){
            double cosU = Math.cos(((2 * x + 1) * u * Math.PI) / 16);
            double cosV = Math.cos(((2 * y + 1) * v * Math.PI) / 16);
            sum += alpha(u)*alpha(v)*matrix[u][v]*cosU*cosV;
        }
        return sum;
    }

    private double alpha(int value) {
        return value > 0 ? 1 : (1 / Math.sqrt(2.0));
    }

    private void addValue(List<Block> blocks){
        for(Block block: blocks)
        _addValue(block);
    }

    private void _addValue(Block block){
        int[][]  r = new int[8][8];
        for(int i=0; i<8; i++)
            for(int j=0; j<8; j++)
                r[i][j] = block.getGMatrix()[i][j] + 128;
        block.setGMatrix(r);

    }


    private void changeGtoBlockMatrix(List<Block> blocks){
        for(Block block: blocks)
            _changeGtoBlockMatrix(block);
    }

    private void _changeGtoBlockMatrix(Block b){
        BlockOperations.fromGtoBlock(b);
    }
}
