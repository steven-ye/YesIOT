package com.example.yesiot.ui.esptouch;

import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.yesiot.R;
import com.google.android.material.textfield.TextInputEditText;

import java.nio.charset.StandardCharsets;

public class EspViewModel {
    public TextInputEditText apPasswordEdit;
    public TextInputEditText deviceCountEdit;
    public Button scanBtn;
    public Button confirmBtn;

    TextView apSsidTV;
    TextView apBssidTV;
    RadioGroup packageModeGroup;
    TextView messageView;

    String ssid;
    byte[] ssidBytes;
    String bssid;

    CharSequence message;

    boolean confirmEnable;

    public EspViewModel(View root){
        apSsidTV =root.findViewById(R.id.apSsidText);
        apBssidTV =root.findViewById(R.id.apBssidText);
        apPasswordEdit = root.findViewById(R.id.apPasswordEdit);
        deviceCountEdit = root.findViewById(R.id.deviceCountEdit);
        messageView = root.findViewById(R.id.messageView);
        scanBtn = root.findViewById(R.id.btn_scan_wifi);
        confirmBtn = root.findViewById(R.id.confirmBtn);
        packageModeGroup = root.findViewById((R.id.packageModeGroup));
    }

    public void invalidateAll() {
        apSsidTV.setText(ssid);
        apBssidTV.setText(bssid);
        messageView.setText(message);
        confirmBtn.setEnabled(confirmEnable);
    }
}
