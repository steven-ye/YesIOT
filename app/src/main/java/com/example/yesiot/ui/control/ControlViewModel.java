package com.example.yesiot.ui.control;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.yesiot.object.Device;
import com.example.yesiot.object.Panel;

import java.util.ArrayList;
import java.util.List;

public class ControlViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mLocked = new MutableLiveData<>();
    private final MutableLiveData<String> mCloud = new MutableLiveData<>();
    private final MutableLiveData<List<Panel>> mList = new MutableLiveData<>();

    public ControlViewModel() {
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
    public LiveData<List<Panel>> getList() {
        return mList;
    }
    public void setListValue(List<Panel> list) {
        mList.postValue(list);
    }
}