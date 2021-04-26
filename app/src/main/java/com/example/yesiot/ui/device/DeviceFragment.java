package com.example.yesiot.ui.device;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.yesiot.dialog.ImagesDialog;
import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.helper.DeviceHelper;
import com.example.yesiot.object.Device;
import com.example.yesiot.util.Utils;
import com.google.android.material.textfield.TextInputLayout;

public class DeviceFragment extends Fragment {

    private DeviceViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        viewModel = new DeviceViewModel(root);

        viewModel.et_name.addTextChangedListener(new EmptyTextWachter(viewModel.layout_name, "设备名称不能为空"));

        viewModel.et_code.addTextChangedListener(new EmptyTextWachter(viewModel.layout_code, "设备ID不能为空"));

        viewModel.device_image.setOnClickListener(v->{
            ImageView iv = (ImageView) v;
            ImagesDialog dialog = new ImagesDialog();
            //dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogFullScreen);//添加上面创建的style
            dialog.show(getParentFragmentManager(), "ImageDialog");
            dialog.setOnClickListener((imageView, path) -> {
                viewModel.setImage(path);
                iv.setImageDrawable(imageView.getDrawable());
            });
        });

        viewModel.device_option.setOnClickListener(v->{
            viewModel.device_option.toggle();
            if(viewModel.device_option.isChecked()){
                viewModel.row_device_option.setVisibility(View.VISIBLE);
            }else{
                viewModel.row_device_option.setVisibility(View.GONE);
            }
        });
        viewModel.device_option.callOnClick();

        Bundle args = getArguments();
        int id = args != null ? args.getInt("id") : 0;
        Log.e("DeviceFragment", "ID is "+id);
        if(id > 0){
            setTitle("编辑设备");
            Device device = DeviceHelper.get(id);
            Log.e("DeviceFragment", "Device name is "+device.getName());
            viewModel.setDevice(device);
            Bitmap bmp = Utils.getAssetsBitmap(getActivity(), device.getImage());
            if(bmp != null){
                viewModel.device_image.setImageBitmap(bmp);
            }
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.action_save){
            save();
        }
        return super.onOptionsItemSelected(item);
    }

    private void save(){
        viewModel.invalidateAll();
        Device device = viewModel.getDevice();
        if(TextUtils.isEmpty(device.getName())){
            viewModel.layout_name.setError("设备名称不能为空");
            return;
        }else if(device.getName().length()<4 || device.getName().length()>20){
            viewModel.layout_name.setError("设备名称要求 6 - 20 个字符");
            return;
        }
        if(TextUtils.isEmpty(device.getCode())){
            viewModel.layout_code.setError("设备ID不能为空");
            return;
        }

        if(DeviceHelper.save(viewModel.getDevice())){
            Navigation.findNavController(getView()).navigateUp();
            Utils.showToast("保存成功");
        }else{
            Utils.showToast("保存失败");
        }
    }

    private void setTitle(String title) {
        MainActivity activity = (MainActivity) getActivity();
        activity.getSupportActionBar().setTitle(title);
    }

    static class EmptyTextWachter implements TextWatcher {
        String message;
        TextInputLayout textInputLayout;
        public EmptyTextWachter(TextInputLayout inputLayout,String error){
            textInputLayout = inputLayout;
            message = error;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String error = TextUtils.isEmpty(s) ? message : "";
            textInputLayout.setError(error);
        }
    }
}