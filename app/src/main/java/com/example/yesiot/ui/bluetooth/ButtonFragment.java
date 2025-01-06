package com.example.yesiot.ui.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.R;
import com.example.yesiot.adapter.SpinnerAdapter;
import com.example.yesiot.helper.BlueButtonHelper;
import com.example.yesiot.object.BlueButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonFragment extends AbsFragment {
    //private final String TAG = "ButtonFragment";
    private ButtonModel viewModel;

    SpinnerAdapter spinnerAdapter;
    List<String> mCharList = new ArrayList<>();
    BlueButton blueButton;

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth_button, container, false);
        viewModel = new ButtonModel(root);
        viewModel.btnCancel.setOnClickListener(clickListener);
        viewModel.btnSave.setOnClickListener(clickListener);

        Bundle args = getArguments();
        assert args != null;
        blueButton = (BlueButton) args.getSerializable("button");
        mCharList = (List<String>) args.getSerializable("charList");

        viewModel.setInput(blueButton);

        initSpinner();

        return root;
    }

    private void initSpinner() {
        //声明一个下拉列表的数组适配器
        spinnerAdapter = new SpinnerAdapter(requireContext(), mCharList);
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

    @Override
    public void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.blue_button, menu);
        super.onOptionsMenuCreated(menu, inflater);
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsMenuSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_item_save:
                handleSave();
                break;
            case R.id.menu_item_remove:
                confirm("确定要删除？", v -> {
                    if(BlueButtonHelper.remove(blueButton.id)){
                        showToast("删除成功");
                        navigateUp();
                    } else {
                        showToast("删除失败");
                    }
                });
                break;
        }
        return super.onOptionsMenuSelected(item);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.button_cancel)
            {
                Navigation.findNavController(requireView()).navigateUp();
            } else if (view.getId() == R.id.button_save) {
                //showToast("注册蓝牙设备");
                handleSave();
            }
        }
    };

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

        blueButton.uuid = uuid;
        blueButton.name = name;
        blueButton.title = title;
        blueButton.caption = caption;
        //blueButton.unit = unit;
        blueButton.payload = payload;

        if(BlueButtonHelper.save(blueButton)){
            showToast("保存成功");
            navigateUp();
        } else {
            alert("保存失败, 请重试");
        }
    }
}
