package com.example.yesiot.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.adapter.SpinnerAdapter;
import com.example.yesiot.dialog.LoadingDialog;
import com.example.yesiot.helper.BlueDeviceHelper;
import com.example.yesiot.helper.BluetoothHelper;
import com.example.yesiot.object.BlueDevice;
import com.example.yesiot.service.BleGattClient;
import com.example.yesiot.service.BlueSocketClient;
import com.example.yesiot.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends AbsFragment {
    private final String TAG = "RegisterFragment";
    private RegisterModel viewModel;
    private LoadingDialog loadingDailog;

    private BleGattClient gattClient;
    private final List<String> serviceList = new ArrayList<>();

    SpinnerAdapter spinnerAdapter;
    BlueDevice mDevice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDailog = new LoadingDialog(requireContext());
        loadingDailog.setMessage("连接中，请稍等...");
    }

    @SuppressLint({"NonConstantResourceId", "MissingPermission"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth_register, container, false);
        viewModel = new RegisterModel(root);
        viewModel.radioGroup.setOnCheckedChangeListener((radioGroup, checkId) -> {
            switch (checkId)
            {
                case R.id.type_radio_0:
                    mDevice.setType(BluetoothDevice.DEVICE_TYPE_UNKNOWN);
                    break;
                case R.id.type_radio_1:
                    mDevice.setType(BluetoothDevice.DEVICE_TYPE_CLASSIC);
                    break;
                case R.id.type_radio_2:
                    mDevice.setType(BluetoothDevice.DEVICE_TYPE_LE);
                    break;
                case R.id.type_radio_3:
                    mDevice.setType(BluetoothDevice.DEVICE_TYPE_DUAL);
                    break;
            }
        });
        viewModel.btnCancel.setOnClickListener(clickListener);
        viewModel.btnSave.setOnClickListener(clickListener);

        Bundle args = getArguments();
        assert args != null;
        String address = args.getString("address","");
        String name = args.getString("name", address);

        viewModel.et_mac.setText(address);
        viewModel.et_name.setText(name);

        mDevice = BlueDeviceHelper.get(address);

        if(mDevice == null) {
            BluetoothDevice bluetoothDevice = BluetoothHelper.getDevice(address);
            mDevice = new BlueDevice(bluetoothDevice);
        }
        switch (mDevice.getType()){
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                viewModel.radioGroup.check(R.id.type_radio_0);
                break;
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                viewModel.radioGroup.check(R.id.type_radio_1);
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                viewModel.radioGroup.check(R.id.type_radio_2);
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                viewModel.radioGroup.check(R.id.type_radio_3);
                break;
        }

        initSpinner();

        gattClient = new BleGattClient(getContext(), gattListener);
        gattClient.connect(address);
        return root;
    }

    private void initSpinner() {
        //声明一个下拉列表的数组适配器
        spinnerAdapter = new SpinnerAdapter(requireContext(), serviceList);
        //设置数组适配器的布局样式
        //spinnerAdapter.setDropDownViewResource(R.layout.item_gatt_dropdown);
        //从布局文件中获取名叫sp_dialog的下拉框
        //Spinner sp = findViewById(R.id.spinner);
        //设置下拉框的标题，不设置就没有难看的标题了
        viewModel.spinner.setPrompt("请选择发送特征");
        //设置下拉框的数组适配器
        viewModel.spinner.setAdapter(spinnerAdapter);
        //设置下拉框默认的显示第一项
        viewModel.spinner.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        viewModel.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String uuid = serviceList.get(pos);
                //showToast("你选择了 " + uuid);
                viewModel.et_service_uuid.setText(uuid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth, menu);
        viewModel.getLinked().observe(getViewLifecycleOwner(), linked -> {
            //Utils.showToast("连接状态: " + s);
            MenuItem menuItem = menu.findItem(R.id.menu_item_scan);
            if(null == menuItem) return;
            int menuIcon = linked ? R.drawable.ic_baseline_bluetooth_audio_24 : R.drawable.ic_baseline_bluetooth_disabled_24;
            menuItem.setIcon(menuIcon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //int menuResId = linked ? R.color.selector_active : R.color.material_on_primary_disabled;
                //ColorStateList tint = getContext().getColorStateList(menuResId);
                int menuColor = linked ? Color.GREEN : Color.LTGRAY;
                ColorStateList tint = ColorStateList.valueOf(menuColor);
                menuItem.setIconTintList(tint);
            }
        });
        super.onOptionsMenuCreated(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_scan:
                if(viewModel.getLinkedValue()){
                    gattClient.disconnect();
                }else{
                    gattClient.connect();
                }
                break;
            case R.id.menu_item_option:
                //bluetoothHelper.connectGatt(bluetoothDevice);
                break;
        }
        return super.onOptionsMenuSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gattClient.disconnect();
    }

    BleGattClient.GattListener gattListener = new BleGattClient.GattListener() {
        @Override
        public void onConnectSuccess(BluetoothGatt gatt) {
            Log.d(TAG, "GATT连接成功");
            showToast("GATT连接成功");
            viewModel.setLinked(true);
            loadingDailog.dismiss();
        }

        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            showToast("GATT连接断开");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onConnectFail(BluetoothGatt gatt) {
            showToast("GATT连接失败");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt) {
            serviceList.clear();
            for(BluetoothGattService service: gatt.getServices()){
                //addLog("服务UUID: " + service.getUuid());
                //addLog("Service Type = " + service.getType());
                serviceList.add(service.getUuid().toString());
            }
            //update
            spinnerAdapter.notifyDataSetChanged();

            int selectPos = serviceList.indexOf(mDevice.getServiceUuid());
            if(selectPos == -1) selectPos = 0;
            viewModel.spinner.setSelection(selectPos);
        }

        @Override
        public void onReceiveData(byte[] data) {
            viewModel.setLinked(true);
            if(data != null){
                String message = viewModel.checkboxHex.isChecked()?Utils.bytes2PrintHex(data):new String(data);
                Log.d(TAG, "Received: " + message);
            }
        }

        @Override
        public void onSendSuccess(byte[] data) {
            showToast("数据发送成功");
        }
        @Override
        public void onNotifySuccess(BluetoothGatt gatt){
            showToast("通知描述写入成功，可以进行通信");
        }
    };

    BlueSocketClient.Listener socketListener = new BlueSocketClient.Listener() {
        @Override
        public void onConnectSuccess() {
            Log.d(TAG,"连接成功");
            showToast("连接成功");
            viewModel.setLinked(true);
            loadingDailog.dismiss();
        }

        @Override
        public void onConnectFail() {
            Log.d(TAG,"连接失败");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onError() {
            Log.d(TAG,"连接断开");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onDataReceive(byte[] data) {
            String msg = new String(data, StandardCharsets.UTF_8);
            Log.d(TAG, "Received: " + msg);
        }
    };

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.button_cancel)
            {
                Navigation.findNavController(requireView()).navigateUp();
            } else if (view.getId() == R.id.button_save) {
                //showToast("注册蓝牙设备");
                String mac = viewModel.et_mac.getText().toString().trim();
                String name = viewModel.et_name.getText().toString().trim();
                String serive_uuid = viewModel.et_service_uuid.getText().toString().trim();

                if(TextUtils.isEmpty(mac)){
                    alert("Mac地址不能为空");
                    return;
                }
                if(mac.length() != 17){
                    alert("无效Mac地址");
                    return;
                }
                String[] arr = mac.split(":");
                if(arr.length != 6){
                    alert("无效Mac地址");
                    return;
                }

                //BlueDevice mDevice = new BlueDevice();
                mDevice.setMac(mac);
                mDevice.setName(name);
                mDevice.setServiceUuid(serive_uuid);

                if(BlueDeviceHelper.save(mDevice)){
                    showToast("保存成功");
                    Navigation.findNavController(requireView()).navigateUp();
                } else {
                    alert("保存失败, 请重试");
                }
            }
        }
    };
}
