package com.saasquatch.json_schema_inferrer.examples;

import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.json_schema_inferrer.AdditionalPropertiesPolicies;
import com.saasquatch.json_schema_inferrer.JsonSchemaInferrer;
import com.saasquatch.json_schema_inferrer.RequiredPolicies;
import com.saasquatch.json_schema_inferrer.SpecVersion;
import com.saasquatch.json_schema_inferrer.TitleGenerators;

public class Example {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer =
      JsonSchemaInferrer.newBuilder()
          .withSpecVersion(SpecVersion.DRAFT_06)
          .withAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
          .withRequiredPolicy(RequiredPolicies.nonNullCommonFieldNames())
          .withTitleGenerator(TitleGenerators.useFieldNames())
          .build();

  public static void main(String[] args) throws Exception {
    final JsonNode sample1 = mapper.readTree(
        "{\"one\":\"https://saasquatch.com\",\"two\":[-1.5,\"hello@saasquat.ch\",false],\"three\":3}");
    final JsonNode sample2 = mapper.readTree(
        "{\"one\":1,\"two\":{\"three\":true,\"four\":[2,\"2\"],\"five\":null},\"three\":null}");
    final ObjectNode resultForSample1 = inferrer.inferForSample(sample1);
    final ObjectNode resultForSample1And2 =
        inferrer.inferForSamples(Arrays.asList(sample1, sample2));
    for (JsonNode j : Arrays.asList(sample1, sample2, resultForSample1, resultForSample1And2)) {
      System.out.println(mapper.writeValueAsString(j));
    }
  }

}
