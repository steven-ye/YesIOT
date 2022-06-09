package com.example.yesiot.object;

public class Constants {
    public final static String DB_NAME = "yesiot";
    public final static int DB_VERSION = 1;
    public final static int TCP_SERVER_PORT = 8302;
    public final static String BROKER = "BROKER";
    public final static String TOPIC_PREFIX = "yesiot";

    public static final String DEVICE_SEARCH_MSG = "YESIOT";
    public static final int DEVICE_SEARCH_PORT = 10302;
    public static final int DEVICE_SEARCH_TIMEOUT = 5000;
    public static final int SCANNING_THREAD_COUNT = 90;
    public static final int SEARCH_DEVICE_TIMES = 3;
    public static final int SEARCH_DEVICE_MAX = 200;
    public static final byte PACKET_PREFIX = 111;
    public static final byte PACKET_TYPE_SEARCH_DEVICE_RSP = 110;
    public static final byte PACKET_TYPE_SEARCH_DEVICE_REQ = 120;

    public static final int DEFAULT_PANEL_SIZE = 150;

}
