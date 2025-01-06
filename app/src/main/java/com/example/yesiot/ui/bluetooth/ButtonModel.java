package com.example.yesiot.ui.bluetooth;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.lifecycle.MutableLiveData;

import com.example.yesiot.R;
import com.example.yesiot.object.BlueButton;

public class ButtonModel {
    private final MutableLiveData<Boolean> mLinked = new MutableLiveData<>();

    public TextView textView;
    public EditText et_name;
    public EditText et_title;
    public EditText et_caption;
    public EditText et_unit;
    public EditText et_uuid;
    public EditText et_payload;
    public Button btnSave;
    public Button btnCancel;
    public RadioGroup radioGroup;
    public Spinner spinner;
    public CheckBox checkboxHex;

    public ButtonModel(View root) {
        mLinked.setValue(false);
        btnSave = root.findViewById(R.id.button_save);
        btnCancel = root.findViewById(R.id.button_cancel);
        et_name = root.findViewById(R.id.input_name);
        et_title = root.findViewById(R.id.input_title);
        et_caption = root.findViewById(R.id.input_caption);
        //et_unit = root.findViewById(R.id.input_unit);
        et_uuid = root.findViewById(R.id.input_uuid);
        et_payload = root.findViewById(R.id.input_payload);
        spinner = root.findViewById(R.id.spinner);
    }

    public void setInput(BlueButton button)
    {
        et_name.setText(button.name);
        et_title.setText(button.title);
        et_caption.setText(button.caption);
        et_uuid.setText(button.uuid);
        et_payload.setText(button.payload);
    }
}
