package com.saasquatch.jsonschemainferrer.examples;

import java.net.URI;
import java.net.URL;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.AdditionalPropertiesPolicies;
import com.saasquatch.jsonschemainferrer.ArrayLengthFeature;
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
import com.saasquatch.jsonschemainferrer.TitleGenerators;

public class Example2 {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
      .setSpecVersion(SpecVersion.DRAFT_06)
      .setIntegerTypeCriterion(IntegerTypeCriteria.mathematicalInteger())
      .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3))
      .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.existingTypes())
      .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
      .setTitleGenerator(TitleGenerators.useFieldNames())
      .setFormatInferrer(FormatInferrers.chained(FormatInferrers.email(),
          FormatInferrers.dateTime(), FormatInferrers.ip(), Example2::absoluteUriFormatInferrer))
      .setMultipleOfPolicy(MultipleOfPolicies.gcd())
      .enable(ArrayLengthFeature.MIN_ITEMS, ArrayLengthFeature.MAX_ITEMS)
      .enable(ObjectSizeFeature.MIN_PROPERTIES, ObjectSizeFeature.MAX_PROPERTIES)
      .enable(StringLengthFeature.MIN_LENGTH, StringLengthFeature.MAX_LENGTH)
      .enable(NumberRangeFeature.MINIMUM, NumberRangeFeature.MAXIMUM)
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
