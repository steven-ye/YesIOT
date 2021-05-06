package com.example.yesiot.ui.broker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.object.Constants;
import com.example.yesiot.util.SPUtils;
import com.example.yesiot.util.Utils;

import java.util.Map;
import java.util.Objects;

public class BrokerFragment extends Fragment {
    final String TAG = "BrokerFragment";
    private BrokerViewModel viewModel;
    Map<String,String> settings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_broker, container, false);

        String[] protocols = getActivity().getResources().getStringArray(R.array.protocol_values);

        viewModel = new BrokerViewModel(root);
        viewModel.setProtocols(protocols);
        initValue();

        viewModel.ctv_lastwill.setOnClickListener(v->{
            CheckedTextView view = (CheckedTextView)v;
            if(view.isChecked()){
                viewModel.row_lastwill.setVisibility(View.GONE);
                view.setChecked(false);
            }else{
                viewModel.row_lastwill.setVisibility(View.VISIBLE);
                view.setChecked(true);
            }
        });
        viewModel.btn_okay.setOnClickListener(v->{
            if(saveValue()){
                //Utils.showToast(getActivity(),"保存成功");
                String[] items = new String[]{"保存成功"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setCancelable(false)
                        .setTitle("温馨提示")
                        .setItems(items,null)
                        .setPositiveButton("立即生效", (dialog, which) -> {
                            MainActivity activity = (MainActivity)getActivity();
                            assert activity != null;
                            activity.mqttConnect();
                            Navigation.findNavController(getView()).navigateUp();
                        }).setNegativeButton("重启生效", (dialog, which) -> Navigation.findNavController(getView()).navigateUp());

                builder.show();
            }
        });
        return root;
    }

    public void initValue(){
        settings = SPUtils.getInstance().getMap(Constants.BROKER);
        if(TextUtils.isEmpty(settings.get("name"))){
            settings.put("name","YesIOT_"+Utils.getRandomString(4));
        }
        if(TextUtils.isEmpty(settings.get("clientId"))){
            settings.put("clientId",Utils.getIMEIDeviceId(getActivity()));
        }
        if(TextUtils.isEmpty(settings.get("ip"))){
            settings.put("ip","192.168.1.2");
        }
        if(TextUtils.isEmpty(settings.get("port"))){
            settings.put("port","1883");
        }
        viewModel.et_name.setText(settings.get("name"));
        viewModel.et_ip.setText(settings.get("ip"));
        viewModel.et_port.setText(settings.get("port"));
        viewModel.et_clientid.setText(settings.get("clientId"));
        viewModel.et_username.setText(settings.get("username"));
        viewModel.et_password.setText(settings.get("password"));
        viewModel.et_alive.setText(settings.get("alive"));
        viewModel.et_timeout.setText(settings.get("timeout"));
        viewModel.et_topic.setText(settings.get("topic"));
        viewModel.et_message.setText(settings.get("message"));
        viewModel.cb_auto.setChecked(Objects.equals(settings.get("auto"), "yes"));
        viewModel.cb_session.setChecked(Objects.equals(settings.get("session"),"yes"));
        int position = viewModel.getProtocolPosition(settings.get("protocol"));
        viewModel.spinner.setSelection(position,true);
    }

    public boolean saveValue(){
        settings.put("name",viewModel.et_name.getText().toString());
        settings.put("ip",viewModel.et_ip.getText().toString());
        settings.put("port",viewModel.et_port.getText().toString());
        settings.put("username",viewModel.et_username.getText().toString());
        settings.put("password",viewModel.et_password.getText().toString());
        settings.put("clientId",viewModel.et_clientid.getText().toString());
        settings.put("alive",viewModel.et_alive.getText().toString());
        settings.put("timeout",viewModel.et_timeout.getText().toString());
        settings.put("topic",viewModel.et_topic.getText().toString());
        settings.put("message",viewModel.et_message.getText().toString());
        settings.put("auto",viewModel.cb_auto.isChecked()?"yes":"no");
        settings.put("session",viewModel.cb_session.isChecked()?"yes":"no");
        int pos = viewModel.spinner.getSelectedItemPosition();
        settings.put("protocol", viewModel.getProtocol(pos));

        if(TextUtils.isEmpty(settings.get("name"))){
            Utils.showToast(getActivity(),"连接名称不能为空");
            return false;
        }
        if(TextUtils.isEmpty(settings.get("clientId"))){
            Utils.showToast(getActivity(),"客户端ID不能为空");
            return false;
        }
        if(TextUtils.isEmpty(settings.get("ip"))){
            Utils.showToast(getActivity(),"IP地址不能为空");
            return false;
        }
        if(TextUtils.isEmpty(settings.get("port"))){
            settings.put("port","1883");
        }
        if(TextUtils.isEmpty(settings.get("timeout"))){
            settings.put("timeout","30");
        }
        if(TextUtils.isEmpty(settings.get("alive"))){
            settings.put("alive","60");
        }

        SPUtils.getInstance().putMap(Constants.BROKER, settings);
        return true;
    }
}
