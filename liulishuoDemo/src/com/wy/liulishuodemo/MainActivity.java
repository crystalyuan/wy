package com.wy.liulishuodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity implements OnClickListener{
	
	private Button forcast;
	private Button timeup;
	private Button picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initView();
        setListener();
    }
    private void initView() {
    	forcast = (Button) findViewById(R.id.forecast_btn);
    	timeup = (Button) findViewById(R.id.timeup_btn);
    	picture = (Button) findViewById(R.id.picture_btn);
    }
    
    private void setListener() {
    	forcast.setOnClickListener(this);
    	timeup.setOnClickListener(this);
    	picture.setOnClickListener(this);
    }
	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		case R.id.forecast_btn: {
			Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
			startActivity(intent);
			
			}
			break;
		case R.id.timeup_btn: {
			Intent intent = new Intent(MainActivity.this, TimeupActivity.class);
			startActivity(intent);
			
			}

			break;
		case R.id.picture_btn:{
			Intent intent = new Intent(MainActivity.this, PictureActivity.class);
			startActivity(intent);
			
			}

			break;

		default:
			break;
		}
	}

}
