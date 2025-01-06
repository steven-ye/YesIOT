package com.example.yesiot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.yesiot.object.MqttEvent;
import com.example.yesiot.service.MQTTConnection;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.ui.home.HomeViewModel;
import com.example.yesiot.util.Utils;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity  implements MQTTService.MQTTCallBack {
    private AppBarConfiguration mAppBarConfiguration;
    private Intent mqttService;
    private MQTTConnection mqttConnection;
    private boolean bound;
    private HomeViewModel homeViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawerLayout)
                //.setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mqttService = new Intent(this, MQTTService.class);
        mqttConnection = MQTTConnection.getInstance();
        mqttConnection.setMqttCallBack(this);
        startMqttService();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * 启动后台服务
     */
    public void startMqttService() {
        stopMqttService();
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){
            Log.e("MainActivity", e.toString());
        }
        homeViewModel.setCloud("unknown");
        Intent intent = new Intent(this, MQTTService.class);
        bound = bindService(intent, mqttConnection, Context.BIND_AUTO_CREATE);
        //startForegroundService(mqttService);
        startService(mqttService);
    }

    public void stopMqttService(){
        if(bound)unbindService(mqttConnection);
        stopService(mqttService);
        bound = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMqttService();
    }

    @Override
    public void onSuccess() {
        Utils.showToast(this, "MQTT服务器连接成功");
        homeViewModel.setCloud("online");
        MqttEvent event = new MqttEvent(MqttEvent.SUCCESS);
        EventBus.getDefault().postSticky(event);
    }

    @Override
    public void onFailure() {
        Utils.showToast(this,"MQTT服务器连接失败");
        homeViewModel.setCloud("offline");
        MqttEvent event = new MqttEvent(MqttEvent.FAILURE);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onLost() {
        Utils.showToast(this,"MQTT服务器连接丢失");
        homeViewModel.setCloud("offline");
        MqttEvent event = new MqttEvent(MqttEvent.LOST);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        //Log.w(TAG, "["+topic+"] "+message);
        if(message.startsWith("#"))message = message.substring(1);
        MqttEvent event = new MqttEvent(topic.trim(), message.trim());
        EventBus.getDefault().post(event);
    }
}