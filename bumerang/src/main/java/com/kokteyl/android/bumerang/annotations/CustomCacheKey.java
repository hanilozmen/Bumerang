package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * Custom hashCode for http caching
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface CustomCacheKey {

}
