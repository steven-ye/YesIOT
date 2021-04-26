package com.example.yesiot.object;

import java.util.List;

public class DeviceBean {
    private String name;
    private String code;
    private String theme;
    private List<String> pins;
    private String ip;
    private int port;

    public DeviceBean(String ip, int port, String uuid){
        this.ip = ip;
        this.port = port;
        code = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
    public String getTheme() {
        return theme;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        return ip;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }

    public void setPins(List<String> pins) {
        this.pins = pins;
    }
    public List<String> getPins() {
        return pins;
    }

}