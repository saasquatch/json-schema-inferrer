package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link GenericSchemaAddOn}
 *
 * @author sli
 */
@NoExternalImpl
public interface GenericSchemaAddOnInput {

  /**
   * @return The current samples
   */
  @Nonnull
  Collection<? extends JsonNode> getSamples();

  /**
   * @return The current type, if available
   */
  @Nullable
  String getType();

  /**
   * @return The current {@link SpecVersion}
   */
  @Nonnull
  SpecVersion getSpecVersion();

}
