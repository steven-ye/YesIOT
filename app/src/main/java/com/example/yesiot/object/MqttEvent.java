package com.example.yesiot.object;

public class MqttEvent {
    public static final int SUCCESS = 1;
    public static final int FAILURE = 2;
    public static final int LOST = 3;
    private int type = 0;
    private String topic;
    private String message;

    public MqttEvent(int type){
        this.type = type;
    }
    public MqttEvent(String topic, String message){
        this.topic = topic;
        this.message = message;
    }

    public int getType(){
        return type;
    }
    public void setType(int type){
        this.type = type;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
