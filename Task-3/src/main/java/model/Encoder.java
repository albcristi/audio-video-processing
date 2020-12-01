package model;

import com.sun.tools.javac.util.Pair;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class Encoder {
    private PPM image;
    private List<Block> encodedY;
    private List<Block> encodedU;
    private List<Block> encodedV;
    public List<int[]> encZigY;
    public List<int[]> encZigU;
    public List<int[]> encZigV;
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

    private HashMap<Integer, List<Integer>> amplitudeMap;
    public List<EntropyElement> entropy;

    public List<Pair<Integer,Integer>> getPositions(String type){
        List<Block> enc = new ArrayList<>();
        if(type.equals("Y"))
            enc = encodedY;
        if(type.equals("U"))
            enc = encodedU;
        if(type.equals("V"))
            enc = encodedY;
        List<Pair<Integer,Integer>> coordinates = new ArrayList<>();
        for(Block b: enc)
            coordinates.add(new Pair<>(b.getXCoordinate(), b.getYCoordinate()));
        return coordinates;
    }

    private void populateAmplitudeMap(){
        amplitudeMap = new HashMap<>();
        amplitudeMap.put(1, Arrays.asList(-1, 1));
        amplitudeMap.put(2, Arrays.asList(-3, -2, 2, 3));
        amplitudeMap.put(3, Arrays.asList(-7, -4, 4, 7));
        amplitudeMap.put(4, Arrays.asList(-15, -8, 8, 15));
        amplitudeMap.put(5, Arrays.asList(-31, -16, 16, 31));
        amplitudeMap.put(6, Arrays.asList(-63, -32, 32, 63));
        amplitudeMap.put(7, Arrays.asList(-127, -64, 64, 127));
        amplitudeMap.put(8, Arrays.asList(-225, -128, 128, 255));
        amplitudeMap.put(9, Arrays.asList(-511, -256, 256, 511));
        amplitudeMap.put(10, Arrays.asList(-1023, -512, 512, 1023));
    }

    public Encoder(){}

    public Encoder(PPM image){
        this.image = image;
        encodedY = BlockOperations.toBlocks(image, image.getY(), "Y");
        encodedU = BlockOperations.toBlocks(image, image.getU(), "U");
        encodedV = BlockOperations.toBlocks(image, image.getV(), "V");
        encodedU = BlockOperations.decompressBlocks(encodedU);
        encodedV = BlockOperations.decompressBlocks(encodedV); // lab 1

        subtractValues(encodedY);
        subtractValues(encodedU);
        subtractValues(encodedV);

        executeForwardDCTonBlocks(encodedY);
        executeForwardDCTonBlocks(encodedU);
        executeForwardDCTonBlocks(encodedV);

        quantizationPhase(encodedY);
        quantizationPhase(encodedU);
        quantizationPhase(encodedV);

        // lab 3
        entropy = new ArrayList<>();
        populateAmplitudeMap();
        entropyEncoding();
        writeToFile();
    }

    // lab 3
    int[] zigZagParsing(int[][] matrix){
        // each block we will perform a zig zag parsing is an 8x8 block
        int[] result = new int[64];
        int currentLine = 0;
        int currentColumn = 0;
        int index = 0;

        // we parse in zig zag in 2 phases:
        //     - first half of the matrix
        //     - second half
        result[0] = matrix[0][0];
        while (true){
             // move to the next column
             currentColumn += 1;
             index += 1;
             result[index] = matrix[currentLine][currentColumn];
             // we slide from on the matrix until column becomes 0
             // slide column-- and line++
            do {
                currentColumn -= 1;
                currentLine += 1;
                index += 1;
                result[index] = matrix[currentLine][currentColumn];
            } while (currentColumn != 0);
            if(currentLine == 7)
                break;
            // now go down with one line
            currentLine += 1;
            index += 1;
            result[index] = matrix[currentLine][currentColumn];
            // we climb on the matrix now
            // meaning line--, column++
            do{
                currentLine -= 1;
                currentColumn += 1;
                index += 1;
                result[index] = matrix[currentLine][currentColumn];
            }
            while (currentLine != 0);

        }

        // now we deal with the second half
        while (true){
            currentColumn += 1;
            index += 1;
            result[index] = matrix[currentLine][currentColumn];
            if(currentColumn == 7)
                break; // we reached end of zig zag parsing
            do{
                // we climb till col=7
                currentColumn += 1 ;
                currentLine -= 1;
                index +=1 ;
                result[index] = matrix[currentLine][currentColumn];
            }while (currentColumn != 7);

            currentLine += 1;
            index += 1;
            result[index] = matrix[currentLine][currentColumn];
            do{
                currentColumn -= 1;
                currentLine += 1;
                index += 1;
                result[index] = matrix[currentLine][currentColumn];

            }while (currentLine !=7);
        }
        return result;
    }

    private int getSizeBasedOnAmplitudeMap(int amplitudeValue){
        if(amplitudeValue == 0)
            return 0;
        if(amplitudeValue == -1 || amplitudeValue == 1)
            return 1;
        for(int key: amplitudeMap.keySet()){
            if(key==1)
                continue;
            List<Integer> bounds = amplitudeMap.get(key);
            if(amplitudeValue >= bounds.get(0) && amplitudeValue <= bounds.get(1))
                return key;
            if(amplitudeValue >= bounds.get(2) && amplitudeValue <= bounds.get(3))
                return key;
        }
        return -1;
    }

    public  void tryEncode(int[] zigZag){
        List<EntropyElement> entropy = new ArrayList<>();
        populateAmplitudeMap();
        EntropyElement  dcComponent = new EntropyElement(getSizeBasedOnAmplitudeMap(zigZag[0]), zigZag[0]);
        entropy.add(dcComponent);
        Integer index = 1;
        while (true){

            int noZeros = 0;
            while (index < 5 && zigZag[index] == 0){
                noZeros += 1;
                index+=1;
            }
            if(index == 5){
                entropy.add(new EntropyElement(0,0));
                break;
            }
            else{
                entropy.add(new EntropyElement(noZeros, getSizeBasedOnAmplitudeMap(zigZag[index]), zigZag[index]));
            }
            if (index==4)
                break;
            index++;
        }
        System.out.println(entropy);
    }

    private void encode(Block block){
        int[] zigZag = zigZagParsing(block.getGMatrix());

        EntropyElement  dcComponent = new EntropyElement(getSizeBasedOnAmplitudeMap(zigZag[0]), zigZag[0]);
        entropy.add(dcComponent);
        Integer index = 1;
        while (true){

            int noZeros = 0;
            while (index < 64 && zigZag[index] == 0){
                noZeros += 1;
                index+=1;
            }
            if(index == 64){
                entropy.add(new EntropyElement(-2,0,0));
                break;
            }
            else{
                entropy.add(new EntropyElement(noZeros, getSizeBasedOnAmplitudeMap(zigZag[index]), zigZag[index]));
            }
            if(index==63)
                break;
            index++;
        }

    }

    private void entropyEncoding(){
        /*
        encZigU = new ArrayList<int[]>();
        encZigY = new ArrayList<int[]>();
        encZigV = new ArrayList<int[]>();*/
        for(int i=0; i<encodedY.size(); i++){
            /*
            encZigY.add(zigZagParsing(encodedY.get(i).getGMatrix()));
            encZigU.add(zigZagParsing(encodedU.get(i).getGMatrix()));
            encZigV.add(zigZagParsing(encodedV.get(i).getGMatrix()));*/

            encode(encodedY.get(i));
            encode(encodedU.get(i));
            encode(encodedV.get(i));
        }
    }

    private void writeToFile(){
        try{
            FileWriter fileWriter = new FileWriter("./sources/entropy.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for(EntropyElement element: entropy){
                if(element.run_length == -1){
                    printWriter.println(element.size+","+element.amplitude);
                }
                else
                    printWriter.println(element.run_length+","+element.size+","+element.amplitude);
            }
            printWriter.close();
        }
        catch (Exception e){
            //System.out.println(e.getMessage());
        }
    }


    // lab 2

    public PPM getImage(){return this.image; }

    public List<Block> getEncodedY(){return encodedY;}
    public List<Block> getEncodedU(){return encodedU;}
    public List<Block> getEncodedV(){return encodedV;}

    private void subtractValues(List<Block> blocks){
        for(Block block: blocks)
            for(int i=0; i<8; i++)
                for(int j=0; j<8; j++)
                    block.setBlockValue(i,j, block.getBlockValue(i,j)-128);
    }

    // Forward DCT

    private void executeForwardDCTonBlocks(List<Block> blocks){
        for(Block block:blocks){
            _executeForwardDCT(block);
        }
    }

    private void _executeForwardDCT(Block block){
        double[][] matrix = block.getBlock();
        double constant = (double) 1/4;
        int[][] g = new int[8][8];
        for(int u=0; u<8; u++)
            for(int v=0; v<8; v++)
                g[u][v] = (int) (constant*alpha(u)*alpha(v)*sumOnX(matrix, u, v));
        block.setGMatrix(g);

    }

    private double alpha(int value) {
        return value > 0 ? 1 : (1 / Math.sqrt(2.0));
    }

    private double sumOnX(double[][] matrix, int u, int v){
        double sum = 0.0;
        for(int x=0; x<8; x++)
            sum += sumOnY(matrix, u, v, x);
        return sum;
    }

    private double sumOnY(double[][] matrix, int u, int v, int x){
        double sum = 0.0;
        for(int y = 0 ; y<8; y++){
            double cosU = Math.cos(((2 * x + 1) * u * Math.PI) / 16);
            double cosV = Math.cos(((2 * y + 1) * v * Math.PI) / 16);
            sum += matrix[x][y]*cosU*cosV;
        }
        return sum;
    }


    // Quantization
    private void quantizationPhase(List<Block> blocks){
        for(Block block: blocks)
            _quantizationPhase(block);
    }

    private void _quantizationPhase(Block block){
        int result[][] = new int[8][8];
        for(int i=0; i<8; i++)
            for(int j=0; j<8; j++){
                double res = block.getGMatrix()[i][j] / Q[i][j];
                result[i][j] = res < 0? (int) Math.ceil(res) : (int) Math.floor(res);
            }
        block.setGMatrix(result);
    }

}

