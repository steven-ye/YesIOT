package com.example.yesiot.object;

import java.util.Map;

public class DeviceBean {
    public String name;
    public String code;
    public String topicIn;
    public String topicOut;
    public Map<String, Integer> events;
    public String ip;
    public int tcpPort;
    public String host;
    public int port;
    public int brokerId=0;
}