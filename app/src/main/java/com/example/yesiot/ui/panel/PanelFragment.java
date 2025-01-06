package com.example.yesiot.ui.panel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yesiot.AbsFragment;
import com.example.yesiot.PanelLayout;
import com.example.yesiot.R;
import com.example.yesiot.TypeAdapter;
import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.helper.PanelHelper;
import com.example.yesiot.object.Panel;
import com.example.yesiot.util.Utils;

public class PanelFragment extends AbsFragment {

    private PanelViewModel viewModel;
    String[] radios;
    Panel panel = new Panel();
    boolean hasError = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_panel, container, false);
        viewModel = new PanelViewModel(root);

        radios = getResources().getStringArray(R.array.panel_types);

        Bundle args = getArguments();
        assert args != null;
        int id = args.getInt("id");
        Log.e("PanelFragment", "ID is "+id);
        if(id > 0){
            setTitle("编辑面板");
            Log.e("PanelFragment", "Title is "+panel.title);
            panel = PanelHelper.get(id);
        }else{
            panel = new Panel();
            panel.deviceId = args.getInt("deviceId");
        }
        initView();
        addTextWatcher();
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
            save();
        }else if(item.getItemId()==R.id.action_remove){
            if(panel.id>0){
                ConfirmDialog.show(getParentFragmentManager(), "确认要删除此面板/按钮？", v -> {
                    if(PanelHelper.delete(panel.id)) {
                        showToast("删除面板/按钮成功");
                        Navigation.findNavController(requireView()).navigateUp();
                    }else{
                        showToast("删除失败");
                    }
                });
            }
        }
        return super.onOptionsMenuSelected(item);
    }

    public void addTextWatcher(){
        viewModel.et_name.addTextChangedListener(new EmptyTextWachter(viewModel.et_name,"名称不能为空"));
        viewModel.et_title.addTextChangedListener(new EmptyTextWachter(viewModel.et_title,"标题不能为空"));
        viewModel.et_width.addTextChangedListener(new EmptyTextWachter(viewModel.et_width,"宽度不能为空"));
        viewModel.et_height.addTextChangedListener(new EmptyTextWachter(viewModel.et_height,"高度不能为空"));
    }

    TypeAdapter adapter;
    public void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        viewModel.recyclerView.setLayoutManager(layoutManager);
        if(panel.type == 2){
            adapter = new TypeAdapter(PanelLayout.DATA_OPTIONS);
            if(panel.design>2)panel.design = 0;
        }else{
            adapter = new TypeAdapter(PanelLayout.BUTTON_OPTIONS);
            if(panel.design>4)panel.design = 4;
        }
        adapter.setSelected(panel.design);
        viewModel.recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(v -> panel.design = viewModel.recyclerView.getChildAdapterPosition(v));
    }

    @SuppressLint("SetTextI18n")
    public void initView(){
        initRecyclerView();
        viewModel.et_name.setText(panel.name);
        viewModel.et_title.setText(panel.title);
        viewModel.et_unit.setText(panel.unit);
        viewModel.et_width.setText(panel.width+"");
        viewModel.et_height.setText(panel.height+"");
        viewModel.et_cmd_on.setText(panel.on);
        viewModel.et_cmd_off.setText(panel.off);
        viewModel.et_title_size.setText(panel.title_size);
        viewModel.et_unit_size.setText(panel.unit_size);
        viewModel.et_payload.setText(panel.payload);

        viewModel.setVisible(panel.type);

        for(int i=0;i<radios.length;i++){
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(i);
            radioButton.setTag("panel_radio_"+i);
            radioButton.setText(radios[i]);
            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            viewModel.radioGroup.addView(radioButton, layoutParams);
        }

        //viewModel.radioGroup.clearCheck();
        //viewModel.radioGroup.check(panel.type);
        Log.w("PanelFragment", "checked type is "+panel.type);
        viewModel.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            showToast(radios[checkedId]);
            panel.type = checkedId;
            if(panel.type == 2){
                adapter.setList(PanelLayout.DATA_OPTIONS);
            }else{
                adapter.setList(PanelLayout.BUTTON_OPTIONS);
            }
            viewModel.setVisible(panel.type);
        });

        //viewModel.radioGroup.clearCheck();
        viewModel.radioGroup.check(panel.type);
        //Log.w("PanelFragment", "checked type is "+panel.type);
    }

    public void invalidateAll(){
        panel.name = viewModel.et_name.getText().toString().trim();
        panel.title = viewModel.et_title.getText().toString().trim();
        panel.unit = viewModel.et_unit.getText().toString().trim();
        panel.title_size = viewModel.et_title_size.getText().toString().trim();
        panel.unit_size = viewModel.et_unit_size.getText().toString().trim();
        String width = viewModel.et_width.getText().toString().trim();
        String height = viewModel.et_height.getText().toString().trim();
        if(!TextUtils.isEmpty(width)){
            panel.width = Integer.parseInt(width);
            if(panel.height < 10) {
                panel.height = panel.height * 100;
            }
            if(panel.height < 100) {
                panel.height = 100;
            }
        }
        if(!TextUtils.isEmpty(height)){
            panel.height = Integer.parseInt(height);
            if(panel.height < 10) {
                panel.height = panel.height * 100;
            }
            if(panel.height < 100) {
                panel.height = 100;
            }
        }
        panel.on = viewModel.et_cmd_on.getText().toString().trim();
        panel.off = viewModel.et_cmd_off.getText().toString().trim();
        panel.payload = viewModel.et_payload.getText().toString().trim();
    }

    private void save(){
        if(hasError)return;
        invalidateAll();
        if(TextUtils.isEmpty(panel.name)){
            alert("面板名称不能为空");
            return;
        }else if(panel.name.length()<2 || panel.name.length()>20){
            Utils.alert(getContext(),"面板名称要求 2 - 20 个字符");
            return;
        }
        if(TextUtils.isEmpty(panel.title)){
            alert("面板不能为空");
            return;
        }

        //int maxWidth = Utils.getScreenWidth(requireContext());
        Size screenSize = Utils.getScreenSize(requireContext());
        int maxWidth = screenSize.getWidth();
        if(panel.width>maxWidth){
            alert("宽度不能超过手机屏幕宽度 " + maxWidth);
            return;
        }
        if(panel.height>maxWidth){
            alert("高度不能超过手机屏幕宽度 " + maxWidth);
            return;
        }

        if(PanelHelper.save(panel)){
            Navigation.findNavController(requireView()).navigateUp();
            showToast("保存成功");
        }else{
            showToast("保存失败");
        }
    }

    private class EmptyTextWachter implements TextWatcher {
        EditText mView;
        String message;
        int maxLength = 0;
        public EmptyTextWachter(EditText view, String error) {
            mView = view;
            message = error;
            InputFilter[] filters = mView.getFilters();
            for(InputFilter filter: filters){
                if(filter instanceof InputFilter.LengthFilter){
                    InputFilter.LengthFilter lengthFilter =  (InputFilter.LengthFilter) filter;
                    maxLength = lengthFilter.getMax();
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            viewModel.tv_error.setText("");
            hasError = false;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           if(maxLength>0 && s.length() == maxLength){
               viewModel.tv_error.setText("最多输入 "+maxLength+" 个字符");
           }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length() == 0){
                showToast(message);
                mView.requestFocus();
                viewModel.tv_error.setText(message);
                hasError = true;
            }else{
                hasError = false;
            }
        }
    }
}