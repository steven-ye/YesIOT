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
    private int[] typeList;
    private List<State> stateList=new ArrayList<>();

    public TypeAdapter(int[] list){
        typeList = list;
        for(int ignored :list){
            State state = new State();
            stateList.add(state);
        }
    }

    public void setSelected(int position){
        stateList.get(position).setSelected(true);
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
        View v = inflater.inflate(typeList[position],null);
        final LinearLayout linearLayout = (LinearLayout)holder.view;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.removeAllViews();
        linearLayout.addView(v,lp);
        if(stateList.get(position).isSelected()){
            v.setBackgroundResource(R.drawable.border_blue);
        }else{
            v.setBackgroundResource(R.drawable.border_grey);
        }
        linearLayout.setOnClickListener(v1 -> {
            if(listener !=null){
                listener.onItemClick(v1);
            }
            for (State state : stateList) {
                state.setSelected(false);
            }
            stateList.get(position).setSelected(true);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return typeList.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        public ViewHolder(View view){
            super(view);
            this.view = view;
        }
    }

    static public class State {
        private boolean isSelected;
        public boolean isSelected() {
            return isSelected;
        }
        public void setSelected(boolean selected) {
            isSelected = selected;
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