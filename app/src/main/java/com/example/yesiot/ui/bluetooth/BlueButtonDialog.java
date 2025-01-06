package com.example.yesiot.ui.bluetooth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.example.yesiot.R;
import com.example.yesiot.adapter.SpinnerAdapter;
import com.example.yesiot.helper.BlueButtonHelper;
import com.example.yesiot.object.BlueButton;
import com.example.yesiot.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlueButtonDialog extends DialogFragment {
    Context mContext;
    ButtonModel viewModel;
    SpinnerAdapter spinnerAdapter;
    List<String> mCharList = new ArrayList<>();
    BlueButton mBlueButton = new BlueButton();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mContext = requireContext();
        View root = inflater.inflate(R.layout.fragment_bluetooth_button, container, false);
        viewModel = new ButtonModel(root);
        viewModel.btnSave.setOnClickListener(clickListener);
        viewModel.btnCancel.setOnClickListener(clickListener);
        //viewModel.setInput(mBlueButton);
        initSpinner();
        return root;
    }

    @Override
    public void onStart() {
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        assert window != null;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        //params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
        viewModel.setInput(mBlueButton);
        super.onStart();
    }

    public void show(FragmentManager manager)
    {
        show(manager, "BlueButtonDialog");
    }

    public void setBlueButton(BlueButton blueButton)
    {
        mBlueButton = blueButton;
    }

    public void setCharList(List<String> charList)
    {
        mCharList.clear();
        mCharList.addAll(charList);
        if(spinnerAdapter != null)
            spinnerAdapter.notifyDataSetChanged();
    }

    private void initSpinner() {
        //声明一个下拉列表的数组适配器
        spinnerAdapter = new SpinnerAdapter(mContext, mCharList);
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
                String uuid = mCharList.get(pos);
                //showToast("你选择了 " + uuid);
                viewModel.et_uuid.setText(uuid);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void alert(String string)
    {
        Utils.alert(mContext, string);
    }

    private void handleSave()
    {
        String title = viewModel.et_title.getText().toString().trim();
        //String unit = viewModel.et_unit.getText().toString().trim();
        String caption = viewModel.et_caption.getText().toString().trim();
        String name = viewModel.et_name.getText().toString().trim();
        String uuid = viewModel.et_uuid.getText().toString().trim();
        String payload = viewModel.et_payload.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            alert("名称不能为空");
            return;
        }
        if(TextUtils.isEmpty(name)){
            alert("标题不能为空");
            return;
        }
        if(TextUtils.isEmpty(uuid)){
            alert("uuid不能为空");
            return;
        }

        mBlueButton.uuid = uuid;
        mBlueButton.name = name;
        mBlueButton.title = title;
        mBlueButton.caption = caption;
        //mBlueButton.unit = unit;
        mBlueButton.payload = payload;

        if(BlueButtonHelper.save(mBlueButton)){
            Utils.showToast(mContext, "保存成功");
            dismiss();
            if(null != savedListener) savedListener.onClick(mBlueButton);
        } else {
            Utils.showToast(mContext,"保存失败, 请重试");
        }
    }

    View.OnClickListener clickListener = view -> {
        if(view.getId() == R.id.button_cancel)
        {
            dismiss();
        } else if (view.getId() == R.id.button_save) {
            //showToast("注册蓝牙设备");
            handleSave();
        }
    };

    private SavedListener savedListener = null;
    public void setOnSavedListener(SavedListener listener)
    {
        savedListener = listener;
    }

    public interface SavedListener
    {
        void onClick(BlueButton blueButton);
    }
}