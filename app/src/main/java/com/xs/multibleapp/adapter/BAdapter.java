package com.xs.multibleapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-08-12 16:41
 * @email Xs.lin@foxmail.com
 */
public abstract class BAdapter<T> extends RecyclerView.Adapter{

    private Object mLock = new Object();

    private List<T> mList;

    protected BAdapter() {
        mList = new ArrayList<>();
    }

    protected List<T> getListData() {
        return mList;
    }

    public void add(int position, T item) {
        synchronized (mLock) {
            getListData().add(position, item);
            notifyItemInserted(position);
        }
    }

    public boolean add(T item) {
        synchronized (mLock) {
            if (getListData().add(item)) {
                int position = getListData().size();
                notifyItemInserted(position - 1);
                return true;
            }
            return false;
        }
    }

    public boolean addAll(T... items) {
        synchronized (mLock) {
            int size = getListData().size();
            if (Collections.addAll(getListData(), items)) {
                notifyItemRangeInserted(size, items.length);
                return true;
            }
            return false;
        }
    }

    public boolean addAll(Collection<T> items) {
        synchronized (mLock) {
            int size = getListData().size();
            if (getListData().addAll(items)) {
                notifyItemRangeInserted(size, items.size());
                return true;
            }
            return false;
        }
    }

    public boolean addAll(int position, Collection<T> items) {
        synchronized (mLock) {
            if (getListData().addAll(position, items)) {
                notifyItemRangeInserted(position, items.size());
                return true;
            }
            return false;
        }
    }

    public T getItem(int position) {
        return getListData().get(position);
    }


    public void clear() {
        synchronized (mLock) {
            int size = getItemCount();
            getListData().clear();
            notifyItemRangeRemoved(0, size);
        }

    }

    public void setData(Collection<T> items) {
        synchronized (mLock) {
            clear();
            addAll(items);
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public int getItemCount() {
        return getListData().size();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View view, T t, int position);
    }





}
