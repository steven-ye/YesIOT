package com.example.yesiot.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.adapter.BleDeviceAdapter;
import com.example.yesiot.R;
import com.example.yesiot.dialog.LoadingDialog;
import com.example.yesiot.helper.BlueDeviceHelper;
import com.example.yesiot.helper.BluetoothHelper;
import com.example.yesiot.dialog.ListDialog;
import com.example.yesiot.object.BlueDevice;
import com.google.android.material.tabs.TabLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListFragment extends AbsFragment {
    private final List<BlueDevice> regDevices = new ArrayList<>();
    private final List<BlueDevice> bondDevices = new ArrayList<>();
    private final List<BlueDevice> scanDevices = new ArrayList<>();
    private BleDeviceAdapter regAdapter;
    private BleDeviceAdapter bondedAdapter;
    private BleDeviceAdapter scanAdapter;
    private LoadingDialog loadingDialog;
    private ViewModel viewModel;

    private static class ViewModel {
        public TabLayout tabLayout;
        public RefreshLayout refreshLayout;

        public ViewModel(View root) {
            tabLayout = root.findViewById(R.id.tab_layout);
            refreshLayout = root.findViewById(R.id.refresh_layout);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothHelper.init(requireActivity());
    }

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        viewModel = new ViewModel(root);
        //bluetoothHelper = new BluetoothHelper(getActivity());
        BluetoothHelper.setBondCallback(device -> {
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDED:
                    showToast("配对成功");
                    BlueDevice blueDevice = new BlueDevice(device);
                    bondDevices.add(blueDevice);
                    scanDevices.remove(blueDevice);
                    bondedAdapter.notifyDataSetChanged();
                    scanAdapter.notifyDataSetChanged();
                    break;
                case BluetoothDevice.BOND_NONE:
                    showToast("配对失败");
                    break;
                case BluetoothDevice.BOND_BONDING:
                    showToast("正在配对 ...");
                    break;
            }
        });
        bondDevices.clear();
        for (BluetoothDevice device : BluetoothHelper.getBondedDevices(getActivity())) {
            BlueDevice blueDevice = new BlueDevice(device);
            bondDevices.add(blueDevice);
        }

        //tabLayout.getSelectedTabPosition();
        viewModel.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            private final int[] tabs = {R.id.tabview_registered, R.id.refresh_layout, R.id.tabview_bonded};

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = root.findViewById(tabs[tab.getPosition()]);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = root.findViewById(tabs[tab.getPosition()]);
                view.setVisibility(View.GONE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewModel.refreshLayout.setOnRefreshListener(refreshlayout -> {
            //refreshlayout.finishRefresh(1000/*,false*/);//传入false表示刷新失败
            startScan();
        });

        loadingDialog = new LoadingDialog(requireContext());
        loadingDialog.setMessage("努力扫描中，请稍等 ...");
        loadingDialog.setOnDismissListener(dialogInterface -> {
            stopScan();
            viewModel.refreshLayout.finishRefresh();
        });

        init_listview(root);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        regDevices.clear();
        List<BlueDevice> blueDevices = BlueDeviceHelper.getList();
        regDevices.addAll(blueDevices);
        /*
        for (BlueDevice blueDevice : blueDevices) {
            BluetoothDevice device = bluetoothHelper.getDevice(blueDevice.getMac());
            BlueDevice bleDevice = new BlueDevice(device);
            regDevices.add(blueDevice);
        }
         */
        regAdapter.notifyDataSetChanged();
    }

    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth, menu);
        super.onOptionsMenuCreated(menu, inflater);
    }

    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_scan) {
            showToast("扫描蓝牙设备");
            startScan();
        }
        return super.onOptionsMenuSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BluetoothHelper.onDestroy(requireActivity());
    }

    private void init_listview(View root) {
        //registered devices
        ListView listView = root.findViewById(R.id.bluetooth_listview_registered);
        listView.setEmptyView(root.findViewById(R.id.list_registered_empty));
        regAdapter = new BleDeviceAdapter(getContext(), regDevices);
        listView.setAdapter(regAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            regAdapter.setSelectedPosition(position);
            BlueDevice blueDevice = regDevices.get(position);
            gotoNavPage(R.id.nav_bluetooth_device, blueDevice);
        });
        regAdapter.setItemActionLister((parent, view, position) -> {
            showDialog(regDevices, position, 0);
        });
        //scan devices
        ListView listView1 = root.findViewById(R.id.bluetooth_listview_scan);
        listView1.setEmptyView(root.findViewById(R.id.list_scan_empty));
        scanAdapter = new BleDeviceAdapter(getContext(), scanDevices);
        listView1.setAdapter(scanAdapter);
        listView1.setOnItemClickListener((parent, view, position, id) -> {
            scanAdapter.setSelectedPosition(position);
            BlueDevice blueDevice = scanDevices.get(position);
            gotoNavPage(R.id.nav_bluetooth_register, blueDevice);
        });
        scanAdapter.setItemActionLister((parent, view, position) -> {
            //BluetoothDevice bluetoothDevice = bondDevices.get(position);
            showDialog(scanDevices, position, 1);
        });
        //bonded devices
        ListView listView2 = root.findViewById(R.id.bluetooth_listview_bonded);
        listView2.setEmptyView(root.findViewById(R.id.list_bonded_empty));
        bondedAdapter = new BleDeviceAdapter(getContext(), bondDevices);
        listView2.setAdapter(bondedAdapter);
        listView2.setOnItemClickListener((parent, view, position, id) -> {
            bondedAdapter.setSelectedPosition(position);
            BlueDevice blueDevice = bondDevices.get(position);
            gotoNavPage(R.id.nav_bluetooth_register, blueDevice);
        });
        bondedAdapter.setItemActionLister((parent, view, position) -> {
            //BluetoothDevice bluetoothDevice = bondDevices.get(position);
            showDialog(bondDevices, position, 2);
        });
    }

    private void startScan() {
        scanDevices.clear();
        scanAdapter.notifyDataSetChanged();
        final Handler handler = new Handler();
        handler.postDelayed(this::stopScan, 8000);
        Log.e("提示", "---------->start scanning");
        loadingDialog.show();
        BluetoothHelper.startDiscovery(discoveryCallback);
        BluetoothHelper.startLeScan(scanCallback);
    }

    private void stopScan() {
        Log.e("提示", "---------->stop scanning");
        BluetoothHelper.cancelDiscovery();
        BluetoothHelper.stopLeScan(scanCallback);
        loadingDialog.dismiss();
    }


    @SuppressLint("MissingPermission")
    private void handleFoundDevice(BluetoothDevice device, int rssi) {
        Log.d(TAG, "Found " + device.getName() + "(" + device.getAddress() + "), RSSI: " + rssi);
        if(device.getBondState() == BluetoothDevice.BOND_BONDED){
            if(bondedAdapter.getPosition(device.getAddress()) > -1){
                bondedAdapter.updateDevice(device, rssi);
            }else{
                bondedAdapter.addDevice(device, rssi);
            }
        }
        if(scanAdapter.getPosition(device.getAddress()) > -1){
            scanAdapter.updateDevice(device, rssi);
        }else{
            scanAdapter.addDevice(device, rssi);
        }
        regAdapter.updateDevice(device, rssi);
    }

    @SuppressLint("MissingPermission")
    private void gotoNavPage(int pageResId, BlueDevice device) {
        if(device == null) return;
        //跳转页面
        Bundle args = new Bundle();
        String name = device.getName();
        String address = device.getAddress();
        if(TextUtils.isEmpty(name)) name = address;
        args.putString("name", name);
        args.putString("address", address);
        args.putInt("id", device.getId());
        navigate(pageResId, args);
    }

    private void showDialog(List<BlueDevice> deviceList, int position, int which){
        BlueDevice bleDevice = deviceList.get(position);
        String[] titles = new String[]{"删除","取消"};
        ListDialog dialog = getListDialog(titles);
        //dialog.setCancelable(false);
        dialog.show(getParentFragmentManager(), "ListDialog");
        dialog.setOnClickListener((v, pos) -> {
            String name = bleDevice.getName();
            if (name == null) name = bleDevice.getAddress();
            String title = titles[pos];
            if (title.equals("删除")) {
                switch (which){
                    case 0:
                        final String finalName = name;
                        confirm("确定要删除此设备？", v1 -> {
                            if(BlueDeviceHelper.remove(bleDevice.getAddress()))
                            {
                                deviceList.remove(position);
                                regAdapter.notifyDataSetChanged();
                                showToast("成功删除蓝牙设备 " + finalName);
                            } else {
                                showToast("删除失败请重试" + finalName);
                            }
                        });
                        break;
                    case 1:
                        deviceList.remove(position);
                        scanAdapter.notifyDataSetChanged();
                        showToast("成功删除蓝牙设备 " + name);
                        break;
                    case 2:
                        deviceList.remove(position);
                        bondedAdapter.notifyDataSetChanged();
                        showToast("成功删除蓝牙设备 " + name);
                        break;
                }
            } else {
                dialog.dismiss();
            }
        });
    }

    private @NonNull ListDialog getListDialog(String[] titles) {
        int[] icons = new int[]{R.drawable.ic_baseline_done_24,R.drawable.ic_baseline_edit_24,
                R.drawable.ic_baseline_delete_outline_24,R.drawable.ic_baseline_close_24};
        int[] colors = new int[]{Color.GREEN,Color.BLUE,Color.RED,Color.GRAY};
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

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();

            handleFoundDevice(device, rssi);
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //throw new RuntimeException("Scan error");
            showToast("扫描失败");
        }
    };

    private final BluetoothHelper.DiscoveryCallback discoveryCallback = new BluetoothHelper.DiscoveryCallback() {
        @Override
        public void onScanStarted() {
            loadingDialog.show();
        }

        @Override
        public void onScanFinished() {
            Log.d(TAG, "Discovery is finished");
            loadingDialog.dismiss();
        }

        @Override
        public void onFoundDevice(BluetoothDevice device, int rssi) {
            handleFoundDevice(device, rssi);
        }
    };
}