package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;

@FunctionalInterface
public interface AdditionalPropertiesPolicy {

  void process(@Nonnull AdditionalPropertiesPolicyInput input);

}
