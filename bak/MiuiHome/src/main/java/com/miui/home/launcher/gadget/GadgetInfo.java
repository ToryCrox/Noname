package com.miui.home.launcher.gadget;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import com.miui.home.R;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.ItemInfo;
import com.miui.home.launcher.common.Utilities;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class GadgetInfo extends ItemInfo {
    private static final int DEFAULT_WIDGET_CELL_HEIGHT = Utilities.getDipPixelSize(74 - Utilities.getDipPixelSize(1));
    private static final int DEFAULT_WIDGET_CELL_WIDTH = Utilities.getDipPixelSize(80 - Utilities.getDipPixelSize(1));
    private static final int[] GADGET_TITLE_ID = new int[]{R.string.gadget_clear_button_label, R.string.widget_gadget_player, R.string.gadget_clock_label, R.string.gadget_photo_label, R.string.gadget_weather_title, R.string.gadget_search_label, R.string.gadget_calendar_label, R.string.gadget_notes_label, R.string.gadget_calculator_label, R.string.gadget_mtz_label};
    private static final HashMap<String, Integer> sCategoryMaps = new HashMap();
    private int mCategoryId;
    private int mGadgetId;
    private Drawable mIcon;
    private int mIconResId;
    private ZipFile mMtzFile;
    private boolean mMtzLoaded;
    private int mMtzMockWidgetId;
    private ComponentName mMtzMockWidgetProvider;
    private String mMtzTitle;
    private Uri mMtzUri;
    private Drawable mPreview;
    private int mPreviewImageResId;
    private int mTitleResId;

    static {
        sCategoryMaps.put("clean", Integer.valueOf(0));
        sCategoryMaps.put("player", Integer.valueOf(1));
        sCategoryMaps.put("clock", Integer.valueOf(2));
        sCategoryMaps.put("photo", Integer.valueOf(3));
        sCategoryMaps.put("weather", Integer.valueOf(4));
        sCategoryMaps.put("search", Integer.valueOf(5));
        sCategoryMaps.put("calendar", Integer.valueOf(6));
        sCategoryMaps.put("notes", Integer.valueOf(7));
        sCategoryMaps.put("calculator", Integer.valueOf(8));
    }

    public GadgetInfo(int gadgetId) {
        this.mPreviewImageResId = -1;
        this.mCategoryId = -1;
        this.mMtzFile = null;
        this.mMtzUri = null;
        this.mMtzLoaded = false;
        this.mMtzMockWidgetProvider = null;
        this.mMtzMockWidgetId = -1;
        this.mGadgetId = gadgetId;
        this.itemType = 5;
    }

    public GadgetInfo(int gadgetId, int spanX, int spanY, int titleResId, int iconResId, int previewResId, int categoryId) {
        this(gadgetId);
        this.spanX = DeviceConfig.getWidgetSpanX(DEFAULT_WIDGET_CELL_WIDTH * spanX);
        this.spanY = DeviceConfig.getWidgetSpanY(DEFAULT_WIDGET_CELL_HEIGHT * spanY);
        this.mTitleResId = titleResId;
        this.mIconResId = iconResId;
        this.mPreviewImageResId = previewResId;
        this.mCategoryId = categoryId;
    }

    public GadgetInfo(Uri mtzUri) {
        this(1000);
        this.mMtzUri = mtzUri;
    }

    public int getGadgetId() {
        return this.mGadgetId;
    }

    public String getTitle(Context context) {
        return isMtzGadget() ? this.mMtzTitle : context.getResources().getString(this.mTitleResId);
    }

    public Drawable getIcon(Context context) {
        if (this.mIcon == null) {
            try {
                ZipFile mtz = getMtzZipFile();
                if (mtz != null) {
                    this.mIcon = getMtzInnerDrawable(context, mtz, "thumbnail_" + Locale.getDefault().toString() + "/thumbnail_xhdpi.png");
                    if (this.mIcon == null) {
                        this.mIcon = getMtzInnerDrawable(context, mtz, "thumbnail/thumbnail_xhdpi.png");
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (this.mIcon == null) {
                this.mIcon = context.getResources().getDrawable(this.mIconResId);
            }
        }
        return this.mIcon;
    }

    public Drawable getPreviewImage(Context context) {
        if (this.mPreview == null) {
            try {
                ZipFile mtz = getMtzZipFile();
                if (mtz != null) {
                    this.mPreview = getMtzInnerDrawable(context, mtz, "preview_" + Locale.getDefault().toString() + "/preview_0.jpg");
                    if (this.mPreview == null) {
                        this.mPreview = getMtzInnerDrawable(context, mtz, "preview/preview_0.jpg");
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (this.mPreview == null && this.mPreviewImageResId != -1) {
                this.mPreview = context.getResources().getDrawable(this.mPreviewImageResId);
            }
        }
        return this.mPreview;
    }

    public int getCategoryId() {
        return (this.mCategoryId == -1 && isMtzGadget()) ? 1000 : this.mCategoryId;
    }

    public String getCategoryTitle(Context context) {
        return context.getResources().getString(GADGET_TITLE_ID[this.mCategoryId]);
    }

    public Uri getMtzUri() {
        return this.mMtzUri;
    }

    public boolean loadMtzGadgetFromUri(Uri uri) {
        this.mMtzUri = uri;
        try {
            return loadMtzGadget();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
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

    public boolean getBoolean(String boolTag) {
        boolean result = false;
        if (isMtzGadget()) {
            HashMap<String, String> localeTitle = new HashMap();
            ZipFile mtz = null;
            InputStream desc = null;
            try {
                mtz = getMtzZipFile();
                desc = getMtzInnerInputStream(mtz, "description.xml");
                if (desc != null) {
                    XmlPullParser descriptionXml = XmlPullParserFactory.newInstance().newPullParser();
                    descriptionXml.setInput(desc, null);
                    String tag;
                    do {
                        tag = moveToNextStartTag(descriptionXml);
                        if (tag == null) {
                            break;
                        }
                    } while (!boolTag.equals(tag));
                    result = Boolean.parseBoolean(descriptionXml.nextText().trim());
                }
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (XmlPullParserException e2) {
                e2.printStackTrace();
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (NumberFormatException e4) {
                e4.printStackTrace();
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (IOException e322) {
                e322.printStackTrace();
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (Throwable th) {
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            }
        }
        return result;
    }

    public Date getDate(String dateTag) {
        Date result = null;
        if (isMtzGadget()) {
            HashMap<String, String> localeTitle = new HashMap();
            ZipFile mtz = null;
            InputStream desc = null;
            try {
                mtz = getMtzZipFile();
                desc = getMtzInnerInputStream(mtz, "description.xml");
                if (desc != null) {
                    XmlPullParser descriptionXml = XmlPullParserFactory.newInstance().newPullParser();
                    descriptionXml.setInput(desc, null);
                    String tag;
                    do {
                        tag = moveToNextStartTag(descriptionXml);
                        if (tag == null) {
                            break;
                        }
                    } while (!dateTag.equals(tag));
                    result = stringToDate(descriptionXml.nextText().trim(), "yyyy-MM-dd HH:mm:ss");
                }
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (XmlPullParserException e2) {
                e2.printStackTrace();
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (NumberFormatException e4) {
                e4.printStackTrace();
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (IOException e322) {
                e322.printStackTrace();
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e3222) {
                        e3222.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (Throwable th) {
                if (desc != null) {
                    try {
                        desc.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (mtz != null) {
                    mtz.close();
                }
            }
        }
        return result;
    }

    private Date stringToDate(String aDate, String aFormat) {
        if (aDate == null) {
            return null;
        }
        return new SimpleDateFormat(aFormat).parse(aDate, new ParsePosition(0));
    }

    public boolean loadMtzGadget() throws IOException {
        if (this.mMtzLoaded) {
            return true;
        }
        if (isMtzGadget()) {
            HashMap<String, String> localeTitle = new HashMap();
            ZipFile mtz = null;
            InputStream desc = null;
            try {
                mtz = getMtzZipFile();
                desc = getMtzInnerInputStream(mtz, "description.xml");
                if (desc != null) {
                    XmlPullParser descriptionXml = XmlPullParserFactory.newInstance().newPullParser();
                    descriptionXml.setInput(desc, null);
                    while (true) {
                        String tag = moveToNextStartTag(descriptionXml);
                        if (tag == null) {
                            break;
                        } else if ("MIUI-Theme".equals(tag)) {
                            String size = descriptionXml.getAttributeValue(null, "size");
                            if (size != null) {
                                String[] sizeVal = size.split(":");
                                if (sizeVal.length == 2) {
                                    this.spanX = Integer.parseInt(sizeVal[0]);
                                    this.spanY = Integer.parseInt(sizeVal[1]);
                                }
                            }
                            String category = descriptionXml.getAttributeValue(null, "category");
                            if (!TextUtils.isEmpty(category) && sCategoryMaps.containsKey(category)) {
                                this.mCategoryId = ((Integer) sCategoryMaps.get(category)).intValue();
                            }
                        } else if ("title".equals(tag)) {
                            String locale = descriptionXml.getAttributeValue(null, "locale");
                            if (locale == null) {
                                locale = "";
                            }
                            localeTitle.put(locale, descriptionXml.nextText().trim());
                        } else if ("mock_widget".equals(tag)) {
                            this.mMtzMockWidgetProvider = ComponentName.unflattenFromString(descriptionXml.nextText().trim());
                        }
                    }
                }
                if (desc != null) {
                    desc.close();
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                if (desc != null) {
                    desc.close();
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (NumberFormatException e2) {
                e2.printStackTrace();
                if (desc != null) {
                    desc.close();
                }
                if (mtz != null) {
                    mtz.close();
                }
            } catch (Throwable th) {
                if (desc != null) {
                    desc.close();
                }
                if (mtz != null) {
                    mtz.close();
                }
            }
            if (this.spanX > 0 && this.spanY > 0) {
                this.mCategoryId = this.mCategoryId != -1 ? this.mCategoryId : 9;
                this.mMtzTitle = (String) localeTitle.get(Locale.getDefault().toString());
                if (this.mMtzTitle == null) {
                    this.mMtzTitle = (String) localeTitle.get(Locale.US.toString());
                }
                if (this.mMtzTitle == null) {
                    this.mMtzTitle = (String) localeTitle.get("");
                }
                this.mMtzLoaded = true;
                return true;
            }
        }
        this.mMtzUri = null;
        return false;
    }

    private Drawable getMtzInnerDrawable(Context context, ZipFile mtz, String entryName) throws IOException {
        if (!isMtzGadget()) {
            return null;
        }
        InputStream is = getMtzInnerInputStream(mtz, entryName);
        if (is == null) {
            return null;
        }
        Drawable d = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is));
        is.close();
        return d;
    }

    private InputStream getMtzInnerInputStream(ZipFile mtz, String entryName) throws IOException {
        if (mtz != null) {
            try {
                ZipEntry entry = mtz.getEntry(entryName);
                if (entry != null) {
                    return mtz.getInputStream(entry);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isMtzGadget() {
        return this.mGadgetId >= 1000;
    }

    private ZipFile getMtzZipFile() throws IOException {
        if (this.mMtzFile != null || this.mMtzUri == null) {
            return null;
        }
        return new ZipFile(this.mMtzUri.getPath());
    }

    public ComponentName getMtzMockWidgetProvider() {
        return this.mMtzMockWidgetProvider;
    }

    public void setMtzMockWidgetId(int id) {
        this.mMtzMockWidgetId = id;
    }

    public int getMtzMockWidgetId() {
        return this.mMtzMockWidgetId;
    }

    public void load(Context context, Cursor c) {
        super.load(context, c);
        if (isMtzGadget()) {
            String uri = c.getString(15);
            if (uri != null) {
                loadMtzGadgetFromUri(Uri.parse(uri));
            }
            if (!c.isNull(9)) {
                this.mMtzMockWidgetId = c.getInt(9);
            }
        }
    }

    public void onAddToDatabase(Context context, ContentValues values) {
        super.onAddToDatabase(context, values);
        values.put("appWidgetId", Integer.valueOf(this.mGadgetId));
        if (isMtzGadget()) {
            values.put("uri", this.mMtzUri.toString());
            if (this.mMtzMockWidgetId != -1) {
                values.put("appWidgetId", Integer.valueOf(this.mMtzMockWidgetId));
            }
        }
    }

    public boolean isValid() {
        return (isMtzGadget() && this.mMtzUri == null) ? false : true;
    }

    public GadgetInfo clone() {
        return (GadgetInfo) super.clone();
    }

    public String toString() {
        return "Gadget(id=" + Integer.toString(this.mGadgetId) + ")";
    }
}
