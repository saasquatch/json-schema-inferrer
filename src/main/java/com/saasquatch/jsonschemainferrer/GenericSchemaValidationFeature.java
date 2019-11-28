package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * For generating add-on fields (things like maxProperties) for a schema.
 *
 * @author sli
 * @see GenericSchemaValidationFeatures
 */
public interface GenericSchemaValidationFeature {

  /**
   * Get the add-on result to be merged in with the schema
   */
  @Nullable
  ObjectNode getFeatureResult(@Nonnull GenericSchemaValidationFeatureInput input);

}
