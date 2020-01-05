package com.misbahulard.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SectionResponse {
    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Section> data;
    @SerializedName("time")
    private double time;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Section> getData() {
        return data;
    }

    public void setData(List<Section> data) {
        this.data = data;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
