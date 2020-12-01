package model;

import lombok.ToString;

@ToString
public class EntropyElement {
    //(RUNLENGTH,SIZE)(AMPLITUDE)
    public Integer run_length;
    public Integer size;
    public Integer amplitude;

    EntropyElement(Integer size, Integer amplitude){
        run_length=-1;
        this.size = size;
        this.amplitude = amplitude;
    }

    public EntropyElement(Integer run_length, Integer size, Integer amplitude) {
        this.run_length = run_length;
        this.size = size;
        this.amplitude = amplitude;
    }

}
