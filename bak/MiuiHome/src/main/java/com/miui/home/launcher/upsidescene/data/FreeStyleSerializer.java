package com.miui.home.launcher.upsidescene.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.android.internal.util.FastXmlSerializer;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.SpecificDeviceConfig;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.upsidescene.data.FreeStyle.MtzGadgetInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;
import libcore.io.IoUtils;
import miui.graphics.BitmapFactory;
import miui.util.FileAccessable;
import miui.util.FileAccessable.DeskFile;
import miui.util.FileAccessable.Factory;
import miui.util.FileAccessable.ZipInnerFile;
import miui.util.InputStreamLoader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class FreeStyleSerializer implements FreeStyleSerializable {
    public static final String DATA_PATH = "/data/system/theme/com.miui.home.freestyle";
    private static final Comparator<Sprite> sSpriteIndexComparator = new Comparator<Sprite>() {
        public int compare(Sprite s1, Sprite s2) {
            return s1.mIndex - s2.mIndex;
        }
    };
    private Context mContext;
    private String mFreeStyleName;

    public FreeStyleSerializer(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public boolean exists() {
        return new File(DATA_PATH).exists();
    }

    public FreeStyle load() {
        boolean inChildrenMode = Launcher.isChildrenModeEnabled();
        try {
            if (!exists()) {
                return null;
            }
            this.mFreeStyleName = loadFreeStyleName();
            if (TextUtils.isEmpty(this.mFreeStyleName)) {
                Log.d("FreeStyleSerializer", "not found 'name' in scene.");
                return null;
            }
            FreeStyle originalOrEditStyle = loadOriginalOrEdit();
            originalOrEditStyle.mName = this.mFreeStyleName;
            mergeAdditionSprites(originalOrEditStyle, loadNew());
            List<FreeButtonInfo> freeButtons = loadAllFreeButtons(originalOrEditStyle.getSceneScale());
            List<MtzGadgetInfo> mtzGadgets = loadAllMtzGadgets();
            originalOrEditStyle.mFreeButtonInfos.addAll(freeButtons);
            originalOrEditStyle.mMtzGadgets.addAll(mtzGadgets);
            return originalOrEditStyle;
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return null;
        } catch (Exception ex2) {
            throw new RuntimeException(ex2);
        }
    }

    public void save(FreeStyle freeStyle) {
        if (this.mFreeStyleName == null) {
            this.mFreeStyleName = freeStyle.mName;
        }
        saveFreeStyle(freeStyle, getEditPersistSettingsPath(this.mContext), false);
        saveFreeStyle(freeStyle, getNewPersistSettingsPath(this.mContext), true);
    }

    public void clear(boolean alsoClearUserData) {
        File fileEdit = new File(getEditPersistSettingsPath(this.mContext));
        if (fileEdit.exists()) {
            fileEdit.delete();
        }
        if (alsoClearUserData) {
            File fileNew = new File(getNewPersistSettingsPath(this.mContext));
            if (fileNew.exists()) {
                fileNew.delete();
            }
        }
    }

    private static FileAccessable getFile(String fileSubpath) {
        try {
            return Factory.create(DATA_PATH, fileSubpath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String loadFreeStyleName() {
        String str = null;
        FileAccessable file = getFile("description.xml");
        if (file.exists()) {
            InputStream is = null;
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                is = file.getInputStream();
                parser.setInput(is, null);
                if ("scene".equals(moveToNextStartTag(parser))) {
                    str = parser.getAttributeValue(null, "name");
                } else {
                    throw new RuntimeException("root tag name must be:scene");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IoUtils.closeQuietly(is);
            }
        }
        return str;
    }

    private void saveFreeStyle(FreeStyle freeStyle, String savePath, boolean isUserCreated) {
        Exception ex;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream os = new FileOutputStream(savePath);
            try {
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(os, "utf-8");
                out.startDocument(null, Boolean.valueOf(true));
                out.startTag(null, "scene");
                out.attribute(null, "width", Integer.toString(freeStyle.mRawWidth));
                for (Screen screen : freeStyle.mScreens) {
                    String type;
                    out.startTag(null, "screen");
                    switch (screen.getType()) {
                        case 1:
                            type = "background";
                            break;
                        case 2:
                            type = "drift";
                            break;
                        case 3:
                            type = "foreground";
                            break;
                        case 4:
                            type = "dock";
                            break;
                        default:
                            throw new RuntimeException("unknown screen type:" + screen.getType());
                    }
                    out.attribute(null, "type", type);
                    out.attribute(null, "width", Integer.toString(screen.mRawWidth));
                    out.attribute(null, "home", Integer.toString(screen.getHome()));
                    int size = screen.getSprites().size();
                    for (int i = 0; i < size; i++) {
                        Sprite sprite = (Sprite) screen.getSprites().get(i);
                        if (isUserCreated == sprite.mIsUserCreated) {
                            out.startTag(null, "sprite");
                            out.attribute(null, "index", Integer.toString(i));
                            out.attribute(null, "left", Integer.toString(sprite.mRawLeft));
                            out.attribute(null, "top", Integer.toString(sprite.mRawTop));
                            out.attribute(null, "rotation", Float.toString(sprite.getRotation()));
                            out.attribute(null, "scale_x", Float.toString(sprite.getScaleX()));
                            out.attribute(null, "scale_y", Float.toString(sprite.getScaleY()));
                            out.attribute(null, "width", Integer.toString(sprite.mRawWidth));
                            out.attribute(null, "height", Integer.toString(sprite.mRawHeight));
                            out.attribute(null, "appearance", sprite.getAppearance().toString());
                            out.attribute(null, "function", sprite.getFunction().toString());
                            out.endTag(null, "sprite");
                        }
                    }
                    out.endTag(null, "screen");
                }
                out.endTag(null, "scene");
                out.flush();
                IoUtils.closeQuietly(os);
            } catch (Exception e) {
                ex = e;
                fileOutputStream = os;
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = os;
            }
        } catch (Exception e2) {
            ex = e2;
            try {
                throw new RuntimeException("save FreeStyle failed.", ex);
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(fileOutputStream);
                throw th;
            }
        }
    }

    private List<FreeButtonInfo> loadAllFreeButtons(float sceneScale) {
        List<FreeButtonInfo> freeButtons = new ArrayList();
        List<FileAccessable> files = getFile("skins/").list();
        if (files != null) {
            for (FileAccessable file : files) {
                float f = sceneScale;
                FreeButtonInfo info = new FreeButtonInfo(file, file.getName(), f, this.mContext, "skins/" + file.getName());
                if (info.isMamlGadget()) {
                    info.setMtzGadgetInfo(loadMtzGadgetInfo(file));
                }
                freeButtons.add(info);
            }
        }
        return freeButtons;
    }

    private List<MtzGadgetInfo> loadAllMtzGadgets() {
        List<MtzGadgetInfo> mtzGadgets = new ArrayList();
        List<FileAccessable> files = getFile("tools/").list();
        if (files != null) {
            for (FileAccessable file : files) {
                MtzGadgetInfo info = loadMtzGadgetInfo(file);
                if (info != null) {
                    mtzGadgets.add(info);
                }
            }
        }
        return mtzGadgets;
    }

    private MtzGadgetInfo loadMtzGadgetInfo(FileAccessable file) {
        InputStream descriptionStream;
        try {
            String title = "";
            if (file.isFile()) {
                if (file instanceof DeskFile) {
                    file = new ZipInnerFile(new ZipFile(((DeskFile) file).getFile()), "/");
                } else {
                    throw new RuntimeException("mtz gadget parse failed:not support zip into zip");
                }
            }
            FileAccessable previewFile = file.createBySubpath("preview/preview_cover_0.png");
            if (!previewFile.exists()) {
                previewFile = file.createBySubpath("preview/preview_cover_0.jpg");
                if (!previewFile.exists()) {
                    previewFile = file.createBySubpath("preview/0.png");
                }
            }
            Bitmap previewBitmap = decodeBitmapFromFile(null, 1.0f, previewFile);
            XmlPullParser descriptionXml = XmlPullParserFactory.newInstance().newPullParser();
            descriptionStream = file.createBySubpath("description.xml").getInputStream();
            descriptionXml.setInput(descriptionStream, null);
            String tag;
            do {
                tag = moveToNextStartTag(descriptionXml);
                if (tag != null) {
                }
                break;
            } while (!"title".equals(tag));
            title = descriptionXml.nextText().trim();
            IoUtils.closeQuietly(descriptionStream);
            MtzGadgetInfo mtzInfo = new MtzGadgetInfo();
            mtzInfo.path = "tools/" + file.getName();
            mtzInfo.title = title;
            mtzInfo.preview = previewBitmap;
            return mtzInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (Throwable th) {
            IoUtils.closeQuietly(descriptionStream);
        }
    }

    private void mergeAdditionSprites(FreeStyle main, FreeStyle addition) {
        if (addition != null) {
            mergeAdditionSpritesInScreen(main.getDriftScreen(), addition.getDriftScreen(), main);
            mergeAdditionSpritesInScreen(main.getDockScreen(), addition.getDockScreen(), main);
        }
    }

    private void mergeAdditionSpritesInScreen(Screen screenMain, Screen screenAddition, FreeStyle mainFreeStyle) {
        if (screenMain != null && screenAddition != null) {
            for (Sprite sprite : screenAddition.mSprites) {
                int index = Collections.binarySearch(screenMain.mSprites, sprite, sSpriteIndexComparator);
                if (index < 0) {
                    index = -1 - index;
                } else {
                    index++;
                }
                sprite.getAppearance().setFreeStyle(mainFreeStyle);
                sprite.mFreeStyle = mainFreeStyle;
                screenMain.mSprites.add(index, sprite);
            }
        }
    }

    private FreeStyle loadOriginalOrEdit() throws IOException, XmlPullParserException {
        File file = new File(getEditPersistSettingsPath(this.mContext));
        if (file.exists()) {
            return loadFreeStyle(new FileInputStream(file), false);
        }
        FileAccessable originalFile = Factory.create(DATA_PATH, "description.xml");
        if (originalFile.exists()) {
            return loadFreeStyle(originalFile.getInputStream(), false);
        }
        Log.e("FreeStyleSerializer", "file not exists. file:" + file.toString());
        return null;
    }

    private FreeStyle loadNew() throws XmlPullParserException, IOException {
        File fileNew = new File(getNewPersistSettingsPath(this.mContext));
        if (fileNew.exists()) {
            return loadFreeStyle(new FileInputStream(fileNew), true);
        }
        return null;
    }

    private FreeStyle loadFreeStyle(InputStream is, boolean isUserCreated) throws XmlPullParserException, IOException {
        FreeStyle freeStyle = null;
        if (is != null) {
            try {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(is, null);
                freeStyle = loadFreeStyle(parser, isUserCreated);
            } finally {
                IoUtils.closeQuietly(is);
            }
        }
        return freeStyle;
    }

    private FreeStyle loadFreeStyle(XmlPullParser parser, boolean isUserCreated) throws XmlPullParserException, IOException {
        FreeStyle freeStyle = new FreeStyle();
        loadCurrentSceneDescription(freeStyle, parser);
        while (true) {
            String tag = moveToNextStartTagOrEnd(parser, "scene");
            if (tag == null) {
                return freeStyle;
            }
            if (tag.equals("screen")) {
                Screen screen = loadScreen(parser, isUserCreated, freeStyle);
                if (screen != null) {
                    freeStyle.mScreens.add(screen);
                }
            } else {
                Log.w("FreeStyleSerializer", "unknown tag:" + tag);
            }
        }
    }

    private Screen loadScreen(XmlPullParser parser, boolean isUserCreated, FreeStyle freeStyle) throws XmlPullParserException, IOException {
        Screen screen = new Screen();
        String type = parser.getAttributeValue(null, "type");
        if ("drift".equals(type)) {
            screen.mType = 2;
        } else if ("background".equals(type)) {
            screen.mType = 1;
        } else if ("foreground".equals(type)) {
            screen.mType = 3;
        } else if ("dock".equals(type)) {
            screen.mType = 4;
        } else {
            Log.w("FreeStyleSerializer", "unknown screen type:" + type);
            return null;
        }
        screen.mRawWidth = parseIntValue(parser.getAttributeValue(null, "width"));
        screen.mWidth = calcSize(screen.mRawWidth, freeStyle.getSceneScale());
        screen.mHome = parseIntValue(parser.getAttributeValue(null, "home"));
        while (true) {
            String tag = moveToNextStartTagOrEnd(parser, "screen");
            if (tag == null) {
                return screen;
            }
            if ("sprite".equals(tag)) {
                Sprite sprite = loadSprite(parser, isUserCreated, freeStyle);
                if (sprite != null) {
                    screen.mSprites.add(sprite);
                }
            } else {
                Log.w("FreeStyleSerializer", "unknown tag:" + tag);
            }
        }
    }

    private Sprite loadSprite(XmlPullParser parser, boolean isUserCreated, FreeStyle freeStyle) throws IOException {
        Sprite sprite = new Sprite(freeStyle);
        sprite.mIsUserCreated = isUserCreated;
        sprite.mRawLeft = parseIntValue(parser.getAttributeValue(null, "left"));
        sprite.mLeft = calcSize(sprite.mRawLeft, freeStyle.getSceneScale());
        sprite.mRawTop = parseIntValue(parser.getAttributeValue(null, "top"));
        sprite.mTop = calcSize(sprite.mRawTop, freeStyle.getSceneScale());
        sprite.mRotation = parseFloatValue(parser.getAttributeValue(null, "rotation"), 0.0f);
        sprite.mScaleX = parseFloatValue(parser.getAttributeValue(null, "scale_x"), 0.0f);
        sprite.mScaleY = parseFloatValue(parser.getAttributeValue(null, "scale_y"), 0.0f);
        sprite.mRawWidth = parseIntValue(parser.getAttributeValue(null, "width"));
        sprite.mRawHeight = parseIntValue(parser.getAttributeValue(null, "height"));
        if (sprite.mRawWidth > 0 && sprite.mRawHeight > 0) {
            sprite.mWidth = calcSize(sprite.mRawWidth, freeStyle.getSceneScale());
            sprite.mHeight = calcSize(sprite.mRawHeight, freeStyle.getSceneScale());
        }
        sprite.mAppearance = Appearance.load(parser.getAttributeValue(null, "appearance"), freeStyle);
        sprite.mFunction = Function.parse(parser.getAttributeValue(null, "function"));
        sprite.mIndex = parseIntValue(parser.getAttributeValue(null, "index"));
        return sprite;
    }

    private void loadCurrentSceneDescription(FreeStyle freeStyle, XmlPullParser parser) throws XmlPullParserException, IOException {
        if ("scene".equals(moveToNextStartTag(parser))) {
            DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
            freeStyle.mHeight = dm.heightPixels;
            freeStyle.mWidth = dm.widthPixels;
            freeStyle.mRawWidth = parseIntValue(parser.getAttributeValue(null, "width"));
            freeStyle.mSceneScale = ((float) freeStyle.mWidth) / ((float) freeStyle.mRawWidth);
            return;
        }
        throw new RuntimeException("root tag name must be:scene");
    }

    private float parseFloatValue(String value, float defaultValue) {
        return TextUtils.isEmpty(value) ? defaultValue : Float.parseFloat(value);
    }

    private int parseIntValue(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        if (value.equalsIgnoreCase("match_parent")) {
            return -1;
        }
        return Integer.parseInt(value);
    }

    public String getPersistDirectory(Context context) {
        return context.getDir("free_style", DeviceConfig.TEMP_SHARE_MODE_FOR_WORLD_WRITEABLE).getAbsolutePath() + "/" + this.mFreeStyleName;
    }

    private String getEditPersistSettingsPath(Context context) {
        if (getPersistDirectory(context) == null) {
            return null;
        }
        File file = new File(getPersistDirectory(context));
        if (!file.exists()) {
            file.mkdirs();
        }
        return getPersistDirectory(context) + "/" + "freestyle_edit";
    }

    private String getNewPersistSettingsPath(Context context) {
        if (getPersistDirectory(context) == null) {
            return null;
        }
        File file = new File(getPersistDirectory(context));
        if (!file.exists()) {
            file.mkdirs();
        }
        return getPersistDirectory(context) + "/" + "freestyle_new";
    }

    public static String moveToNextStartTag(XmlPullParser parser) throws XmlPullParserException, IOException {
        return moveToNextStartTagOrEnd(parser, null);
    }

    public static String moveToNextStartTagOrEnd(XmlPullParser parser, String endTag) throws XmlPullParserException, IOException {
        while (true) {
            int eventType = parser.next();
            if (eventType == 2 || eventType == 1) {
                if (eventType == 1) {
                    return parser.getName().trim();
                }
                return null;
            } else if (endTag != null && eventType == 3 && endTag.equals(parser.getName())) {
                return null;
            }
        }
        if (eventType == 1) {
            return null;
        }
        return parser.getName().trim();
    }

    public static Bitmap decodeBitmapFromFile(HashMap<FileAccessable, Bitmap> bitmapCache, float sceneScale, FileAccessable file) throws IOException {
        if (bitmapCache != null && bitmapCache.containsKey(file)) {
            return (Bitmap) bitmapCache.get(file);
        }
        InputStreamLoader streamLoader = new InputStreamLoader(file);
        Options opt = new Options();
        opt.inJustDecodeBounds = true;
        InputStream is = file.getInputStream();
        BitmapFactory.decodeStream(is, null, opt);
        Utilities.closeFileSafely(is);
        int width = calcSize(opt.outWidth, sceneScale);
        int height = calcSize(opt.outHeight, sceneScale);
        boolean lowQuality = false;
        if (SpecificDeviceConfig.isBigScreenLowMemory() && width * height > (DeviceConfig.getScreenHeight() * DeviceConfig.getScreenWidth()) / 8) {
            lowQuality = true;
            width = (int) (((float) width) * 0.6666667f);
            height = (int) (((float) height) * 0.6666667f);
        }
        Bitmap bitmap = getBitmap(streamLoader, width, height);
        if (lowQuality) {
            bitmap.setDensity((int) (((float) bitmap.getDensity()) * 0.6666667f));
        }
        if (bitmapCache != null) {
            bitmapCache.put(file, bitmap);
        }
        return bitmap;
    }

    private static Bitmap getBitmap(InputStreamLoader streamLoader, int destWidth, int destHeight) {
        int pixelSize = destWidth * destHeight;
        if (destWidth <= 0 || destHeight <= 0) {
            pixelSize = -1;
        }
        Options options = new Options();
        options.inSampleSize = 1;
        options.inScaled = false;
        if (pixelSize > 0) {
            Options optSize = new Options();
            optSize.inJustDecodeBounds = true;
            try {
                BitmapFactory.decodeStream(streamLoader.get(), null, optSize);
                options.inSampleSize = (int) Math.sqrt((((double) optSize.outWidth) * ((double) optSize.outHeight)) / ((double) pixelSize));
            } finally {
                streamLoader.close();
            }
        }
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(streamLoader.get(), null, options);
            if (pixelSize <= 0) {
                return bitmap;
            }
            if (bitmap.getWidth() == destWidth && bitmap.getHeight() == destHeight) {
                return bitmap;
            }
            if (bitmap.getHeight() < destHeight) {
                bitmap.setDensity((int) (((float) bitmap.getDensity()) * (((float) bitmap.getHeight()) / ((float) destHeight))));
                return bitmap;
            }
            Bitmap srcBmp = bitmap;
            bitmap = BitmapFactory.scaleBitmap(srcBmp, destWidth, destHeight);
            srcBmp.recycle();
            return bitmap;
        } finally {
            streamLoader.close();
        }
    }

    static int calcSize(int rawSize, float sceneScale) {
        return (int) ((((float) rawSize) * sceneScale) + 0.5f);
    }
}
