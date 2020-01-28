package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Single header.
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Header {
    String value();
}
