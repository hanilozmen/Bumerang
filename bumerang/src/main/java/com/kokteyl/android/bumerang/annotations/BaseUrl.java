package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Full url in order to override baseUrl
 * **/
@Target(METHOD)
@Retention(RUNTIME)
public @interface BaseUrl {
    /**
     * A base url in order to send request to another api
     */
    String value() default "";
}
