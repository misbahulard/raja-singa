package com.misbahulard.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("code")
    private int code;
    @SerializedName("success")
    private boolean success;
    @SerializedName("data")
    private Auth data;
    @SerializedName("time")
    private double time;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Auth getData() {
        return data;
    }

    public void setData(Auth data) {
        this.data = data;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}
