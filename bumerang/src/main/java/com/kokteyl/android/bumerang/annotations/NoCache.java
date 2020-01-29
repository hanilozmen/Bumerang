package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to prevent response caching
 *
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface NoCache {
}
