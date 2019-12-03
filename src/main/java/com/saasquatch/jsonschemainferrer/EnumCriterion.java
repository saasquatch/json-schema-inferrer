package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;

/**
 * Determine whether the given samples should be an {@code enum}.
 *
 * @author sli
 */
public interface EnumCriterion {

  /**
   * @return Whether the given samples are an {@code enum}
   */
  boolean isEnum(@Nonnull EnumCriterionInput input);

}
