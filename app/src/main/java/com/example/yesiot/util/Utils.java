package com.example.yesiot.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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

import com.example.yesiot.IApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {
    public static Size getScreenSize(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        context.getDisplay().getRealMetrics(dm);
        int width = dm.widthPixels;  // 屏幕宽度（像素）
        int height= dm.heightPixels; // 屏幕高度（像素）

        return new Size(width,height);
    }
    public static void showToast(String message){
        showToast(IApplication._getContext(), message);
    }
    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
}
