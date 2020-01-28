package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Denotes that the request body will use form URL encoding.
 * BaseAsyncTask Type = application/x-www-form-urlencoded
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface FormURLEncoded {
}
