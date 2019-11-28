package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.saasquatch.jsonschemainferrer.annotations.NoExternalImpl;

/**
 * Input for {@link IntegerTypeCriterion}
 *
 * @author sli
 */
@NoExternalImpl
public interface IntegerTypeCriterionInput {

  @Nonnull
  JsonNode getSample();

  @Nonnull
  SpecVersion getSpecVersion();

}
