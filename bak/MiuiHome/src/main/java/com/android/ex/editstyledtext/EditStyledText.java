package com.android.ex.editstyledtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.NoCopySpan.Concrete;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.QuoteSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class EditStyledText extends EditText {
    private static final Concrete SELECTING = new Concrete();
    private static CharSequence STR_CLEARSTYLES;
    private static CharSequence STR_HORIZONTALLINE;
    private static CharSequence STR_PASTE;
    private Drawable mDefaultBackground;
    private ArrayList<EditStyledTextNotifier> mESTNotifiers;
    private InputConnection mInputConnection;
    private EditorManager mManager;

    public class EditModeActions {
        private HashMap<Integer, EditModeActionBase> mActionMap;
        private EditorManager mManager;
        private int mMode;

        public class EditModeActionBase {
            private Object[] mParams;

            protected boolean doNotSelected() {
                return false;
            }

            protected boolean doStartPosIsSelected() {
                return doNotSelected();
            }

            protected boolean doEndPosIsSelected() {
                return doStartPosIsSelected();
            }

            protected boolean doSelectionIsFixed() {
                return doEndPosIsSelected();
            }

            protected boolean doSelectionIsFixedAndWaitingInput() {
                return doEndPosIsSelected();
            }

            protected void addParams(Object[] o) {
                this.mParams = o;
            }
        }

        public void onAction(int newMode, Object[] params) {
            getAction(newMode).addParams(params);
            this.mMode = newMode;
            doNext(newMode);
        }

        public void onAction(int newMode) {
            onAction(newMode, null);
        }

        public void onSelectAction() {
            doNext(5);
        }

        private EditModeActionBase getAction(int mode) {
            if (this.mActionMap.containsKey(Integer.valueOf(mode))) {
                return (EditModeActionBase) this.mActionMap.get(Integer.valueOf(mode));
            }
            return null;
        }

        public boolean doNext(int mode) {
            Log.d("EditModeActions", "--- do the next action: " + mode + "," + this.mManager.getSelectState());
            EditModeActionBase action = getAction(mode);
            if (action == null) {
                Log.e("EditModeActions", "--- invalid action error.");
                return false;
            }
            switch (this.mManager.getSelectState()) {
                case 0:
                    return action.doNotSelected();
                case 1:
                    return action.doStartPosIsSelected();
                case 2:
                    return action.doEndPosIsSelected();
                case 3:
                    if (this.mManager.isWaitInput()) {
                        return action.doSelectionIsFixedAndWaitingInput();
                    }
                    return action.doSelectionIsFixed();
                default:
                    return false;
            }
        }
    }

    public interface EditStyledTextNotifier {
        boolean isButtonsFocused();

        void onStateChanged(int i, int i2);

        boolean sendOnTouchEvent(MotionEvent motionEvent);
    }

    public static class EditStyledTextSpans {

        public static class HorizontalLineDrawable extends ShapeDrawable {
            private static boolean DBG_HL = false;
            private Spannable mSpannable;
            private int mWidth;

            public void draw(Canvas canvas) {
                renewColor();
                canvas.drawRect(new Rect(0, 9, this.mWidth, 11), getPaint());
            }

            public void renewBounds(int width) {
                if (DBG_HL) {
                    Log.d("EditStyledTextSpan", "--- renewBounds:" + width);
                }
                if (width > 20) {
                    width -= 20;
                }
                this.mWidth = width;
                setBounds(0, 0, width, 20);
            }

            private void renewColor(int color) {
                if (DBG_HL) {
                    Log.d("EditStyledTextSpan", "--- renewColor:" + color);
                }
                getPaint().setColor(color);
            }

            private void renewColor() {
                HorizontalLineSpan parent = getParentSpan();
                Spannable text = this.mSpannable;
                ForegroundColorSpan[] spans = (ForegroundColorSpan[]) text.getSpans(text.getSpanStart(parent), text.getSpanEnd(parent), ForegroundColorSpan.class);
                if (DBG_HL) {
                    Log.d("EditStyledTextSpan", "--- renewColor:" + spans.length);
                }
                if (spans.length > 0) {
                    renewColor(spans[spans.length - 1].getForegroundColor());
                }
            }

            private HorizontalLineSpan getParentSpan() {
                Spannable text = this.mSpannable;
                HorizontalLineSpan[] images = (HorizontalLineSpan[]) text.getSpans(0, text.length(), HorizontalLineSpan.class);
                if (images.length > 0) {
                    for (HorizontalLineSpan image : images) {
                        if (image.getDrawable() == this) {
                            return image;
                        }
                    }
                }
                Log.e("EditStyledTextSpan", "---renewBounds: Couldn't find");
                return null;
            }
        }

        public static class HorizontalLineSpan extends DynamicDrawableSpan {
            HorizontalLineDrawable mDrawable;

            public Drawable getDrawable() {
                return this.mDrawable;
            }

            public void resetWidth(int width) {
                this.mDrawable.renewBounds(width);
            }
        }

        public static class MarqueeSpan extends CharacterStyle {
            private int mMarqueeColor;
            private int mType;

            public void resetColor(int bgc) {
                this.mMarqueeColor = getMarqueeColor(this.mType, bgc);
            }

            private int getMarqueeColor(int type, int bgc) {
                int a = Color.alpha(bgc);
                int r = Color.red(bgc);
                int g = Color.green(bgc);
                int b = Color.blue(bgc);
                if (a == 0) {
                    a = 128;
                }
                switch (type) {
                    case 0:
                        if (r <= 128) {
                            r = (255 - r) / 2;
                            break;
                        }
                        r /= 2;
                        break;
                    case 1:
                        if (g <= 128) {
                            g = (255 - g) / 2;
                            break;
                        }
                        g /= 2;
                        break;
                    case 2:
                        return 16777215;
                    default:
                        Log.e("EditStyledText", "--- getMarqueeColor: got illigal marquee ID.");
                        return 16777215;
                }
                return Color.argb(a, r, g, b);
            }

            public void updateDrawState(TextPaint tp) {
                tp.bgColor = this.mMarqueeColor;
            }
        }

        public static class RescalableImageSpan extends ImageSpan {
            private final int MAXWIDTH;
            Uri mContentUri;
            private Context mContext;
            private Drawable mDrawable;
            public int mIntrinsicHeight;
            public int mIntrinsicWidth;

            public Drawable getDrawable() {
                if (this.mDrawable != null) {
                    return this.mDrawable;
                }
                if (this.mContentUri != null) {
                    System.gc();
                    try {
                        Bitmap bitmap;
                        InputStream is = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                        Options opt = new Options();
                        opt.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(is, null, opt);
                        is.close();
                        is = this.mContext.getContentResolver().openInputStream(this.mContentUri);
                        int width = opt.outWidth;
                        int height = opt.outHeight;
                        this.mIntrinsicWidth = width;
                        this.mIntrinsicHeight = height;
                        if (opt.outWidth > this.MAXWIDTH) {
                            width = this.MAXWIDTH;
                            height = (this.MAXWIDTH * height) / opt.outWidth;
                            bitmap = BitmapFactory.decodeStream(is, new Rect(0, 0, width, height), null);
                        } else {
                            bitmap = BitmapFactory.decodeStream(is);
                        }
                        this.mDrawable = new BitmapDrawable(this.mContext.getResources(), bitmap);
                        this.mDrawable.setBounds(0, 0, width, height);
                        is.close();
                    } catch (Exception e) {
                        Log.e("EditStyledTextSpan", "Failed to loaded content " + this.mContentUri, e);
                        return null;
                    } catch (OutOfMemoryError e2) {
                        Log.e("EditStyledTextSpan", "OutOfMemoryError");
                        return null;
                    }
                }
                this.mDrawable = super.getDrawable();
                rescaleBigImage(this.mDrawable);
                this.mIntrinsicWidth = this.mDrawable.getIntrinsicWidth();
                this.mIntrinsicHeight = this.mDrawable.getIntrinsicHeight();
                return this.mDrawable;
            }

            private void rescaleBigImage(Drawable image) {
                Log.d("EditStyledTextSpan", "--- rescaleBigImage:");
                if (this.MAXWIDTH >= 0) {
                    int image_width = image.getIntrinsicWidth();
                    int image_height = image.getIntrinsicHeight();
                    Log.d("EditStyledTextSpan", "--- rescaleBigImage:" + image_width + "," + image_height + "," + this.MAXWIDTH);
                    if (image_width > this.MAXWIDTH) {
                        image_width = this.MAXWIDTH;
                        image_height = (this.MAXWIDTH * image_height) / image_width;
                    }
                    image.setBounds(0, 0, image_width, image_height);
                }
            }
        }
    }

    private class EditorManager {
        private EditModeActions mActions;
        private int mBackgroundColor;
        private int mColorWaitInput;
        private BackgroundColorSpan mComposingTextMask;
        private SpannableStringBuilder mCopyBuffer;
        private int mCurEnd;
        private int mCurStart;
        private EditStyledText mEST;
        private boolean mEditFlag;
        private boolean mKeepNonLineSpan;
        private int mMode;
        private int mSizeWaitInput;
        private SoftKeyReceiver mSkr;
        private boolean mSoftKeyBlockFlag;
        private int mState;
        private boolean mTextIsFinishedFlag;
        private boolean mWaitInputFlag;
        final /* synthetic */ EditStyledText this$0;

        public void onAction(int mode) {
            onAction(mode, true);
        }

        public void onAction(int mode, boolean notifyStateChanged) {
            this.mActions.onAction(mode);
            if (notifyStateChanged) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onStartSelect(boolean notifyStateChanged) {
            Log.d("EditStyledText.EditorManager", "--- onClickSelect");
            this.mMode = 5;
            if (this.mState == 0) {
                this.mActions.onSelectAction();
            } else {
                unsetSelect();
                this.mActions.onSelectAction();
            }
            if (notifyStateChanged) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onCursorMoved() {
            Log.d("EditStyledText.EditorManager", "--- onClickView");
            if (this.mState == 1 || this.mState == 2) {
                this.mActions.onSelectAction();
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onStartSelectAll(boolean notifyStateChanged) {
            Log.d("EditStyledText.EditorManager", "--- onClickSelectAll");
            handleSelectAll();
            if (notifyStateChanged) {
                this.mEST.notifyStateChanged(this.mMode, this.mState);
            }
        }

        public void onFixSelectedItem() {
            Log.d("EditStyledText.EditorManager", "--- onFixSelectedItem");
            fixSelectionAndDoNextAction();
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        public void onClearStyles() {
            this.mActions.onAction(14);
        }

        public void onRefreshStyles() {
            Log.d("EditStyledText.EditorManager", "--- onRefreshStyles");
            Editable txt = this.mEST.getText();
            int len = txt.length();
            int width = this.mEST.getWidth();
            HorizontalLineSpan[] lines = (HorizontalLineSpan[]) txt.getSpans(0, len, HorizontalLineSpan.class);
            for (HorizontalLineSpan line : lines) {
                line.resetWidth(width);
            }
            for (MarqueeSpan marquee : (MarqueeSpan[]) txt.getSpans(0, len, MarqueeSpan.class)) {
                marquee.resetColor(this.mEST.getBackgroundColor());
            }
            if (lines.length > 0) {
                txt.replace(0, 1, "" + txt.charAt(0));
            }
        }

        public void setBackgroundColor(int color) {
            this.mBackgroundColor = color;
        }

        public void setTextComposingMask(int start, int end) {
            int foregroundColor;
            Log.d("EditStyledText", "--- setTextComposingMask:" + start + "," + end);
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            if (!isWaitInput() || this.mColorWaitInput == 16777215) {
                foregroundColor = this.mEST.getForegroundColor(min);
            } else {
                foregroundColor = this.mColorWaitInput;
            }
            int backgroundColor = this.mEST.getBackgroundColor();
            Log.d("EditStyledText", "--- fg:" + Integer.toHexString(foregroundColor) + ",bg:" + Integer.toHexString(backgroundColor) + "," + isWaitInput() + "," + "," + this.mMode);
            if (foregroundColor == backgroundColor) {
                int maskColor = Integer.MIN_VALUE | ((-16777216 | backgroundColor) ^ -1);
                if (this.mComposingTextMask == null || this.mComposingTextMask.getBackgroundColor() != maskColor) {
                    this.mComposingTextMask = new BackgroundColorSpan(maskColor);
                }
                this.mEST.getText().setSpan(this.mComposingTextMask, min, max, 33);
            }
        }

        public void unsetTextComposingMask() {
            Log.d("EditStyledText", "--- unsetTextComposingMask");
            if (this.mComposingTextMask != null) {
                this.mEST.getText().removeSpan(this.mComposingTextMask);
                this.mComposingTextMask = null;
            }
        }

        public boolean isEditting() {
            return this.mEditFlag;
        }

        public boolean isStyledText() {
            Editable txt = this.mEST.getText();
            int len = txt.length();
            if (((ParagraphStyle[]) txt.getSpans(0, len, ParagraphStyle.class)).length > 0 || ((QuoteSpan[]) txt.getSpans(0, len, QuoteSpan.class)).length > 0 || ((CharacterStyle[]) txt.getSpans(0, len, CharacterStyle.class)).length > 0 || this.mBackgroundColor != 16777215) {
                return true;
            }
            return false;
        }

        public boolean isSoftKeyBlocked() {
            return this.mSoftKeyBlockFlag;
        }

        public boolean isWaitInput() {
            return this.mWaitInputFlag;
        }

        public int getBackgroundColor() {
            return this.mBackgroundColor;
        }

        public int getSelectState() {
            return this.mState;
        }

        public void updateSpanPreviousFromCursor(Editable txt, int start, int before, int after) {
            Log.d("EditStyledText.EditorManager", "updateSpanPrevious:" + start + "," + before + "," + after);
            int end = start + after;
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            for (Object span : txt.getSpans(min, min, Object.class)) {
                int spanstart;
                int spanend;
                if ((span instanceof ForegroundColorSpan) || (span instanceof AbsoluteSizeSpan) || (span instanceof MarqueeSpan) || (span instanceof AlignmentSpan)) {
                    spanstart = txt.getSpanStart(span);
                    spanend = txt.getSpanEnd(span);
                    Log.d("EditStyledText.EditorManager", "spantype:" + span.getClass() + "," + spanstart);
                    int tempmax = max;
                    if ((span instanceof MarqueeSpan) || (span instanceof AlignmentSpan)) {
                        tempmax = findLineEnd(this.mEST.getText(), max);
                    } else if (this.mKeepNonLineSpan) {
                        tempmax = spanend;
                    }
                    if (spanend < tempmax) {
                        Log.d("EditStyledText.EditorManager", "updateSpanPrevious: extend span");
                        txt.setSpan(span, spanstart, tempmax, 33);
                    }
                } else if (span instanceof HorizontalLineSpan) {
                    spanstart = txt.getSpanStart(span);
                    spanend = txt.getSpanEnd(span);
                    if (before > after) {
                        txt.replace(spanstart, spanend, "");
                        txt.removeSpan(span);
                    } else if (spanend == end && end < txt.length() && this.mEST.getText().charAt(end) != '\n') {
                        this.mEST.getText().insert(end, "\n");
                    }
                }
            }
        }

        public void updateSpanNextToCursor(Editable txt, int start, int before, int after) {
            Log.d("EditStyledText.EditorManager", "updateSpanNext:" + start + "," + before + "," + after);
            int end = start + after;
            int min = Math.min(start, end);
            int max = Math.max(start, end);
            for (Object span : txt.getSpans(max, max, Object.class)) {
                if ((span instanceof MarqueeSpan) || (span instanceof AlignmentSpan)) {
                    int spanstart = txt.getSpanStart(span);
                    int spanend = txt.getSpanEnd(span);
                    Log.d("EditStyledText.EditorManager", "spantype:" + span.getClass() + "," + spanend);
                    int tempmin = min;
                    if ((span instanceof MarqueeSpan) || (span instanceof AlignmentSpan)) {
                        tempmin = findLineStart(this.mEST.getText(), min);
                    }
                    if (tempmin < spanstart && before > after) {
                        txt.removeSpan(span);
                    } else if (spanstart > min) {
                        txt.setSpan(span, min, spanend, 33);
                    }
                } else if ((span instanceof HorizontalLineSpan) && txt.getSpanStart(span) == end && end > 0 && this.mEST.getText().charAt(end - 1) != '\n') {
                    this.mEST.getText().insert(end, "\n");
                    this.mEST.setSelection(end);
                }
            }
        }

        public boolean canPaste() {
            return this.mCopyBuffer != null && this.mCopyBuffer.length() > 0 && removeImageChar(this.mCopyBuffer).length() == 0;
        }

        private void endEdit() {
            Log.d("EditStyledText.EditorManager", "--- handleCancel");
            this.mMode = 0;
            this.mState = 0;
            this.mEditFlag = false;
            this.mColorWaitInput = 16777215;
            this.mSizeWaitInput = 0;
            this.mWaitInputFlag = false;
            this.mSoftKeyBlockFlag = false;
            this.mKeepNonLineSpan = false;
            this.mTextIsFinishedFlag = false;
            unsetSelect();
            this.mEST.setOnClickListener(null);
            unblockSoftKey();
        }

        private void fixSelectionAndDoNextAction() {
            Log.d("EditStyledText.EditorManager", "--- handleComplete:" + this.mCurStart + "," + this.mCurEnd);
            if (!this.mEditFlag) {
                return;
            }
            if (this.mCurStart == this.mCurEnd) {
                Log.d("EditStyledText.EditorManager", "--- cancel handle complete:" + this.mCurStart);
                resetEdit();
                return;
            }
            if (this.mState == 2) {
                this.mState = 3;
            }
            this.mActions.doNext(this.mMode);
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
        }

        private SpannableStringBuilder removeImageChar(SpannableStringBuilder text) {
            SpannableStringBuilder buf = new SpannableStringBuilder(text);
            for (DynamicDrawableSpan style : (DynamicDrawableSpan[]) buf.getSpans(0, buf.length(), DynamicDrawableSpan.class)) {
                if ((style instanceof HorizontalLineSpan) || (style instanceof RescalableImageSpan)) {
                    buf.replace(buf.getSpanStart(style), buf.getSpanEnd(style), "");
                }
            }
            return buf;
        }

        private void handleSelectAll() {
            if (this.mEditFlag) {
                this.mActions.onAction(11);
            }
        }

        private void resetEdit() {
            endEdit();
            this.mEditFlag = true;
            this.mEST.notifyStateChanged(this.mMode, this.mState);
        }

        private void unsetSelect() {
            Log.d("EditStyledText.EditorManager", "--- offSelect");
            EditStyledText.stopSelecting(this.mEST, this.mEST.getText());
            int currpos = this.mEST.getSelectionStart();
            this.mEST.setSelection(currpos, currpos);
            this.mState = 0;
        }

        private int findLineStart(Editable text, int current) {
            int pos = current;
            while (pos > 0 && text.charAt(pos - 1) != '\n') {
                pos--;
            }
            Log.d("EditStyledText.EditorManager", "--- findLineStart:" + current + "," + text.length() + "," + pos);
            return pos;
        }

        private int findLineEnd(Editable text, int current) {
            int pos = current;
            while (pos < text.length()) {
                if (text.charAt(pos) == '\n') {
                    pos++;
                    break;
                }
                pos++;
            }
            Log.d("EditStyledText.EditorManager", "--- findLineEnd:" + current + "," + text.length() + "," + pos);
            return pos;
        }

        public void showSoftKey(int oldSelStart, int oldSelEnd) {
            Log.d("EditStyledText.EditorManager", "--- showsoftkey");
            if (this.mEST.isFocused() && !isSoftKeyBlocked()) {
                this.mSkr.mNewStart = Selection.getSelectionStart(this.mEST.getText());
                this.mSkr.mNewEnd = Selection.getSelectionEnd(this.mEST.getText());
                if (((InputMethodManager) this.this$0.getContext().getSystemService("input_method")).showSoftInput(this.mEST, 0, this.mSkr) && this.mSkr != null) {
                    Selection.setSelection(this.this$0.getText(), oldSelStart, oldSelEnd);
                }
            }
        }

        public void hideSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- hidesoftkey");
            if (this.mEST.isFocused()) {
                this.mSkr.mNewStart = Selection.getSelectionStart(this.mEST.getText());
                this.mSkr.mNewEnd = Selection.getSelectionEnd(this.mEST.getText());
                ((InputMethodManager) this.mEST.getContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mEST.getWindowToken(), 0, this.mSkr);
            }
        }

        public void blockSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- blockSoftKey:");
            hideSoftKey();
            this.mSoftKeyBlockFlag = true;
        }

        public void unblockSoftKey() {
            Log.d("EditStyledText.EditorManager", "--- unblockSoftKey:");
            this.mSoftKeyBlockFlag = false;
        }
    }

    private class MenuHandler implements OnMenuItemClickListener {
        private MenuHandler() {
        }

        public boolean onMenuItemClick(MenuItem item) {
            return EditStyledText.this.onTextContextMenuItem(item.getItemId());
        }
    }

    public static class SavedStyledTextState extends BaseSavedState {
        public int mBackgroundColor;

        SavedStyledTextState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.mBackgroundColor);
        }

        public String toString() {
            return "EditStyledText.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " bgcolor=" + this.mBackgroundColor + "}";
        }
    }

    private static class SoftKeyReceiver extends ResultReceiver {
        EditStyledText mEST;
        int mNewEnd;
        int mNewStart;

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode != 2) {
                Selection.setSelection(this.mEST.getText(), this.mNewStart, this.mNewEnd);
            }
        }
    }

    public static class StyledTextInputConnection extends InputConnectionWrapper {
        EditStyledText mEST;

        public StyledTextInputConnection(InputConnection target, EditStyledText est) {
            super(target, true);
            this.mEST = est;
        }

        public boolean commitText(CharSequence text, int newCursorPosition) {
            Log.d("EditStyledText", "--- commitText:");
            this.mEST.mManager.unsetTextComposingMask();
            return super.commitText(text, newCursorPosition);
        }

        public boolean finishComposingText() {
            Log.d("EditStyledText", "--- finishcomposing:");
            if (!(this.mEST.isSoftKeyBlocked() || this.mEST.isButtonsFocused() || this.mEST.isEditting())) {
                this.mEST.onEndEdit();
            }
            return super.finishComposingText();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult;
        if (event.getAction() == 1) {
            cancelLongPress();
            boolean editting = isEditting();
            if (!editting) {
                onStartEdit();
            }
            int oldSelStart = Selection.getSelectionStart(getText());
            int oldSelEnd = Selection.getSelectionEnd(getText());
            superResult = super.onTouchEvent(event);
            if (isFocused() && getSelectState() == 0) {
                if (editting) {
                    this.mManager.showSoftKey(Selection.getSelectionStart(getText()), Selection.getSelectionEnd(getText()));
                } else {
                    this.mManager.showSoftKey(oldSelStart, oldSelEnd);
                }
            }
            this.mManager.onCursorMoved();
            this.mManager.unsetTextComposingMask();
        } else {
            superResult = super.onTouchEvent(event);
        }
        sendOnTouchEvent(event);
        return superResult;
    }

    public Parcelable onSaveInstanceState() {
        SavedStyledTextState ss = new SavedStyledTextState(super.onSaveInstanceState());
        ss.mBackgroundColor = this.mManager.getBackgroundColor();
        return ss;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedStyledTextState) {
            SavedStyledTextState ss = (SavedStyledTextState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            setBackgroundColor(ss.mBackgroundColor);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (this.mManager != null) {
            this.mManager.onRefreshStyles();
        }
    }

    public boolean onTextContextMenuItem(int id) {
        boolean selection;
        if (getSelectionStart() != getSelectionEnd()) {
            selection = true;
        } else {
            selection = false;
        }
        switch (id) {
            case 16776961:
                onInsertHorizontalLine();
                return true;
            case 16776962:
                onClearStyles();
                return true;
            case 16776963:
                onStartEdit();
                return true;
            case 16776964:
                onEndEdit();
                return true;
            case 16908319:
                onStartSelectAll();
                return true;
            case 16908320:
                if (selection) {
                    onStartCut();
                    return true;
                }
                this.mManager.onStartSelectAll(false);
                onStartCut();
                return true;
            case 16908321:
                if (selection) {
                    onStartCopy();
                    return true;
                }
                this.mManager.onStartSelectAll(false);
                onStartCopy();
                return true;
            case 16908322:
                onStartPaste();
                return true;
            case 16908328:
                onStartSelect();
                this.mManager.blockSoftKey();
                break;
            case 16908329:
                onFixSelectedItem();
                break;
        }
        return super.onTextContextMenuItem(id);
    }

    protected void onCreateContextMenu(ContextMenu menu) {
        super.onCreateContextMenu(menu);
        MenuHandler handler = new MenuHandler();
        if (STR_HORIZONTALLINE != null) {
            menu.add(0, 16776961, 0, STR_HORIZONTALLINE).setOnMenuItemClickListener(handler);
        }
        if (isStyledText() && STR_CLEARSTYLES != null) {
            menu.add(0, 16776962, 0, STR_CLEARSTYLES).setOnMenuItemClickListener(handler);
        }
        if (this.mManager.canPaste()) {
            menu.add(0, 16908322, 0, STR_PASTE).setOnMenuItemClickListener(handler).setAlphabeticShortcut('v');
        }
    }

    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        if (this.mManager != null) {
            this.mManager.updateSpanNextToCursor(getText(), start, before, after);
            this.mManager.updateSpanPreviousFromCursor(getText(), start, before, after);
            if (after > before) {
                this.mManager.setTextComposingMask(start, start + after);
            } else if (before < after) {
                this.mManager.unsetTextComposingMask();
            }
            if (this.mManager.isWaitInput()) {
                if (after > before) {
                    this.mManager.onCursorMoved();
                    onFixSelectedItem();
                } else if (after < before) {
                    this.mManager.onAction(22);
                }
            }
        }
        super.onTextChanged(text, start, before, after);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        this.mInputConnection = new StyledTextInputConnection(super.onCreateInputConnection(outAttrs), this);
        return this.mInputConnection;
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            onStartEdit();
        } else if (!isButtonsFocused()) {
            onEndEdit();
        }
    }

    private void sendOnTouchEvent(MotionEvent event) {
        if (this.mESTNotifiers != null) {
            Iterator i$ = this.mESTNotifiers.iterator();
            while (i$.hasNext()) {
                ((EditStyledTextNotifier) i$.next()).sendOnTouchEvent(event);
            }
        }
    }

    public boolean isButtonsFocused() {
        boolean retval = false;
        if (this.mESTNotifiers != null) {
            Iterator i$ = this.mESTNotifiers.iterator();
            while (i$.hasNext()) {
                retval |= ((EditStyledTextNotifier) i$.next()).isButtonsFocused();
            }
        }
        return retval;
    }

    private void notifyStateChanged(int mode, int state) {
        if (this.mESTNotifiers != null) {
            Iterator i$ = this.mESTNotifiers.iterator();
            while (i$.hasNext()) {
                ((EditStyledTextNotifier) i$.next()).onStateChanged(mode, state);
            }
        }
    }

    public void onStartEdit() {
        this.mManager.onAction(20);
    }

    public void onEndEdit() {
        this.mManager.onAction(21);
    }

    public void onStartCopy() {
        this.mManager.onAction(1);
    }

    public void onStartCut() {
        this.mManager.onAction(7);
    }

    public void onStartPaste() {
        this.mManager.onAction(2);
    }

    public void onStartSelect() {
        this.mManager.onStartSelect(true);
    }

    public void onStartSelectAll() {
        this.mManager.onStartSelectAll(true);
    }

    public void onFixSelectedItem() {
        this.mManager.onFixSelectedItem();
    }

    public void onInsertHorizontalLine() {
        this.mManager.onAction(12);
    }

    public void onClearStyles() {
        this.mManager.onClearStyles();
    }

    private void onRefreshStyles() {
        this.mManager.onRefreshStyles();
    }

    public void setBackgroundColor(int color) {
        if (color != 16777215) {
            super.setBackgroundColor(color);
        } else {
            setBackgroundDrawable(this.mDefaultBackground);
        }
        this.mManager.setBackgroundColor(color);
        onRefreshStyles();
    }

    public boolean isEditting() {
        return this.mManager.isEditting();
    }

    public boolean isStyledText() {
        return this.mManager.isStyledText();
    }

    public boolean isSoftKeyBlocked() {
        return this.mManager.isSoftKeyBlocked();
    }

    public int getSelectState() {
        return this.mManager.getSelectState();
    }

    public int getBackgroundColor() {
        return this.mManager.getBackgroundColor();
    }

    public int getForegroundColor(int pos) {
        if (pos < 0 || pos > getText().length()) {
            return -16777216;
        }
        ForegroundColorSpan[] spans = (ForegroundColorSpan[]) getText().getSpans(pos, pos, ForegroundColorSpan.class);
        if (spans.length > 0) {
            return spans[0].getForegroundColor();
        }
        return -16777216;
    }

    private static void stopSelecting(View view, Spannable content) {
        content.removeSpan(SELECTING);
    }
}
