package com.tory.library.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;

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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by SuperVC on 2017/5/7.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    public final static int BUFFER_SIZE = 1024;
    public final static String CHARSETNAME = "UTF-8";

    public final static int SIZE_KB = 1024;
    public final static int SIZE_MB = SIZE_KB * 1024;
    public final static int SIZE_GB = SIZE_MB * 1024;

    /**
     * 将给定输入流中的内容转移到给定输出流
     *
     * @throws IOException
     */
    public static void writeStream(InputStream in, OutputStream out) throws IOException {
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
        writeStream(in, out);
        in.close();
        out.close();
    }

    /**
     * 将输入流中的内容生成字符串
     *
     * @throws IOException
     */
    public static String readString(InputStream in) throws IOException {
        return readString(in, CHARSETNAME);
    }

    /**
     * 将输入流中的内容生成字符串
     *
     * @throws IOException
     */
    public static String readString(InputStream in, String charsetName) throws IOException {
        String str = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeStream(in, out);
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
        if (!file.isFile() || !file.exists()) {
            return null;
        }
        FileInputStream in = new FileInputStream(file);
        String str = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeStream(in, out);
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
    public static boolean isFile(File file) {
        return file != null && file.exists() && file.isFile();
    }

    /**
     * 文件是否存在，且是文件
     * @param path
     * @return
     */
    public static boolean isFile(@Nullable String path){
        if (TextUtils.isEmpty(path)){
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * 文件是否存在
     *
     * @param file
     * @return
     */
    public static boolean fileExists(File file) {
        return file != null && file.exists();
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
     * @param destFile
     * @param str
     * @throws IOException
     */
    public static void writeString(File destFile, String str) throws IOException {
        writeString(destFile, str, false);
    }

    /**
     * 写入字符串到文件
     *
     * @param str
     * @param destFilePath
     * @throws IOException
     */
    public static void writeString(@NonNull String destFilePath, String str) throws IOException {
        writeString(new File(destFilePath), str, false);
    }

    /**
     * 写入字符串到文件
     *
     * @param destFilePath
     * @param str
     * @param append       是否追加
     * @throws IOException
     */
    public static void writeString(String destFilePath, String str, boolean append) throws IOException {
        File destFile = new File(destFilePath);
        writeString(destFile, str, append);
    }

    /**
     * 写入字符串到文件
     *
     * @param destFile
     * @param str
     * @param append
     * @throws IOException
     */
    public static void writeString(File destFile, String str, boolean append) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        Writer writer = new BufferedWriter(new FileWriter(destFile, append));
        writer.write(str);
        writer.close();
    }

    public static String getAppPath(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }

    public static String getSDPath() {
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return Environment.getDownloadCacheDirectory().toString();
        }
    }

    public static boolean isExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        } else {
            return true;
        }
    }

    public static String getJoinedPath(String... paths) {
        if (paths == null || paths.length == 0) {
            return null;
        }
        File file = new File(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            file = new File(file, paths[i]);
        }
        return file.getAbsolutePath();
    }

    public static String getExt(String path) {
        if ((path != null) && (path.length() > 0)) {
            int dot = path.lastIndexOf('.');
            if ((dot > -1) && (dot < (path.length() - 1))) {
                return path.substring(dot + 1);
            }
        }
        return path;
    }

    public static String getName(String path) {
        return new File(path).getName();
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return 如果路径为空则返回false，如果文件不存在则返回true，otherwise 按照文件的删除具体结果返回
     */
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        //如果文件不存在即代表文件删除成功，需要注意
        File file = new File(filePath);
        return deleteFile(file);
    }

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] fs = file.listFiles();
            if (fs != null) {
                for (File f : fs) {
                    //递归删除
                    deleteFile(f);
                }
            }
        }
        //如果文件不存在即代表文件删除成功，需要注意
        boolean deleteResult = true;
        if (file.exists()) {
            try {
                deleteResult = file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deleteResult;
    }

    /**
     * 保存图片
     *
     * @param bitmap
     * @param dirPath
     * @param name
     * @return 文件的绝对路径
     */
    public static String saveBitmap(@Nullable Bitmap bitmap, @NonNull String dirPath, @Nullable String name,
                                    @Nullable Bitmap.CompressFormat format) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        if (TextUtils.isEmpty(name)) {
            if (format == Bitmap.CompressFormat.PNG) {
                name = System.currentTimeMillis() + ".png";
            } else if (format == Bitmap.CompressFormat.WEBP) {
                name = System.currentTimeMillis() + ".webp";
            } else {
                name = System.currentTimeMillis() + ".jpg";
            }
        }

        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            //如果不存在，那就建立这个文件夹
            dirFile.mkdirs();
        }
        File file = new File(dirPath, name);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(format == null ? Bitmap.CompressFormat.JPEG : format, 100, outputStream);
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveBitmap dirPath=" + dirPath + ", name=" + name, e);
        } finally {
            IoUtils.closeSilently(outputStream);
        }
        return null;
    }

    public static String saveBitmap(@NonNull Bitmap bitmap, @NonNull String dirPath,
                                    @Nullable String name) {
        return saveBitmap(bitmap, dirPath, name, null);
    }

    public static boolean saveWebpBitmap(String rootDir, Bitmap bitmap, String name) {
        if (bitmap == null || rootDir.isEmpty()) {
            return false;
        }
        return saveBitmap(bitmap, rootDir, name, Bitmap.CompressFormat.WEBP) != null;
    }

    /*
     * Java文件操作 获取文件扩展名
     * */
    public static String getExtensionName(String filename) {
        if ((!TextUtils.isEmpty(filename)) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return null;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * */
    public static String getFileNameNoExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String getUTF8(String str) {
        String utfStr = "";
        try {
            utfStr = URLEncoder.encode(new String(str.toString().getBytes("UTF-8")), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 如果是中文空格会自动转成+
        return utfStr.replace("+", "%20");

    }

    /**
     * 通知图库扫描图片
     *
     * @param context
     * @param filePath
     */
    public static void notifyMediaScanFile(@NonNull Context context, @NonNull String filePath) {
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + filePath)));
    }

    /**
     * 是否存在外置存储
     *
     * @param context
     * @return
     */
    public static boolean hasExternalStorage(@NonNull Context context) {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 得到手机的缓存目录
     * 优先获取外置目录
     *
     * @param context
     * @return
     */
    public static File getCacheDir(@NonNull Context context) {
        return getCacheDir(context, null);
    }


    public static File getCacheDir(@NonNull Context context, String dir) {
        File cacheFile = hasExternalStorage(context) ? context.getExternalCacheDir() : context.getCacheDir();
        if (TextUtils.isEmpty(dir)) {
            return cacheFile;
        } else {
            File newFile = new File(cacheFile, dir);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            return newFile;
        }
    }

    /**
     * 获取files的目录
     *
     * @param context
     * @param dir
     * @return
     */
    public static File getFileDir(@NonNull Context context, String dir) {
        if (hasExternalStorage(context)) {
            File fileDir = context.getExternalFilesDir(dir);
            if (fileDir != null && (fileDir.exists() || fileDir.mkdirs())) {
                return fileDir;
            }
        }
        File fileDir = new File(context.getFilesDir(), dir);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir;
    }

    /**
     * 格式化文件大小字符串,保留1位小数
     *
     * @param size
     * @return
     */
    public static String getFileSizeFormat(long size) {
        if (size < SIZE_KB) {
            return size + "B";
        } else if (size < SIZE_MB) {
            return String.format(Locale.getDefault(), "%.1fKB", size / (float) SIZE_KB);
        } else if (size < SIZE_GB) {
            return String.format(Locale.getDefault(), "%.1fMB", size / (float) SIZE_MB);
        } else {
            return String.format(Locale.getDefault(), "%.1fGB", size / (float) SIZE_GB);
        }
    }

    public static String getFileSizeFormat(@NonNull File file){
        return getFileSizeFormat(isFile(file) ? file.length() : 0);
    }

    public static void deleteEmptyDir(File file, boolean b) {

    }

    public static void clearCache(FragmentActivity activity) {

    }

    public static String getCacheSize(Activity activity) {
        return null;
    }
}