package model;

import java.util.List;

public class Encoder {
    private PPM image;
    private List<Block> encodedY;
    private List<Block> encodedU;
    private List<Block> encodedV;
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

    }

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

