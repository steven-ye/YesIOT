package com.example.yesiot.object;

public class Constants {
    public static final String DB_NAME = "yesiot";
    public static final int DB_VERSION = 3;
    public static final int TCP_SERVER_PORT = 8266;
    public static final String TOPIC_PREFIX = "yesiot";

    public static final String DEVICE_SEARCH_MSG = "YESIOT";
    public static final int DEVICE_SEARCH_PORT = 10302;
    public static final int DEVICE_SEARCH_TIMEOUT = 1000;
    public static final int SCANNING_THREAD_COUNT = 90;
    public static final int SEARCH_DEVICE_TIMES = 3;
    public static final int SEARCH_DEVICE_MAX = 200;
    public static final byte PACKET_PREFIX = 111;
    public static final byte PACKET_TYPE_SEARCH_DEVICE_REQ = 110;
    public static final byte PACKET_TYPE_SEARCH_DEVICE_RSP = 120;

    public static final int DEFAULT_PANEL_SIZE = 150;
}
