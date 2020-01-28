package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
/**
 * Named replacement in a URL path segment.
 * **/
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Path {
    String value();
    boolean encoded() default false;
}