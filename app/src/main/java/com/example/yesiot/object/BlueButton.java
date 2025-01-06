package com.example.yesiot.object;

import com.example.yesiot.util.Utils;

import java.io.Serializable;

public class BlueButton implements Serializable
{
    public int id=0;
    public int type=0;
    public int design=0;
    public int device_id=0;
    public int width=400;
    public int height=400;
    public int weight=0;
    public String uuid = "";
    public String service_uuid = "";
    public String name="";
    public String title="普通按钮";
    public String caption="说明";
    public String unit="";
    public String image="";
    public String cmd_on="1";
    public String cmd_off="0";
    public String payload="";
    public String size = "";
    public String pos = "";
    public String value="";
    public String extra="";

    public BlueButton(){
        name = "btn_" + Utils.getRandomString(4);
    }

    public BlueButton(String service_uuid)
    {
        this.service_uuid = service_uuid;
        name = "btn_" + Utils.getRandomString(4);
    }
    public BlueButton(String service_uuid, String uuid){
        this.service_uuid = service_uuid;
        this.uuid = uuid;
        pos = Utils.getNum(0,300)+"#"+Utils.getNum(200,1200);
    }
}