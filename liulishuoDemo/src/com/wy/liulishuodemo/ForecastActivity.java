package com.wy.liulishuodemo;

import org.json.JSONException;
import org.json.JSONObject;

import com.wy.http.NetController;
import com.wy.http.NetHandler;
import com.wy.utils.FileUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.TextView;

public class ForecastActivity extends Activity {
	private TextView city;
	private TextView weather;
	
	private Handler mHandler = new NetHandler(){
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case NetHandler.OK:
				String response = (String) msg.obj;
				try {
					JSONObject jsonObj = new JSONObject(response);
					JSONObject forecastObj = jsonObj.getJSONObject("forecast");
					JSONObject realtimeObj = jsonObj.getJSONObject("realtime");
					String cityStr = forecastObj.getString("city");
					String weatherStr = realtimeObj.getString("weather");
					city.setText(cityStr);
					weather.setText(weatherStr);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				break;
			case NetHandler.FAIL:
				
				break;
			default:
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast);
		
		initView();
		initData();
	}
	private void initView() {
		city = (TextView) findViewById(R.id.city);
		weather = (TextView) findViewById(R.id.weather);
	}
	private void initData() {
		NetController.pool.execute(new Runnable() {
			
			@Override
			public void run() {
				String response = NetController.getForceCast("http://weatherapi.market.xiaomi.com/wtr-v2/weather?" +
						"cityId=101010100&imei=529e2dd3d767bdd3595eec30dd481050&device=pisces" +
						"&miuiVersion=JXCCNBD20.0&modDevice=&source=miuiWeatherApp");
				if (!TextUtils.isEmpty(response)) {
					mHandler.sendMessage(mHandler.obtainMessage(NetHandler.OK, response));
					FileUtils.getInstance(getApplicationContext()).cacheFile(response, "weather");
				} else {
					mHandler.sendEmptyMessage(NetHandler.FAIL);
				}
			}
		});
	}

}
