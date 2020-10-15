package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Block {
    private String blockType;
    private Integer size;
    private Integer xCoordinate;
    private Integer yCoordinate;
    private Integer originalWidth;
    private Integer originalHeight;
    private double[][] block;

    public Block(int size, String blockType){
        this.size = size;
        this.blockType = blockType;
        this.block = new double[size][size];
        // xCoordinate and yCoordinate, as well the block matrix,
        // will be set a later time using the setter functions
    }
}
