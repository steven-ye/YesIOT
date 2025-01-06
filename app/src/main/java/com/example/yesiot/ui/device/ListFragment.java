package com.example.yesiot.ui.device;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.adapter.DeviceAdapter;
import com.example.yesiot.dialog.DeviceDialog;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.helper.ScanHelper;
import com.example.yesiot.object.Device;
import com.example.yesiot.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends AbsFragment {
    //final String TAG = "DevicesFragment";
    //private HomeViewModel homeViewModel;
    private DeviceAdapter adapter;
    private final List<Device> list = new ArrayList<>();
    private boolean locked = false;
    private Menu mMenu;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_devices, container, false);
        ListView listView = root.findViewById(R.id.devListView);
        listView.setEmptyView(root.findViewById(R.id.text_list_empty));
        adapter = new DeviceAdapter(getActivity(), list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Device device = list.get(position);
            Bundle args = new Bundle();
            args.putInt("id", device.getId());
            args.putString("title", device.getName());
            if(locked){
                Navigation.findNavController(requireView()).navigate(R.id.nav_control, args);
            }else{
                Navigation.findNavController(requireView()).navigate(R.id.nav_device, args);
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDeviceList();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setEditable(!locked);
    }

    private void getDeviceList(){
        int brokerId = SPUtil.getBrokerId();
        List<Device> devices = DeviceHelper.getList(brokerId);
        list.clear();
        list.addAll(devices);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.devices, menu);
        mMenu = menu;
        int menuIcon = locked ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
        MenuItem menuItem = menu.findItem(R.id.action_lock);
        menuItem.setIcon(menuIcon);
        super.onOptionsMenuCreated(menu, inflater);
    }

    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_lock){
            MenuItem menuAdd = mMenu.findItem(R.id.action_add);
            menuAdd.setVisible(locked);
            adapter.setEditable(locked);
            locked = !locked;
            int menuIcon = locked ? R.drawable.ic_baseline_lock_24 : R.drawable.ic_baseline_lock_open_24;
            item.setIcon(menuIcon);
            return true;
        }else if(item.getItemId()==R.id.action_add){
            DeviceDialog dialogFragment = getDeviceDialog();
            dialogFragment.show(getParentFragmentManager(),"DeviceDialog");
        }
        return super.onOptionsMenuSelected(item);
    }

    private @NonNull DeviceDialog getDeviceDialog() {
        DeviceDialog dialogFragment = new DeviceDialog();
        dialogFragment.setCancelable(false);
        dialogFragment.setOnClickListener(v -> {
            if(v.getId()==R.id.add_device){
                Navigation.findNavController(requireView()).navigate(R.id.nav_device);
            }else if(v.getId()==R.id.scan_device){
                ScanHelper searchHelper = ScanHelper.Builder(getContext());
                searchHelper.setCallback(device -> getDeviceList());
                searchHelper.start();
            }
        });
        return dialogFragment;
    }
}