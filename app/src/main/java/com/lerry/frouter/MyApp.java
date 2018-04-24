package com.lerry.frouter;

import android.app.Application;

import com.lerry.route_core.FRouter;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/24.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FRouter.init(this);
    }
}
