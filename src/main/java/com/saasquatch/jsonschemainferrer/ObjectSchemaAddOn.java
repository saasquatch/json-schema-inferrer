package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * For generating add-on fields (things like maxProperties) for a schema of an object.
 *
 * @author sli
 * @see ObjectSchemaAddOns
 */
public interface ObjectSchemaAddOn {

  /**
   * Get the add-on
   */
  @Nullable
  ObjectNode getAddOn(@Nonnull ObjectSchemaAddOnInput input);

}
