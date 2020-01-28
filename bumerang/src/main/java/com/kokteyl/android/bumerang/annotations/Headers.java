package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Adds headers as Map.
 * Example call : ImmutableMap.of("Accept", "text/json", "Accept-Charset", "UTF-8")
 * <p>
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Headers {

}
