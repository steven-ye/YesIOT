package com.example.yesiot.object;

import java.util.ArrayList;
import java.util.List;

public class Device {
    private int id = 0;
    private String name;
    private String code;
    private String theme;
    private String image = "";
    private List<String> pins = new ArrayList<>();
    private String ip = "";
    private int port = Constants.TCP_SERVER_PORT;
    private String sub = "";
    private String topic = "";
    private String payload = "";
    private String cmdOff = "";
    private String status = "";

    public Device(){}
    public Device(String name, String code, String ip){
        this.name = name;
        this.code = code;
        this.ip = ip;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
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

    public void setImage(String image) {
        this.image = image;
    }
    public String getImage() {
        return image;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        return ip;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getSub() {
        return sub;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getTopic() {
        return topic;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
    public String getPayload() {
        return payload;
    }

    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    public void setPins(List<String> pins) {
        this.pins = pins;
    }
    public List<String> getPins() {
        return pins;
    }
}
