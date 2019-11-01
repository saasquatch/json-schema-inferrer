package com.saasquatch.json_schema_inferrer;

import java.util.Arrays;
import java.util.Objects;

class ChainedStringFormatInferrer implements StringFormatInferrer {

  private final StringFormatInferrer[] inferrers;

  public ChainedStringFormatInferrer(StringFormatInferrer[] inferrers) {
    this.inferrers = inferrers;
  }

  @Override
  public String infer(StringFormatInferrerInput input) {
    return Arrays.stream(inferrers)
        .map(inferrer -> inferrer.infer(input))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

}
