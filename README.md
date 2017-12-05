# ViewPageGallery
不用support包实现的ViewPageGallery效果

方便没导入supportV4包的项目实现类似功能,在ViewPageGalleryInterface中提供了便捷的接口给调用者</br>
```
　　void scrollToNextScreen();//跳转到下一卡片</br>
　　void scrollToLastScreen();//跳转到最后一个卡片</br>
　　void snapToScreen(int whichScreen);//跳转到指定卡片</br>
　　void setMinAlpha(float alpha);//设置最小的透明度</br>
　　void setMinScale(float scale);//设置最小的缩放率</br>
　　void setViewSize(int width, int height);//设置子视图的宽高</br>
　　void setEnablClickSelect(boolean enalbe);//是否启用点击滑动</br>
　　void setPageSelectedListenner(OnPageSelectedListenner listenner);//页面选择监听</br>
　　int getCurrentScreen();//获得当前所在屏</br>
　　void setShaderBitmap(Bitmap shaderBitmapL, Bitmap shaderBitmapR, int shaderW);//设置阴影图片及阴影宽度</br>
　　void setGap(int mGap);//设置卡片间距</br>
```
要使用的话只需要</br>
1、找到这个自定义View</br>
　　ViewPageGallery viewPageGallery = (ViewPageGallery)findViewById(R.id.view_page_gallery);</br>
2、设置childview 大小</br>
　　viewPageGallery.setViewSize(480,800);</br>
3、往ViewPageGallery里添加子View</br>

</br></br></br>
Viewpagegallery effects implemented without supportV4 packages !
ViewPageGalleryInterface Provide a convenient interface:
```
　　void scrollToNextScreen();//goto next card</br>
　　void scrollToLastScreen();//goto last card</br>
　　void snapToScreen(int whichScreen);//goto specify card</br>
　　void setMinAlpha(float alpha);//set min alpha</br>
　　void setMinScale(float scale);//set min scale</br>
　　void setViewSize(int width, int height);//set childview width and height</br>
　　void setEnablClickSelect(boolean enalbe);//set enable click</br>
　　void setPageSelectedListenner(OnPageSelectedListenner listenner);//pageselect listeener</br>
　　int getCurrentScreen();//get current ccreen</br>
　　void setShaderBitmap(Bitmap shaderBitmapL, Bitmap shaderBitmapR, int shaderW);//set shader bitmap and shader wudth</br>
　　void setGap(int mGap);//set card gap</br>
```
step1:find this view </br>
　　ViewPageGallery viewPageGallery = (ViewPageGallery)findViewById(R.id.view_page_gallery);</br>
step2:set childview size</br>
　　viewPageGallery.setViewSize(480,800);</br>
step3:ViewPageGallery add child view</br>
