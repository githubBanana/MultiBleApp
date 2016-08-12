package com.xs.multibleapp.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xs.multibleapp.utils.ScreenUtil;

/**
 * Created by Administrator on 2015/8/25.
 * gridview 的条目
 */
public class GridViewItem extends LinearLayout {
    /**
     * 圆形控件
     */
    private ImageView circleView;
    /**
     * 显示文字的控件
     */
    private TextView textView;

    public GridViewItem(Context context) {
        super(context);
        init();
    }

    public GridViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        float padding = ScreenUtil.dip2px(20);
        setPadding(0, (int) padding,0,0);
        circleView = new ImageView(getContext());
        textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.GRAY);
        textView.setTextAppearance(getContext(),android.R.style.TextAppearance_DeviceDefault);
        int dimensionPixelSize = (int) ScreenUtil.dip2px(80);
        addView(circleView, new LayoutParams(dimensionPixelSize,dimensionPixelSize));
        addView(textView,new LayoutParams(-2,-2));

    }


    /**
     * 设置文本内容
     *
     * @param text
     */
    public void setText(String text) {
        this.textView.setText(text);
    }

    /**
     * 设置圆形图片
     *
     * @param resId
     */
    public void setImg(int resId) {
        circleView.setImageResource(resId);
    }


}
