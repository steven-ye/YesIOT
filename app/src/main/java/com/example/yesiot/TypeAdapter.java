package com.example.yesiot;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.ViewHolder>{
    ViewGroup parent;
    private LayoutInflater inflater;
    List<Integer> typeList = new ArrayList<>();
    private int selectedPosition = 0;

    public TypeAdapter(int[] list){
        typeList.clear();
        for(int val: list){
            typeList.add(val);
        }
    }

    public void setList(int[] list){
        typeList.clear();
        for(int val: list){
            typeList.add(val);
        }
        notifyDataSetChanged();
    }

    public void setSelected(int position){
        selectedPosition = position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        inflater = LayoutInflater.from(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_type,parent,false);
        //LinearLayout view = new LinearLayout(parent.getContext());

        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        View v = inflater.inflate(typeList.get(position),null);
        final LinearLayout linearLayout = (LinearLayout)holder.view;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.removeAllViews();
        linearLayout.addView(v,lp);
        if(selectedPosition == position){
            v.setBackgroundResource(R.drawable.border_blue);
        }else{
            v.setBackgroundResource(R.drawable.border_grey);
        }
        linearLayout.setOnClickListener(v1 -> {
            if(listener !=null){
                listener.onItemClick(v1);
            }
            selectedPosition = position;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return typeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        public ViewHolder(View view){
            super(view);
            this.view = view;
        }
    }

    private OnItemClickListener listener;
    public void setOnClickListener(TypeAdapter.OnItemClickListener onItemClickListener){
        this.listener=onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(View v);
    }
}