package com.saasquatch.json_schema_inferrer;

enum NoOpStringFormatInferrer implements FormatInferrer {

  INSTANCE;

  @Override
  public String infer(FormatInferrerInput input) {
    return null;
  }

}
