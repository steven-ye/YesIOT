package com.example.yesiot.helper;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpHelper {
    Callback mCallback;
    Handler mHandler;
    public OkHttpHelper(Callback callback) {
        mCallback = callback;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public OkHttpHelper(){
        this(null);
    }

    /**
     * 同步Get方法
     */
    public void get(String url) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String result = response.body().string(); // response.body().string() 仅能调用一次
                Log.i("okHttp3.GET", result);
                int code = response.code();
                if (!response.isSuccessful()) {
                    result = "Request error: " + url;
                    Log.e("okHttp3.GET", "[" + code + "]" + result);
                }

                if(mCallback != null){
                    String message = result;
                    //mHandler.post(() -> mCallback.result(message, code));
                    mHandler.post(() -> mCallback.result(message, code));
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(mCallback != null){
                    mHandler.post(() -> mCallback.result(e.getMessage(), 2));
                }
            }
        }).start();
    }

    public void get(String url, Callback callback){
        get(url, new HashMap<>(), callback);
    }
    public void get(String url, Map<String, String> header){
        get(url, header, null);
    }
    /**
     * 异步 Get方法
     */
    public void get(String url, Map<String, String> header, Callback callback){
        try {
            OkHttpClient client = new OkHttpClient();
            Request.Builder builder = new Request.Builder().url(url);
            for(String key: header.keySet()){
                builder.addHeader(key, Objects.requireNonNull(header.get(key)));
            }
            Request request = builder.build();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    Log.e("okHttp3.asyncGet",  "Failed");
                    if(callback != null){
                        mHandler.post(() -> callback.result(e.getMessage(), 0));
                    }else if(mCallback != null){
                        mHandler.post(() -> mCallback.result(e.getMessage(), 2));
                    }
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                    assert response.body() != null;
                    int code = response.code();
                    String result = response.body().string();
                    // 注：该回调是子线程，非主线程
                    Log.i("okHttp3.asyncGet","callback thread id is "+Thread.currentThread().getId());
                    Log.i("okHttp3.asyncGet", result);
                    if(callback != null){
                        mHandler.post(() -> callback.result(result, code));
                    }else if(mCallback != null){
                        mHandler.post(() -> mCallback.result(result, code));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if(mCallback != null){
                mHandler.post(() -> mCallback.result(e.getMessage(), 2));
            }
        }
    }

    public void post(String url, Map<String, String> data) {
        new Thread(() -> {
            try {
                // 请求完整url：http://api.k780.com:88/?app=weather.future&weaid=1&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json
                // String url = "http://api.k780.com:88/";
                OkHttpClient okHttpClient = new OkHttpClient();
                /*
                String json = "{'app':'weather.future','weaid':'1','appkey':'10003'," +
                        "'sign':'b59bc3ef6191eb9f747dd4e83c99f2a4','format':'json'}";
                 */
                FormBody.Builder builder = new FormBody.Builder();
                for(String key: data.keySet()){
                    builder.add(key, Objects.requireNonNull(data.get(key)));
                }
                RequestBody formBody = builder.build();
                Request request = new Request.Builder().url(url).post(formBody).build();
                Response response = okHttpClient.newCall(request).execute();
                assert response.body() != null;
                String result = response.body().string();
                Log.i("okHttp3", result);
                if(mCallback == null) return;
                mHandler.post(() -> mCallback.result(result, response.code()));
            } catch (Exception e) {
                e.printStackTrace();
                if(mCallback != null){
                    mHandler.post(() -> mCallback.result(e.getMessage(), 0));
                }
            }
        }).start();
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }
    public interface Callback {
        void result(String message, int code);
    }
}
