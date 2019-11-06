package com.saasquatch.json_schema_inferrer;

final class ChainedFormatInferrer implements FormatInferrer {

  private final FormatInferrer[] formatInferrers;

  public ChainedFormatInferrer(FormatInferrer[] formatInferrers) {
    this.formatInferrers = formatInferrers;
  }

  @Override
  public String infer(FormatInferrerInput input) {
    for (FormatInferrer formatInferrer : formatInferrers) {
      final String result = formatInferrer.infer(input);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

}
