package com.example.yesiot.ui.broker;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.helper.BrokerHelper;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.util.SPUtil;
import com.example.yesiot.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BrokerFragment extends AbsFragment {
    final String TAG = "BrokerFragment";
    private BrokerViewModel viewModel;
    Map<String,String> map = new HashMap<>();
    int id = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_broker, container, false);

        String[] protocols = requireActivity().getResources().getStringArray(R.array.protocol_values);

        viewModel = new BrokerViewModel(root);
        viewModel.setProtocols(protocols);

        viewModel.ctv_export.setOnClickListener(v->{
            CheckedTextView view = (CheckedTextView)v;
            if(view.isChecked()){
                viewModel.row_ctv_export.setVisibility(View.GONE);
                view.setChecked(false);
            }else{
                viewModel.row_ctv_export.setVisibility(View.VISIBLE);
                view.setChecked(true);
            }
        });
        viewModel.ctv_export.callOnClick();

        Bundle args = getArguments();
        id = args == null ? 0 : args.getInt("id");
        Log.w(TAG, "ID is "+id);
        if(id > 0){
            setTitle("修改连接信息");
            map = BrokerHelper.get(id);
            Log.w(TAG, "Broker name is "+map.get("name"));
        }else{
            setTitle("添加连接信息");
            map.put("host", "192.168.1.2");
            map.put("port", "1883");
            map.put("auto","");
            map.put("session","");
            map.put("clientId","");
        }
        map.put("id", id+"");
        showValue();
        return root;
    }

    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.edit, menu);
        super.onOptionsMenuCreated(menu, inflater);
    }
    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_lock){
            if(save()){
                int brokerId = SPUtil.getBrokerId();
                if(id == brokerId){
                    getMainActivity().startMqttService();
                }
                Navigation.findNavController(requireView()).navigateUp();
            }
        }else if(item.getItemId()==R.id.action_remove){
            if(DeviceHelper.hasDevice(id)){
                showToast("无法删除：该连接下有设备");
            }else if(BrokerHelper.has(id)){
                ConfirmDialog.show(getParentFragmentManager(), "确认要删除此连接？", v -> {
                    if(DeviceHelper.remove(id)) {
                        Navigation.findNavController(requireView()).navigateUp();
                        showToast("删除设备成功");
                    }
                });
            }else{
                showToast("连接信息不存在");
            }
        }
        return super.onOptionsMenuSelected(item);
    }

    public void showValue(){
        //map = Utils.getBroker(getContext());
        viewModel.et_name.setText(map.get("name"));
        viewModel.et_host.setText(map.get("host"));
        viewModel.et_port.setText(map.get("port"));
        viewModel.et_path.setText(map.get("path"));
        viewModel.et_clientid.setText(map.get("clientId"));
        viewModel.et_username.setText(map.get("username"));
        viewModel.et_password.setText(map.get("password"));
        viewModel.et_alive.setText(map.get("alive"));
        viewModel.et_timeout.setText(map.get("timeout"));
        viewModel.et_topic.setText(map.get("topic"));
        viewModel.et_message.setText(map.get("message"));
        viewModel.cb_auto.setChecked(Objects.equals(map.get("auto"), "yes"));
        viewModel.cb_session.setChecked(Objects.equals(map.get("session"),"yes"));
        int position = viewModel.getProtocolPosition(map.get("protocol"));
        viewModel.spinner.setSelection(position,true);
    }

    public boolean save(){
        map.put("name",viewModel.et_name.getText().toString());
        map.put("host",viewModel.et_host.getText().toString());
        map.put("port",viewModel.et_port.getText().toString());
        map.put("path",viewModel.et_path.getText().toString());
        map.put("username",viewModel.et_username.getText().toString());
        map.put("password",viewModel.et_password.getText().toString());
        map.put("clientId",viewModel.et_clientid.getText().toString());
        map.put("alive",viewModel.et_alive.getText().toString());
        map.put("timeout",viewModel.et_timeout.getText().toString());
        map.put("topic",viewModel.et_topic.getText().toString());
        map.put("message",viewModel.et_message.getText().toString());
        map.put("auto",viewModel.cb_auto.isChecked()?"yes":"no");
        map.put("session",viewModel.cb_session.isChecked()?"yes":"no");
        int pos = viewModel.spinner.getSelectedItemPosition();
        map.put("protocol", viewModel.getProtocol(pos));

        if(TextUtils.isEmpty(map.get("name"))){
            Utils.showToast(getActivity(),"连接名称不能为空");
            return false;
        }
        /*
        if(TextUtils.isEmpty(map.get("clientId"))){
            //Utils.showToast(getActivity(),"客户端ID不能为空");
            map.put("clientId", Utils.getIMEIDeviceId(getContext()));
        }
         */
        if(TextUtils.isEmpty(map.get("host"))){
            Utils.showToast(getActivity(),"主机地址不能为空");
            return false;
        }
        if(TextUtils.isEmpty(map.get("port"))){
            map.put("port","1883");
        }
        if(TextUtils.isEmpty(map.get("timeout"))){
            map.put("timeout","30");
        }
        if(TextUtils.isEmpty(map.get("alive"))){
            map.put("alive","60");
        }
        Log.w(TAG, map.toString());
        if(BrokerHelper.save(map)){
            Utils.showToast(getActivity(),"保存成功");
            return true;
        }else{
            Utils.showToast(getActivity(),"保存失败");
            return false;
        }
        //SPUtils.putMap(Constants.BROKER, map);
        //return true;
    }
}
