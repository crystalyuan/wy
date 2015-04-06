package com.wy.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.wy.utils.FileUtils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * 图片加载器
 */
public class ImageLoader {
	/** Log TAG */
	private static final String TAG = ImageLoader.class.getSimpleName();
	/** 图片内存缓存 */
	private static LruCache<String, Bitmap>  bmCache;
	/** 文件缓存 */
	private static FileUtils fileCache;
	/** 有上线的线程池 */
	private static final ExecutorService pool = Executors.newFixedThreadPool(10);
	/** 单例 */
	private static ImageLoader instance;
	/** 缓存大小上限 */
	private static int MAX_SIZE;
	/** 消息收发器 */
	private Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Map<String, Object> map = (Map<String, Object>) msg.obj;
			ImageView view = (ImageView) map.get("view");
			Bitmap bm = (Bitmap) map.get("bm");
			String url = (String)map.get("url");
			if(url.equals((String)view.getTag())){
				view.setImageBitmap(bm);
			}else{
				Log.d(TAG, "---------> the url is not for the view ");
			}
		};
	};
	
	/**
	 * 私有构造器
	 * @param context
	 */
	private ImageLoader(Context context) {
		
		/*==============获取系统内存信息================================*/
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		manager.getMemoryInfo(mi);
		/*===============内存缓存最大为系统可用内存的1/8=======================*/
		MAX_SIZE = (int) (mi.availMem/8);
		Log.d(TAG, "------------> MAX_SIZE "+MAX_SIZE/(1024*1024));
		bmCache = new LruCache<String, Bitmap>(MAX_SIZE){
			protected int sizeOf(String key, Bitmap value) {
				return getBitmapSize(value);
			}
		};
		/*======================文件缓存=================================*/
		fileCache = FileUtils.getInstance(context);
	}

	/***
	 * 单例模式
	 * 
	 * @return ImageLoader
	 */
	public synchronized static ImageLoader getInstance(Context context) {
		if (instance == null) {
			instance = new ImageLoader(context);
		}
		return instance;
	}

	/**
	 * 加载图片
	 * @param ImageView view
	 * @param String url
	 * @param int id id=-1 不设置背景
	 */
	public void load(final ImageView view, final String url , int id) {
		
		/*===================避免图片错位控制====================*/
		view.setTag(url);
		
		/*===================内存缓存是否存在==================*/
		if (bmCache.get(url)!=null) {
			Bitmap bitmap = bmCache.get(url);
			if (bitmap != null) {
				Log.d(TAG, "-------------> cache catched it!");
				view.setImageBitmap(bitmap);
				bmCache.put(url, bitmap);
				isNeedToClear();
				return;
			}
		}
		
		/*==================文件缓存=========================*/
		Object obj = fileCache.getCache(Bitmap.class, url);
		if( obj != null){
			Bitmap bm = (Bitmap) obj;
			Log.e(TAG, "--------------> fileCache catched it!");
			view.setImageBitmap(bm);
			return;
		}
		
		/*================设置默认背景==================*/
		if(id != -1){
			view.setImageResource(id);
		}
		
		/*=================加载图片===================*/
		loadWithURL(view, url, new StateListen() {

			public void onSuccess(String url, InputStream is, ImageView view) {
				try {
					Bitmap bm = createBitmapFromInputStream(is, view);
					if (bm != null) {
						Log.d(TAG, "onSuccess ------> load image success");
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("bm", bm);
						map.put("url", url);
						map.put("view", view);

						Message msg = new Message();
						msg.obj = map;
						myHandler.sendMessage(msg);

						bmCache.put(url, bm);
						fileCache.cacheFile(bm, url);
						
					} else {
						Log.d(TAG, "onSuccess ------> 资源已损坏");
					}
				} catch (Exception e) {
					Log.d(TAG, "onSuccess ------> IOException ");
					e.printStackTrace();
				}
			}

			public void onPre(String url) {
				Log.d(TAG, "onPre ------> start to load url : " + url);
			}

			public void onFailed(String url, String info) {
				Log.d(TAG, "onFailed -------> fail to load url : " + url);
				Log.d(TAG, "onFailed -------> error info " + info);
			}
		});
	}

	/**
	 * 加载图片
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private void loadWithURL(final ImageView view, final String urlStr,
			final StateListen listener) {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				URL url;
				try {
					url = new URL(urlStr);
					listener.onPre(urlStr);
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					InputStream is = urlConnection.getInputStream();
					listener.onSuccess(urlStr, is, view);
					urlConnection.disconnect();
				} catch (MalformedURLException e) {
					e.printStackTrace();
					listener.onFailed(urlStr, "MalformedURLException");
				} catch (IOException e) {
					e.printStackTrace();
					listener.onFailed(urlStr, "IOException");
				} catch (Exception e) {
					e.printStackTrace();
					listener.onFailed(urlStr, "未知异常");
				}
			}
		});
	}
	
	/**
	 * 判断缓存是否需要清空
	 */
	private void isNeedToClear(){
		Log.d(TAG, "now cache size "+bmCache.size() / (1024*1024) +" max_size "+MAX_SIZE / (1024*1024)); 
		if(bmCache.size() > (MAX_SIZE-2)){
			Log.d(TAG, " clear cache "); 
			bmCache.evictAll();
		}
	}
	
	/**
	 * 输入流转换为图片
	 * synchronized 控制同一时刻最多只有一个线程可以访问该资源 其他线程排队等待锁解开再次访问
	 * 
	 * @param is
	 * @return bitmap
	 * @throws IOException 
	 */
	private synchronized Bitmap createBitmapFromInputStream(InputStream is, ImageView view) throws IOException {
		/*===============首先判断是否需要清空缓存======================*/
		isNeedToClear();
		/*===================获取图片===========================*/
		Bitmap bitmap = null;
		try {

			bitmap = BitmapFactory.decodeStream(is);
			bitmap = scaleBitmap(bitmap, view);
			 
//	        ByteArrayOutputStream outStream = new ByteArrayOutputStream();        
//	        byte[] buffer = new byte[1024];        
//	        int len = 0;        
//	        while( (len=is.read(buffer)) != -1){        
//	            outStream.write(buffer, 0, len);        
//	        }        
//	        outStream.close();        
//	        is.close();   
//			byte[] bitmapbyte = outStream.toByteArray();
//			
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inJustDecodeBounds = true;
////			BitmapFactory.decodeStream(is, null, options);
//			BitmapFactory.decodeByteArray(bitmapbyte, 0, bitmapbyte.length, options);
//			
////	        int widthRes= bitmap.getWidth();// 获取资源位图的宽
////	        int heightRes = bitmap.getHeight();// 获取资源位图的高
//	        int be = (int)(options.outWidth / width);
//	        if (be <= 0)
//	            be = 1;
//	        options.inSampleSize = be;
//	        options.inJustDecodeBounds = false;
////	        bitmap = BitmapFactory.decodeStream(is, null, options);
//	        bitmap = BitmapFactory.decodeByteArray(bitmapbyte, 0, bitmapbyte.length, options);
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	public static Bitmap scaleBitmap(Bitmap bitmap, ImageView view) {
		
        int widthRes= bitmap.getWidth();// 获取资源位图的宽
        int heightRes = bitmap.getHeight();// 获取资源位图的高
        
        float _width = (float)widthRes;
        float _height = (float)heightRes;
        
        int width = view.getMeasuredWidth();
        float height = (_height / _width) * (float)width;
        view.getLayoutParams().height = (int)height;
        float scale = widthRes / width;
		if (width == 0 ) {
			return bitmap;
		}
		Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);// 获取缩放比例
                    // 根据缩放比例获取新的位图
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, (int)height, matrix, true);
        return newbmp;

	}

	/**
	 * 获取bitmap大小
	 * @param bitmap
	 * @return
	 */
	@SuppressLint("NewApi") private int getBitmapSize(Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount();
			
		}
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * 状态监听
	 * 
	 * @author Administrator
	 * 
	 */
	interface StateListen {
		void onPre(final String url);
		void onSuccess(final String url, final InputStream is,final ImageView view);
		void onFailed(final String url, final String info);
	}

}
