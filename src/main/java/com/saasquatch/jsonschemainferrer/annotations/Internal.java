package com.saasquatch.jsonschemainferrer.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that an element is considered internal and should not be used externally regardless of
 * its actual visibility.
 *
 * @author sli
 */
@Retention(RetentionPolicy.SOURCE) // Only used for documentation
@Internal // This annotation itself is internal
public @interface Internal {

}
