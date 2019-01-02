package com.lerry.test2;

import android.util.Log;

import com.lerry.route_core.template.ITestService;
import com.lerry.router_annotation.Route;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/26.
 */

@Route(path = "/test2/service")
public class Test2Service implements ITestService {

    String test = "jajja";

    private static final String TAG = "Service";

    @Override
    public String test() {
        return test;
    }
}
