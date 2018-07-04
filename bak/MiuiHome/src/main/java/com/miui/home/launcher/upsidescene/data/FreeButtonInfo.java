package com.miui.home.launcher.upsidescene.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.miui.home.launcher.ItemInfo;
import com.miui.home.launcher.upsidescene.data.FreeStyle.MtzGadgetInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import libcore.io.IoUtils;
import miui.maml.ScreenContext;
import miui.maml.util.ZipResourceLoader;
import miui.util.FileAccessable;
import miui.util.FileAccessable.DeskFile;
import miui.util.FileAccessable.ZipInnerFile;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class FreeButtonInfo extends ItemInfo {
    private HashMap<FileAccessable, Bitmap> mBitmapCache;
    FileAccessable mFile;
    ScreenContext mMamlContext;
    MtzGadgetInfo mMtzGadgetInfo;
    String mName;
    String mPackageName;
    Bitmap mPreviewImage;
    FileAccessable mPreviewImageName;
    float mScale;
    HashMap<String, Animation> mStateAnimations;

    class Animation {
        public ArrayList<Pair<FileAccessable, Integer>> frames = new ArrayList();
        public boolean oneshot = true;

        Animation() {
        }
    }

    public FreeButtonInfo(FileAccessable file, String packageName, float scale, Context context, String relativePath) {
        this.mStateAnimations = new HashMap();
        this.mBitmapCache = new HashMap();
        this.itemType = 10;
        this.mFile = file;
        this.mPackageName = packageName;
        this.mScale = scale;
        if (file.isDirectory() && file.createBySubpath("manifest.xml").exists()) {
            if (file instanceof ZipInnerFile) {
                if (!relativePath.endsWith("/")) {
                    relativePath = relativePath + "/";
                }
                this.mMamlContext = new ScreenContext(context, new ZipResourceLoader(FreeStyleSerializer.DATA_PATH, relativePath));
            } else if (file instanceof DeskFile) {
                this.mMamlContext = new ScreenContext(context, new ZipResourceLoader(((DeskFile) file).getFile().getPath()));
            }
            Element root = this.mMamlContext.mResourceManager.getManifestRoot();
            if (!"free_gadget".equalsIgnoreCase(root.getNodeName())) {
                throw new RuntimeException("bad root tag " + root.getNodeName());
            }
        }
    }

    public boolean isMamlGadget() {
        return this.mMamlContext != null;
    }

    public ScreenContext getMamlContext() {
        return this.mMamlContext;
    }

    public void setMtzGadgetInfo(MtzGadgetInfo mtzGadgetInfo) {
        this.mMtzGadgetInfo = mtzGadgetInfo;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    void loadIfNeed() throws XmlPullParserException, IOException {
        if (this.mStateAnimations.size() > 0 || !this.mFile.exists()) {
            return;
        }
        if (this.mFile.isFile()) {
            this.mName = getFileNameNoEx(this.mPackageName);
            this.mPreviewImageName = this.mFile;
            addStaticPictureIfExists("normal", this.mFile, true);
        } else if (this.mFile.createBySubpath("description.xml").exists()) {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            InputStream is = this.mFile.createBySubpath("description.xml").getInputStream();
            parser.setInput(is, null);
            if ("root".equals(FreeStyleSerializer.moveToNextStartTag(parser))) {
                this.mName = parser.getAttributeValue(null, "name");
                if (TextUtils.isEmpty(this.mName)) {
                    this.mName = this.mPackageName;
                }
                while (true) {
                    String tag = FreeStyleSerializer.moveToNextStartTag(parser);
                    if (tag == null) {
                        IoUtils.closeQuietly(is);
                        return;
                    } else if ("animation-list".equals(tag)) {
                        Animation animation = new Animation();
                        String state = parser.getAttributeValue(null, "state");
                        if (TextUtils.isEmpty(state)) {
                            Log.w("FreeButtonInfo", "xml parse failed:free button animation must have 'state'");
                        } else {
                            String strOneshot = parser.getAttributeValue(null, "oneshot");
                            if (!TextUtils.isEmpty(strOneshot)) {
                                animation.oneshot = Boolean.parseBoolean(strOneshot);
                            }
                            while (true) {
                                tag = FreeStyleSerializer.moveToNextStartTagOrEnd(parser, "animation-list");
                                if (tag == null) {
                                    break;
                                } else if ("item".equals(tag)) {
                                    String pic = parser.getAttributeValue(null, "image");
                                    if (this.mPreviewImageName == null && "normal".equals(state)) {
                                        this.mPreviewImageName = this.mFile.createBySubpath(pic);
                                    }
                                    String strDuration = parser.getAttributeValue(null, "duration");
                                    int duration = 20;
                                    if (!TextUtils.isEmpty(strDuration)) {
                                        try {
                                            duration = Integer.parseInt(strDuration);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } catch (Throwable th) {
                                            IoUtils.closeQuietly(is);
                                        }
                                    }
                                    animation.frames.add(new Pair(this.mFile.createBySubpath(pic), Integer.valueOf(duration)));
                                }
                            }
                            this.mStateAnimations.put(state, animation);
                        }
                    }
                }
            } else {
                IoUtils.closeQuietly(is);
            }
        } else {
            this.mName = this.mPackageName;
            this.mPreviewImageName = addStaticPictureIfExists("normal", this.mFile.createBySubpath(this.mPackageName + "_normal"), false);
            addStaticPictureIfExists("normal_pressed", this.mFile.createBySubpath(this.mPackageName + "_normal_pressed"), false);
            addStaticPictureIfExists("open", this.mFile.createBySubpath(this.mPackageName + "_open"), false);
            addStaticPictureIfExists("open_pressed", this.mFile.createBySubpath(this.mPackageName + "_open_pressed"), false);
        }
    }

    public static String getFileNameNoEx(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        int dot = fileName.lastIndexOf(46);
        if (dot != -1) {
            return fileName.substring(0, dot);
        }
        return fileName;
    }

    private FileAccessable addStaticPictureIfExists(String state, FileAccessable file, boolean pathIncludeExtension) {
        FileAccessable finalFile = file;
        if (!pathIncludeExtension) {
            finalFile = file.createByExtension(".png");
            if (!finalFile.exists()) {
                finalFile = file.createByExtension(".jpg");
                if (!finalFile.exists()) {
                    return null;
                }
            }
        } else if (!file.exists()) {
            return null;
        }
        Animation animation = new Animation();
        animation.frames.add(new Pair(finalFile, Integer.valueOf(0)));
        this.mStateAnimations.put(state, animation);
        return finalFile;
    }

    public Drawable getDrawable(String state, Context context) {
        try {
            loadIfNeed();
            Animation animation = (Animation) this.mStateAnimations.get(state);
            if (animation == null) {
                return null;
            }
            if (animation.frames.size() == 1) {
                return getBitmapDrawable((FileAccessable) ((Pair) animation.frames.get(0)).first, context);
            }
            Drawable animDrawable = new AnimationDrawable();
            animDrawable.setOneShot(animation.oneshot);
            Iterator i$ = animation.frames.iterator();
            while (i$.hasNext()) {
                Pair<FileAccessable, Integer> frame = (Pair) i$.next();
                animDrawable.addFrame(getBitmapDrawable((FileAccessable) frame.first, context), ((Integer) frame.second).intValue());
            }
            return animDrawable;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private BitmapDrawable getBitmapDrawable(FileAccessable file, Context context) throws IOException {
        return new BitmapDrawable(context.getResources(), FreeStyleSerializer.decodeBitmapFromFile(this.mBitmapCache, this.mScale, file)) {
        };
    }

    public Bitmap getPreviewImage() {
        if (this.mMtzGadgetInfo != null) {
            return this.mMtzGadgetInfo.preview;
        }
        try {
            loadIfNeed();
            this.mPreviewImage = FreeStyleSerializer.decodeBitmapFromFile(this.mBitmapCache, this.mScale, this.mPreviewImageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.mPreviewImage;
    }

    public FileAccessable getFile() {
        return this.mFile;
    }
}
