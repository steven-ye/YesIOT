package com.example.yesiot.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.yesiot.R;
import com.example.yesiot.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImagesDialog extends DialogFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Light_NoTitleBar);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_images, container, false);
        root.findViewById(R.id.dialog_close).setOnClickListener(v->{
            dismiss();
        });
        GridView gridView = root.findViewById(R.id.gridView);
        gridView.setEmptyView(root.findViewById(R.id.list_empty));

        final List<Map<String,Object>> imageList = getImageList();
        /*
         * 新建适配器
         * 参数一是上下文
         * 参数二是数据源
         * 参数三是子布局页面
         * 参数四是数据源集合的键名
         * 参数五是子布局控件id
         */
        SimpleAdapter adapter=new SimpleAdapter(getActivity(), imageList,R.layout.item_gridview_image,new String[]{"bitmap"},new int[]{R.id.imgView});
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View v, Object data, String textRepresentation) {
                // TODO Auto-generated method stub
                if((v instanceof ImageView) && (data instanceof Bitmap)) {
                    ImageView imageView = (ImageView) v;
                    Bitmap bmp = (Bitmap) data;
                    imageView.setImageBitmap(bmp);
                    return true;
                }
                return false;
            }
        };
        adapter.setViewBinder(viewBinder);
        gridView.setAdapter(adapter);           //GridView加载适配器
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String path = imageList.get(position).get("name").toString();
                ImageView iv = v.findViewById(R.id.imgView);
                if(clickListener != null){
                    clickListener.onClick(iv, path);
                }
                dismiss();
            }
        });  //GridView加载点击事件
        return root;
    }

    private List<Map<String,Object>> getImageList() {
        String ImagePath = "img/devices";
        List<Map<String,Object>> imageList= new ArrayList<>();//新建数据源
        List<String> imageNames = Utils.getAssetPicPath(getActivity(), ImagePath);
        //Log.v(TAG,"imageNames size is "+imageNames.size());
        for(int i=0;i<imageNames.size();i++){
            Map<String,Object>map=new HashMap<String, Object>();
            map.put("name",imageNames.get(i));
            map.put("bitmap",Utils.getAssetsBitmap(getActivity(),imageNames.get(i)));
            imageList.add(map);                //向集合中添加数据
        }
        return imageList;
    }

    @Override
    public void onStart() {
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        //params.gravity = Gravity.BOTTOM;
        //getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        super.onStart();
    }

    private OnItemClickListener clickListener;
    public void setOnClickListener(OnItemClickListener listener){
        clickListener = listener;
    }

    public interface OnItemClickListener{
        void onClick(ImageView v, String path);
    }
}