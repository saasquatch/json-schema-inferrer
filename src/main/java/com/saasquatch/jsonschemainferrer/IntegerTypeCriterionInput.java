package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input for {@link IntegerTypeCriterion}
 *
 * @author sli
 */
public interface IntegerTypeCriterionInput {

  @Nonnull
  JsonNode getSample();

  @Nonnull
  SpecVersion getSpecVersion();

}
