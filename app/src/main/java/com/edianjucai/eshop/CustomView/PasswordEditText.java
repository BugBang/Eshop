package com.edianjucai.eshop.CustomView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.edianjucai.eshop.R;

/**
 * Created by user on 2016-09-21.
 */
public class PasswordEditText extends EditText implements View.OnFocusChangeListener, TextWatcher {

    private Drawable mClearDrawable;
    private boolean hasFocus;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
//        setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        // getCompoundDrawables() Returns drawables for the left(0), top(1), right(2) and bottom(3)
        mClearDrawable = getCompoundDrawables()[2]; // 获取drawableRight
        if (mClearDrawable == null) {
            // 如果为空，即没有设置drawableRight，则使用R.mipmap.close这张图片
            mClearDrawable = getResources().getDrawable(R.mipmap.login_eyes);
        }
        mClearDrawable.setBounds(0, 0,(int)(mClearDrawable.getIntrinsicWidth() *0.7),(int)(mClearDrawable.getIntrinsicHeight() *0.7));
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
        // 默认隐藏图标
        setDrawableVisible(false);
    }


    private boolean mIsPass = true;
    /**
     * 直接给EditText设置点击事件，通过按下的位置来模拟点击事件
     * 当我们按下的位置在图标包括图标到控件右边的间距范围内均算有效
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                int start = getWidth() - getTotalPaddingRight() + getPaddingRight(); // 起始位置
                int end = getWidth(); // 结束位置
                boolean available = (event.getX() > start) && (event.getX() < end);
                if (available) {
                    if (mIsPass) {
                        setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        setSelection(getText().length());
                        mIsPass = !mIsPass;
                    } else {
                        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        setSelection(getText().length());
                        mIsPass = !mIsPass;
                    }

                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (hasFocus && getText().length() > 0) {
            setDrawableVisible(true); // 有焦点且有文字时显示图标
        } else {
            setDrawableVisible(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (hasFocus) {
            setDrawableVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    protected void setDrawableVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

}
