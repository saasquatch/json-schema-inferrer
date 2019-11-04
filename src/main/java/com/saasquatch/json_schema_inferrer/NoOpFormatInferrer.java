package com.saasquatch.json_schema_inferrer;

enum NoOpFormatInferrer implements FormatInferrer {

  INSTANCE;

  @Override
  public String infer(FormatInferrerInput input) {
    return null;
  }

}
