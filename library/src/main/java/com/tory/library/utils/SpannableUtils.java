package com.tory.library.utils;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.ColorInt;

/**
 * Author: xutao
 * Version V1.0
 * Date: 2019-08-09
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2019-08-09 xutao 1.0
 * Why & What is modified:
 */
public class SpannableUtils {

    private static final String TAG = "SpannableUtils";

    public static final char HOT_TAG = '#';
    public static final char HOT_TAG_ESCAPE = '&';
    public static final char START_TAG = '<';
    public static final char END_TAG = '>';

    public interface SpanFactory{
        Object createSpan(CharSequence sequence);
    }

    /**
     * 对以'#'渲染重点词
     * 要单独显示'#'，以'&'来转义
     * @param hotText
     * @param spanFactory
     * @return
     */
    public static SpannableStringBuilder parseSpannableText(@NonNull String hotText, @NonNull SpanFactory spanFactory) {
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        StringBuilder sb = null;
        char[] chars = hotText.toCharArray();
        for (char c : chars) {
            if (c == START_TAG){
                if (sb != null){
                    ssb.append(START_TAG).append(sb);
                }
                sb = new StringBuilder();
            } else if (c == END_TAG && sb != null){
                ssb.append(sb, spanFactory.createSpan(sb.toString()), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                sb = null;
            } else {
                if (sb != null) {
                    sb.append(c);
                } else {
                    ssb.append(c);
                }
            }
        }
        if (sb != null) {
            DuLogger.t(TAG).d( "parseHotText: lost sb = " + sb);
            ssb.append(sb);
        }
        return ssb;
    }


    public static SpannableStringBuilder parseHotText(@NonNull String hotText, @ColorInt int hotTextColor){
        return parseSpannableText(hotText, sequence -> new ForegroundColorSpan(hotTextColor));
    }

    public static SpannableStringBuilder parseFontText(@NonNull Context context, @NonNull String hotText, int textSize){
        return parseSpannableText(hotText, sequence -> {
            Typeface typeface = FontManager.getInstance(context).getDefaultFont();
            return new TypefaceSpan(typeface, textSize);

        });
    }

    public static SpannableStringBuilder parseFontText(@NonNull Context context, @NonNull String hotText){
        return parseFontText(context, hotText, 0);
    }


    static class TypefaceSpan extends MetricAffectingSpan {

        private final Typeface mTypeface;
        private final int mTextSize;

        public TypefaceSpan(@NonNull Typeface typeface, int textSize) {
            mTypeface = typeface;
            mTextSize = textSize;
        }

        public Typeface getTypeface() {
            return mTypeface;
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            updateTypeface(ds);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint paint) {
            updateTypeface(paint);
        }

        private void updateTypeface(@NonNull Paint paint) {
            paint.setTypeface(mTypeface);
            if (mTextSize > 0){
                paint.setTextSize(mTextSize);
            }
        }
    }
}
