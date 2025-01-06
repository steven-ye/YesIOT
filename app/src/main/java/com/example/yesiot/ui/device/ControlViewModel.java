package com.example.yesiot.ui.device;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesiot.object.Device;
import com.example.yesiot.object.Panel;

import java.util.ArrayList;
import java.util.List;

public class ControlViewModel extends ViewModel {

    final MutableLiveData<Boolean> mLocked = new MutableLiveData<>();
    final MutableLiveData<List<Panel>> mList = new MutableLiveData<>();
    final MutableLiveData<Integer> mOption = new MutableLiveData<>();
    final MutableLiveData<String>  mStatus = new MutableLiveData<>();
    final MutableLiveData<Boolean>  mOnline = new MutableLiveData<>();

    public ControlViewModel() {
        mLocked.setValue(true);
        mList.setValue(new ArrayList<>());
        mOption.setValue(0);
        mStatus.setValue("");
        mOnline.setValue(false);
    }

    public LiveData<Boolean> getLocked() {
        return mLocked;
    }
    public void setLocked(Boolean locked) {
        mLocked.postValue(locked);
    }
    public LiveData<List<Panel>> getList() {
        return mList;
    }
    public void setListValue(List<Panel> list) {
        mList.postValue(list);
    }
    public LiveData<Integer> getOption() {
        return mOption;
    }
    public int getOptionValue() {
        return mOption.getValue();
    }
    public void setOptionValue(int val) {
        mOption.setValue(val);
    }
    public LiveData<String> getStatus() {
        return mStatus;
    }
    public void setStatus(String status) {
        mStatus.setValue(status);
    }
    public void setOnline(boolean online) {
        mOnline.postValue(online);
    }
    public LiveData<Boolean> getOnline() {
        return mOnline;
    }
    public boolean isOnline() {
        return mOnline.getValue();
    }
}