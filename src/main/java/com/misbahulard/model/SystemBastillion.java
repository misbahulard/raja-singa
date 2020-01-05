package com.misbahulard.model;

public class SystemBastillion {
    private String host;
    private String user;
    private String displayName;

    public SystemBastillion() {

    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SystemBastillion) {
            SystemBastillion myObj = (SystemBastillion) o;
            return this.host.equals(myObj.getHost()) && this.user.equals(myObj.getUser());
        } else {
            return false;
        }
    }

}
