package com.example.yesiot.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesiot.object.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mLocked = new MutableLiveData<>();
    private final MutableLiveData<String> mCloud = new MutableLiveData<>();
    private final MutableLiveData<List<Device>> mList = new MutableLiveData<>();

    public HomeViewModel() {
        mLocked.setValue(true);
        mCloud.setValue("unknown");
        mList.setValue(new ArrayList<>());
    }

    public LiveData<Boolean> getLocked() {
        return mLocked;
    }
    public void setLocked(Boolean locked) {
        mLocked.postValue(locked);
    }
    public LiveData<String> getCloud() {
        return mCloud;
    }
    public void setCloud(String status) {
        mCloud.setValue(status);
    }
    public LiveData<List<Device>> getList() {
        return mList;
    }
    public void setListValue(List<Device> list) {
        mList.postValue(list);
    }
}