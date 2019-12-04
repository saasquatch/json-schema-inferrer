package com.saasquatch.jsonschemainferrer.examples;

import java.net.URI;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.AdditionalPropertiesPolicies;
import com.saasquatch.jsonschemainferrer.ArrayLengthFeature;
import com.saasquatch.jsonschemainferrer.DescriptionGenerators;
import com.saasquatch.jsonschemainferrer.EnumCriteria;
import com.saasquatch.jsonschemainferrer.ExamplesPolicies;
import com.saasquatch.jsonschemainferrer.FormatInferrerInput;
import com.saasquatch.jsonschemainferrer.FormatInferrers;
import com.saasquatch.jsonschemainferrer.IntegerTypeCriteria;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.MultipleOfPolicies;
import com.saasquatch.jsonschemainferrer.NumberRangeFeature;
import com.saasquatch.jsonschemainferrer.ObjectSizeFeature;
import com.saasquatch.jsonschemainferrer.RequiredPolicies;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import com.saasquatch.jsonschemainferrer.StringLengthFeature;

public class Example2 {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
      .setSpecVersion(SpecVersion.DRAFT_06)
      .setIntegerTypeCriterion(IntegerTypeCriteria.mathematicalInteger())
      .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3))
      .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.existingTypes())
      .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
      .setDescriptionGenerator(DescriptionGenerators.useFieldNamesAsTitles())
      .addFormatInferrers(FormatInferrers.email(), FormatInferrers.dateTime(), FormatInferrers.ip(),
          Example2::absoluteUriFormatInferrer)
      .setMultipleOfPolicy(MultipleOfPolicies.gcd())
      .setEnumCriterion(EnumCriteria.or(EnumCriteria.isValidEnumIgnoreCase(DayOfWeek.class),
          EnumCriteria.isValidEnumIgnoreCase(Month.class),
          input -> input.getSamples().size() < 100))
      .setArrayLengthFeatures(EnumSet.allOf(ArrayLengthFeature.class))
      .setObjectSizeFeatures(EnumSet.allOf(ObjectSizeFeature.class))
      .setStringLengthFeatures(EnumSet.allOf(StringLengthFeature.class))
      .setNumberRangeFeatures(EnumSet.allOf(NumberRangeFeature.class))
      .build();

  private static String absoluteUriFormatInferrer(@Nonnull FormatInferrerInput input) {
    final String textValue = input.getSample().textValue();
    if (textValue == null) {
      return null;
    }
    try {
      final URI uri = new URI(input.getSample().textValue());
      if (uri.isAbsolute()) {
        return "uri";
      }
    } catch (Exception e) {
      // ignore
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
    final JsonNode sample = mapper.readTree(new URL(
        "https://cdn.jsdelivr.net/gh/quicktype/quicktype@3ea476df5c1c4a6821d3cca7c1de359724a90a92/test/inputs/json/samples/reddit.json"));
    final ObjectNode schema = inferrer.inferForSample(sample);
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
  }

}
