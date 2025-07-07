package com.swolo.lpy.pysx.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.swolo.lpy.pysx.R;


/**
 * Created by hezhisu on 16/8/23.
 */

public class CommonLoadingDialog extends Dialog{

    private Context mContext;
    public CommonLoadingDialog(Context context) {
        super(context, R.style.Theme_dialog);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.layout_loading_dialog, null);
        this.setCanceledOnTouchOutside(false);
        setContentView(contentView);
    }
}
