package com.org.tk.softlock.gesture;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.org.tk.softlock.R;

import java.util.ArrayList;
import java.util.List;

public class GestureLockViewGroup extends RelativeLayout {

    private static final String TAG = "GestureLockViewGroup";

    /**
     * 保留所有的GestureLockView
     */
    private GestureLockView[] mGestureLockViews;

    /**
     * 每个边上gestureLockView的个数
     */
    private int mCount = 3;

    /**
     * 存储答案
     */
    private int[] mAnswer={0,1,2,3};

    /**
     * 保持用户选中的GestureLockView的ID
     */
    private List<Integer> mChoose = new ArrayList<>();

    private Paint mPaint;

    /**
     * 每个GestureLockView中间的间距，设置为mGestureLockViewWidth * 25%
     */
    private int mMarginBetweenLockView = 30;

    /**
     * GestureLockView的边长 4 * mWidth / ( 5 * mCount + 1 )
     */
    private int mGestureLockViewWith;

    /**
     * GestureLockView无手指触摸的状态下内圆的颜色
     */
    private int mNoFingerInnerCircleColor = 0xFF939090;

    /**
     * GestureLockView无手指触摸的状态下外圆的颜色
     */
    private int mNoFingerOuterCircleColor = 0xFFE0DBDB;

    /**
     * GestureLockView手指触摸的状态下内圆和外圆的颜色
     */
    private int mFingerOnColor = 0xFF378FC9;

    /**
     * GestureLockView手指抬起的状态下内圆和外圆的颜色
     */
    private int mFingerUpColor = 0xFFFF0000;

    /**
     * 宽度
     */
    private int mWidth;
    /**
     * 高度
     */
    private int mHeight;

    private Path mPath;
    /**
     * 指引线的开始位置x
     */
    private int mLastPathX;
    /**
     * 指引线的开始位置y
     */
    private int mLastPathY;
    /**
     * 指引下的结束位置
     */
    private Point mTmpTarget = new Point();

    /**
     * 最大尝试次数
     */
    private int mTryTimes = 4;
    /**
     * 回调接口
     */
    private OnGestureLockViewListener mOnGestureLockViewListener;

    public GestureLockViewGroup(Context context) {
        super(context);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /**
         * 获得自定义的参数值
         */
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GestureLockViewGroup,defStyleAttr,0);
        int n=a.getIndexCount();
        for (int i=0;i<n;i++){
            int attr = a.getIndex(i);
            switch (attr){
                case R.styleable.GestureLockViewGroup_color_no_finger_inner_circle:
                    mNoFingerInnerCircleColor = a.getColor(attr,
                            mNoFingerInnerCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_no_finger_outer_circle:
                    mNoFingerOuterCircleColor = a.getColor(attr,
                            mNoFingerOuterCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_on:
                    mFingerOnColor = a.getColor(attr, mFingerOnColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_up:
                    mFingerUpColor = a.getColor(attr, mFingerUpColor);
                    break;
                case R.styleable.GestureLockViewGroup_count:
                    mCount = a.getInt(attr, 3);
                    break;
                case R.styleable.GestureLockViewGroup_tryTimes:
                    mTryTimes = a.getInt(attr, 5);
                default:
                    break;
            }
            a.recycle();
            //初始化画笔
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPath = new Path();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mHeight = mWidth = mWidth<mHeight?mWidth:mHeight;
        if (mGestureLockViews == null){
            //https://www.cnblogs.com/jjx2013/p/6223661.html
        }
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /**
     * 设置回调接口
     *
     * @param listener
     */
    public void setOnGestureLockViewListener(OnGestureLockViewListener listener)
    {
        this.mOnGestureLockViewListener = listener;
    }


    public interface OnGestureLockViewListener
    {
        /**
         * 单独选中元素的Id
         *
         * @param position
         */
        public void onBlockSelected(int cId);

        /**
         * 是否匹配
         *
         * @param matched
         */
        public void onGestureEvent(boolean matched);

        /**
         * 超过尝试次数
         */
        public void onUnmatchedExceedBoundary();
    }
}
