package com.picfun.abreak;

import java.util.List;

/**
 * @author Secret
 * @since 2019/9/18
 */
public class DeviceRank {

    private String brand;
    private String name;
    private String model;
    private String device;
    private String buId;
    private String modelId;
    private String score;
    private String avgscore;
    private String memory;
    private String phonememory;
    private List<Integer> s;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBuId() {
        return buId;
    }

    public void setBuId(String buId) {
        this.buId = buId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getAvgscore() {
        return avgscore;
    }

    public void setAvgscore(String avgscore) {
        this.avgscore = avgscore;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getPhonememory() {
        return phonememory;
    }

    public void setPhonememory(String phonememory) {
        this.phonememory = phonememory;
    }

    public List<Integer> getS() {
        return s;
    }

    public void setS(List<Integer> s) {
        this.s = s;
    }
}
