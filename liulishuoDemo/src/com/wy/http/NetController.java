package com.wy.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetController {

	public static ExecutorService pool = Executors.newFixedThreadPool(5);
	
	public static String getForceCast(String url) {
		HttpAgent httpAgent = new HttpAgent();
		return httpAgent.doGet(url, "");
	}

}
