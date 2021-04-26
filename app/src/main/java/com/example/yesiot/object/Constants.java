package com.example.yesiot.object;

public class Constants {
    public final static String DB_NAME = "yesiot";
    public final static int DB_VERSION = 1;
    public final static int TCP_SERVER_PORT = 80;
    public final static String BROKER = "BROKER";
    public final static String TOPIC_PREFIX = "yesiot";

    public static final int SCANNING_THREADS_COUNT = 90;
    public static final int SCANNING_TCP_PORT = 80;
    public static final int DEVICE_SEARCH_PORT = 8114;
    public static final int SEARCH_DEVICE_TIMES = 3;
    public static final int SEARCH_DEVICE_MAX = 200;
    public static final int RECEIVE_TIME_OUT = 3000;
    public static final byte PACKET_PREFIX = 111;
    public static final byte PACKET_TYPE_SEARCH_DEVICE_RSP = 110;
    public static final byte PACKET_TYPE_SEARCH_DEVICE_REQ = 120;

}
