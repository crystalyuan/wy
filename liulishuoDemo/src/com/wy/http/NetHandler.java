package com.wy.http;

import android.os.Handler;
import android.os.Message;

public class NetHandler extends Handler {

	public static final int OK = 1;
	public static final int FAIL = 2;
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
	}
}
