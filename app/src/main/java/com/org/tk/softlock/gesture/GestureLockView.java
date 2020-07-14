package com.org.tk.softlock.gesture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class GestureLockView extends View {

    private static final String TAG = "GestureLockView";

    /**
     * gestrueLockView的三种状态
     */
    enum Mode{
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP;
    }

    /**
     * gestrueLockView的当前状态
     */
    private Mode mCurrentStatus = Mode.STATUS_NO_FINGER;

    /**
     * 宽度
     */
    private int mWidth;

    /**
     * 高度
     */
    private int mHeight;

    /**
     * 外圆半径
     */
    private int mRadius;

    /**
     * 画笔宽度
     */
    private int mStrockWidth = 2;

    /**
     * 圆心坐标
     */
    private int mCenterX;
    private int mCenterY;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 箭头（小三角最长边的一半长度 = mArrowRate*mWidth/2）
     */
    private float mArrowRate = 0.333f;
    private int mArrowDegree = -1;
    private Path mArrowPath;

    /**
     * 内圆半径 = mInnerCircleRadiusRate * mRadius
     */
    private float mInnerCircleRadiusRate = 0.3f;

    /**
     * 四个颜色，可由用户自定义，初始化时GestureLockViewGroup传入
     */
    private int mColorNoFingerInner;
    private int mColorNoFingerOuter;
    private int mColorFingerOn;
    private int mColorFingerUp;

    public GestureLockView(Context context, int mColorNoFingerInner, int mColorNoFingerOuter, int mColorFingerOn, int mColorFingerUp) {
        super(context);
        this.mColorNoFingerInner = mColorNoFingerInner;
        this.mColorNoFingerOuter = mColorNoFingerOuter;
        this.mColorFingerOn = mColorFingerOn;
        this.mColorFingerUp = mColorFingerUp;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        //去长和高的最小值
        mWidth = mWidth<mHeight?mWidth:mHeight;
        mRadius = mCenterX = mCenterY = mWidth/2;
        mRadius -=mStrockWidth/2;

        //绘制三角形，初始时是默认箭头朝上的一个等腰三角形，用户绘制结束后，根据两个GestureLockView决定旋转多少度
        float mArrowLength = mWidth/2*mArrowRate;
        mArrowPath.moveTo(mWidth/2,mStrockWidth+2);
        mArrowPath.lineTo(mWidth/2 - mArrowLength,mStrockWidth+2+mArrowLength);
        mArrowPath.lineTo(mWidth/2+mArrowLength,mStrockWidth+2+mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentStatus){
            case STATUS_FINGER_ON:
                //绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorFingerOn);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX,mCenterY,mRadius,mPaint);
                //绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX,mCenterY,mRadius*mInnerCircleRadiusRate,mPaint);
                break;
            case STATUS_FINGER_UP:
                //绘制外圆
                mPaint.setColor(mColorFingerUp);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX,mCenterY,mRadius,mPaint);
                //绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX,mCenterY,mRadius*mInnerCircleRadiusRate,mPaint);

                drawArrow(canvas);
                break;
            case STATUS_NO_FINGER:
                //绘制外圆
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mColorNoFingerOuter);
                canvas.drawCircle(mCenterX,mCenterY,mRadius,mPaint);
                //绘制内圆
                mPaint.setColor(mColorNoFingerInner);
                canvas.drawCircle(mCenterX,mCenterY,mRadius*mInnerCircleRadiusRate,mPaint);
                break;
        }
    }

    /**
     * 绘制箭头
     * @param canvas
     */
    private void drawArrow(Canvas canvas) {
        if (mArrowDegree != -1){
            mPaint.setStyle(Paint.Style.FILL);

            canvas.save();
            canvas.rotate(mArrowDegree,mCenterX,mCenterY);
            canvas.drawPath(mArrowPath,mPaint);

            canvas.restore();
        }
    }

    /**
     * 设置当前模式并重绘界面
     */
    public void setMode(Mode mode){
        this.mCurrentStatus = mode;
        invalidate();
    }

    public void setArrowDegree(int mArrowDegree) {
        this.mArrowDegree = mArrowDegree;
    }

    public void setmColorFingerUp(int mColorFingerUp) {
        this.mColorFingerUp = mColorFingerUp;
    }

    public int getArrowDegree() {
        return mArrowDegree;
    }
}
