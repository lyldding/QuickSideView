package com.lyldding.quicksideview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyldding
 * @date 2019/2/21
 */
public class SideView extends View {
    private static final String TAG = "SideLetterView";
    private List<String> mLetters;
    private List<Float> mCacheWidth;
    private Paint mPaint;
    private int mTextHeight;
    private int mInitWidth;
    private int mRadius = 0;
    private int mItemHeight;
    private float mCurrentY;
    private int mCurrentPosition = -1;
    private int mDefaultTextSize = 36;
    private float mDrawTextX;
    private boolean mIsTouching;
    private boolean mIsInit;
    private int mPaddingTop;
    private Interpolator mInterpolator;
    private int mSelectColor;
    private int mDefaultColor;


    private OnSelectedListener mOnSelectedListener;

    public SideView(Context context) {
        super(context);
        init(context);
    }

    public SideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SideView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setTextSize(mDefaultTextSize);

        mSelectColor = Color.RED;
        mDefaultColor = Color.BLACK;
        mInterpolator = new LinearInterpolator();
        initLettersInfo();
        mIsInit = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = getSize(widthMeasureSpec, 100);
        int heightSize = getSize(heightMeasureSpec, mTextHeight * mLetters.size() + 50);

        if (mIsInit) {
            mInitWidth = widthSize;
            mIsInit = false;
        }
        mPaddingTop = getPaddingTop();
        mItemHeight = (heightSize - getPaddingBottom() - mPaddingTop) / mLetters.size();
        setMeasuredDimension(mRadius + mInitWidth, heightSize);
    }

    private int getSize(int measureSpec, int defaultSize) {
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        if (mode != MeasureSpec.EXACTLY) {
            size = defaultSize;
        }
        return size;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int index = 0; index < mLetters.size(); index++) {
            updateDrawText(index);
            canvas.drawText(mLetters.get(index),
                    mDrawTextX,
                    mPaddingTop + mItemHeight * (index + 1) - (mItemHeight - mTextHeight) * 0.5f,
                    mPaint);
        }
    }

    /**
     * 更新绘制文字的属性
     *
     * @param index
     */
    private void updateDrawText(int index) {
        int textSize = mDefaultTextSize;
        mDrawTextX = mRadius + (mInitWidth - mCacheWidth.get(index)) * 0.5f;
        if (mIsTouching) {
            float scale = Math.abs(mCurrentY - index * mItemHeight) * 1f / mRadius;
            if (scale <= 1) {
                mDrawTextX = (mInterpolator.getInterpolation(scale)) * mRadius;
                textSize = (int) (mDefaultTextSize * (2 - scale));
            }
        }
        mPaint.setTextSize(textSize);
        mPaint.setColor(mCurrentPosition == index ? mSelectColor : mDefaultColor);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        Log.d(TAG, "onTouchEvent: action = " + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsTouching = true;
                mCurrentY = event.getY();
                mRadius = 300;
                notifyCurrentPosition();
                requestLayout();
                break;
            case MotionEvent.ACTION_MOVE:
                mIsTouching = true;
                mCurrentY = event.getY();
                notifyCurrentPosition();
                ViewCompat.postInvalidateOnAnimation(this);
                break;
            default:
                mRadius = 0;
                mIsTouching = false;
                mCurrentPosition = -1;
                requestLayout();
        }

        return true;
    }

    /**
     * 更新选择的位置
     */
    private void notifyCurrentPosition() {
        int oldPosition = mCurrentPosition;
        mCurrentPosition = (int) (mCurrentY / mItemHeight);
        if (mCurrentPosition < 0) {
            mCurrentPosition = 0;
        } else if (mCurrentPosition >= mLetters.size()) {
            mCurrentPosition = mLetters.size() - 1;
        }
        if (mOnSelectedListener != null && oldPosition != mCurrentPosition) {
            mOnSelectedListener.onSelected(mCurrentPosition, mLetters.get(mCurrentPosition));
        }
    }

    interface OnSelectedListener {
        /**
         * 选择内容
         *
         * @param currentPosition 位置
         * @param text            内容
         */
        void onSelected(int currentPosition, String text);
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        mOnSelectedListener = onSelectedListener;
    }

    /**
     * 设置快速选择内容
     *
     * @param letters
     */
    public void setLetters(List<String> letters) {
        mLetters.clear();
        mLetters.addAll(letters);
        if (mLetters.size() == 0) {
            throw new IllegalArgumentException("mLetters.size() == 0");
        }
        cacheLetterWidth();
        requestLayout();
    }

    /**
     * 初始化
     */
    private void initLettersInfo() {
        mLetters = new ArrayList<>();
        mCacheWidth = new ArrayList<>();
        for (char index = 'A'; index <= 'Z'; index++) {
            mLetters.add(String.valueOf(index));
        }
        cacheLetterWidth();
    }

    /**
     * 缓存每个类别的初始宽度
     */
    private void cacheLetterWidth() {
        mCacheWidth.clear();
        for (String letter : mLetters) {
            float width = mPaint.measureText(letter);
            mCacheWidth.add(width);
        }
        Rect rect = new Rect();
        mPaint.getTextBounds(mLetters.get(0), 0, mLetters.get(0).length(), rect);
        mTextHeight = rect.height();
    }

    /**
     * 设置插值器
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * 设置选中颜色
     *
     * @param selectColor
     */
    public void setSelectColor(int selectColor) {
        mSelectColor = selectColor;
    }

    /**
     * 设置默认颜色
     *
     * @param defaultColor
     */
    public void setDefaultColor(int defaultColor) {
        mDefaultColor = defaultColor;
    }
}
