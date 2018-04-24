package com.lerry.route_core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.lerry.route_core.callback.NavigationCallback;
import com.lerry.route_core.exception.NoRouteFoundException;
import com.lerry.route_core.template.IRouteGroup;
import com.lerry.route_core.template.IRouteRoot;
import com.lerry.route_core.utils.ClassUtils;
import com.lerry.router_annotation.model.RouteMeta;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;


/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/24.
 */

public class FRouter {
    private static final String TAG = "FRouter";
    private static final String ROUTE_ROOT_PAKCAGE = "com.lerry.frouter.routes";
    private static final String SDK_NAME = "FRouter";
    private static final String SEPARATOR = "$$";
    private static final String SUFFIX_ROOT = "Root";

    private static Application mContext;

    private static FRouter instance;


    public static FRouter getInstance() {
        synchronized (FRouter.class) {
            if (instance == null) {
                instance = new FRouter();
            }
        }
        return instance;
    }

    private FRouter() {

    }

    /**
     * 初始化
     *
     * @param application
     */
    public static void init(Application application) {
        mContext = application;
        try {
            loadInto();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    /**
     * 加载类信息
     */
    private static void loadInto() throws InterruptedException, IOException, PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Set<String> routerMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PAKCAGE);
        for (String className : routerMap) {
            if (className.startsWith(ROUTE_ROOT_PAKCAGE + "." + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
//                注册的是分组信息 将分组信息加入仓库中
                ((IRouteRoot) Class.forName(className).getConstructor().newInstance()).loadInto(Warehouse.groupsIndex);
            }
        }
    }

    public Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("路由地址无效!");
        } else {
            return build(path, extractGroup(path));
        }
    }

    public Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new RuntimeException("路由地址无效!");
        } else {
            return new Postcard(path, group);
        }
    }

    /**
     * 获得组别
     *
     * @param path
     * @return
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException(path + " : 不能提取group.");
        }
        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new RuntimeException(path + " : 不能提取group.");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Object navigation(Context context, final Postcard postcard, final int requestCode,

                                final NavigationCallback callback) {
        try {
            prepareCard(postcard);

        } catch (NoRouteFoundException exception) {
            if (callback != null) {
                callback.onLost(postcard);
            }
        }

        if (callback != null) {
            callback.onFound(postcard);
        }
        switch (postcard.getType()) {
            case ACTIVITY:
                final Context currentContext = context == null ? mContext : context;
                final Intent intent = new Intent(currentContext, postcard.getDestination());
                intent.putExtras(postcard.getExtras());
                int flags = postcard.getFlags();
                if (flags != -1) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //可能需要返回码
                        if (requestCode > 0) {
                            ActivityCompat.startActivityForResult((Activity) currentContext, intent,
                                    requestCode, postcard.getOptionsBundle());
                        } else {
                            ActivityCompat.startActivity(currentContext, intent, postcard
                                    .getOptionsBundle());
                        }

                        if ((0 != postcard.getEnterAnim() || 0 != postcard.getExitAnim()) &&
                                currentContext instanceof Activity) {
                            //老版本
                            ((Activity) currentContext).overridePendingTransition(postcard
                                            .getEnterAnim()
                                    , postcard.getExitAnim());
                        }
                        //跳转完成
                        if (null != callback) {
                            callback.onArrival(postcard);
                        }
                    }
                });
                break;
            case ISERVICE:
                break;
            default:
                break;
        }
        return null;

    }

    private void prepareCard(Postcard postcard) {
        RouteMeta routeMeta = Warehouse.routes.get(postcard.getPath());
//        没有准备好
        if (routeMeta == null) {
            Class<? extends IRouteGroup> groupMeta = Warehouse.groupsIndex.get(postcard.getGroup());
            if (groupMeta == null) {
                throw new NoRouteFoundException("没找到对应路由: " + postcard.getGroup() + " " +
                        postcard.getPath());
            }
            IRouteGroup iRouteGroup = null;
            try {
                iRouteGroup = groupMeta.getConstructor().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            iRouteGroup.loadInto(Warehouse.routes);

            Warehouse.groupsIndex.remove(postcard.getGroup());

            prepareCard(postcard);
        } else {
            postcard.setDestination(routeMeta.getDestination());
            postcard.setType(routeMeta.getType());
            switch (routeMeta.getType()) {

            }
        }
    }

}
