package com.example.yesiot.object;

import com.example.yesiot.util.Utils;

import java.util.ArrayList;

public class Panel {
    public int id=0;
    public int type=0;
    public int deviceId=0;
    public int width=250;
    public int height=250;
    public String name="";
    public String title="";
    public String unit="";
    public String image="";
    public String on="";
    public String off="";
    public String pos="";
    public String sub="";
    public String payload="";
    public String topic="";
    public String message="";

    public String status="";
    public boolean state=false;
    public int viewId;

    public Panel(){
        name = "panel_" + Utils.getRandomString(4);
        pos = Utils.getNum(0,300)+"#"+Utils.getNum(200,1200);
    }
}