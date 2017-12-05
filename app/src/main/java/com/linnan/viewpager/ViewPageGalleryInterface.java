package com.linnan.viewpager;

import android.graphics.Bitmap;

/**
 * ViewPageGallery提供的功能接口
 * @author maolinnan create date:2015/03/05
 */

public interface ViewPageGalleryInterface {
    void scrollToNextScreen();//跳转到下一卡片
    void scrollToLastScreen();//跳转到最后一个卡片
    void snapToScreen(int whichScreen);//跳转到指定卡片
    void setMinAlpha(float alpha);//设置最小的透明度
    void setMinScale(float scale);//设置最小的缩放率
    void setViewSize(int width, int height);//设置子视图的宽高
    void setEnablClickSelect(boolean enalbe);//是否启用点击滑动
    void setPageSelectedListenner(OnPageSelectedListenner listenner);//页面选择监听
    int getCurrentScreen();//获得当前所在屏
    void setShaderBitmap(Bitmap shaderBitmapL, Bitmap shaderBitmapR, int shaderW);//设置阴影图片及阴影宽度
    void setGap(int mGap);//设置卡片间距
}
