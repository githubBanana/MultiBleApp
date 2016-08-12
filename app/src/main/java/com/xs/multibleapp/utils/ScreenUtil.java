package com.xs.multibleapp.utils;

import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;

/**
 * @version V1.0 <手机屏幕信息获取工具类>
 * @author: Xs
 * @date: 2016-06-28 10:57
 * @email Xs.lin@foxmail.com
 */
public class ScreenUtil {

    /**
     *  dp 与 px 转换
     * @param pxValue
     * @return
     */
    public static int px2dip(int pxValue) {
        // 公式 1: px = dp * (dpi / 160)
        // 公式 2: dp = px / denistity;
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float dip2px(float dipValue) {
        // 公式 1: px = dp * (dpi / 160)
        // 公式 2: dp = px / denistity;
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return  (dipValue * scale + 0.5f);
    }

    /**
     * 获取屏幕区域
     */
    public static Rect getScreenRect() {
        DisplayMetrics displayMetric = new DisplayMetrics();
        displayMetric = Resources.getSystem().getDisplayMetrics();
        Rect rect = new Rect(0, 0, displayMetric.widthPixels, displayMetric.heightPixels);
        return rect;
    }

    /**
     * 获取屏幕宽度
     *
     */
    public static int getScreenWidth() {
        return getScreenRect().width();
    }

    /**
     * 获取屏幕高度
     *
     */
    public static int getScreenHeight() {
        return getScreenRect().height();
    }

}
