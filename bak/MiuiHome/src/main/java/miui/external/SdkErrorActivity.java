package miui.external;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import miui.external.SdkConstants.SdkError;

public class SdkErrorActivity extends Activity implements SdkConstants {
    private String h;
    private OnClickListener i = new OnClickListener(this) {
        final /* synthetic */ SdkErrorActivity k;

        {
            this.k = r1;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            this.k.finish();
            System.exit(0);
        }
    };
    private OnClickListener j = new OnClickListener(this) {
        final /* synthetic */ SdkErrorActivity k;

        {
            this.k = r1;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            final Dialog a = this.k.k();
            new a(this.k, a).show(this.k.getFragmentManager(), "SdkUpdatePromptDialog");
            new AsyncTask<Void, Void, Boolean>(this) {
                final /* synthetic */ AnonymousClass2 m;

                protected /* synthetic */ Object doInBackground(Object[] objArr) {
                    return a((Void[]) objArr);
                }

                protected /* synthetic */ void onPostExecute(Object obj) {
                    a((Boolean) obj);
                }

                protected Boolean a(Void... voidArr) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Boolean.valueOf(this.m.k.o());
                }

                protected void a(Boolean bool) {
                    a.dismiss();
                    new a(this.m.k, bool.booleanValue() ? this.m.k.l() : this.m.k.m()).show(this.m.k.getFragmentManager(), "SdkUpdateFinishDialog");
                }
            }.execute(new Void[0]);
        }
    };

    class a extends DialogFragment {
        final /* synthetic */ SdkErrorActivity k;
        private Dialog o;

        public a(SdkErrorActivity sdkErrorActivity, Dialog dialog) {
            this.k = sdkErrorActivity;
            this.o = dialog;
        }

        public Dialog onCreateDialog(Bundle bundle) {
            return this.o;
        }
    }

    protected void onCreate(Bundle bundle) {
        Dialog i;
        setTheme(16973909);
        super.onCreate(bundle);
        this.h = Locale.getDefault().getLanguage();
        SdkError sdkError = null;
        Intent intent = getIntent();
        if (intent != null) {
            sdkError = (SdkError) intent.getSerializableExtra("com.miui.sdk.error");
        }
        if (sdkError == null) {
            sdkError = SdkError.GENERIC;
        }
        switch (sdkError) {
            case NO_SDK:
                i = i();
                break;
            case LOW_SDK_VERSION:
                i = j();
                break;
            default:
                i = h();
                break;
        }
        new a(this, i).show(getFragmentManager(), "SdkErrorPromptDialog");
    }

    private Dialog a(String str, String str2, OnClickListener onClickListener) {
        return new Builder(this).setTitle(str).setMessage(str2).setPositiveButton(17039370, onClickListener).setIcon(17301543).setCancelable(false).create();
    }

    private Dialog a(String str, String str2, OnClickListener onClickListener, OnClickListener onClickListener2) {
        return new Builder(this).setTitle(str).setMessage(str2).setPositiveButton(17039370, onClickListener).setNegativeButton(17039360, onClickListener2).setIcon(17301543).setCancelable(false).create();
    }

    private Dialog h() {
        String str;
        String str2;
        if (Locale.CHINESE.getLanguage().equals(this.h)) {
            str = "MIUI SDK发生错误";
            str2 = "请重新安装MIUI SDK再运行本程序。";
        } else {
            str = "MIUI SDK encounter errors";
            str2 = "Please re-install MIUI SDK and then re-run this application.";
        }
        return a(str, str2, this.i);
    }

    private Dialog i() {
        String str;
        String str2;
        if (Locale.CHINESE.getLanguage().equals(this.h)) {
            str = "没有找到MIUI SDK";
            str2 = "请先安装MIUI SDK再运行本程序。";
        } else {
            str = "MIUI SDK not found";
            str2 = "Please install MIUI SDK and then re-run this application.";
        }
        return a(str, str2, this.i);
    }

    private Dialog j() {
        String str;
        String str2;
        if (n()) {
            if (Locale.CHINESE.getLanguage().equals(this.h)) {
                str = "MIUI SDK版本过低";
                str2 = "请先升级MIUI SDK再运行本程序。是否现在升级？";
            } else {
                str = "MIUI SDK too old";
                str2 = "Please upgrade MIUI SDK and then re-run this application. Upgrade now?";
            }
            return a(str, str2, this.j, this.i);
        }
        if (Locale.CHINESE.getLanguage().equals(this.h)) {
            str = "MIUI SDK版本过低";
            str2 = "请先升级MIUI SDK再运行本程序。";
        } else {
            str = "MIUI SDK too old";
            str2 = "Please upgrade MIUI SDK and then re-run this application.";
        }
        return a(str, str2, this.i);
    }

    private Dialog k() {
        CharSequence charSequence;
        CharSequence charSequence2;
        if (Locale.CHINESE.getLanguage().equals(this.h)) {
            charSequence = "MIUI SDK正在更新";
            charSequence2 = "请稍候...";
        } else {
            charSequence = "MIUI SDK updating";
            charSequence2 = "Please wait...";
        }
        return ProgressDialog.show(this, charSequence, charSequence2, true, false);
    }

    private Dialog l() {
        String str;
        String str2;
        if (Locale.CHINESE.getLanguage().equals(this.h)) {
            str = "MIUI SDK更新完成";
            str2 = "请重新运行本程序。";
        } else {
            str = "MIUI SDK updated";
            str2 = "Please re-run this application.";
        }
        return a(str, str2, this.i);
    }

    private Dialog m() {
        String str;
        String str2;
        if (Locale.CHINESE.getLanguage().equals(this.h)) {
            str = "MIUI SDK更新失败";
            str2 = "请稍后重试。";
        } else {
            str = "MIUI SDK update failed";
            str2 = "Please try it later.";
        }
        return a(str, str2, this.i);
    }

    private boolean n() {
        try {
            return ((Boolean) a.g().getMethod("supportUpdate", new Class[]{Map.class}).invoke(null, new Object[]{null})).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean o() {
        try {
            HashMap hashMap = new HashMap();
            return ((Boolean) a.g().getMethod("update", new Class[]{Map.class}).invoke(null, new Object[]{hashMap})).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
