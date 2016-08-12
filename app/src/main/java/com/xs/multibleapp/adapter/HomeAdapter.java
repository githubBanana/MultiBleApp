package com.xs.multibleapp.adapter;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xs.multibleapp.R;
import com.xs.multibleapp.bean.GridViewItemData;
import com.xs.multibleapp.widget.GridViewItem;

import java.util.List;

/**
 * Created by Administrator on 2015/8/25.
 *  主页适配器
 */
public class HomeAdapter extends BaseQuickAdapter<GridViewItemData> {

    public HomeAdapter(Context context, List<GridViewItemData> datas) {
        super(R.layout.item_main,datas);
    }

    @Override
    protected void convert(com.chad.library.adapter.base.BaseViewHolder baseViewHolder, GridViewItemData data) {
        GridViewItem gridViewItem = (GridViewItem) baseViewHolder.itemView;
        gridViewItem.setImg(data.resId);
        gridViewItem.setText(data.text);
    }
}
