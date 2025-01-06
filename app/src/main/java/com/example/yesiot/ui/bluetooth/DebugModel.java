package com.example.yesiot.ui.bluetooth;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.example.yesiot.R;
import com.google.android.material.tabs.TabLayout;

public class DebugModel {
    private final MutableLiveData<Boolean> mLinked = new MutableLiveData<>();
    private final MutableLiveData<Integer> mType = new MutableLiveData<>();

    public ScrollView scrollView;
    public TextView textView;
    public EditText editText;
    public Button btnSend;
    public Button btnClear;
    public Button btnConnect;
    public RadioGroup radioGroup;
    public Spinner spinner;
    public CheckBox checkboxHex;
    public TabLayout tablayout;

    public DebugModel(View root) {
        mLinked.setValue(false);
        mType.setValue(0);
        btnClear = root.findViewById(R.id.button_clear_log);
        btnConnect = root.findViewById(R.id.button_connect);
        scrollView = root.findViewById(R.id.scrollView);
        textView = root.findViewById(R.id.text_view);
        editText = root.findViewById(R.id.et_message);
        btnSend = root.findViewById(R.id.button_send_message);
        radioGroup = root.findViewById(R.id.type_radio_group);
        spinner = root.findViewById(R.id.spinner);
        checkboxHex = root.findViewById(R.id.checkbox_hex);
    }

    public MutableLiveData<Boolean> getLinked() {
        return mLinked;
    }
    public void setLinked(Boolean linked) {
        mLinked.setValue(linked);
    }
    public boolean getLinkedValue() {
        return mLinked.getValue();
    }

    public MutableLiveData<Integer> getType() {
        return mType;
    }
    public void setType(int type) {
        mType.postValue(type);
    }
    public int getTypeValue() {
        return mType.getValue();
    }
}
