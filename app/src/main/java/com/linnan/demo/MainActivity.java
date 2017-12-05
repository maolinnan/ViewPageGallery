package com.linnan.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.linnan.viewpager.ViewPageGallery;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPageGallery viewPageGallery = (ViewPageGallery)findViewById(R.id.view_page_gallery);
        viewPageGallery.setViewSize(480,800);//设置卡片大小
        LinearLayout layout1 = new LinearLayout(this);
        LinearLayout layout2 = new LinearLayout(this);
        LinearLayout layout3 = new LinearLayout(this);
        layout1.setBackgroundColor(Color.RED);
        layout2.setBackgroundColor(Color.GREEN);
        layout3.setBackgroundColor(Color.BLUE);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        viewPageGallery.addView(layout1,lp);
        viewPageGallery.addView(layout2,lp);
        viewPageGallery.addView(layout3,lp);
    }
}
