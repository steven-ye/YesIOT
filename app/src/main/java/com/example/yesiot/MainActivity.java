package com.example.yesiot;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.yesiot.object.MqttEvent;
import com.example.yesiot.service.MQTTConnection;
import com.example.yesiot.service.MQTTService;
import com.example.yesiot.ui.home.HomeViewModel;
import com.example.yesiot.util.Utils;
import com.google.android.material.navigation.NavigationView;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity  implements MQTTService.MQTTCallBack {
    private final String TAG = "MainActivity";
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
        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         */
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
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
            e.printStackTrace();
        }
        homeViewModel.setCloud("unknown");
        Intent intent = new Intent(this, MQTTService.class);
        bound = bindService(intent, mqttConnection, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mqttService);
        } else {
            startService(mqttService);
        }
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
        Utils.showToast("MQTT服务器连接成功");
        homeViewModel.setCloud("online");
        //String topic = "/yesiot/status/"+Utils.getIMEIDeviceId(this);
        //MQTTService.subscribe(topic);
        //MQTTService.publish(topic,"#online");
        MqttEvent event = new MqttEvent(MqttEvent.SUCCESS);
        EventBus.getDefault().postSticky(event);
    }

    @Override
    public void onFailure() {
        Utils.showToast("MQTT服务器连接失败");
        homeViewModel.setCloud("offline");
        MqttEvent event = new MqttEvent(MqttEvent.FAILURE);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onLost() {
        Utils.showToast("MQTT服务器连接丢失");
        homeViewModel.setCloud("offline");
        MqttEvent event = new MqttEvent(MqttEvent.LOST);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onMessageArrived(String topic, String message) {
        Log.w(TAG, "["+topic+"] "+message);
        //只处理来自单片机的信息
        if(message.startsWith("#")){
            message = message.replaceFirst("#","");
        }
        MqttEvent event = new MqttEvent(topic.trim(), message.trim());
        EventBus.getDefault().post(event);
    }
}