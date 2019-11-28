package com.saasquatch.jsonschemainferrer.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that an interface itself is public, but it should not be implemented externally. What
 * this effectively means is that adding methods to interfaces with this annotation will not break
 * external code.
 *
 * @author sli
 */
@Documented
// This annotation is used for documentation only
@Retention(RetentionPolicy.SOURCE)
public @interface NoExternalImpl {
}
