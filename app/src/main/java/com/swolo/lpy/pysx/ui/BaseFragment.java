package com.swolo.lpy.pysx.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.swolo.lpy.pysx.eventbus.EventCenter;
import com.swolo.lpy.pysx.ui.view.CommonLoadingDialog;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by hezhisu on 16/8/23.
 */

public abstract class BaseFragment extends Fragment {

    protected View mParentView;
    private CommonLoadingDialog mLoadingDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mParentView = inflater.inflate(getContentViewRes(), container,false);
        mLoadingDialog = new CommonLoadingDialog(getActivity());
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
        initView();
        initData();
        bindAction();
        return mParentView;
    }

    public void showToast(String msg){
        Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show();
    }
    public void showLoading(){
        if(mLoadingDialog != null && !mLoadingDialog.isShowing() && getActivity() != null){
            mLoadingDialog.show();
        }
    }

    public void stopLoading(){
        if(mLoadingDialog != null && mLoadingDialog.isShowing() && getActivity() != null){
            mLoadingDialog.dismiss();
        }
    }

    protected abstract void initView();
    protected abstract void initData();
    protected abstract void bindAction();
    protected abstract int getContentViewRes();

    @Subscribe
    public void onEventMainThread(EventCenter eventCenter) {
        if (null != eventCenter) {
            onEventComming(eventCenter);
        }
    }

    /**
     * when event comming
     *
     * @param eventCenter
     */
    protected void onEventComming(EventCenter eventCenter){
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
