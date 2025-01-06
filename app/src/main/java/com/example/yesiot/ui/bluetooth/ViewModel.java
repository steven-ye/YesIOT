package com.example.yesiot.ui.bluetooth;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.example.yesiot.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewModel {
    private final MutableLiveData<Boolean> mLinked = new MutableLiveData<>();
    private final MutableLiveData<Integer> mType = new MutableLiveData<>();
    public GridView gridView;
    public ScrollView scrollView;
    public TextView textView;
    public EditText editText;
    public Button btnSend;
    public Button btnClear;
    public ImageButton btnCloseDebug;
    public Spinner spinner;
    public CheckBox checkboxHex;
    public View debugView;
    public FloatingActionButton fab;

    public ViewModel(View root) {
        mLinked.setValue(false);
        mType.setValue(0);
        gridView = root.findViewById(R.id.gridView);
        scrollView = root.findViewById(R.id.scrollView);
        textView = root.findViewById(R.id.text_view);
        editText = root.findViewById(R.id.et_message);
        btnClear = root.findViewById(R.id.button_clear_log);
        btnSend = root.findViewById(R.id.button_send_message);
        spinner = root.findViewById(R.id.spinner);
        checkboxHex = root.findViewById(R.id.checkbox_hex);
        debugView = root.findViewById(R.id.debug_view);
        btnCloseDebug = root.findViewById(R.id.button_close_debug);
        fab = root.findViewById(R.id.fab);
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
