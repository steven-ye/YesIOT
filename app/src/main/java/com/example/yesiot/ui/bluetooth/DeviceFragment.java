package com.example.yesiot.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.adapter.ButtonAdapter;
import com.example.yesiot.R;
import com.example.yesiot.adapter.SpinnerAdapter;
import com.example.yesiot.dialog.ListDialog;
import com.example.yesiot.helper.BlueButtonHelper;
import com.example.yesiot.helper.BlueDeviceHelper;
import com.example.yesiot.object.BlueDevice;
import com.example.yesiot.object.BlueButton;
import com.example.yesiot.service.BleGattClient;
import com.example.yesiot.dialog.LoadingDialog;
import com.example.yesiot.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeviceFragment extends AbsFragment {
    private final String TAG = "DeviceFragment";
    private int mDeviceId;
    private String mTitle;
    private String mAddress;
    private ViewModel viewModel;
    private LoadingDialog loadingDialog;
    private boolean mLocked = true;

    //private static final String[] SERVICE_UUID_LIST = new String[] { "0000FFE0-0000-1000-8000-00805F9B34FB", "0000FFF0-0000-1000-8000-00805F9B34FB" };
    private static String Service_UUID = "0000FFF0-0000-1000-8000-00805F9B34FB";
    //private static final String CHAR_NOTIFY_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
    //private static final String CHAR_WRITE_UUID = "0000FFE2-0000-1000-8000-00805F9B34FB";


    private BleGattClient gattClient;
    private BluetoothGattService mGattService;

    List<BlueButton> buttonList = new ArrayList<>();
    ButtonAdapter mAdapter;
    PopupWindow mPopupWindow;
    List<String> dataList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    private final List<String> charList = new ArrayList<>();
    SpinnerAdapter spinnerAdapter;
    BlueButtonDialog mButtonDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("连接中，请稍等...");
        mButtonDialog = new BlueButtonDialog();
        mButtonDialog.setCancelable(false);
        mButtonDialog.setOnSavedListener((blueButton ->  getButtonList()));
    }

    @SuppressLint("NonConstantResourceId")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth_device, container, false);
        viewModel = new ViewModel(root);
        initSpinner(viewModel.spinner);
        viewModel.btnClear.setOnClickListener(view -> viewModel.textView.setText(""));
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
            gattClient.send(data);
        });
        viewModel.btnCloseDebug.setOnClickListener(v2 -> viewModel.debugView.setVisibility(View.GONE));

        viewModel.fab.setOnClickListener( view -> {
            //showToast("Add new button");
            BlueButton blueButton = new BlueButton(Service_UUID);
            blueButton.device_id = mDeviceId;
            showButtonDialog(blueButton);
        });

        if(mLocked)
            viewModel.fab.hide();
        else
            viewModel.fab.show();

        mAdapter = new ButtonAdapter(mContext, buttonList);
        viewModel.gridView.setAdapter(mAdapter);
        //viewModel.gridView.setNumColumns(3);
        viewModel.gridView.setOnItemClickListener((adapterView, view, position, l) -> {
            BlueButton button = buttonList.get(position);

            if(!mLocked){
                button.service_uuid = Service_UUID;
                button.device_id = mDeviceId;
                /*
                Bundle args = new Bundle();
                args.putString("title", "编辑按钮");
                args.putSerializable("button", button);
                args.putSerializable("charList", (Serializable)charList);
                navigate(R.id.nav_bluetooth_button, args);
                 */

                //showButtonDialog(button);
                ListDialog listDialog = getListDialog();
                listDialog.setOnClickListener((v, pos) -> {
                    switch (pos) {
                        case 0:
                            showButtonDialog(button);
                            break;
                        case 1:
                            confirm("确定删除此按钮？", v1 -> {
                                if(BlueButtonHelper.remove(button.id))
                                {
                                    showToast("删除成功");
                                    getButtonList();
                                }
                            });
                    }
                });
                listDialog.show(getParentFragmentManager(), "ListDialog");
                return;
            }

            //showToast("Clicked " + button.name);
            addLog("Clicked " + button.name + ", uuid = " + button.uuid + "!");
            BluetoothGattCharacteristic gattChar = mGattService.getCharacteristic(UUID.fromString(button.uuid));
            if(gattChar != null)
            {
                gattClient.setSendCharacteristic(gattChar);
                String message = button.name;
                if(!TextUtils.isEmpty(button.payload))
                    message += "," + button.payload;
                gattClient.send(message);
            }
        });

        mPopupWindow = getPopupWindow();

        Bundle args = getArguments();
        assert args != null;
        mAddress = args.getString("address","");
        mTitle = args.getString("name", mAddress);
        mDeviceId = args.getInt("id", 0);

        BlueDevice blueDevice = BlueDeviceHelper.get(mAddress);
        if(blueDevice != null) {
            Service_UUID = blueDevice.getServiceUuid();
        }

        gattClient = new BleGattClient(getContext(), gattListener);
        gattClient.connect(mAddress);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getButtonList();
        //connect();
    }

    private void getButtonList()
    {
        buttonList.clear();
        List<BlueButton> buttons = BlueButtonHelper.getList(Service_UUID);
        buttonList.addAll(buttons);
        mAdapter.notifyDataSetChanged();
    }

    private void showButtonDialog(BlueButton button)
    {
        mButtonDialog.setCharList(charList);
        mButtonDialog.setBlueButton(button);
        mButtonDialog.show(getParentFragmentManager());
    }

    private PopupWindow getPopupWindow()
    {
        View contentView = getLayoutInflater().inflate(R.layout.listview_container, null, false);
        ListView mListView = contentView.findViewById(R.id.popup_listview);
        TextView emptyText = contentView.findViewById(R.id.list_empty);
        mListView.setEmptyView(emptyText);
        arrayAdapter = new ArrayAdapter<>(mContext, R.layout.listview_item_uuid, R.id.list_option_title, dataList);
        mListView.setAdapter(arrayAdapter);

        PopupWindow mPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(0xcc000000));    //要为popWindow设置一个背景才有效

        mListView.setOnItemClickListener((adapterView, view, i, l) -> {
            showToast("clicked: " + dataList.get(i));
            mPopupWindow.dismiss();
            setService(dataList.get(i));
        });

        return mPopupWindow;
    }

    private @NonNull ListDialog getListDialog() {
        int[] icons = new int[] {
                R.drawable.ic_baseline_edit_24,
                R.drawable.ic_baseline_delete_outline_24,
                R.drawable.ic_baseline_close_24
        };
        int[] colors = new int[]{ Color.BLUE, Color.RED, Color.GRAY };
        String[] titles = new String[] { "编辑", "删除", "取消"};
        List<Map<String,Object>> actions = new ArrayList<>();
        for(int i = 0; i< titles.length; i++){
            Map<String,Object> action = new HashMap<>();
            action.put("icon", icons[i]);
            action.put("title", titles[i]);
            action.put("color",colors[i]);
            actions.add(action);
        }

        return new ListDialog(actions);
    }

    private void connect()
    {
        loadingDialog.show();
        gattClient.connect(mAddress);
    }

    private void initSpinner(Spinner spinner) {
        //声明一个下拉列表的数组适配器
        spinnerAdapter = new SpinnerAdapter(requireContext(), charList);
        //设置数组适配器的布局样式
        //spinnerAdapter.setDropDownViewResource(R.layout.item_gatt_dropdown);
        //从布局文件中获取名叫sp_dialog的下拉框
        //Spinner sp = findViewById(R.id.spinner);
        //设置下拉框的标题，不设置就没有难看的标题了
        spinner.setPrompt("请选择发送特征");
        //设置下拉框的数组适配器
        spinner.setAdapter(spinnerAdapter);
        //设置下拉框默认的显示第一项
        spinner.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                UUID charUuid = UUID.fromString(charList.get(pos));
                BluetoothGattCharacteristic characteristic = mGattService.getCharacteristic(charUuid);
                //showToast("你选择了 " + characteristic.getUuid());
                gattClient.setSendCharacteristic(characteristic);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void updateCharList(){
        if(mGattService == null) return;
        charList.clear();
        for(BluetoothGattCharacteristic characteristic: mGattService.getCharacteristics())
        {
            List<String> props = gattClient.getProperties(characteristic);
            if(props.contains("write") || props.contains("write_no_response"))
                charList.add(characteristic.getUuid().toString());
        }

        spinnerAdapter.notifyDataSetChanged();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.ble_device, menu);
        MenuItem item = menu.findItem(R.id.menu_item_lock);
        int icon = mLocked ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
        item.setIcon(icon);
        // 显示图标
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        viewModel.getLinked().observe(getViewLifecycleOwner(), linked -> {
            //Utils.showToast("连接状态: " + s);
            MenuItem menuItem = menu.findItem(R.id.menu_item_scan);
            if(menuItem != null) {
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
            }
        });

        super.onOptionsMenuCreated(menu, inflater);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Bundle args = new Bundle();
        switch(itemId) {
            case R.id.menu_item_scan:
                if(viewModel.getLinkedValue())
                {
                    showToast("已经连接");
                    gattClient.disconnect();
                }else{
                    showToast("开始连接");
                    connect();
                }
                break;
            case R.id.menu_item_lock:
                //showToast("lock/unlock");
                mLocked = ! mLocked;
                int menuIcon = mLocked ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
                item.setIcon(menuIcon);
                if(mLocked) {
                    viewModel.fab.hide();
                }else{
                    viewModel.fab.show();
                }
                break;
            case R.id.menu_item_debug:
                //showToast("debug");
                if(viewModel.debugView.getVisibility() == View.VISIBLE)
                    viewModel.debugView.setVisibility(View.GONE);
                else
                    viewModel.debugView.setVisibility(View.VISIBLE);
                break;
            case R.id.menu_item_edit:
                args.putString("title", mTitle);
                args.putString("name", mTitle);
                args.putString("address", mAddress);
                args.putInt("id", mDeviceId);
                navigate(R.id.nav_bluetooth_register, args);
                break;
            case R.id.menu_item_popup:
                //mPopupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0);
                mPopupWindow.showAsDropDown(requireView(), 0, 10);
                break;
            default:
                return super.onOptionsMenuSelected(item);
        }

        return true;
    }

    private void onMessage(String message)
    {
        Log.d(TAG, "Received: " + message);
        addLog("Received： " + message, 2);
        for(BlueButton button: buttonList)
        {
            if(message.startsWith(button.name+','))
            {
                String payload = message.substring(button.name.length()+1);
                //addLog("button="+ button.name + ", payload=" + payload);
                button.value = payload.trim();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gattClient.disconnect();
    }

    private void addLog(String message, int type) {
        String[] types = new String[] { "提示", "发送", "收到" };
        viewModel.textView.append("["+types[type] + "] " + message + "\n");
        viewModel.scrollView.post(() -> viewModel.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void addLog(String message) {
        addLog(message, 0);
    }

    private void setService(String Service_UUID)
    {
        mGattService = gattClient.getService(Service_UUID);
        if (mGattService != null) {
            addLog("==== 选定的Service UUID: " + mGattService.getUuid());
            for (BluetoothGattCharacteristic gattCharacteristic : mGattService.getCharacteristics()) {
                addLog("Characteristic UUID: " + gattCharacteristic.getUuid());
                List<String> props = gattClient.getProperties(gattCharacteristic);
                addLog("特征权限： " + TextUtils.join(", ", props));
                gattClient.prepareNotify(gattCharacteristic);
            }

            //update
            mAdapter.notifyDataSetChanged();
            //spinnerAdapter.notifyDataSetChanged();
            updateCharList();
        }
    }

    BleGattClient.GattListener gattListener = new BleGattClient.GattListener() {
        @Override
        public void onConnectSuccess(BluetoothGatt gatt) {
            Log.d(TAG, "GATT连接成功");
            addLog("GATT连接成功");
            viewModel.setLinked(true);
            loadingDialog.dismiss();
        }

        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            showToast("GATT连接断开");
            addLog("GATT连接断开");
            viewModel.setLinked(false);
            loadingDialog.dismiss();
        }

        @Override
        public void onConnectFail(BluetoothGatt gatt) {
            showToast("GATT连接失败");
            addLog("GATT连接失败");
            viewModel.setLinked(false);
            loadingDialog.dismiss();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt) {
            //gattClient.send("Hello".getBytes(StandardCharsets.UTF_8));
            dataList.clear();
            List<BluetoothGattService> serviceList = gatt.getServices();
            for(BluetoothGattService service: serviceList){
                String serviceUuid = service.getUuid().toString();
                addLog("服务UUID: " + serviceUuid);
                //addLog("Service Type = " + service.getType());
                //Map<String, String> map = new HashMap<>();
                //map.put("title", serviceUuid);
                dataList.add(serviceUuid);
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic: characteristics){
                    List<String> props = gattClient.getProperties(characteristic);
                    String charUUID =  characteristic.getUuid().toString();
                    addLog("特征UUID： " + charUUID);
                    addLog("特征权限： " + TextUtils.join(", ", props));
                    byte[] charValue = characteristic.getValue();
                    String charStr = charValue == null ? "null" : new String(charValue);
                    addLog("特征值： " + charStr);

                    for(BluetoothGattDescriptor descriptor: characteristic.getDescriptors()){
                        byte[] descValue = descriptor.getValue();
                        String descStr = descValue == null ? "null" : new String(descValue);
                        addLog("描述UUID: " + descriptor.getUuid());
                        addLog("描述值: " + descStr);
                    }

                    //gattClient.prepareNotify(characteristic);
                }
            }
            arrayAdapter.notifyDataSetChanged();

            //根据指定的服务uuid获取指定的服务
            setService(Service_UUID);

            /*
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
                assert message != null;
                onMessage(message.trim());
            }
        }

        @Override
        public void onSendSuccess(byte[] data) {
            showToast("数据发送成功");
        }
        @Override
        public void onNotifySuccess(BluetoothGatt gatt){
            addLog("通知描述写入成功，可以进行通信");
            showToast("通知描述写入成功，可以进行通信");
        }
    };
}
