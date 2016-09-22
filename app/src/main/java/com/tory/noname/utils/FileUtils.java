package com.tory.noname.utils;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public class FileUtils {

	public final static int BUFFER_SIZE = 1024;

	public final static String FILE_EXTENSION_SEPARATOR = ".";

	public final static String CHARSETNAME = "UTF-8";

	/**
	 * 将给定输入流中的内容转移到给定输出流
	 * 
	 * @throws IOException
	 */
	public static void getInputStream(InputStream in, OutputStream out) throws IOException {
		if (in == null || out == null) {
			return;
		}
		// 定义缓冲区
		byte[] buf = new byte[1024];
		int len = -1;
		while ((len = in.read(buf)) != -1) { // 循环读取出入流中的内容，并写入输出流，直到输入流末尾
			out.write(buf, 0, len);
		}
		out.flush();
	}

	/**
	 * 写入到文件
	 * 
	 * @param in
	 * @param file
	 * @throws IOException
	 */
	public static void writeStream(InputStream in, File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		getInputStream(in, out);
		in.close();
		out.close();
	}

	/**
	 * 将输入流中的内容生成字符串
	 * 
	 * @throws IOException
	 */
	public static String readString(InputStream in) throws IOException {
		String str = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getInputStream(in, out);
		str = new String(out.toByteArray());
		in.close();
		return str;
	}

	/**
	 * 将输入流中的内容生成字符串
	 * 
	 * @throws IOException
	 */
	public static String readString(InputStream in, String charsetName) throws IOException {
		String str = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getInputStream(in, out);
		str = new String(out.toByteArray(), charsetName);
		in.close();
		return str;
	}

	/**
	 * 将输入流中的内容生成字符串
	 * 
	 * @throws IOException
	 */
	public static String readString(File file, String charsetName) throws IOException {
		if(!file.isFile()||!file.exists()) return null;
		FileInputStream in = new FileInputStream(file);
		String str = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		getInputStream(in, out);
		str = new String(out.toByteArray(), charsetName);
		in.close();
		out.close();
		return str;
	}

	/**
	 * 从文件中读取字符串
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readString(File file) throws IOException {
		return readString(file, CHARSETNAME);
	}

    public static String readString(String file) throws IOException {
        return readString(new File(file), CHARSETNAME);
    }

	/**
	 * 判断文件是否存在
	 * 
	 * @param file
	 * @return
	 */
	public static boolean dirExits(File file) {
		return file != null && file.isDirectory();
	}

	/**
	 * 创建文件夹
	 * 
	 * @param file
	 * @return
	 */
	public static boolean mkdir(File file) {
		if (!dirExits(file)) {
			return file.mkdirs();
		}
		return true;
	}

	/**
	 * 写入字符串到文件
	 * 
	 * @param str
	 * @param destFile
	 * @throws IOException
	 */
	public static void writeString(String str, File destFile) throws IOException {
		writeString(str, destFile, false);
	}

	public static void writeString(String str, String destFileName) throws IOException {
		File destFile = new File(destFileName);
		writeString(str, destFile, false);
	}

	public static void writeString(String str, String destFileName, boolean append) throws IOException {
		File destFile = new File(destFileName);
		writeString(str, destFile, append);
	}

	public static void writeString(String str, File destFile, boolean append) throws IOException {
		if(!destFile.exists()) destFile.createNewFile();
		Writer writer = new BufferedWriter(new FileWriter(destFile, append));
		writer.write(str);
		writer.close();
	}

	/*
	 * Java文件操作 获取文件扩展名
	 * 
	 * Created on: 2011-8-2 Author: blueeagle
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return "";
	}


	/**
	 * 得到手机的缓存目录
	 *
	 * @param context
	 * @return
	 */
	public static File getCacheDir(Context context) {
		Log.i("getCacheDir", "cache sdcard state: " + Environment.getExternalStorageState());
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File cacheDir = context.getExternalCacheDir();
			if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs())) {
				Log.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());
				return cacheDir;
			}
		}

		File cacheDir = context.getCacheDir();
		Log.i("getCacheDir", "cache dir: " + cacheDir.getAbsolutePath());

		return cacheDir;
	}

	public static void clearCache(Context context){
		L.d("clear cache start");
		long t1 = SystemClock.uptimeMillis();
		File cacheDir = getCacheDir(context);
		if(cacheDir!= null && cacheDir.isDirectory()){
			for (File file: cacheDir.listFiles()) {
				delete(file);
			}
		}
		L.d("clear cache end :"+(SystemClock.uptimeMillis() - t1));
	}


	public static String getCacheSize(Context context){
		long httpSize = getFileSize(FileUtils.getCacheDir(context));
		return getFileSizeFormat(httpSize);
	}

	/**
	 * 删除文件或文件夹
	 * @param dest
     */
	public static void delete(File dest){
		if(dest == null || !dest.exists()) return;
		if(dest.isFile()) {
			dest.delete();
			return;
		}
		for (File file: dest.listFiles()) {
			delete(file);
		}
		dest.delete();
	}

	public static long getFileSize(File dest){
		if(dest == null || !dest.exists()) return 0;
		if(dest.isFile()){
			return dest.length();
		}
		long len = 0;
		for (File file: dest.listFiles()) {
			len += getFileSize(file);
		}
		return len;
	}

	public static String getFileSizeFormat(File dest){
		L.d("compute file size start :" + Thread.currentThread().getName());
		long t1 = SystemClock.uptimeMillis();
		long size = getFileSize(dest);
		L.d("compute file end :"+(SystemClock.uptimeMillis() - t1));
		return getFileSizeFormat(size);
	}

	public static String getFileSizeFormat(long size){
		if(size < 1024){
			return size + "B";
		}else if(size < 1024 * 1024){
			return String.format("%.1fK",size/1024.0f);
		}else if(size < 1024 * 1024 * 1024){
			return String.format("%.1fM",size/(1024.0 * 1024.0f));
		}else{
			return String.format("%.1fG",size/(1024.0f * 1024 * 1024));
		}
	}

}