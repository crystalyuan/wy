package com.wy.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaMuxer.OutputFormat;
import android.os.Environment;
import android.util.Log;

/**
 * 文件工具
 * @author Administrator
 *
 */
public class FileUtils {
	
	/** log TAG */
	private static String TAG = FileUtils.class.getSimpleName();
	/** 系统根目录 */
	private static String SYS_ROOT;
	/** 文件缓存根目录 */
	private static String PACKAGE_FOLDER;
	/** 文件工具类 单例模式 */
	private static FileUtils fileUtil;
	
	/**
	 * 私有构造器 单例模式
	 * @param Context context
	 */
	private FileUtils(Context context){
		/*************判断文件SDcard是否存在并且是否可以读写********/
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			/* 存在sdcard并且具有读写权限 根目录为sdcard根目录 */
			SYS_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
		}else{
			/* 不存在sdcard或者sdcard不具有读写权限 根目录为android系统根目录 */
			SYS_ROOT = Environment.getRootDirectory().getAbsolutePath();
		}
		/* 默认根目录为app包名目录 */
		String packageName = context.getApplicationInfo().packageName;
		PACKAGE_FOLDER = SYS_ROOT+"/"+packageName;
	}
	
	/**
	 * 工厂模式 单例模式
	 * @param Contex context
	 * @return FileUtils
	 */
	public synchronized static FileUtils getInstance(Context context){
		if(fileUtil == null){
			fileUtil = new FileUtils(context);
		}
		return fileUtil;
	}
	
	/**
	 * 创建文件夹
	 * @param folderPath
	 */
	public String createFolder(String folderPath){
		folderPath = PACKAGE_FOLDER + "/"+folderPath;
		File file = new File(folderPath);
		if(file.exists()){
			Log.d(TAG, "----------------> folder "+folderPath+" has existed!");
			return folderPath;
		}
		file.mkdirs();
		return folderPath;
	}
	
	/**
	 * 文件缓存
	 * @param Object obj
	 * @param String cachePath null时去类名作为缓存目录
	 * @param String fileName 缓存文件名称
	 */
	public void cacheFile(Object obj , String fileName){
		try {
			String cachePath = PACKAGE_FOLDER+"/"+obj.getClass().getSimpleName();
			createFolder(cachePath);
			String cacheFile = cachePath+"/"+fileName.hashCode();
			File file = new File(cacheFile);
			
			if(file.exists()){
				/* 文件最后修改信息 */
				long lastModified = file.lastModified();
				/* 当前时间 */
				long currentMillions = System.currentTimeMillis();
				/* 更新时间间隔 默认一星期更新一次 */
				long updateInterval = 1000 * 60 * 60 * 24 * 7;
				
				if((currentMillions - lastModified) < updateInterval){
					Log.i(TAG, "------------> file not need to update ");
					return;
				}
				Log.w(TAG,"-----------> file existed and need to update , override it!!");
			}else{
				file.createNewFile();
				Log.i(TAG,"-----------> file not existes and create it!!");
			}
			FileOutputStream fos = new FileOutputStream(file);
			if(obj instanceof Bitmap){
				Bitmap bm = (Bitmap) obj;
				bm.compress(CompressFormat.PNG, 100, fos);
				fos.close();
			}else if( obj instanceof Serializable){
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(obj);
				oos.close();
				fos.close();
			}else{
				Log.e(TAG,"--------------> cache obj cant serializable!");
			}
		} catch (Exception e) {
			Log.e(TAG, "--------------> create file "+fileName+" hashcode "+fileName.hashCode()+" failed! ");
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取文件缓存
	 * @param Class clazz 文件类型
	 * @param String filePath
	 * @return InputStream is
	 */
	public Object getCache (Class clazz , String filePath){
		Object obj = null;
		String cachePath = clazz.getSimpleName();
		/* 组织缓存文件目录位置 */
		filePath = PACKAGE_FOLDER+"/"+cachePath + "/" + clazz.getSimpleName()+"/"+filePath.hashCode();
		
		File file = new File(filePath);
		if(!file.exists()){
			Log.w(TAG, "-------------> file "+filePath +" path hashcode "+filePath.hashCode()+" not exists!");
			return null;
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			if(clazz.getSimpleName().equals(Bitmap.class.getSimpleName())){
				/* 图片类型 */
				Bitmap bm = BitmapFactory.decodeStream(fis);
				obj = bm;
			}else{
				/* 其他信息 */
				obj = inputStream2String(fis);
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * 输入流转化为String
	 * @param is
	 * @return
	 */
	public String inputStream2String(InputStream is){
		String res = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		try {
			while(is.read()!=-1){
				is.read(buffer, 0, buffer.length);
				bos.write(buffer);
			}
			res = bos.toString();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
}
