package com.example.yesiot.helper;

import static android.content.Context.WIFI_SERVICE;
import static android.net.ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.core.location.LocationManagerCompat;

import com.example.yesiot.R;
import com.example.yesiot.util.NetUtils;

import java.net.InetAddress;

public class EspTouchHelper {
    Context mContext;
    WifiManager mWifiManager;

    NetworkRequest mRequest;
    ConnectivityManager mConnectivityManager;
    WifiInfo mWifiInfo;
    boolean mWifiConnected = false;

    public EspTouchHelper(Context context){
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build();
        requestNetwork(mRequest);
    }
    public StateResult checkState() {
        StateResult result = new StateResult();
        checkPermission(result);
        if (!result.enable) {
            return result;
        }

        checkLocation(result);
        if (!result.enable) {
            return result;
        }

        return checkWifi(result);
    }

    private void checkPermission(StateResult result) {
        result.permissionGranted = true;
        boolean locationGranted = mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!locationGranted) {
            String[] splits = mContext.getString(R.string.esptouch_message_permission).split("\n");
            if (splits.length != 2) {
                throw new IllegalArgumentException("Invalid String @RES esptouch_message_permission");
            }
            SpannableStringBuilder ssb = new SpannableStringBuilder(splits[0]);
            ssb.append('\n');
            SpannableString clickMsg = new SpannableString(splits[1]);
            ForegroundColorSpan clickSpan = new ForegroundColorSpan(0xFF0022FF);
            clickMsg.setSpan(clickSpan, 0, clickMsg.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ssb.append(clickMsg);
            result.message = ssb;


            result.permissionGranted = false;
            result.enable = false;
        }

    }

    private void checkLocation(StateResult result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager manager = mContext.getSystemService(LocationManager.class);
            boolean enable = manager != null && LocationManagerCompat.isLocationEnabled(manager);
            if (!enable) {
                result.message = mContext.getString(R.string.esptouch_message_location);
                result.enable = false;
            }
        }
    }

    private StateResult checkWifi(StateResult result) {
        result.wifiConnected = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            result.wifiConnected = NetUtils.isWifiConnected(mContext);
        }else{
            result.wifiConnected = NetUtils.isWifiConnected(mWifiManager);
        }

        if (!result.wifiConnected) {
            result.message = mContext.getString(R.string.esptouch_message_wifi_connection);
            return result;
        }

        getWifiInfo(result);

        return result;
    }

    private void getWifiInfo(StateResult result){
        mWifiInfo = mWifiManager.getConnectionInfo();
        if(mWifiInfo == null) return;
        String ssid = NetUtils.getSsidString(mWifiInfo);
        int ipValue = mWifiInfo.getIpAddress();
        if (ipValue != 0) {
            result.address = NetUtils.getAddress(mWifiInfo.getIpAddress());
        } else {
            result.address = NetUtils.getIPv4Address();
            if (result.address == null) {
                result.address = NetUtils.getIPv6Address();
            }
        }

        result.wifiConnected = true;
        result.message = "";
        result.is5G = NetUtils.is5G(mWifiInfo.getFrequency());
        if (result.is5G) {
            result.message = mContext.getString(R.string.esptouch_message_wifi_frequency);
        }
        result.ssid = ssid;
        result.ssidBytes = NetUtils.getRawSsidBytesOrElse(mWifiInfo, ssid.getBytes());
        result.bssid = mWifiInfo.getBSSID();

        result.enable = result.wifiConnected;
    }

    final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            mWifiConnected = true;
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                mWifiInfo = (WifiInfo) networkCapabilities.getTransportInfo();
            }else{
                mWifiInfo = mWifiManager.getConnectionInfo();
            }
        }
    };

    private void listenNetwork(NetworkRequest request) {
        mConnectivityManager.registerNetworkCallback(request, mNetworkCallback); // For listen
    }

    private void requestNetwork(NetworkRequest request) {
        mConnectivityManager.registerNetworkCallback(request, mNetworkCallback);
    }

    private void unrequestNetwork() {
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
    }


    public static class StateResult {
        public CharSequence message = null;

        public boolean enable = true;

        public boolean permissionGranted = false;

        public boolean wifiConnected = false;

        public boolean is5G = false;
        public InetAddress address = null;
        public String ssid = null;
        public byte[] ssidBytes = null;
        public String bssid = null;
    }
}
