package com.misbahulard.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HostResponse {
    @SerializedName("code")
    private int code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Host> data;
    @SerializedName("time")
    private double time;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public List<Host> getData() {
        return data;
    }

    public void setData(List<Host> data) {
        this.data = data;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
