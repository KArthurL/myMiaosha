package com.lcf.miaosha.dataobject;

public class SequenceDO {

    private String name;
    private Integer current_value;
    private Integer step;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCurrent_value() {
        return current_value;
    }

    public void setCurrent_value(Integer current_value) {
        this.current_value = current_value;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
}
