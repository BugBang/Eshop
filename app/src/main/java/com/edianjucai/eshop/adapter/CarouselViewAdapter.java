package com.edianjucai.eshop.adapter;

import android.view.View;

/**
 * Created by user on 2016-09-23.
 */
public interface CarouselViewAdapter {
    boolean isEmpty();
    View getView(int position);
    int getCount();
}
