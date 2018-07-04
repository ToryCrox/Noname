package miui.external;

import android.util.Log;

class a implements SdkConstants {
    public static Class<?> g() throws ClassNotFoundException {
        Class<?> cls;
        try {
            cls = Class.forName("miui.core.SdkManager");
        } catch (ClassNotFoundException e) {
            try {
                cls = Class.forName("com.miui.internal.core.SdkManager");
                Log.w("miuisdk", "using legacy sdk");
            } catch (ClassNotFoundException e2) {
                Log.e("miuisdk", "no sdk found");
                throw e2;
            }
        }
        return cls;
    }
}
