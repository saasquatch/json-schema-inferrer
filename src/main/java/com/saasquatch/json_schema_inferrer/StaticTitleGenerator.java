package com.saasquatch.json_schema_inferrer;

/**
 * Built-in static implementations of {@link TitleGenerator}
 *
 * @author sli
 */
public enum StaticTitleGenerator implements TitleGenerator {

  /**
   * Always returns null. Does not generate titles.
   */
  NO_OP {
    @Override
    public String generate(TitleGeneratorInput input) {
      return null;
    }
  },
  /**
   * Use field names as titles
   */
  FIELD_NAME {
    @Override
    public String generate(TitleGeneratorInput input) {
      return input.getFieldName();
    }
  };

}
