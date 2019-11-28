package com.saasquatch.jsonschemainferrer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates code (including public classes, methods, etc.) that is considered internal to this
 * library and should not be depended on by external code.
 *
 * @author sli
 */
@Documented
// This annotation is used for documentation only
@Retention(RetentionPolicy.SOURCE)
public @interface Internal {
}
