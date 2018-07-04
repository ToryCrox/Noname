package miui.external;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import miui.external.SdkConstants.SdkError;

public class Application extends android.app.Application implements SdkConstants {
    private boolean a;
    private boolean b;
    private ApplicationDelegate c;

    public Application() {
        if (a() && b()) {
            this.a = true;
        }
    }

    private boolean a() {
        try {
            if (c.isMiuiSystem() || d.load(c.getApkPath(null, "com.miui.core", "miui"), null, c.getLibPath(null, "com.miui.core"), Application.class.getClassLoader())) {
                return true;
            }
            b.a(SdkError.NO_SDK);
            return false;
        } catch (Throwable th) {
            a(th);
            return false;
        }
    }

    private boolean b() {
        try {
            HashMap hashMap = new HashMap();
            int intValue = ((Integer) a.g().getMethod("initialize", new Class[]{android.app.Application.class, Map.class}).invoke(null, new Object[]{this, hashMap})).intValue();
            if (intValue == 0) {
                return true;
            }
            a("initialize", intValue);
            return false;
        } catch (Throwable th) {
            a(th);
            return false;
        }
    }

    private boolean c() {
        try {
            HashMap hashMap = new HashMap();
            int intValue = ((Integer) a.g().getMethod("start", new Class[]{Map.class}).invoke(null, new Object[]{hashMap})).intValue();
            if (intValue == 1) {
                b.a(SdkError.LOW_SDK_VERSION);
                return false;
            } else if (intValue == 0) {
                return true;
            } else {
                a("start", intValue);
                return false;
            }
        } catch (Throwable th) {
            a(th);
            return false;
        }
    }

    private void a(Throwable th) {
        while (th != null && th.getCause() != null) {
            if (!(th instanceof InvocationTargetException)) {
                if (!(th instanceof ExceptionInInitializerError)) {
                    break;
                }
                th = th.getCause();
            } else {
                th = th.getCause();
            }
        }
        Log.e("miuisdk", "MIUI SDK encounter errors, please contact miuisdk@xiaomi.com for support.", th);
        b.a(SdkError.GENERIC);
    }

    private void a(String str, int i) {
        Log.e("miuisdk", "MIUI SDK encounter errors, please contact miuisdk@xiaomi.com for support. phase: " + str + " code: " + i);
        b.a(SdkError.GENERIC);
    }

    public ApplicationDelegate onCreateApplicationDelegate() {
        return null;
    }

    public final ApplicationDelegate getApplicationDelegate() {
        return this.c;
    }

    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        if (this.a && c()) {
            this.c = onCreateApplicationDelegate();
            if (this.c != null) {
                this.c.a(this);
            }
            this.b = true;
        }
    }

    public final void onCreate() {
        if (!this.b) {
            return;
        }
        if (this.c != null) {
            this.c.onCreate();
        } else {
            d();
        }
    }

    final void d() {
        super.onCreate();
    }

    public final void onTerminate() {
        if (this.c != null) {
            this.c.onTerminate();
        } else {
            e();
        }
    }

    final void e() {
        super.onTerminate();
    }

    public final void onLowMemory() {
        if (this.c != null) {
            this.c.onLowMemory();
        } else {
            f();
        }
    }

    final void f() {
        super.onLowMemory();
    }

    public final void onTrimMemory(int i) {
        if (this.c != null) {
            this.c.onTrimMemory(i);
        } else {
            a(i);
        }
    }

    final void a(int i) {
        super.onTrimMemory(i);
    }

    public final void onConfigurationChanged(Configuration configuration) {
        if (this.c != null) {
            this.c.onConfigurationChanged(configuration);
        } else {
            a(configuration);
        }
    }

    final void a(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }
}
