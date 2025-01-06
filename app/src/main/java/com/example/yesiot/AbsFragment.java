package com.example.yesiot;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;

import com.example.yesiot.dialog.ConfirmDialog;
import com.example.yesiot.util.Utils;

public abstract class AbsFragment extends Fragment {
    protected final String TAG = getClass().getSimpleName();
    protected Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        //在fragment中使用onCreateOptionsMenu时需要在onCrateView中添加此方法，否则不会调用
        //setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setOptionsMenu();
    }

    private void setOptionsMenu()
    {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                // Handle for example visibility of menu items
                //menu.findItem(R.id.action_search).isVisible = false;
                onOptionsMenuPrepared(menu);
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                //menuInflater.inflate(menuId, menu);
                onOptionsMenuCreated(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return onOptionsMenuSelected(menuItem);
                //return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    protected void onOptionsMenuPrepared(@NonNull Menu menu)
    {
        //menu.findItem(R.id.action_search).isVisible = false;
    }
    protected void onOptionsMenuCreated(@NonNull Menu menu, @NonNull MenuInflater menuInflater)
    {
        //menuInflater.inflate(R.menu.home, menu);
    }

    protected boolean onOptionsMenuSelected(@NonNull MenuItem menuItem)
    {
        return false;
    }

    protected void navigate(int pageResId, Bundle args)
    {
        Navigation.findNavController(requireView()).navigate(pageResId, args);
    }

    protected void navigateUp()
    {
        Navigation.findNavController(requireView()).navigateUp();
    }
    public MainActivity getMainActivity(){
        return (MainActivity)requireActivity();
    }

    public String getTitle() {
        return getMainActivity().getTitle().toString();
    }
    public void setTitle(String title) {
        ActionBar actionBar = getMainActivity().getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(title);
    }

    public void confirm(String message, ConfirmDialog.OnConfirmListener okayListener){
        ConfirmDialog.show(getParentFragmentManager(),message,okayListener);
    }

    public void alert(String message){
        Utils.alert(mContext, message);
    }

    public void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void showToast(String message, int type) {
        Toast.makeText(mContext, message, type).show();
    }
}