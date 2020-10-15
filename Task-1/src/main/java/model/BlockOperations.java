package model;

import java.util.ArrayList;
import java.util.List;

public class BlockOperations {
    // the main operations we have to to is the splitting in block operation
    // meaning that from a PPM image we transform the Y matrix in blocks of
    // 8x8 and the U, respectively V, will be transformed in some special 4x4
    // blocks
    public static List<Block>  toBlocks(PPM image, double[][] original, String type){
        List<Block> blocks = new ArrayList<>();
        for(int i=0; i<image.getHeight(); i=i+8)
            for(int j=0; j<image.getWidth(); j=j+8) {
                Block block;
                double[][] elements;
                int size;
                if(type.equals("Y")){
                    size = 8;
                    elements = getSubMatrix8x8(i,j,original);
                }
                else{
                    size = 4;
                    elements = getSubMatrixWithSubSampling(i, j, original);
                }
                block = new Block(size, type);
                block.setOriginalHeight(image.getHeight());
                block.setOriginalWidth(image.getWidth());
                block.setBlock(elements);
                block.setXCoordinate(i);
                block.setYCoordinate(j);
                blocks.add(block);
            }
        return blocks;
    }

    private static double[][] getSubMatrix8x8(int iPos, int jPos, double[][] original){
        // this method simply retrieves us the subMatrix of size 8x8 with the upper
        // left corner having coordinates iPos and jPos
        // it will be applied for the Y matrix
        double[][] result = new double[8][8];
        for(int i=0; i<8; i++)
            for(int j=0; j<8; j++)
                result[i][j] = original[iPos+i][jPos+j];
        return result;
    }

    private static double[][] getSubMatrixWithSubSampling(int iPos, int jPos, double[][] original){
        // this method will extract a 4x4 matrix by performing a 4:2:0 sub sampling a 8x8 matrix that
        // has the upper left corner in coordinates iPos and jPos
        // used for transformations applied on the U and V matrixes
        double[][] subMatrix = BlockOperations.getSubMatrix8x8(iPos, jPos, original);
        double[][] result = new double[4][4];
        int line, column;
        line=column=0;
        for(int i=0; i<4; i++){
            for(int j=0; j<4; j++){
                result[i][j] = (subMatrix[line][column]+
                        subMatrix[line+1][column]+
                        subMatrix[line][column+1]+
                        subMatrix[line+1][column+1])/4.0;
                column += 2;
                }
            line+=2;
            column=0;
        }
        return result;
    }

    public static List<Block> decompressBlocks(List<Block> blocks){
        List<Block> decompressedBlocks = new ArrayList<>();
        blocks.forEach(b -> decompressedBlocks.add(to8x8Format(b)));
        return decompressedBlocks;
    }
    private static Block to8x8Format(Block originalBlock){
        // given a compressed matrix on 4x4 we create the decompressed
        // version, an 8x8 version, where each 2x2 square has the value
        // provided by one element from the compressed 4x4 matrix
        Block newBlock = new Block(8, originalBlock.getBlockType());
        newBlock.setOriginalWidth(originalBlock.getOriginalWidth());
        newBlock.setOriginalHeight(originalBlock.getOriginalHeight());
        newBlock.setXCoordinate(originalBlock.getXCoordinate());
        newBlock.setYCoordinate(originalBlock.getYCoordinate());
        newBlock.setXCoordinate(originalBlock.getXCoordinate());
        newBlock.setYCoordinate(originalBlock.getYCoordinate());
        double[][] elements = new double[8][8];
        double[][] olderVersion = originalBlock.getBlock();
        int line, column;
        line=column=0;
        for(int i=0; i<4; i++){
            for(int j=0; j<4; j++){
                elements[line][column] = olderVersion[i][j];
                elements[line+1][column] = olderVersion[i][j];
                elements[line][column+1] = olderVersion[i][j];
                elements[line+1][column+1] = olderVersion[i][j];
                column += 2;
            }
            column = 0;
            line += 2;
        }
        newBlock.setBlock(elements);
        return newBlock;
    }


    public static double[][] reconstructMatrix(List<Block> blocks){
        double[][] elements = new double[blocks.get(0).getOriginalHeight()][blocks.get(0).getOriginalWidth()];
        for(Block block: blocks){
            int line, column;
            line=column=0;
            for(int i=block.getXCoordinate(); i<block.getXCoordinate()+8; i++){
                for(int j=block.getYCoordinate(); j<block.getYCoordinate()+8; j++){
                    elements[i][j] = block.getBlock()[line][column];
                    column++;
                }
                column=0;
                line++;
            }
        }
        return elements;
    }
}
