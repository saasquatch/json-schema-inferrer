package com.saasquatch.jsonschemainferrer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that an element is only visible for testing.
 *
 * @author sli
 */
@Retention(RetentionPolicy.SOURCE) // Only used for documentation
public @interface VisibleForTesting {

}
