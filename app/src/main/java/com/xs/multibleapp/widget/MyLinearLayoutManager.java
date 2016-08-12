package com.xs.multibleapp.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 深圳州富科技有限公司
 * Created by lwg on 2016/7/26.
 */

public class MyLinearLayoutManager extends LinearLayoutManager {
    public MyLinearLayoutManager(Context context) {
        super(context);
    }

    public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public MyLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void addView(View child) {
        if(child.getParent()!=null){
            ((ViewGroup)child.getParent()).removeView(child);
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if(child.getParent()!=null){
            ((ViewGroup)child.getParent()).removeView(child);
        }
        super.addView(child, index);
    }
}
