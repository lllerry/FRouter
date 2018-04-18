package com.lerry.route_core.template;

import com.lerry.router_annotation.model.RouteMeta;

import java.util.Map;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/18.
 */

public interface IRouteGroup {
    void loadInto(Map<String, RouteMeta> altas);
}
