package com.saasquatch.jsonschemainferrer.examples;

import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasquatch.jsonschemainferrer.AdditionalPropertiesPolicies;
import com.saasquatch.jsonschemainferrer.EnumCriteria;
import com.saasquatch.jsonschemainferrer.FormatInferrers;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.RequiredPolicies;
import com.saasquatch.jsonschemainferrer.SpecVersion;

public class Example {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
      .setSpecVersion(SpecVersion.DRAFT_06)
      // Requires commons-validator
      .addFormatInferrers(FormatInferrers.email(), FormatInferrers.ip())
      .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
      .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
      .setEnumCriterion(EnumCriteria.or(EnumCriteria.validEnum(java.time.DayOfWeek.class),
          EnumCriteria.validEnum(java.time.Month.class)))
      .build();

  public static void main(String[] args) throws Exception {
    final JsonNode sample1 = mapper.readTree(
        "{\"🙈\":\"https://saasquatch.com\",\"🙉\":[-1.5,2,\"hello@saasquat.ch\",false],\"🙊\":3,\"weekdays\":[\"MONDAY\",\"TUESDAY\"]}");
    final JsonNode sample2 = mapper.readTree(
        "{\"🙈\":1,\"🙉\":{\"🐒\":true,\"🐵\":[2,\"1234:5678::\"],\"🍌\":null},\"🙊\":null,\"months\":[\"JANUARY\",\"FEBRUARY\"]}");
    final JsonNode resultForSample1 = inferrer.inferForSample(sample1);
    final JsonNode resultForSample1And2 =
        inferrer.inferForSamples(Arrays.asList(sample1, sample2));
    for (JsonNode j : Arrays.asList(sample1, sample2, resultForSample1, resultForSample1And2)) {
      System.out.println(mapper.writeValueAsString(j));
    }
  }

}
