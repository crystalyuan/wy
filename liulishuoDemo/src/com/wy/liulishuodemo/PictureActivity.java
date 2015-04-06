package com.wy.liulishuodemo;

import com.wy.http.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PictureActivity extends Activity {
	private Button start;
	private ImageView img1;
	private ImageView img2;
	private ImageView img3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		
		initView();
	}
	
	private void initView() {
		start = (Button) findViewById(R.id.start);
		img1 = (ImageView) findViewById(R.id.imageView1);
		img2 = (ImageView) findViewById(R.id.imageView2);
		img3 = (ImageView) findViewById(R.id.imageView3);
		
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ImageLoader.getInstance(getApplicationContext()).
					load(img1, "http://llss.qiniudn.com/forum/image/525d1960c008906923000001_1397820588.jpg", -1);
				ImageLoader.getInstance(getApplicationContext()).
				load(img2, "http://llss.qiniudn.com/forum/image/e8275adbeedc48fe9c13cd0efacbabdd_1397877461243.jpg", -1);
				ImageLoader.getInstance(getApplicationContext()).
				load(img3, "http://llss.qiniudn.com/uploads/forum/topic/attached_img/5350db2ffcfff258b500dcb2/_____2014-04-18___3.52.33.png", -1);
			}
		});
	}

}
