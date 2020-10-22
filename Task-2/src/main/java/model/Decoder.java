package model;

import java.util.ArrayList;
import java.util.List;

public class Decoder {
    private Encoder encoder;
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


    public Decoder(Encoder encoder){
        this.encoder = encoder;

        encodedU = encoder.getEncodedU();
        encodedV = encoder.getEncodedV();
        encodedY = encoder.getEncodedY();

        deQuantizationPhase(encodedY);
        deQuantizationPhase(encodedU);
        deQuantizationPhase(encodedV);

        inverseDCT(encodedY);
        inverseDCT(encodedU);
        inverseDCT(encodedV);

        addValue(encodedY);
        addValue(encodedU);
        addValue(encodedV);

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
