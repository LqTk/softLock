package com.org.tk.softlock.gesture;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.org.tk.softlock.LockApplication;
import com.org.tk.softlock.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

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
    private int[] mAnswer=new int[0];

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
    private int mGestureLockViewWidth;

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
    private int mFingerUpColor = 0xFF3700B3;

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

    /**
     * 计时清除线
     */
    Handler handler = new Handler();

    Runnable clearPath = new Runnable() {
        @Override
        public void run() {
            reset();
            invalidate();
        }
    };
    private OnGestureLockViewListener mOnGestureLockViewListener;

    public GestureLockViewGroup(Context context) {
        super(context);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs,0);
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
        }
        a.recycle();
        //初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mHeight = mWidth = mWidth<mHeight?mWidth:mHeight;

        //初始化mGestureLockView
        if (mGestureLockViews == null){
            mGestureLockViews = new GestureLockView[mCount*mCount];
            //计算每个gestureLockView的宽度
            mGestureLockViewWidth = (int) (4 * mWidth*1.0f/(5 * mCount +1));
            //计算每个gestureLockView的间距
            mMarginBetweenLockView = (int) (mGestureLockViewWidth*0.25);
            //设置画笔的宽度为gestureLockView的内圆直径稍微小点
            mPaint.setStrokeWidth(mGestureLockViewWidth *0.29f);

            for (int i=0;i<mGestureLockViews.length;i++){
                mGestureLockViews[i] = new GestureLockView(getContext(),
                        mNoFingerInnerCircleColor,mNoFingerOuterCircleColor,
                        mFingerOnColor,mFingerUpColor);
                mGestureLockViews[i].setId(i+1);
                //设置参数，主要是定位gestureLockView间的位置
                RelativeLayout.LayoutParams lockParams = new RelativeLayout.LayoutParams(mGestureLockViewWidth, mGestureLockViewWidth);

                //不是每行的第一个，则设置位置为前一个的右边
                if (i%mCount !=0){
                    //lockParams.addRule(RelativeLayout.RIGHT_OF,mGestureLockViews[i-1].getId())
                    // 就表示RelativeLayout中的相应节点放置在一个id值为date的兄弟节点的右边。
                    lockParams.addRule(RelativeLayout.RIGHT_OF,mGestureLockViews[i-1].getId());
                }
                //从第二行开始，设置为上一行同一位置View的下面
                if (i>mCount-1){
                    lockParams.addRule(RelativeLayout.BELOW,mGestureLockViews[i-mCount].getId());
                }
                //设置右下左上的边距
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMargin = 0;
                int topMargin = 0;
                /**
                 * 每个View都有右外边距和底外边距 第一行有上外边距，第一列有左外边距
                 */
                if (i>=0 && i<mCount){//第一行
                    topMargin = mMarginBetweenLockView;
                }
                if (i%mCount==0){//第一列
                    leftMargin = mMarginBetweenLockView;
                }
                lockParams.setMargins(leftMargin,topMargin,rightMargin,bottomMargin);
                mGestureLockViews[i].setMode(GestureLockView.Mode.STATUS_NO_FINGER);
                addView(mGestureLockViews[i],lockParams);
                Log.d(TAG, "mWidth = " + mWidth + " ,  mGestureViewWidth = "
                        + mGestureLockViewWidth+ " , mMarginBetweenLockView = "
                        + mMarginBetweenLockView);
            }
        }
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x= (int) event.getX();
        int y = (int) event.getY();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(clearPath);
                reset();
                break;
            case MotionEvent.ACTION_MOVE:
                mPaint.setColor(mFingerOnColor);
                mPaint.setAlpha(50);
                GestureLockView child = getChildIdByPos(x,y);
                if (child!=null){
                    int cId = child.getId();
                    if (!mChoose.contains(cId)){
                        mChoose.add(cId);
                        child.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                        if (mOnGestureLockViewListener != null){
                            mOnGestureLockViewListener.onBlockSelected(cId);
                        }
                        //设置指引线的起点
                        mLastPathX = child.getLeft() /2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;

                        if (mChoose.size() == 1){//添加了第一个
                            mPath.moveTo(mLastPathX,mLastPathY);
                        }else {
                            mPath.lineTo(mLastPathX,mLastPathY);
                        }
                    }
                }
                //指引线的终点
                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:
                if (checkAnswer()){
                    mPaint.setColor(Color.parseColor("#159F15"));
                    resetFingerUpColor(0xff159F15);
                }else {
                    if (mAnswer!=null && mAnswer.length>0){
                        mPaint.setColor(0xFFFF0000);
                        resetFingerUpColor(0xFFFF0000);
                    }else {
                        mPaint.setColor(mFingerUpColor);
                        resetFingerUpColor(mFingerUpColor);
                    }
                }
                mPaint.setAlpha(50);
                this.mTryTimes--;

                SharedPreferences preference = getContext().getSharedPreferences(LockApplication.softPre,MODE_PRIVATE);
                boolean lastError = preference.getBoolean(LockApplication.lastError, false);
                //回调是否成功
                if (mOnGestureLockViewListener != null && mChoose.size()>0){
                    if (lastError){
                        long lastTime = preference.getLong(LockApplication.lastTime,System.currentTimeMillis());
                        if (System.currentTimeMillis()-lastTime>LockApplication.lockTime){
                            preference.edit().putBoolean(LockApplication.lastError,false).apply();
                            if (this.mTryTimes > 0) {
                                mOnGestureLockViewListener.onGestureEvent(checkAnswer(), mAnswer);
                            } else {
                                mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                            }
                        }else {
                            mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                        }
                    }else {
                        if (this.mTryTimes > 0) {
                            mOnGestureLockViewListener.onGestureEvent(checkAnswer(), mAnswer);
                        } else {
                            preference.edit().putBoolean(LockApplication.lastError,true).apply();
                            preference.edit().putLong(LockApplication.lastTime,System.currentTimeMillis()).apply();
                            mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                        }
                    }
                }
                Log.e(TAG,"mUnMatchExceedBoundary = "+mTryTimes);
                Log.e(TAG,"mChoose = "+mChoose);
                //将终点设置位置为起点，即取消指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                //改变子元素的状态为UP
                changeItemMode();

                //计算每个元素中箭头需要旋转的角度
                for (int i=0;i+1<mChoose.size();i++){
                    int childId = mChoose.get(i);
                    int nextChildId = mChoose.get(i+1);

                    GestureLockView startChild = findViewById(childId);
                    GestureLockView nextChild = findViewById(nextChildId);

                    int dx = nextChild.getLeft() - startChild.getLeft();
                    int dy = nextChild.getTop() - startChild.getTop();
                    //计算角度
                    int angle = (int) Math.toDegrees(Math.atan2(dy,dx))+90;
                    startChild.setArrowDegree(angle);
                }
                handler.postDelayed(clearPath,2000);
                break;
        }
        invalidate();
        return true;
    }

    private void changeItemMode() {
        for (GestureLockView gestureLockView:mGestureLockViews){
            if (mChoose.contains(gestureLockView.getId())){
                gestureLockView.setMode(GestureLockView.Mode.STATUS_FINGER_UP);
            }
        }
    }

    /**
     * 检查用户绘制的手势是否正确
     * @return
     */
    private boolean checkAnswer() {
        if (mAnswer.length != mChoose.size()){
            return false;
        }
        for (int i=0;i<mAnswer.length;i++){
            if (mAnswer[i] != mChoose.get(i)){
                return false;
            }
        }
        return true;
    }

    private void resetFingerUpColor(int color){
        for (GestureLockView gestureLockView:mGestureLockViews){
            gestureLockView.setmColorFingerUp(color);
        }
    }
    /**
     *  通过x,y获得落入的GestureLockView
     * @param x
     * @param y
     * @return
     */
    private GestureLockView getChildIdByPos(int x, int y) {
        for (GestureLockView gestureLockView:mGestureLockViews){
            if (checkPositionInChild(gestureLockView,x,y)){
                return gestureLockView;
            }
        }
        return null;
    }

    /**
     * 检查当前左边是否在child中
     * @param child
     * @param x
     * @param y
     * @return
     */
    private boolean checkPositionInChild(GestureLockView child, int x, int y) {
        //设置了内边距，即x,y必须落入下GestureLockView的内部中间的小区域中，可以通过调整padding使得x,y落入范围不变大，或者不设置padding
        int padding = (int) (mGestureLockViewWidth*0.15);

        if (x>=child.getLeft() + padding && x<=child.getRight() - padding
        && y>=child.getTop()+padding
        && y<= child.getBottom()-padding){
            return true;
        }
        return false;
    }

    //重置
    private void reset(){
        mChoose.clear();
        mPath.reset();
        for (GestureLockView gestureLockView:mGestureLockViews){
            gestureLockView.setMode(GestureLockView.Mode.STATUS_NO_FINGER);
            gestureLockView.setArrowDegree(-1);
            gestureLockView.setmColorFingerUp(mFingerUpColor);
        }
    }

    /**
     * 对外公布设置答案的方法
     * @param mAnswer
     */
    public void setmAnswer(int[] mAnswer) {
        this.mAnswer = mAnswer;
    }

    /**
     * 设置最大尝试次数
     * @param mTryTimes
     */
    public void setmTryTimes(int mTryTimes) {
        this.mTryTimes = mTryTimes;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //绘制gestrueLockView间的连线
        if (mPath != null){
            canvas.drawPath(mPath,mPaint);
        }
        //绘制指引线
        if (mChoose.size()>=0){
            if (mLastPathX != 0 && mLastPathY != 0){
                canvas.drawLine(mLastPathX,mLastPathY,mTmpTarget.x,mTmpTarget.y,mPaint);
            }
        }
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
         */
        public void onBlockSelected(int cId);

        /**
         * 是否匹配
         *
         * @param matched
         */
        public void onGestureEvent(boolean matched, int[] password);

        /**
         * 超过尝试次数
         */
        public void onUnmatchedExceedBoundary();
    }
}
