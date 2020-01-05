package com.misbahulard.model;

import com.google.gson.annotations.SerializedName;

public class Auth {
    @SerializedName("token")
    private String token;
    @SerializedName("expires")
    private String expires;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }
}
