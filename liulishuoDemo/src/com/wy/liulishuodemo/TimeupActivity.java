package com.wy.liulishuodemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

public class TimeupActivity extends Activity {
	private TextView tv;
	private TextView count;
	private ScaleAnimation scaleAnimation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeup);
		
		initView();
	}
	private void initView() {
		tv = (TextView) findViewById(R.id.tv);
		count = (TextView) findViewById(R.id.tv_count);
		
		initAnimation();
	}
	private void initAnimation() {
		AnimationSet animationSet = new AnimationSet(true);
		
		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f,  Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimation.setDuration(800);
		scaleAnimation.setRepeatCount(2);
		scaleAnimation.setFillAfter(true);
		scaleAnimation.setFillEnabled(true);
		Interpolator interpolator = new AnticipateInterpolator();
		scaleAnimation.setInterpolator(interpolator);
		
		
		final AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		alphaAnimation.setDuration(600);
		alphaAnimation.setFillAfter(true);
		
		scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
			int id = 3;
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				id--;
				count.setText(id + "");
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(getApplicationContext(), R.anim.set);
				tv.startAnimation(set);
				count.startAnimation(alphaAnimation);
			}
		});
		tv.startAnimation(animationSet);
	}

}
