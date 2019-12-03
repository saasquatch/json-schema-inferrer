package com.saasquatch.jsonschemainferrer;

/**
 * Built-in singleton implementations of {@link DescriptionGenerator}. Not public.
 *
 * @author sli
 */
enum BuiltInDescriptionGenerators implements DescriptionGenerator {

  NO_OP {},

  USE_FIELD_NAMES_AS_TITLES {
    @Override
    public String generateTitle(DescriptionGeneratorInput input) {
      return input.getFieldName();
    }
  },;

}
