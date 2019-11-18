package com.saasquatch.jsonschemainferrer.examples;

import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.AdditionalPropertiesPolicies;
import com.saasquatch.jsonschemainferrer.FormatInferrers;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.RequiredPolicies;
import com.saasquatch.jsonschemainferrer.SpecVersion;

public class Example {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
      .setSpecVersion(SpecVersion.DRAFT_06)
      .setFormatInferrer(FormatInferrers.chained(
          // Requires commons-validator
          FormatInferrers.email(), FormatInferrers.ip()))
      .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
      .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
      .build();

  public static void main(String[] args) throws Exception {
    final JsonNode sample1 = mapper.readTree(
        "{\"one\":\"https://saasquatch.com\",\"two\":[-1.5,2,\"hello@saasquat.ch\",false],\"three\":3}");
    final JsonNode sample2 = mapper.readTree(
        "{\"one\":1,\"two\":{\"four\":true,\"five\":[2,\"1234:5678::\"],\"six\":null},\"three\":null}");
    final ObjectNode resultForSample1 = inferrer.inferForSample(sample1);
    final ObjectNode resultForSample1And2 =
        inferrer.inferForSamples(Arrays.asList(sample1, sample2));
    for (JsonNode j : Arrays.asList(sample1, sample2, resultForSample1, resultForSample1And2)) {
      System.out.println(mapper.writeValueAsString(j));
    }
  }

}
