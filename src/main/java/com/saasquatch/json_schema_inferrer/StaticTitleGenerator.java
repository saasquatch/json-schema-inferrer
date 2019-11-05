package com.saasquatch.json_schema_inferrer;

public enum StaticTitleGenerator implements TitleGenerator {

  NO_OP {
    @Override
    public String generate(TitleGeneratorInput input) {
      return null;
    }
  },;

}
