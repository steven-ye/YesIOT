package com.example.yesiot.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
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
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.dialog.LoadingDialog;
import com.example.yesiot.helper.BluetoothHelper;
import com.example.yesiot.service.BleGattClient;
import com.example.yesiot.service.BlueSocketClient;
import com.example.yesiot.util.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DebugFragment extends AbsFragment {
    private final String TAG = "DeviceFragment";
    private DebugModel viewModel;
    private LoadingDialog loadingDailog;

    private BluetoothDevice bluetoothDevice;
    private final BlueSocketClient socketClient = new BlueSocketClient();
    private BleGattClient gattClient;
    private final List<BluetoothGattCharacteristic> charList = new ArrayList<>();

    //private static final String Service_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB";
    //private static final String CHAR_NOTIFY_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
    //private static final String CHAR_WRITE_UUID = "0000FFE2-0000-1000-8000-00805F9B34FB";

    SpinnerAdapter spinnerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDailog = new LoadingDialog(requireContext());
        loadingDailog.setMessage("连接中，请稍等...");
    }

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth_debug, container, false);
        viewModel = new DebugModel(root);
        initSpinner();
        viewModel.setType(1);
        viewModel.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton radioButton = radioGroup.findViewById(i);
            Log.d(TAG, "RadioGroup checked " + radioButton.getText());
            switch(i) {
                case R.id.type_radio_1:
                    viewModel.setType(1);
                    break;
                case R.id.type_radio_2:
                    viewModel.setType(2);
                    break;
            }
            //断开连接
            socketClient.disconnect();
            gattClient.disconnect();
        });
        viewModel.btnClear.setOnClickListener(view -> viewModel.textView.setText(""));
        viewModel.btnConnect.setOnClickListener(view -> {
            if(viewModel.getLinkedValue()) {
                //disconnectGatt();
                socketClient.disconnect();
                gattClient.disconnect();
                return;
            }
            loadingDailog.show();
            switch (viewModel.getTypeValue()){
                case 1:
                    gattClient.connect(bluetoothDevice);
                    break;
                case 2:
                    socketClient.connect(bluetoothDevice);
                    break;
            }
        });
        viewModel.btnSend.setOnClickListener(view -> {
            if(!viewModel.getLinkedValue()) {
                showToast("请先连接蓝牙设备");
                return;
            }
            String message = viewModel.editText.getText().toString();
            if(TextUtils.isEmpty(message)) {
                alert("请输入内容");
                return;
            }
            byte[] data;
            if(viewModel.checkboxHex.isChecked()){
                try {
                    data = Utils.hexToBytes(message);
                    message = Utils.bytes2PrintHex(data);
                }catch (NumberFormatException e) {
                    showToast("请不要输入0-9和A-F之外的字符");
                    addLog("数据格式错误");
                    return;
                }
            }else{
                data = message.getBytes();
            }
            addLog(message, 1);
            switch (viewModel.getTypeValue()){
                case 1:
                    gattClient.send(data);
                    break;
                case 2:
                    socketClient.send(data);
                    break;
            }
        });

        Bundle args = getArguments();
        assert args != null;
        String address = args.getString("address","");
        //String name = args.getString("name", address);

        //BluetoothHelper bluetoothHelper = new BluetoothHelper(getActivity());
        bluetoothDevice = BluetoothHelper.getDevice(address);
        //connectGatt();
        socketClient.setListener(socketListener);
        //bluetoothHelper.setGattListener(gattListener);

        gattClient = new BleGattClient(getContext(), gattListener);

        return root;
    }

    private void initSpinner() {
        //声明一个下拉列表的数组适配器
        spinnerAdapter = new SpinnerAdapter(requireContext(), charList);
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
                BluetoothGattCharacteristic characteristic = charList.get(pos);
                //showToast("你选择了 " + characteristic.getUuid());
                gattClient.setSendCharacteristic(characteristic);
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
            int menuIcon = linked ? R.drawable.ic_baseline_bluetooth_audio_24 : R.drawable.ic_baseline_bluetooth_disabled_24;
            menuItem.setIcon(menuIcon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //int menuResId = linked ? R.color.selector_active : R.color.material_on_primary_disabled;
                //ColorStateList tint = getContext().getColorStateList(menuResId);
                int menuColor = linked ? Color.GREEN : Color.LTGRAY;
                ColorStateList tint = ColorStateList.valueOf(menuColor);
                menuItem.setIconTintList(tint);
            }
            int buttenBgSource = linked ? R.drawable.selector_button_blue : R.drawable.selector_button_disable;
            viewModel.btnSend.setBackgroundResource(buttenBgSource);
            if(linked) {
                viewModel.btnConnect.setText("断开");
            }else{
                viewModel.btnConnect.setText("连接");
            }
        });
        super.onOptionsMenuCreated(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_scan:
                //socketClient.connect(bluetoothDevice);
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
        socketClient.disconnect();
    }

    private void addLog(String message, int type) {
        String[] types = new String[] { "提示", "发送", "收到" };
        viewModel.textView.append("["+types[type] + "] " + message + "\n");
        viewModel.scrollView.post(() -> viewModel.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void addLog(String message) {
        addLog(message, 0);
    }


    BleGattClient.GattListener gattListener = new BleGattClient.GattListener() {
        @Override
        public void onConnectSuccess(BluetoothGatt gatt) {
            Log.d(TAG, "GATT连接成功");
            addLog("GATT连接成功");
            viewModel.setLinked(true);
            loadingDailog.dismiss();
        }

        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            showToast("GATT连接断开");
            addLog("GATT连接断开");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onConnectFail(BluetoothGatt gatt) {
            showToast("GATT连接失败");
            addLog("GATT连接失败");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt) {
            //gattClient.send("Hello".getBytes(StandardCharsets.UTF_8));
            for(BluetoothGattService service: gatt.getServices()){
                addLog("服务UUID: " + service.getUuid());
                //addLog("Service Type = " + service.getType());
                charList.clear();
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic: characteristics){
                    List<String> propList = gattClient.getProperties(characteristic);
                    addLog("特征UUID： " + characteristic.getUuid());
                    addLog("特征权限： " + TextUtils.join(", ",propList));
                    byte[] charValue = characteristic.getValue();
                    String charStr = charValue == null ? "null" : new String(charValue);
                    addLog("特征值： " + charStr);
                    List<String> props = gattClient.getProperties(characteristic);
                    if(props.contains("write") || props.contains("write_no_response")) {
                        charList.add(characteristic);
                    }

                    for(BluetoothGattDescriptor descriptor: characteristic.getDescriptors()){
                        byte[] descValue = descriptor.getValue();
                        String descStr = descValue == null ? "null" : new String(descValue);
                        addLog("描述UUID: " + descriptor.getUuid());
                        addLog("描述值: " + descStr);
                    }
                    /*
                    if (gatt.setCharacteristicNotification(characteristic, true)) {
                        Log.d(TAG, "onServicesDiscovered--设置通知成功=--" + characteristic.getUuid());
                        //获取特征值其对应的通知Descriptor
                        BluetoothGattDescriptor notifyDescriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if(notifyDescriptor != null){
                            Log.d(TAG, "onServicesDiscovered--写入通知描述=--" + notifyDescriptor.getUuid());
                            //写入你需要传递给外设的特征的描述值（即传递给外设的信息）
                            notifyDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            //通过GATT实体类将，特征值写入到外设中。成功则在 onDescriptorWrite 回调
                            gatt.writeDescriptor(notifyDescriptor);
                        }
                    }
                    */
                }
                //update
                spinnerAdapter.notifyDataSetChanged();
            }

            /*
            //根据指定的服务uuid获取指定的服务
            BluetoothGattService gattService = gatt.getService(UUID.fromString(Service_UUID));
            if(gattService == null) return;
            //根据指定特征值uuid获取指定的特征值A
            BluetoothGattCharacteristic mGattCharacteristicA = gattService.getCharacteristic(UUID.fromString(CHAR_NOTIFY_UUID));
            if(mGattCharacteristicA == null) return;
            //设置特征A通知,即设备的值有变化时会通知该特征A，即回调方法onCharacteristicChanged会有该通知
            gatt.setCharacteristicNotification(mGattCharacteristicA , true);

            //获取特征值其对应的通知Descriptor
            BluetoothGattDescriptor descriptor = mGattCharacteristicA.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            //if(descriptor == null) return;
            //写入你需要传递给外设的特征的描述值（即传递给外设的信息）
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            //通过GATT实体类将，特征值写入到外设中。成功则在 onDescriptorWrite 回调
            gatt.writeDescriptor(descriptor);
             */

        }

        @Override
        public void onReceiveData(byte[] data) {
            viewModel.setLinked(true);
            if(data != null){
                String message = viewModel.checkboxHex.isChecked()?Utils.bytes2PrintHex(data):new String(data);
                Log.d(TAG, "Received: " + message);
                addLog(message, 2);
            }
        }

        @Override
        public void onSendSuccess(byte[] data) {
            showToast("数据发送成功");
        }
        @Override
        public void onNotifySuccess(BluetoothGatt gatt){
            addLog("通知描述写入成功，可以进行通信");
        }
    };

    BlueSocketClient.Listener socketListener = new BlueSocketClient.Listener() {
        @Override
        public void onConnectSuccess() {
            Log.d(TAG,"连接成功");
            showToast("连接成功");
            addLog("SPP连接成功");
            viewModel.setLinked(true);
            loadingDailog.dismiss();
        }

        @Override
        public void onConnectFail() {
            Log.d(TAG,"连接失败");
            addLog("连接失败");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onError() {
            Log.d(TAG,"连接断开");
            addLog("连接断开");
            viewModel.setLinked(false);
            loadingDailog.dismiss();
        }

        @Override
        public void onDataReceive(byte[] data) {
            String msg = new String(data, StandardCharsets.UTF_8);
            Log.d(TAG, "Received: " + msg);
            addLog("Received： " + msg, 2);
        }
    };

    class SpinnerAdapter extends BaseAdapter {
        Context mContext;
        List<BluetoothGattCharacteristic> mList;
        public SpinnerAdapter(@NonNull Context context, @NonNull List<BluetoothGattCharacteristic> objects) {
            mContext = context;
            mList = objects;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public BluetoothGattCharacteristic getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("InflateParams")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_gatt_spinner, null);
            BluetoothGattCharacteristic characteristic = getItem(position);
            TextView textView = (TextView) convertView;
            textView.setText(characteristic.getUuid().toString());
            return convertView;
        }
    }
}
