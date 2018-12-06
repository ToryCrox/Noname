package com.tory.library.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public final static int SIZE_KB = 1024;
    public final static int SIZE_MB = SIZE_KB * 1024;
    public final static int SIZE_GB = SIZE_MB * 1024;


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
        return readString(in,CHARSETNAME);
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


    public static String readAssets(Context context, String filePath) throws IOException {
        return readString(context.getAssets().open(filePath));
    }

    public static String readAssets(Context context,@RawRes int rawResId) throws IOException {
        return readString(context.getResources().openRawResource(rawResId));
    }

    /**
     * 判断目录是否存在
     * 
     * @param file
     * @return
     */
    public static boolean isDir(File file) {
        return file != null && file.isDirectory();
    }

    /**
     * 判断是否是文件
     *
     * @param file
     * @return
     */
    public static boolean isFile(File file){
        return file != null &&  file.exists() && file.isFile();
    }

    /**
     * 文件是否存在
     * @param file
     * @return
     */
    public static boolean fileExists(File file){
        return file != null &&  file.exists();
    }

    /**
     * 创建文件夹
     * 
     * @param file
     * @return
     */
    public static boolean mkdir(File file) {
        if (!isDir(file)) {
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

    /**
     * 写入字符串到文件
     * @param str
     * @param destFileName
     * @throws IOException
     */
    public static void writeString(String str, String destFileName) throws IOException {
        writeString(str, new File(destFileName), false);
    }

    /**
     * 写入字符串到文件
     * @param str
     * @param destFileName
     * @param append 是否追加
     * @throws IOException
     */
    public static void writeString(String str, String destFileName, boolean append) throws IOException {
        File destFile = new File(destFileName);
        writeString(str, destFile, append);
    }

    /**
     * 写入字符串到文件
     * @param str
     * @param destFile
     * @param append
     * @throws IOException
     */
    public static void writeString(String str, File destFile, boolean append) throws IOException {
        if(!destFile.exists()) destFile.createNewFile();
        Writer writer = new BufferedWriter(new FileWriter(destFile, append));
        writer.write(str);
        writer.close();
    }


    /**
     * Java文件操作 获取文件扩展名
     * @param filename
     * @return
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
     * 删除文件或文件夹
     * 可递归删除所有
     * @param dest
     */
    public static void delete(File dest,boolean deleteSelf){
        if(dest == null || !dest.exists()) return;
        if(dest.isFile()) {
            dest.delete();
            return;
        }
        File[] files = dest.listFiles();
        for (File file: files) {
            delete(file,true);
        }
        if(deleteSelf){
            dest.delete();
        }
    }

    /**
     * 删除文件或文件夹
     * 可递归删除所有
     * @param dest
     */
    public static void delete(File dest){
        delete(dest,false);
    }

    public static boolean deletEmptyDir(File dest,boolean deleteSelf){
        if(!isDir(dest)) return false;
        File[] files = dest.listFiles();
        boolean empty =  true;
        if(files != null){
            for (File file : files) {
                if(!deletEmptyDir(file,true)){
                    empty = false;
                    break;
                }
            }
        }
        if(empty && deleteSelf){
            return dest.delete();
        }
        return false;
    }

    /**
     * 计算文件夹大小
     * @param dest
     * @return
     */
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

    /**
     * 获取模式化好的文件或文件夹大小
     * @param dest
     * @return
     */
    public static String getFileSizeFormat(File dest){
        long size = getFileSize(dest);
        return getFileSizeFormat(size);
    }

    /**
     * 格式化文件大小字符串,保留1位小数
     * @param size
     * @return
     */
    public static String getFileSizeFormat(long size){
        if(size < SIZE_KB){
            return size + "B";
        }else if(size < SIZE_MB){
            return String.format("%.1fK",size/(float)SIZE_KB);
        }else if(size < SIZE_GB){
            return String.format("%.1fM",size/(float)SIZE_MB);
        }else{
            return String.format("%.1fG",size/(float)SIZE_GB);
        }
    }


    /***********************Android 操作***********************************************************/
    /**
     * 得到手机的缓存目录
     *
     * @param context
     * @return
     */
    public static File getCacheDir(@NonNull Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir != null && (cacheDir.exists() || cacheDir.mkdirs())) {
                return cacheDir;
            }
        }

        File cacheDir = context.getCacheDir();
        return cacheDir;
    }

    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir != null ? sdDir.toString() : null;
    }

    /**
     * 清除缓存
     * @param context
     */
    public static void clearCache(@NonNull Context context){
        File cacheDir = getCacheDir(context);
        if(cacheDir!= null && cacheDir.isDirectory()){
            for (File file: cacheDir.listFiles()) {
                delete(file);
            }
        }
    }


    /**
     * 获取缓存大小
     * @param context
     * @return
     */
    public static String getCacheSize(@NonNull Context context){
        long httpSize = getFileSize(getCacheDir(context));
        return getFileSizeFormat(httpSize);
    }


    public static boolean saveBitmap(Bitmap bitmap, String filePath){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 通知系统扫描文件
     * @param context
     * @param filePath
     */
    public static void notifyMediaScanFile(@NonNull Context context, @NonNull String filePath){
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }


}