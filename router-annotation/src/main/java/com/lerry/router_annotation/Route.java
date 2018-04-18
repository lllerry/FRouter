package com.lerry.router_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/18.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    /**
     * 路由的路劲
     *
     * @return
     */
    String path();

    /**
     * 路由组
     *
     * @return
     */
    String group() default "";
}
