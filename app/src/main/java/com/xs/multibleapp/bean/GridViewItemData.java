package com.xs.multibleapp.bean;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-12 16:44
 * @email Xs.lin@foxmail.com
 */
public class GridViewItemData {
    public int id;
    /**
     * 文本内容
     */
    public String text;
    /**
     * 对应的icon图片
     */
    public int resId;

    public GridViewItemData(String text, int resId) {
        this.text = text;
        this.resId = resId;
    }
}
