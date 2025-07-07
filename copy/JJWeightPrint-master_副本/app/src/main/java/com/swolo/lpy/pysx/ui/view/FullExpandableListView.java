package com.swolo.lpy.pysx.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * Created by hezhisu on 2017/1/8.
 */

public class FullExpandableListView extends ExpandableListView{
    public FullExpandableListView(Context context) {
        super(context);
    }

    public FullExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,

                MeasureSpec.AT_MOST);

        super.onMeasure(widthMeasureSpec, expandSpec);

    }
}
