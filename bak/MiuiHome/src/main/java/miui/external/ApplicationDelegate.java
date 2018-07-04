package miui.external;

import android.content.ComponentCallbacks;
import android.content.ComponentCallbacks2;
import android.content.ContextWrapper;
import android.content.res.Configuration;

public abstract class ApplicationDelegate extends ContextWrapper implements ComponentCallbacks2 {
    private Application d;

    public ApplicationDelegate() {
        super(null);
    }

    void a(Application application) {
        this.d = application;
        attachBaseContext(application);
    }

    public void onCreate() {
        this.d.d();
    }

    public void onTerminate() {
        this.d.e();
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.d.a(configuration);
    }

    public void onLowMemory() {
        this.d.f();
    }

    public void onTrimMemory(int i) {
        this.d.a(i);
    }

    public void registerComponentCallbacks(ComponentCallbacks componentCallbacks) {
        this.d.registerComponentCallbacks(componentCallbacks);
    }

    public void unregisterComponentCallbacks(ComponentCallbacks componentCallbacks) {
        this.d.unregisterComponentCallbacks(componentCallbacks);
    }
}
