package com.example.yesiot.object;

import com.example.yesiot.util.Utils;

public class Panel {
    public int id=0;
    public int type=0;
    public int design=0;
    public int deviceId=0;
    public int width=400;
    public int height=400;
    public String name;
    public String title="";
    public String unit="";
    public String image="";
    public String on="1";
    public String off="0";
    public String size="";
    public String pos;
    public String payload="";
    public String message="";
    public String title_size="";
    public String unit_size="";

    public Panel(){
        name = "btn_" + Utils.getRandomString(4);
        pos = Utils.getNum(0,300)+"#"+Utils.getNum(200,1200);
    }
    public Panel(int deviceId, String name){
        this.deviceId = deviceId;
        this.name = name;
        pos = Utils.getNum(0,300)+"#"+Utils.getNum(200,1200);
    }
}