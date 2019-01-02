package com.lerry.router_annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gmail: lerryletter@gmail.com
 * -----------
 * Blog: imlerry.com
 *
 * @author lerry on 2018/4/26.
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Extra {
    String name() default "";
}
