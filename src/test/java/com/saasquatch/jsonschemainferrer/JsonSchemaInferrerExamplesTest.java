package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.format;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.mapper;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersionDetector;
import com.networknt.schema.ValidationMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import org.junit.jupiter.api.Test;

public class JsonSchemaInferrerExamplesTest {

  private static final Collection<JsonSchemaInferrer> testInferrers = getTestInferrers();

  @Test
  public void test() {
    for (String resourceName : getTestExamplesResources()) {
      doTestForResourceName(resourceName);
    }
  }

  @Test
  public void testMulti() {
    for (List<String> resourceNames : Iterables.partition(getTestExamplesResources(), 4)) {
      doTestForResourceNames(resourceNames);
    }
  }

  private static List<String> getTestExamplesResources() {
    try (
        InputStream in = JsonSchemaInferrerExamplesTest.class.getResourceAsStream("testExamples");
        BufferedReader br = new BufferedReader(
            new InputStreamReader(Objects.requireNonNull(in), UTF_8))
    ) {
      return br.lines()
          .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".json"))
          .map("testExamples/"::concat)
          .collect(ImmutableList.toImmutableList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static JsonNode loadJsonFromResource(String resourceName) {
    try (InputStream in = JsonSchemaInferrerTest.class.getResourceAsStream(resourceName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      System.out.printf(Locale.ROOT, "Exception encountered loading JSON from resource[%s]. "
          + "Error message: [%s].%n", resourceName, e.getMessage());
      throw new UncheckedIOException(e);
    }
  }

  private static List<String> validateJsonSchema(JsonNode schemaJson, JsonNode instance) {
    final SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
    schemaValidatorsConfig.setPathType(PathType.JSON_POINTER);
    return JsonSchemaFactory.getInstance(SpecVersionDetector.detect(schemaJson))
        .getSchema(schemaJson, schemaValidatorsConfig)
        .validate(instance)
        .stream()
        .map(ValidationMessage::getMessage)
        .collect(ImmutableList.toImmutableList());
  }

  private static void doTestForResourceName(String resourceName) {
    final JsonNode sampleJson = loadJsonFromResource(resourceName);
    System.out.printf(Locale.ROOT, "Got valid JSON from resource[%s]%n", resourceName);
    for (JsonSchemaInferrer inferrer : testInferrers) {
      final ObjectNode schemaJson = inferrer.inferForSample(sampleJson);
      assertNotNull(schemaJson, format("Inferred schema for resource[%s] is null", resourceName));
      final List<String> schemaErrors;
      try {
        schemaErrors = validateJsonSchema(schemaJson, sampleJson);
      } catch (RuntimeException e) {
        fail(format("Unable to parse the inferred schema for resource[%s]", resourceName), e);
        throw e;
      }
      if (!schemaErrors.isEmpty()) {
        System.out.println("Schema validation failed");
        System.out.println(schemaJson.toPrettyString());
        System.out.println("Error messages:");
        schemaErrors.forEach(System.out::println);
        fail(format("Inferred schema for resource[%s] failed to validate with errors{}",
            resourceName, schemaErrors));
      }
    }
  }

  private static void doTestForResourceNames(Collection<String> resourceNames) {
    final List<JsonNode> sampleJsons = resourceNames.stream()
        .map(JsonSchemaInferrerExamplesTest::loadJsonFromResource)
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
    System.out.printf(Locale.ROOT, "Got valid JSONs from resourceNames%s%n", resourceNames);
    for (JsonSchemaInferrer inferrer : testInferrers) {
      final ObjectNode schemaJson = inferrer.inferForSamples(sampleJsons);
      assertNotNull(schemaJson,
          format("Inferred schema for resourceNames%s is null", resourceNames));
      for (JsonNode sampleJson : sampleJsons) {
        final List<String> schemaErrors;
        try {
          schemaErrors = validateJsonSchema(schemaJson, sampleJson);
        } catch (RuntimeException e) {
          fail(format("Unable to parse the inferred schema for resourceNames%s", resourceNames), e);
          throw e;
        }
        if (!schemaErrors.isEmpty()) {
          System.out.println("Schema validation failed");
          System.out.println(schemaJson.toPrettyString());
          System.out.println("Error messages:");
          schemaErrors.forEach(System.out::println);
          fail(format("Inferred schema for resourceNames%s failed to validate with errors{}",
              resourceNames, schemaErrors));
        }
      }
    }
  }

  private static Collection<JsonSchemaInferrer> getTestInferrers() {
    final List<JsonSchemaInferrer> inferrers = new ArrayList<>();
    final Iterator<DefaultPolicy> defaultPolicyIter = Iterators.cycle(
        DefaultPolicies.useFirstSamples(), DefaultPolicies.useLastSamples());
    final Iterator<RequiredPolicy> requiredPolicyIter = Iterators.cycle(
        RequiredPolicies.commonFields(), RequiredPolicies.nonNullCommonFields());
    final Iterator<AdditionalPropertiesPolicy> additionalPropPolicyIter = Iterators.cycle(
        AdditionalPropertiesPolicies.existingTypes(), AdditionalPropertiesPolicies.notAllowed());
    final List<SpecVersion> specVersions = Arrays.asList(SpecVersion.values());
    Collections.shuffle(specVersions, ThreadLocalRandom.current());
    for (SpecVersion specVersion : specVersions) {
      for (boolean extraFeatures : Arrays.asList(true, false)) {
        final JsonSchemaInferrerBuilder builder = JsonSchemaInferrer.newBuilder()
            .setSpecVersion(specVersion);
        if (extraFeatures) {
          builder.addFormatInferrers(FormatInferrers.dateTime(), FormatInferrers.email(),
                  FormatInferrers.ip())
              .setArrayLengthFeatures(EnumSet.allOf(ArrayLengthFeature.class))
              .setObjectSizeFeatures(EnumSet.allOf(ObjectSizeFeature.class))
              .setStringLengthFeatures(EnumSet.allOf(StringLengthFeature.class))
              .setNumberRangeFeatures(EnumSet.allOf(NumberRangeFeature.class))
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(10))
              .setDefaultPolicy(defaultPolicyIter.next())
              .addEnumExtractors(EnumExtractors.validEnum(Month.class),
                  EnumExtractors.validEnum(DayOfWeek.class), input -> {
                    final Set<? extends JsonNode> primitives = input.getSamples().stream()
                        .filter(JsonNode::isValueNode)
                        .collect(ImmutableSet.toImmutableSet());
                    if (primitives.size() <= 3 && primitives.size() > 0) {
                      return Collections.singleton(primitives);
                    }
                    return Collections.emptySet();
                  })
              .setTitleDescriptionGenerator(new TitleDescriptionGenerator() {

                @Override
                public String generateTitle(@Nonnull TitleDescriptionGeneratorInput input) {
                  return input.getFieldName();
                }

                @Override
                public String generateDescription(@Nonnull TitleDescriptionGeneratorInput input) {
                  return input.getFieldName();
                }

                @Override
                public String generateComment(@Nonnull TitleDescriptionGeneratorInput input) {
                  if (input.getSpecVersion().compareTo(SpecVersion.DRAFT_07) < 0) {
                    return null;
                  }
                  return input.getFieldName();
                }

              }).setAdditionalPropertiesPolicy(additionalPropPolicyIter.next())
              .setRequiredPolicy(requiredPolicyIter.next());
        }
        try {
          inferrers.add(builder.build());
        } catch (IllegalArgumentException e) {
          // Ignore
        }
      }
    }
    return Collections.unmodifiableList(inferrers);
  }

}
