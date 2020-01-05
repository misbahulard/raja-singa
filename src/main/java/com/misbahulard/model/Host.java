package com.misbahulard.model;

import com.google.gson.annotations.SerializedName;

public class Host {
    @SerializedName("hostname")
    private String hostname;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
