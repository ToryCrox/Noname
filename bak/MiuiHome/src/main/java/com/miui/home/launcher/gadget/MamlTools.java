package com.miui.home.launcher.gadget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.SystemClock;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.Utilities;
import java.io.File;
import miui.maml.ResourceLoader;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.elements.AdvancedSlider;
import miui.maml.elements.ScreenElement;
import miui.maml.elements.ScreenElementFactory;
import miui.maml.util.Utils;
import miui.maml.util.ZipResourceLoader;
import org.w3c.dom.Element;

public class MamlTools {

    static class LockscreenElementFactory extends ScreenElementFactory {
        LockscreenElementFactory() {
        }

        public ScreenElement createInstance(Element ele, ScreenElementRoot root) {
            if (ele.getTagName().equalsIgnoreCase("Unlocker")) {
                return new AdvancedSlider(ele, root);
            }
            return super.createInstance(ele, root);
        }
    }

    public static Bitmap snapshootLockscreen(Context context, int wallpaperColorMode) {
        ResourceLoader resourceLoader;
        int transX = 0;
        int transY = 0;
        String dataPath = "/data/system/theme/lockscreen";
        if (new File(dataPath).exists()) {
            resourceLoader = new ZipResourceLoader(dataPath, "advance/");
        } else {
            dataPath = "/system/media/theme/.data/content/clock_2x4/clock.mrc";
            if (!new File(dataPath).exists()) {
                return null;
            }
            transX = DeviceConfig.getWorkspaceCellPaddingSide();
            transY = DeviceConfig.getStatusBarHeight() + DeviceConfig.getWorkspaceCellPaddingTop();
            resourceLoader = new ZipResourceLoader(dataPath);
        }
        resourceLoader.setLocal(context.getResources().getConfiguration().locale);
        ScreenContext screenContext = new ScreenContext(context, resourceLoader, new LockscreenElementFactory());
        ScreenElementRoot root = new ScreenElementRoot(screenContext);
        if (!root.load()) {
            return null;
        }
        root.init();
        Utils.putVariableNumber("applied_light_wallpaper", screenContext.mVariables, wallpaperColorMode == 2 ? 1.0d : 0.0d);
        Bitmap result = Utilities.createBitmapSafely(DeviceConfig.getScreenWidth(), DeviceConfig.getScreenHeight(), Config.ARGB_8888);
        if (result == null) {
            return null;
        }
        Canvas canvas = new Canvas(result);
        canvas.translate((float) transX, (float) transY);
        root.tick(SystemClock.elapsedRealtime());
        root.render(canvas);
        root.finish();
        Utils.putVariableNumber("applied_light_wallpaper", screenContext.mVariables, WallpaperUtils.hasAppliedLightWallpaper() ? 1.0d : 0.0d);
        return result;
    }

    public static boolean usingDefaultLockScreen() {
        if (new File("/data/system/theme/lockscreen").exists()) {
            return false;
        }
        return true;
    }
}
