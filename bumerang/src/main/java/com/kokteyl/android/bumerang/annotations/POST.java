package com.kokteyl.android.bumerang.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface POST {
    /**
     * A relative or absolute path, or full URL of the endpoint. This path is compulsory.
     */
    String value() default "";
}
