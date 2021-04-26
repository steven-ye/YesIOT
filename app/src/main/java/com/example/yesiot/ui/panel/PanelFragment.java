package com.example.yesiot.ui.panel;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yesiot.MainActivity;
import com.example.yesiot.R;
import com.example.yesiot.TypeAdapter;
import com.example.yesiot.helper.PanelHelper;
import com.example.yesiot.object.Panel;
import com.example.yesiot.util.Utils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

public class PanelFragment extends Fragment {

    private PanelViewModel viewModel;
    final int[] panel_types = {
            R.layout.option_panel_text,
            R.layout.option_panel_image,
            R.layout.option_panel_vertical,
            R.layout.option_panel_horizontal,
            R.layout.option_panel_switch,
            R.layout.option_panel_data_ring,
    };
    Panel panel = new Panel();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //在fragment中使用oncreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_panel, container, false);
        viewModel = new PanelViewModel(root);

        Bundle args = getArguments();
        assert args != null;
        int id = args.getInt("id");
        panel = PanelHelper.get(id);
        panel.deviceId = args.getInt("deviceId");
        Log.e("PanelFragment", "ID is "+id);
        if(panel.id > 0){
            setTitle("编辑面板");
            Log.e("PanelFragment", "Title is "+panel.title);
        }
        initView();
        initRecyclerView();
        addTextWatcher();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        viewModel.expert_option.setOnClickListener(v->{
            viewModel.expert_option.toggle();
            if(viewModel.expert_option.isChecked()){
                viewModel.row_expert_option.setVisibility(View.VISIBLE);
            }else{
                viewModel.row_expert_option.setVisibility(View.GONE);
            }
        });
        viewModel.expert_option.callOnClick();
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

    public void addTextWatcher(){
        viewModel.et_name.addTextChangedListener(new EmptyTextWachter(viewModel.layout_name,"名称不能为空"));
        viewModel.et_title.addTextChangedListener(new EmptyTextWachter(viewModel.layout_title,"标题不能为空"));
        viewModel.et_width.addTextChangedListener(new EmptyTextWachter(viewModel.layout_width,"宽度不能为空"));
        viewModel.et_height.addTextChangedListener(new EmptyTextWachter(viewModel.layout_height,"高度不能为空"));
    }

    public void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        viewModel.recyclerView.setLayoutManager(layoutManager);
        TypeAdapter adapter = new TypeAdapter(panel_types);
        adapter.setSelected(panel.type);
        viewModel.recyclerView.setAdapter(adapter);
        adapter.setOnClickListener(v -> panel.type = viewModel.recyclerView.getChildAdapterPosition(v));
    }

    public void initView(){
        viewModel.et_name.setText(panel.name);
        viewModel.et_title.setText(panel.title);
        viewModel.et_unit.setText(panel.unit);
        viewModel.et_topic.setText(panel.topic);
        viewModel.et_sub.setText(panel.sub);
        viewModel.et_width.setText(panel.width+"");
        viewModel.et_height.setText(panel.height+"");
        viewModel.et_cmd_on.setText(panel.on);
        viewModel.et_cmd_off.setText(panel.off);
        //viewModel.et_payload.setText(panel.payload);
    }

    public void invalidateAll(){
        panel.title = viewModel.et_title.getText().toString();
        panel.unit = viewModel.et_unit.getText().toString();
        panel.name = viewModel.et_name.getText().toString();
        panel.topic = viewModel.et_topic.getText().toString();
        panel.sub = viewModel.et_sub.getText().toString();
        String width = viewModel.et_width.getText().toString();
        String height = viewModel.et_height.getText().toString();
        if(!TextUtils.isEmpty(width)){
            panel.width = Integer.parseInt(width);
        }
        if(!TextUtils.isEmpty(height)){
            panel.height = Integer.parseInt(height);
        }
        panel.payload = viewModel.et_cmd_on.getText().toString();
        panel.on = viewModel.et_cmd_on.getText().toString();
        panel.off = viewModel.et_cmd_off.getText().toString();
    }

    private void save(){
        invalidateAll();
        if(TextUtils.isEmpty(panel.name)){
            viewModel.layout_name.setError("面板名称不能为空");
            return;
        }else if(panel.name.length()<4 || panel.name.length()>20){
            viewModel.layout_name.setError("面板名称要求 4 - 20 个字符");
            return;
        }
        if(TextUtils.isEmpty(panel.title)){
            viewModel.layout_title.setError("面板不能为空");
            return;
        }

        if(PanelHelper.save(panel)){
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