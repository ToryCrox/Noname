package com.tory.library.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.SystemClock;
import android.util.Log;


/**
 * Created by tao.xu2 on 2017/4/25.
 */

public class SimilarImageUtil {

    public static final boolean DEBUG = true;
    private static final String TAG = "SimilarImageUtil";

    static final char hexs[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * 将彩色图转换为灰度图
     *
     * @param img 位图
     * @return 返回转换好的位图
     */
    public static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int original = pixels[width * i + j];
                int red = ((original & 0x00FF0000) >> 16);
                int green = ((original & 0x0000FF00) >> 8);
                int blue = (original & 0x000000FF);

                int grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;

            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static int getAvg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);

        int avgPixel = 0;
        for (int pixel : pixels) {
            avgPixel += pixel;
        }
        return avgPixel / pixels.length;
    }

    public static String getBinary(Bitmap img, int average) {
        StringBuilder sb = new StringBuilder();

        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = width * i + j;
                int original = pixels[index];
                int diff = original >= average ? 1 : 0;
                sb.append(diff);

            }
        }
        return sb.toString();
    }

    public static String bs2hex(String bString) {
        if (bString == null || bString.equals("") || bString.length() % 8 != 0){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int iTmp;
        for (int i = 0; i < bString.length(); i += 4) {
            iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            sb.append(Integer.toHexString(iTmp));
        }
        return sb.toString();
    }

    public static String bs2hex2(String bs) {
        if (bs == null || bs.equals("") || bs.length() % 8 != 0){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bs.length(); i += 4) {
            int in = Integer.parseInt(bs.substring(i,i+4), 2);
            sb.append(hexs[in]);
        }
        return sb.toString();
    }

    /**
     * @param img
     * @param average
     * @return
     */
    public static Bitmap convertWABImg(Bitmap img, int average) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int original = pixels[width * i + j];
                if (original >= average) {
                    pixels[width * i + j] = -16777216;
                } else {
                    pixels[width * i + j] = -1;
                }
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static String computImageHash(Bitmap bitmap){
        if(bitmap == null || bitmap.isRecycled()){
            return "";
        }
        Bitmap bitmap8 = ThumbnailUtils.extractThumbnail(bitmap, 8, 8); // 缩小
        Bitmap bitmapG = convertGreyImg(bitmap8); // 灰度图像
        return bs2hex2(getBinary(bitmapG, getAvg(bitmapG)));
    }

    public static int diffImageHash(String s1, String s2) {
        if(s1.length() != s2.length()){
            return  10;
        }
        char[] s1s = s1.toCharArray();
        char[] s2s = s2.toCharArray();
        int diffNum = 0;
        for (int i = 0; i<s1s.length; i++) {
            if (s1s[i] != s2s[i]) {
                diffNum++;
            }
        }
        return diffNum;
    }

    public static int compareImages(Bitmap bitmap1, Bitmap bitmap2){
        String hex1 = computImageHash(bitmap1);
        String hex2 = computImageHash(bitmap2);
        int diff = diffImageHash(hex1, hex2);
        return diff;
    }

    public static int compareImages(Drawable d1, Drawable d2){
        long start;
        if(DEBUG){
            start = SystemClock.uptimeMillis();
        }
        Bitmap bitmap1 = ImageUtils.getDrawableBitmap(d1);
        Bitmap bitmap2 = ImageUtils.getDrawableBitmap(d2);
        String hex1 = computImageHash(bitmap1);
        String hex2 = computImageHash(bitmap2);
        int diff = diffImageHash(hex1, hex2);
        if(DEBUG){
            Log.d(TAG, "compareImages hex1="+hex1+", hex2="+hex2 + ", diff="+diff
                    + ", time spent="+(SystemClock.uptimeMillis() - start));
        }
        return diff;
    }
}
