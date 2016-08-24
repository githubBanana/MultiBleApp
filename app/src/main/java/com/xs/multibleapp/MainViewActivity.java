package com.xs.multibleapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.PullLoaderRecyclerView;
import com.xs.multibleapp.adapter.HomeAdapter;
import com.xs.multibleapp.bean.GridViewItemData;
import com.xs.multibleapp.ui.Hts2Activity;
import com.xs.multibleapp.ui.HtsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-12 16:53
 * @email Xs.lin@foxmail.com
 */
public class MainViewActivity extends AppCompatActivity implements BaseQuickAdapter.OnRecyclerViewItemClickListener{


    private List<GridViewItemData> _list;
    public final static String TITLE = "title";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainview_activity);
        final PullLoaderRecyclerView mGv = (PullLoaderRecyclerView) findViewById(R.id.recycler_view);
        _list = new ArrayList<>();

        _list.add(new GridViewItemData(getString(R.string.hts_title),R.mipmap.home_1));
        _list.add(new GridViewItemData(getString(R.string.ropeskip_title),R.mipmap.home_2));
        _list.add(new GridViewItemData(getString(R.string.sport_title),R.mipmap.home_3));
        _list.add(new GridViewItemData(getString(R.string.hts_title2),R.mipmap.home_4));
        _list.add(new GridViewItemData("5",R.mipmap.home_5));
        _list.add(new GridViewItemData("6",R.mipmap.home_6));

        mGv.setGridLayout(3);
        HomeAdapter adapter = new HomeAdapter(this,_list);
        adapter.setOnRecyclerViewItemClickListener(this);
        mGv.setAdapter(adapter);

    }

    @Override
    public void onItemClick(View view, int i) {
        GridViewItemData item = _list.get(i);
        if (getString(R.string.hts_title).equals(item.text)) {
            HtsActivity.runAct(this,getString(R.string.hts_title));
        } else if (getString(R.string.hts_title2).equals(item.text)) {
            Hts2Activity.runAct(this,getString(R.string.hts_title2));
        }
    }
}
