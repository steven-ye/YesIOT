package com.example.yesiot.ui.esptouch;

import static android.content.Context.WIFI_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.example.yesiot.AbsFragment;
import com.example.yesiot.ApListDialog;
import com.example.yesiot.IApplication;
import com.example.yesiot.R;
import com.example.yesiot.helper.EspTouchHelper;
import com.example.yesiot.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EspTouchFragment extends AbsFragment {
    private static final int REQUEST_PERMISSION = 0x01;

    Activity mActivity;
    Context mContext;
    EspViewModel mViewModel;
    ApListDialog mListDialog;
    ExecutorTask mTask;
    WifiManager mWifiManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_esptouch, container, false);
        mActivity = getActivity();
        mContext = getContext();
        assert mContext != null;
        mWifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);

        mViewModel = new EspViewModel(root);
        mViewModel.scanBtn.setOnClickListener(v->showApList());
        mViewModel.confirmBtn.setOnClickListener(v->executeEsptouch());
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        mActivity.requestPermissions(permissions, REQUEST_PERMISSION);
        IApplication.getInstance().observeBroadcast(this, broadcast -> {
            Log.d(TAG, "onCreate: Broadcast=" + broadcast);
            onWifiChanged();
        });
        onWifiChanged();
        mViewModel.invalidateAll();
        return root;
    }

    private void executeEsptouch(){
        String apPassword = Objects.requireNonNull(mViewModel.apPasswordEdit.getText()).toString();
        String devCountStr = Objects.requireNonNull(mViewModel.deviceCountEdit.getText()).toString();
        int deviceCount = TextUtils.isEmpty(devCountStr) ? 0 : Integer.parseInt(devCountStr);
        boolean broadcast = mViewModel.packageModeGroup.getCheckedRadioButtonId() == R.id.packageBroadcast;

        if(TextUtils.isEmpty(apPassword)){
            Utils.alert(mContext,"密码不能为空");
            mViewModel.apPasswordEdit.requestFocus();
            return;
        }
        if(mTask == null){
            mTask = new ExecutorTask(mContext);
            mTask.setCallback((results -> {
                for(IEsptouchResult result: results){
                    Log.d("配网", result.toString());
                    if(result.isSuc()){
                        mActivity.runOnUiThread(()-> alert("配网成功: " + result.getInetAddress().getHostAddress()));
                    }
                }
            }));
        }
        mTask.execute(mViewModel.ssid, mViewModel.bssid, apPassword, broadcast, deviceCount);
    }


    private void onWifiChanged() {
        EspTouchHelper helper = new EspTouchHelper(mContext);
        EspTouchHelper.StateResult stateResult = helper.checkState();
        mViewModel.message = stateResult.message;
        mViewModel.ssid = stateResult.ssid;
        mViewModel.ssidBytes = stateResult.ssidBytes;
        mViewModel.bssid = stateResult.bssid;
        mViewModel.confirmEnable = false;
        if (stateResult.wifiConnected) {
            mViewModel.confirmEnable = true;
            if (stateResult.is5G) {
                mViewModel.message = getString(R.string.esptouch1_wifi_5g_message);
            }
        } else {
            new AlertDialog.Builder(mContext)
                    .setMessage(R.string.esptouch1_configure_wifi_change_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    public void showApList(){
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        @SuppressLint("MissingPermission") List<ScanResult> results = wifiManager.getScanResults();
        for(ScanResult result: results){
            Log.d(TAG, result.toString());
        }
        if(null == mListDialog){
            mListDialog = new ApListDialog(getParentFragmentManager());
            mListDialog.setOnClickListener((view, position)->{
                ScanResult result = results.get(position);
                mViewModel.ssid = result.SSID;
                mViewModel.bssid = result.BSSID;
                mViewModel.invalidateAll();
                mListDialog.dismiss();
            });
        }
        mListDialog.show(results);
    }

    private static class ExecutorTask{
        Context mContext;
        AlertDialog mDialog;
        ExecutorService mService;
        EsptouchTask mEsptouchTask;
        public ExecutorTask(Context context){
            mContext = context;
            mDialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setMessage("配网中，请稍等 ...")
                    .setNegativeButton("取消", (dialogInterface, i) -> cancelTask()).create();
        }

        public void cancelTask(){
            if(mEsptouchTask != null) mEsptouchTask.interrupt();
            mService.shutdownNow();
            mDialog.dismiss();
        }

        public void execute(String ssid, String bssid, String password, boolean broadcast, int deviceCount){
            mDialog.show();
            Log.d("配网", "开始");
            mService = Executors.newSingleThreadExecutor();
            mService.execute(()->{
                try{
                    mEsptouchTask = new EsptouchTask(ssid, bssid, password, mContext);
                    mEsptouchTask.setPackageBroadcast(broadcast); // if true send broadcast packets, else send multicast packets
                    mEsptouchTask.setEsptouchListener(result -> Log.d("配网", result.toString()));
                    List<IEsptouchResult> results = mEsptouchTask.executeForResults(deviceCount);
                    Log.d("配网", "结束");
                    if(null != mCallback) mCallback.done(results);
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d("配网", "出错");
                }finally{
                    mDialog.dismiss();
                }
            });
            mService.shutdown();
        }

        Callback mCallback;
        public void setCallback(Callback callback){
            mCallback = callback;
        }
        public interface Callback {
            void done(List<IEsptouchResult> results);
        }
    }
}
