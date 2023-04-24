package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;

/**
 * Built-in singleton implementations of {@link TitleDescriptionGenerator}. Not public.
 *
 * @author sli
 */
enum BuiltInTitleDescriptionGenerators implements TitleDescriptionGenerator {

  NO_OP {},

  USE_FIELD_NAMES_AS_TITLES {
    @Override
    public String generateTitle(@Nonnull TitleDescriptionGeneratorInput input) {
      return input.getFieldName();
    }
  },
  ;

}
