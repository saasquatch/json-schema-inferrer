package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;

/**
 * Policy for {@code additionalProperties}
 *
 * @author sli
 * @see AdditionalPropertiesPolicies
 */
@FunctionalInterface
public interface AdditionalPropertiesPolicy {

  void process(@Nonnull AdditionalPropertiesPolicyInput input);

}
