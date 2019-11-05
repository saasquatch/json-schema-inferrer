package com.saasquatch.json_schema_inferrer;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Input for {@link AdditionalPropertiesPolicy}
 *
 * @author sli
 */
public interface AdditionalPropertiesPolicyInput {

  ObjectNode getSchema();

  SpecVersion specVersion();

}
