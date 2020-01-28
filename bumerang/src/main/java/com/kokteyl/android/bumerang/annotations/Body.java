package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * HTTP Body parameters may not be {@code null}.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Body {
}
