package com.example.yesiot.util;

import static android.content.Context.WIFI_SERVICE;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

public class NetUtils {
    public static boolean isWifiConnected(WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo != null
                && wifiInfo.getNetworkId() != -1
                && !"<unknown ssid>".equals(wifiInfo.getSSID());
    }

    public static boolean isWifiConnected(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }
                    /*
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                     */
                }
            } else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        return false;
    }

    public static byte[] getRawSsidBytes(WifiInfo info) {
        try {
            Method method = info.getClass().getMethod("getWifiSsid");
            method.setAccessible(true);
            Object wifiSsid = method.invoke(info);
            if (wifiSsid == null) {
                return null;
            }
            method = wifiSsid.getClass().getMethod("getOctets");
            method.setAccessible(true);
            return (byte[]) method.invoke(wifiSsid);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NullPointerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getRawSsidBytesOrElse(WifiInfo info, byte[] orElse) {
        byte[] raw = getRawSsidBytes(info);
        return raw != null ? raw : orElse;
    }

    public static String getSsidString(WifiInfo info) {
        if(null == info) return "";
        String ssid = info.getSSID();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public static InetAddress getBroadcastAddress(WifiManager wifi) {
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp != null) {
            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++) {
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            }
            try {
                return InetAddress.getByAddress(quads);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        try {
            return InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // Impossible arrive here
        return null;
    }

    public static boolean is5G(int frequency) {
        return frequency > 4900 && frequency < 5900;
    }

    public static InetAddress getAddress(int ipAddress) {
        byte[] ip = new byte[]{
                (byte) (ipAddress & 0xff),
                (byte) ((ipAddress >> 8) & 0xff),
                (byte) ((ipAddress >> 16) & 0xff),
                (byte) ((ipAddress >> 24) & 0xff)
        };

        try {
            return InetAddress.getByAddress(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // Impossible arrive here
            return null;
        }
    }

    private static InetAddress getAddress(boolean isIPv4) {
        try {
            Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
            while (enums.hasMoreElements()) {
                NetworkInterface ni = enums.nextElement();
                Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress address = addrs.nextElement();
                    if (!address.isLoopbackAddress()) {
                        if (isIPv4 && address instanceof Inet4Address) {
                            return address;
                        }
                        if (!isIPv4 && address instanceof Inet6Address) {
                            return address;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InetAddress getIPv4Address() {
        return getAddress(true);
    }

    public static InetAddress getIPv6Address() {
        return getAddress(false);
    }

    /**
     * @param bssid the bssid like aa:bb:cc:dd:ee:ff
     * @return byte array converted from bssid
     */
    public static byte[] convertBssid2Bytes(String bssid) {
        String[] bssidSplits = bssid.split(":");
        if (bssidSplits.length != 6) {
            throw new IllegalArgumentException("Invalid bssid format");
        }
        byte[] result = new byte[bssidSplits.length];
        for (int i = 0; i < bssidSplits.length; i++) {
            result[i] = (byte) Integer.parseInt(bssidSplits[i], 16);
        }
        return result;
    }

    public static List<ScanResult> getApList(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        return wifiManager.getScanResults();
    }
}
