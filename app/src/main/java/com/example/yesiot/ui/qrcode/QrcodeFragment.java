package com.example.yesiot.ui.qrcode;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.helper.OkHttpHelper;
import com.example.yesiot.object.ResultBean;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.HashMap;
import java.util.Map;

public class QrcodeFragment extends AbsFragment {
    String TAG = getClass().getSimpleName();
    ViewModel viewModel;
    String foundUrl="";

    private static class ViewModel {
        TextView tvMessage;
        TextView tvReason;
        AppCompatButton button;
        TextView tvZXing;

        public ViewModel(View root){
            tvMessage = root.findViewById(R.id.qrcode_message);
            tvReason = root.findViewById(R.id.qrcode_reason);
            button = root.findViewById(R.id.qrcode_button_ok);
            tvZXing = root.findViewById(R.id.zxing_version);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_qrcode, container, false);
        viewModel = new ViewModel(root);
        viewModel.tvZXing.setText("当前版本：v4.3.0");

        OkHttpHelper http = new OkHttpHelper(okhttp_callback);

        viewModel.button.setOnClickListener(view -> {
            if(TextUtils.isEmpty(foundUrl)){
                showToast("目标网址为空, 请重新扫描二维码");
                return;
            }
            view.setVisibility(View.GONE);
            Map<String, String> map = new HashMap<>();
            map.put("user_id", "1");
            map.put("account", "steven");
            map.put("apikey", "181bc855e5877b283f33d5273e570b07");
            map.put("client_id", "yesiot");
            http.post(foundUrl, map);
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.qrcode, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.menu_item_qrcode){
            qrcodeScan();
        }
        return super.onOptionsItemSelected(item);
    }

    private void qrcodeScan(){
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(true);
        barcodeLauncher.launch(options);
        //barcodeLauncher.launch(new ScanOptions());
    }

    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                viewModel.tvReason.setText("");
                if(result.getContents() == null) {
                    showToast("取消扫描");
                    viewModel.tvMessage.setText("取消扫描");
                } else {
                    showToast("扫描成功");
                    viewModel.tvMessage.setText(result.getContents());
                    if(result.getContents().startsWith("http://") || result.getContents().startsWith("https://")){
                        foundUrl = result.getContents();
                        viewModel.button.setVisibility(View.VISIBLE);
                    }
                }
            });

    private final OkHttpHelper.Callback okhttp_callback = new OkHttpHelper.Callback() {
        @Override
        public void result(String result, int code) {
            Log.d(TAG, result);
            try{
                Gson gson = new Gson();
                ResultBean resultBean = gson.fromJson(result, ResultBean.class);
                if(resultBean.code > 0 || resultBean.result == null) {
                    viewModel.tvMessage.setText("登录失败，请重新扫描");
                    viewModel.tvReason.setText(resultBean.message);
                    return;
                }
                Map<String, String> map = resultBean.result;
                if(TextUtils.isEmpty(map.get("token"))) {
                    viewModel.tvMessage.setText("登录失败，请重新扫描");
                    viewModel.tvReason.setText(resultBean.message);
                }else{
                    showToast("登录成功");
                    viewModel.tvMessage.setText("登录成功");
                }
            }catch (Exception e){
                viewModel.tvMessage.setText("服务器返回错误");
                viewModel.tvReason.setText(result);
                e.printStackTrace();
            }
        }
    };
}