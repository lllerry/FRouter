package com.lerry.router_compiler.utils;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/18.
 */

public final class Constants {

    public static final String ARGUMENTS_NAME = "moduleName";
    public static final String ANN_TYPE_ROUTE = "com.lerry.router_annotation.Route";
    public static final String ANN_TYPE_EXTRA = "com.lerry.router_annotation.Extra";

    public static final String ACTIVITY = "android.app.Activity";
    public static final String ISERVICE = "com.lerry.route_core.template.IService";
    public static final String SLASH = "/";

    public static final String IROUTE_GROUP = "com.lerry.route_core.template.IRouteGroup";
    public static final String IROUTE_ROOT = "com.lerry.route_core.template.IRouteRoot";

    public static final String METHOD_LOAD_INTO = "loadInto";
    public static final String METHOD_LOAD_EXTRA = "loadExtra";

    public static final String SEPARATOR = "$$";
    public static final String PROJECT = "FRouter";
    public static final String NAME_OF_ROOT = PROJECT + SEPARATOR + "Root" + SEPARATOR;
    public static final String NAME_OF_GROUP = PROJECT + SEPARATOR + "Group" + SEPARATOR;

    public static final String PACKAGE_OF_GENERATE_FILE = "com.lerry.frouter.routes";
}
