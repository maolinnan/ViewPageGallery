package com.linnan.viewpager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 类似照片展示的一个View
 * 支持设置展未框的尺寸、透明度变化、缩放、以及点击选中功能
 * @author maolinnan create date:2015/03/05
 */
public class ViewPageGallery extends ViewGroup implements ViewPageGalleryInterface{
    private static final int SNAP_VELOCITY = 600;//默认速率
    private static final int MOVE_MIX_DISTANCE = 5;//滑动最小距离
    private static final int ALPHA_MAX_VALUE = 255;//透明度最大值

    private Paint paint = new Paint();
    private VelocityTracker velocityTracker;//滑动速率计算
    private OnPageSelectedListenner onPageSelectedListenner;//页面选择监听
    private Scroller scroller;//视图的滚动控制
    private int myselfHeight = 0;//自身View高度
    /*卡片状态相关*/
    private int childViewWidth,childViewHeight;//子视图的宽和高
    private float childViewScale = 0.95f;//左右滑动时子视图的缩放比例
    private float childViewAlpha = 0.7f;//非主卡片的透明度
    private int curScreen = 0;//当前所在屏的下标
    private int childViewGap = 0;//卡片间距
    private boolean enableClickSelect = true;//是否允许点击滑动切换卡片
    private boolean isClick = true;//是否可以点击
    /*点击相关*/
    private float lastMotionX;//最后移动的坐标
    private float downX;//点击的时候的X坐标
    private long downTime = 0;//记录按下时间
    private float interceptDownX,interceptDownY;//拦截按下X,Y的坐标
    /*阴影相关*/
    private Bitmap shaderBitmapLeft,shaderBitmapRight;//左右阴影图
    private int shaderWidth;//阴影宽度
    private int shaderTop = 0;//阴影离顶部距离

    private boolean isFirstDispatch = true;//是否第一次绘制第一次绘制滚动到第一屏
    private boolean isClickCurrent = true;//是否点击当前屏

    /**
     * 构造方法
     * @param context
     */
    public ViewPageGallery(Context context) {
        super(context);
        scroller = new Scroller(context);
        setOnClickListener(null);
    }
    public ViewPageGallery(Context context, AttributeSet attribute) {
        super(context, attribute);
        scroller = new Scroller(context);
        setOnClickListener(null);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //适配通知栏和导航栏高度变化
        if (myselfHeight == 0){
            myselfHeight = getHeight();
        }else{
            int newHeight = getHeight();
            if (myselfHeight != newHeight){
                childViewHeight += (newHeight - myselfHeight);
                myselfHeight = newHeight;
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    child.invalidate();
                    child.requestLayout();
                }
                return;
            }
        }
        int count = getChildCount();
        int boder = ((b - t) - childViewHeight) / 2;
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            int left = i * (childViewWidth + childViewGap * 2) + childViewGap;
            int right = left + childViewWidth;
            int top = boder;
            int bottom = top + childViewHeight;
            view.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(childViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childViewHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                interceptDownX = event.getX();
                interceptDownY = event.getY();
                if (isTouchCurrentView(event)) {
                    isClickCurrent = false;
                } else {
                    isClickCurrent = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(interceptDownY-event.getY()) > Math.abs(interceptDownX-event.getX())) return false;
                if (Math.abs(interceptDownX - event.getX()) > MOVE_MIX_DISTANCE || !isTouchCurrentView(event)) {
                    isClickCurrent = true;
                }
                break;
        }
        return isClickCurrent;
    }

    /**
     * 是否是点击当前卡片
     * @param event
     * @return
     */
    private boolean isTouchCurrentView(MotionEvent event) {
        View view = getChildAt(curScreen);
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        RectF rect = new RectF(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        boolean isInViewRect = rect.contains(event.getRawX(), event.getRawY());
        return isInViewRect;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN://点击
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                lastMotionX = x;
                isClick = true;
                downTime = System.currentTimeMillis();
                downX = x;
                break;

            case MotionEvent.ACTION_MOVE://移动
                int deltaX = (int) (lastMotionX - x);
                if (deltaX > MOVE_MIX_DISTANCE || deltaX < -MOVE_MIX_DISTANCE) {
                    isClick = false;
                }

                lastMotionX = x;

                int dstX = getScrollX() + deltaX;
                // 到达第一页
                int viewRange = childViewWidth + childViewGap * 2;
                int minX = (getWidth() - viewRange) / 2;
                if (dstX <= -minX - viewRange / 2) {
                    break;
                }
                // 到达最后一页
                int maxX = viewRange * (getChildCount() - 1) - (getWidth() - viewRange) / 2
                        + viewRange / 2;
                if (dstX >= maxX) {
                    break;
                }

                scrollBy(deltaX, 0);
                break;

            case MotionEvent.ACTION_UP://抬起
            case MotionEvent.ACTION_CANCEL://清除
                velocityTracker.computeCurrentVelocity(1000);

                if (enableClickSelect && isClick) {
                    long time = System.currentTimeMillis();
                    if (time - downTime < 1000) {
                        clickToScreen((int)  downX);
                    } else {
                        snapToScreen();
                    }
                } else {
                    int velocityX = (int) velocityTracker.getXVelocity();
                    if (velocityX > SNAP_VELOCITY && curScreen > 0) {
                        scrollToLastScreen();
                    } else if (velocityX < -SNAP_VELOCITY && curScreen < getChildCount() - 1) {
                        scrollToNextScreen();
                    } else {
                        snapToScreen();
                    }
                }

                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                downX = -1;
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        float parentCentX = getWidth() / 2;//父视图中心位置

        if (isFirstDispatch) {//第一次滚动
            isFirstDispatch = false;
            snapToScreen();
        }
        for (int i = 0; i < getChildCount(); i++) {
            canvas.save();//保存canvas层
            View view = getChildAt(i);
            float childViewCentX = (view.getLeft() - getScrollX()) + view.getWidth() / 2;//子视图中心位置
            float detalX = childViewCentX - parentCentX;//中心位置差值
            //计算缩放值
            float scale = Math.abs(detalX) / 200f * (1 - childViewScale);
            scale = scale > (1 - childViewScale) ? (1 - childViewScale) : scale;
            scale = 1 - scale;
            //计算透明度
            float alpha = Math.abs(detalX) / 200f * (1 - childViewAlpha);
            alpha = alpha > (1 - childViewAlpha) ? (1 - childViewAlpha) : alpha;
            alpha = (1 - alpha) * ALPHA_MAX_VALUE;
            RectF rect = new RectF();
            rect.left = 0;
            rect.right = getWidth();
            rect.top = 0;
            rect.bottom = getHeight();
            Matrix matrix = new Matrix();
            matrix.preScale(scale, scale);
            matrix.preTranslate(-view.getWidth() / 2, -view.getHeight() / 2);
            matrix.postTranslate(view.getLeft() + view.getWidth() / 2, view.getTop() + view.getHeight() / 2);
            canvas.concat(matrix);
            canvas.saveLayerAlpha(rect, (int) alpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            view.draw(canvas);
            canvas.restore();
            canvas.restore();
        }
        drawLRshader(canvas);
    }


    /**
     * 绘制左右阴影
     * @param canvas
     */
    private void drawLRshader(Canvas canvas) {
        if (shaderBitmapLeft != null && shaderBitmapRight != null) {
            canvas.save();
            float scale = getScale(0);
            canvas.translate(getScrollX(), 0);

            Rect src = new Rect();
            Rect dst = new Rect();
            src.left = 0;
            src.top = 0;
            src.bottom = shaderBitmapLeft.getHeight();
            src.right = shaderBitmapLeft.getWidth();
            shaderTop = (int) ((getHeight() - childViewHeight * scale) / 2f);
            dst.top = shaderTop - 10;
            dst.bottom = dst.top + (int) (childViewHeight * scale) + 20;
            dst.left = 0;
            dst.right = shaderWidth;
            paint.setAlpha(getAlpha(true));
            canvas.drawBitmap(shaderBitmapLeft, src, dst, paint);

            src.left = shaderBitmapLeft.getWidth();
            src.right = 0;

            scale = getScale(getChildCount() - 1);
            shaderTop = (int) (getHeight() - childViewHeight * scale) / 2;
            src.left = 0;
            src.top = 0;
            src.bottom = shaderBitmapRight.getHeight();
            src.right = shaderBitmapRight.getWidth();
            dst.top = shaderTop - 10;
            dst.bottom = dst.top + (int) (childViewHeight * scale) + 20;
            dst.left = getWidth() - shaderWidth;
            dst.right = getWidth();
            paint.setAlpha(getAlpha(false));
            canvas.drawBitmap(shaderBitmapRight, src, dst, paint);
            canvas.restore();
        }
    }

    /**
     * 获取当前透明度值
     * @param isleft
     * @return
     */
    private int getAlpha(Boolean isleft) {
        float alpha = 1f;
        if (isleft) {
            alpha = -getScrollX() / (float) shaderWidth;
            alpha = alpha < 0 ? 0 : alpha;
            alpha = alpha > 1 ? 1 : alpha;
        } else {
            alpha = (getWidth() - (getChildCount() * childViewWidth - getScrollX())) / (float) shaderWidth;
            alpha = alpha < 0 ? 0 : alpha;
            alpha = alpha > 1 ? 1 : alpha;
        }
        alpha = 1 - alpha;
        return (int) (alpha * 255);
    }

    /**
     * 获取缩放值
     * @param which
     * @return
     */
    private float getScale(int which) {
        if (which < 0 || which >= getChildCount())
            return 1;
        float scale;
        float parentCentX = getWidth() / 2;
        View view = getChildAt(which);
        float childViewCentX = (view.getLeft() - getScrollX()) + view.getWidth() / 2;
        float detalX = childViewCentX - parentCentX;

        scale = Math.abs(detalX) / 200f * (1 - childViewScale);
        scale = scale > (1 - childViewScale) ? (1 - childViewScale) : scale;
        scale = 1 - scale;
        return scale;
    }

    /**
     * 跳转到当前卡片
     */
    private void snapToScreen() {
        int scroll = getScrollX();
        int viewRange = childViewWidth + childViewGap * 2;
        int which = (scroll + getWidth() / 2) / viewRange;
        curScreen = which;
        int dest = viewRange * which + viewRange / 2 - getWidth() / 2;
        scroller.startScroll(scroll, 0, -(scroll - dest), 0);
        invalidate();
    }
    /**
     * 点击跳转卡片
     * @param downX
     */
    private void clickToScreen(int downX) {
        int scroll = getScrollX() + downX;
        int viewRange = childViewWidth + childViewGap * 2;
        int which = scroll / viewRange;
        which = which < 0 ? 0 : which;
        which = which >= getChildCount() ? getChildCount() - 1 : which;

        curScreen = which;
        int dest = viewRange * which + viewRange / 2 - getWidth() / 2;
        scroller.startScroll(getScrollX(), 0, -(getScrollX() - dest), 0);
        invalidate();
    }
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
            // 判断滑动是否停止
            if (scroller.isFinished()) {
                if (onPageSelectedListenner != null) {
                    onPageSelectedListenner.onUpdate(curScreen);
                }
            }
        }
    }

    /**
     * 跳转到下一卡片
     */
    @Override
    public void scrollToNextScreen() {
        int viewRange = childViewWidth + childViewGap * 2;
        int which = curScreen + 1;
        which = which < 0 ? 0 : which;
        which = which >= getChildCount() ? getChildCount() - 1 : which;
        curScreen = which;
        int dest = viewRange * which + viewRange / 2 - getWidth() / 2;
        scroller.startScroll(getScrollX(), 0, -(getScrollX() - dest), 0);
        invalidate();
    }

    /**
     * 跳转到最后一个卡片
     */
    @Override
    public void scrollToLastScreen() {
        int viewRange = childViewWidth + childViewGap * 2;
        int which = curScreen - 1;
        which = which < 0 ? 0 : which;
        which = which >= getChildCount() ? getChildCount() - 1 : which;
        curScreen = which;
        int dest = viewRange * which + viewRange / 2 - getWidth() / 2;
        scroller.startScroll(getScrollX(), 0, -(getScrollX() - dest), 0);
        invalidate();
    }

    /**
     * 跳转到指定卡片
     * @param whichScreen
     */
    @Override
    public void snapToScreen(int whichScreen) {
        int viewRange = childViewWidth + childViewGap * 2;
        int x = viewRange * whichScreen + viewRange / 2 - getWidth() / 2;
        scrollTo(x, 0);
        curScreen = whichScreen;
        if (onPageSelectedListenner != null) {
            onPageSelectedListenner.onUpdate(curScreen);
        }
        invalidate();
    }

    /**
     * 设置最小的透明度
     */
    @Override
    public void setMinAlpha(float alpha) {
        childViewAlpha = alpha;
    }

    /**
     * 设置最小的缩放率
     */
    @Override
    public void setMinScale(float scale) {
        childViewScale = scale;
    }

    /**
     * 设置子视图的宽高
     * @param widht
     * @param height
     */
    @Override
    public void setViewSize(int widht, int height) {
        childViewWidth = widht;
        childViewHeight = height;
    }

    /**
     * 是否启用点击滑动
     */
    @Override
    public void setEnablClickSelect(boolean enalbe) {
        enableClickSelect = enalbe;
    }

    /**
     * 页面选择监听
     * @param listenner
     */
    @Override
    public void setPageSelectedListenner(OnPageSelectedListenner listenner) {
        onPageSelectedListenner = listenner;
    }

    /**
     * 获得当前所在屏
     * @return
     */
    @Override
    public int getCurrentScreen() {
        return curScreen;
    }

    /**
     * 设置阴影图片
     * @param shaderBitmapL
     * @param shaderBitmapR
     * @param shaderW
     */
    @Override
    public void setShaderBitmap(Bitmap shaderBitmapL, Bitmap shaderBitmapR, int shaderW) {
        shaderBitmapLeft = shaderBitmapL;
        shaderBitmapRight = shaderBitmapR;
        shaderWidth = shaderW;
    }

    /**
     * 设置卡片间距
     * @param mGap
     */
    @Override
    public void setGap(int mGap) {
        this.childViewGap = mGap / 2;
    }
}
