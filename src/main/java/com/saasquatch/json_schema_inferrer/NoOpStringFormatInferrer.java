package com.saasquatch.json_schema_inferrer;

enum NoOpStringFormatInferrer implements StringFormatInferrer {

  INSTANCE;

  @Override
  public String infer(StringFormatInferrerInput input) {
    return null;
  }

}
