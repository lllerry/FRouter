package com.lerry.frouter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lerry.route_core.FRouter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * app壳工程
 */
public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ImageView mIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View toTest = findViewById(R.id.btn_start);
        TextView time = findViewById(R.id.time_text);
        mIcon = findViewById(R.id.icon_Local);
        toTest.setOnClickListener(this);
//        time.setText(getTime() + "");

        getApplicationInfoInApp();
    }

    private int getTime() {
        return Calendar.getInstance(Locale.CHINA).get(Calendar.YEAR);
    }

    private void getApplicationInfoInApp() {
        PackageManager packageManager = this.getPackageManager();
        if (packageManager != null) {
            List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            for (PackageInfo installedPackage : installedPackages) {
                ApplicationInfo applicationInfo = installedPackage.applicationInfo;
                if ("com.ktcp.video".equals(applicationInfo.packageName)) {
                    Drawable drawable = applicationInfo.loadIcon(packageManager);
                    mIcon.setImageDrawable(drawable);
//                    Log.d(TAG, "getApplicationInfoInApp: sourceDir = " + sourceDir);
                }
//                String sourceDir = applicationInfo.sourceDir;

            }
        }
    }

    @Override
    public void onClick(View v) {
        run(this, "com.ktcp.video");
//        FRouter.getInstance().build("/test/main").navigation();
    }

    /**
     * 运行第三方app
     *
     * @param context     上下文背景
     * @param packageName 包名
     */
    // TODO: 2018/5/24 lerry 兼容leanback launcher
    public void run(Context context, String packageName) {
        /**
         * 记录最近打开的app
         */
//        MyAppFlagmentHelper.savePackageInfo(packageName, context);

        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        if (intent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent = context.getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
            }
        }
        if (intent != null) {
            context.startActivity(intent);
        } else {
            PackageInfo pi = null;
            try {
                pi = context.getPackageManager()
                        .getPackageInfo(packageName, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (pi == null) {
                return;
            }
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_HOME);
            resolveIntent.setPackage(pi.packageName);
            List<ResolveInfo> apps = context.getPackageManager()
                    .queryIntentActivities(resolveIntent, 0);
            if (apps.size() > 0) {
                ResolveInfo ri = apps.iterator().next();
                if (ri != null) {
                    String packageName1 = ri.activityInfo.packageName;
                    String className = ri.activityInfo.name;

                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName cn = new ComponentName(packageName1,
                            className);
                    intent.setComponent(cn);
                    context.startActivity(intent);
                }
            }

        }
    }
}
