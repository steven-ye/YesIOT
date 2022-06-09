package com.example.yesiot.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Size;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.example.yesiot.IApplication;
import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.object.Constants;
import com.example.yesiot.object.Device;
import com.example.yesiot.service.MQTTService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class Utils {
    public static String getTopic(Device device){
        String topic = device.getTopic();
        if(!TextUtils.isEmpty(topic))return topic;
        topic = Constants.TOPIC_PREFIX;
        if(!TextUtils.isEmpty(device.getTheme())){
            topic = topic + device.getTheme() + "/";
        }
        if(!TextUtils.isEmpty(device.getCode())) {
            topic = topic + "/" + device.getCode();
        }
        return topic + "/cmd";
    }
    public static String getSubTopic(Device device){
        String topic = device.getSub();
        if(!TextUtils.isEmpty(topic))return topic;
        topic = Constants.TOPIC_PREFIX;
        if(!TextUtils.isEmpty(device.getTheme())){
            topic = topic + "/" + device.getTheme();
        }
        if(!TextUtils.isEmpty(device.getCode())) {
            topic = topic + "/" + device.getCode();
        }
        return topic + "/status";
    }

    public static Size getScreenSize(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        context.getDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;  // 屏幕宽度（像素）
        int height= dm.heightPixels; // 屏幕高度（像素）

        return new Size(width,height);
    }

    public static int getScreenWidth(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        context.getDisplay().getRealMetrics(dm);
        return dm.widthPixels;
    }
    public static int getScreenHeight(Context context){
        Size size = getScreenSize(context);
        return size.getHeight();
    }

    public static void showToast(String message){
        showToast(IApplication._getContext(), message);
    }
    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void alert(Context context, String message){
        String[] items = new String[]{message};
        alert(context, items);
    }

    public static void alert(Context context, String[] items){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setIcon(R.drawable.ic_led)
                .setTitle("温馨提示")
                .setItems(items,null)
                .setPositiveButton("确定", null);

        builder.show();
    }

    public static void createNotification(Context context, String message){
        String name = "channelName";
        String CHANNEL_ID = context.getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{3});
            channel.enableLights(false);
            channel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        notificationManager.notify(1,buildNotification(context, CHANNEL_ID, message));
        //context.startForeground(NOTIFICATION_ID, buildNotification());
    }

    public static Notification buildNotification(Context context, String channel_id, String message) {
        Intent intent=new Intent(context, MainActivity.class);
        //PendingIntent点击通知后跳转，一参：context 二参：一般为0 三参：intent对象 四参：一般为0
        PendingIntent pendingIntent=PendingIntent.getActivity(context,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) //完成跳转自动取消通知
                .setWhen(System.currentTimeMillis())
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                .setVibrate(new long[]{3})
                .setSound(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channel_id);
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
        return notification;
    }

    /*
     * 是否为浮点数？double或float类型。
     * @param str 传入的字符串。
     * @return 是浮点数返回true,否则返回false。
     */
    public static boolean isDoubleOrFloat(String str) {
        if(TextUtils.isEmpty(str))return false;
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    /** 根据路径获取Bitmap图片
     * @param context Context
     * @param path String
     * @return bitmap
     */
    public static Bitmap getAssetsBitmap(Context context, String path){
        if(TextUtils.isEmpty(path))return null;
        AssetManager am = context.getAssets();
        Bitmap bitmap = null;
        try {
            InputStream inputStream = am.open(path);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /** 根据路径获取Drawable图片
     * @param context Context
     * @param path String
     * @return drawable
     */
    public static Drawable getAssetsDrawable(Context context, String path){
        AssetManager am = context.getAssets();
        InputStream inputStream = null;
        Drawable drawable = null;
        try {
            inputStream = am.open(path);
            drawable = Drawable.createFromStream(inputStream, null);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //img.setImageDrawable(drawable);
        //img.setScaleType(ScaleType.FIT_XY);
        //img.setAdjustViewBounds(true);  // 重点
        return drawable;
    }

    /**
     * 生成一个startNum 到 endNum之间的随机数(不包含endNum的随机数)
     * @param startNum
     * @param endNum
     * @return
     */
    public static int getNum(int startNum,int endNum){
        if(endNum > startNum){
            Random random = new Random();
            return random.nextInt(endNum - startNum) + startNum;
        }
        return 0;
    }
    //length用户要求产生字符串的长度
    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static int getRandomInt(int digitNumber) {
        if (digitNumber == 0) return 0;
        return (int) ((Math.random() * 9 + 1) * (Math.pow(10, digitNumber - 1)));
    }

    @SuppressLint("HardwareIds")
    public static String getIMEIDeviceId(Context context) {

        String deviceId;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "";
                }
            }
            assert mTelephony != null;
            if (mTelephony.getDeviceId() != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    deviceId = mTelephony.getImei();
                }else {
                    deviceId = mTelephony.getDeviceId();
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        return deviceId;
    }

    public static List<String> getAssetPicPath(Context context, String ImagePath){
        AssetManager assets = context.getAssets();
        List<String> picPaths = new ArrayList<>();
        String[] paths = null;
        try {
            paths = assets.list(ImagePath);  // ""获取所有,填入目录获取该目录下所有资源
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String path : paths) {
            if (path.endsWith(".png")) {  // 根据图片特征找出图片
                if(!TextUtils.isEmpty(ImagePath)){
                    path = ImagePath+"/"+path;
                }
                picPaths.add(path);
            }
        }
        return picPaths;
    }

    static public String getRegexBySub(String topic){
        // 注册的命令topic为$sys/{pid}/{device-name}/cmd/#
        if (topic.startsWith("$")) {
            topic = "\\" + topic;
        }

        return topic.replaceAll("/", "\\\\/")
                .replaceAll("\\+", "[^/]+")
                .replaceAll("#", "(.+)") + "$";
    }

    /** Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }



    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }
}
