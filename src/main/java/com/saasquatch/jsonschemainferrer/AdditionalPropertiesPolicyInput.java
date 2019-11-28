package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link AdditionalPropertiesPolicy}
 *
 * @author sli
 */
@NoExternalImpl
public interface AdditionalPropertiesPolicyInput {

  /**
   * @return The input JSON schema
   */
  @Nonnull
  ObjectNode getSchema();

  @Nonnull
  SpecVersion getSpecVersion();

}
