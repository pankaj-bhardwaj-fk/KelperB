package com.flipkart;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created on 06/03/17 by dark magic.
 *
 * Annotation for Hack/dirty code
 * should have found a better way to solve this.
 * inspired by vivekm
 */
@Retention(value = SOURCE)
public @interface Hack {
    String value() default "";
}
