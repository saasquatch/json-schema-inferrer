package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.toStringSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class JsonSchemaInferrerOptionsTest {

  @Test
  public void testNoOp() {
    assertNull(RequiredPolicies.noOp().getRequired(null));
    assertNull(DefaultPolicies.noOp().getDefault(null));
    assertNull(MultipleOfPolicies.noOp().getMultipleOf(null));
    assertNull(ExamplesPolicies.noOp().getExamples(null));
    assertNull(AdditionalPropertiesPolicies.noOp().getAdditionalProperties(null));
  }

  @Test
  public void testJsonTypeInference() {
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder().build();
    final List<? extends JsonNode> unexpectedNodes =
        ImmutableList.of(jnf.objectNode(), jnf.arrayNode(), jnf.missingNode(), jnf.pojoNode(1));
    for (JsonNode jsonNode : unexpectedNodes) {
      assertThrows(IllegalStateException.class, () -> inferrer.inferPrimitiveType(jsonNode, false));
    }
    assertEquals("string",
        inferrer.inferPrimitiveType(jnf.binaryNode("foo".getBytes(UTF_8)), false));
    assertEquals("string", inferrer.inferPrimitiveType(jnf.numberNode(Double.NaN), false));
    assertEquals("string",
        inferrer.inferPrimitiveType(jnf.numberNode(Double.NEGATIVE_INFINITY), false));
    assertEquals("string",
        inferrer.inferPrimitiveType(jnf.numberNode(Double.POSITIVE_INFINITY), false));
    assertEquals("string", inferrer.inferPrimitiveType(jnf.numberNode(Float.NaN), false));
    assertEquals("string",
        inferrer.inferPrimitiveType(jnf.numberNode(Float.NEGATIVE_INFINITY), false));
    assertEquals("string",
        inferrer.inferPrimitiveType(jnf.numberNode(Float.POSITIVE_INFINITY), false));
  }

  @Test
  public void testFormatInferrers() {
    // Fake format inferrer that always uses the string length as the format
    final FormatInferrer testStrLenFormatInferrer = input -> {
      final String textValue = input.getSample().textValue();
      assertNotNull(input.getSpecVersion());
      if (textValue == null) {
        return null;
      }
      return textValue.length() + "";
    };
    assertSame(FormatInferrers.noOp(), FormatInferrers.chained());
    assertSame(FormatInferrers.noOp(),
        FormatInferrers.chained(FormatInferrers.chained(FormatInferrers.noOp())));
    assertThrows(NullPointerException.class,
        () -> FormatInferrers.chained(FormatInferrers.dateTime(), null));
    assertSame(FormatInferrers.dateTime(), FormatInferrers.chained(FormatInferrers.dateTime()));
    assertNull(JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime())
        .setSpecVersion(SpecVersion.DRAFT_07).build().inferForSample(jnf.textNode("aaaaaaaaa"))
        .path("format").textValue());
    assertEquals("date-time",
        JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime()).build()
            .inferForSample(jnf.textNode(Instant.now().toString())).path("format").textValue());
    assertNull(JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime())
        .setSpecVersion(SpecVersion.DRAFT_06).build().inferForSample(jnf.textNode("1900-01-01"))
        .path("format").textValue());
    assertEquals("date",
        JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime())
            .setSpecVersion(SpecVersion.DRAFT_07).build().inferForSample(jnf.textNode("1900-01-01"))
            .path("format").textValue());
    assertNull(JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime())
        .setSpecVersion(SpecVersion.DRAFT_06).build().inferForSample(jnf.textNode("20:20:39"))
        .path("format").textValue());
    assertEquals("time",
        JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime())
            .setSpecVersion(SpecVersion.DRAFT_07).build().inferForSample(jnf.textNode("20:20:39"))
            .path("format").textValue());
    assertEquals("time",
        JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.dateTime())
            .setSpecVersion(SpecVersion.DRAFT_07).build()
            .inferForSample(jnf.textNode("20:20:39+00:00")).path("format").textValue());
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setFormatInferrer(
              FormatInferrers.chained(FormatInferrers.dateTime(), testStrLenFormatInferrer))
          .build();
      assertEquals("date-time", inferrer.inferForSample(jnf.textNode(Instant.now().toString()))
          .path("format").textValue());
      assertEquals("0", inferrer.inferForSample(jnf.textNode("")).path("format").textValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setFormatInferrer(FormatInferrers.ip()).build();
      assertEquals("ipv4",
          inferrer.inferForSample(jnf.textNode("12.34.56.78")).path("format").textValue());
      assertEquals("ipv6",
          inferrer.inferForSample(jnf.textNode("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))
              .path("format").textValue());
      assertEquals("ipv6",
          inferrer.inferForSample(jnf.textNode("a::F")).path("format").textValue());
      assertEquals("ipv6", inferrer.inferForSample(jnf.textNode("5::")).path("format").textValue());
      assertEquals("ipv6", inferrer.inferForSample(jnf.textNode("::")).path("format").textValue());
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setFormatInferrer(FormatInferrers.chained(FormatInferrers.noOp(), FormatInferrers.noOp(),
              FormatInferrers.noOp(), FormatInferrers.ip(), FormatInferrers.email()))
          .build();
      assertNull(inferrer.inferForSample(jnf.textNode(Instant.now().toString())).get("format"));
    }
    {
      final String dateTimeString = Instant.now().toString();
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setFormatInferrer(
              FormatInferrers.chained(testStrLenFormatInferrer, FormatInferrers.dateTime()))
          .build();
      assertEquals("" + dateTimeString.length(),
          inferrer.inferForSample(jnf.textNode(dateTimeString)).path("format").textValue());
      assertEquals("0", inferrer.inferForSample(jnf.textNode("")).path("format").textValue());
    }
  }

  @Test
  public void testAdditionalProperties() {
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.noOp()).build();
      final ObjectNode schema = inferrer.inferForSample(jnf.objectNode());
      assertNull(schema.get("additionalProperties"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setAdditionalPropertiesPolicy(input -> {
            assertNotNull(input.getSchema());
            assertNotNull(input.getSpecVersion());
            return null;
          }).build();
      final ObjectNode schema = inferrer.inferForSample(jnf.objectNode());
      assertNull(schema.get("additionalProperties"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.allowed()).build();
      final ObjectNode schema = inferrer.inferForSample(jnf.objectNode());
      assertEquals(jnf.booleanNode(true), schema.path("additionalProperties"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed()).build();
      final ObjectNode schema = inferrer.inferForSample(jnf.objectNode());
      assertEquals(jnf.booleanNode(false), schema.path("additionalProperties"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.existingTypes()).build();
      final ObjectNode schema = inferrer.inferForSamples(Arrays
          .asList(jnf.objectNode().put("1", 1).put("2", "2"), jnf.objectNode().put("1", "1")));
      assertEquals(ImmutableSet.of("string", "integer"),
          toStringSet(schema.path("additionalProperties").path("type")));
    }
  }

  @Test
  public void testIntegerTypePreference() {
    final List<JsonNode> intAndFloats = ImmutableList.of(jnf.numberNode(1), jnf.numberNode(1.5));
    final List<JsonNode> intsOnly =
        ImmutableList.of(jnf.numberNode(1), jnf.numberNode(4L), jnf.numberNode(BigInteger.ONE));
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setIntegerTypePreference(IntegerTypePreference.IF_ALL).build();
      assertEquals("number", inferrer.inferForSamples(intAndFloats).path("type").textValue());
      assertEquals("integer", inferrer.inferForSamples(intsOnly).path("type").textValue());
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setIntegerTypePreference(IntegerTypePreference.IF_ANY).build();
      assertEquals(ImmutableSet.of("integer", "number"),
          toStringSet(inferrer.inferForSamples(intAndFloats).path("type")));
      assertEquals("integer", inferrer.inferForSamples(intsOnly).path("type").textValue());
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setIntegerTypePreference(IntegerTypePreference.NEVER).build();
      assertEquals("number", inferrer.inferForSamples(intAndFloats).path("type").textValue());
      assertEquals("number", inferrer.inferForSamples(intsOnly).path("type").textValue());
    }
  }

  @Test
  public void testIntegerConfigCombo() {
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setIntegerTypeCriterion(input -> {
            Objects.requireNonNull(input.getSample());
            Objects.requireNonNull(input.getSpecVersion());
            return IntegerTypeCriteria.mathematicalInteger().isInteger(input);
          }).setIntegerTypePreference(IntegerTypePreference.IF_ANY).build();
      assertEquals("integer",
          inferrer.inferForSample(jnf.numberNode(1.0)).path("type").textValue());
    }
  }

  @Test
  public void testRequired() {
    final List<ObjectNode> samples = ImmutableList.of(jnf.objectNode().put("1", 1).put("2", 2),
        jnf.objectNode().put("1", "1").put("2", (String) null));
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setRequiredPolicy(RequiredPolicies.noOp()).build();
      assertNull(inferrer.inferForSamples(samples).get("required"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setRequiredPolicy(input -> {
            assertNotNull(input.getSamples());
            assertNotNull(input.getSpecVersion());
            return null;
          }).build();
      assertNull(inferrer.inferForSamples(samples).get("required"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setRequiredPolicy(RequiredPolicies.commonFields()).build();
      assertEquals(ImmutableSet.of("1", "2"),
          toStringSet(inferrer.inferForSamples(samples).get("required")));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setRequiredPolicy(RequiredPolicies.nonNullCommonFields()).build();
      assertEquals(ImmutableSet.of("1"),
          toStringSet(inferrer.inferForSamples(samples).get("required")));
    }
  }

  @Test
  public void testTitleAndDescriptionGenerator() {
    final JsonNode sample = jnf.objectNode().put("fieldName", "value");
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setTitleGenerator(TitleGenerators.noOp()).build();
      assertNull(inferrer.inferForSample(sample).path("properties").path("fieldName").get("title"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setDescriptionGenerator(DescriptionGenerators.noOp()).build();
      assertNull(
          inferrer.inferForSample(sample).path("properties").path("fieldName").get("description"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setTitleGenerator(TitleGenerators.useFieldNames()).build();
      assertEquals("fieldName", inferrer.inferForSample(sample).path("properties").path("fieldName")
          .get("title").textValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setDescriptionGenerator(input -> {
            assertNotNull(input.getSpecVersion());
            return null;
          }).build();
      assertNull(
          inferrer.inferForSample(sample).path("properties").path("fieldName").get("description"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setTitleGenerator(input -> {
            assertNotNull(input.getSpecVersion());
            return Optional.ofNullable(input.getFieldName()).map(String::toUpperCase).orElse(null);
          }).build();
      assertEquals("FIELDNAME", inferrer.inferForSample(sample).path("properties").path("fieldName")
          .get("title").textValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setDescriptionGenerator(input -> {
            assertNotNull(input.getSpecVersion());
            return Optional.ofNullable(input.getFieldName()).map(String::toUpperCase).orElse(null);
          }).build();
      assertEquals("FIELDNAME", inferrer.inferForSample(sample).path("properties").path("fieldName")
          .get("description").textValue());
    }
  }

  @Test
  public void testDefault() {
    final List<JsonNode> samples =
        ImmutableList.of(jnf.textNode("a"), jnf.textNode("b"), jnf.textNode("c"));
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setDefaultPolicy(DefaultPolicies.noOp()).build();
      assertNull(inferrer.inferForSamples(samples).get("default"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setDefaultPolicy(input -> {
            assertNotNull(input.getSpecVersion());
            return null;
          }).build();
      assertNull(inferrer.inferForSamples(samples).get("default"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setDefaultPolicy(DefaultPolicies.useFirstSamples()).build();
      assertEquals("a", inferrer.inferForSamples(samples).path("default").textValue());
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setDefaultPolicy(DefaultPolicies.useLastSamples()).build();
      assertEquals("c", inferrer.inferForSamples(samples).path("default").textValue());
    }
  }

  @Test
  public void testExamples() {
    assertSame(ExamplesPolicies.noOp(), ExamplesPolicies.useFirstSamples(0));
    assertThrows(IllegalArgumentException.class, () -> ExamplesPolicies.useFirstSamples(-1));
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_04)
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3)).build();
      final ObjectNode schema = inferrer.inferForSamples(IntStream.range(0, 5)
          .mapToObj(Integer::toString).map(jnf::textNode).collect(Collectors.toList()));
      assertNull(schema.get("examples"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_06)
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3)).build();
      final ObjectNode schema = inferrer.inferForSamples(IntStream.range(0, 5)
          .mapToObj(Integer::toString).map(jnf::textNode).collect(Collectors.toList()));
      assertEquals(ImmutableSet.of("0", "1", "2"), toStringSet(schema.path("examples")));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_06)
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3, "boolean"::equals)).build();
      final ObjectNode schema = inferrer.inferForSamples(IntStream.range(0, 5)
          .mapToObj(Integer::toString).map(jnf::textNode).collect(Collectors.toList()));
      assertNull(schema.get("examples"));
    }
    {
      final JsonNode examples = ExamplesPolicies.useFirstSamples(1)
          .getExamples(new GenericSchemaFeatureInput() {

            @Override
            public String getType() {
              return "string";
            }

            @Override
            public SpecVersion getSpecVersion() {
              return SpecVersion.DRAFT_07;
            }

            @Override
            public Collection<JsonNode> getSamples() {
              return Collections.emptyList();
            }

            @Override
            public ObjectNode getSchema() {
              return jnf.objectNode();
            }

          });
      assertNull(examples);
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_06)
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3, "string"::equals)).build();
      final ObjectNode schema = inferrer.inferForSamples(IntStream.range(0, 5)
          .mapToObj(Integer::toString).map(jnf::textNode).collect(Collectors.toList()));
      assertEquals(ImmutableSet.of("0", "1", "2"), toStringSet(schema.path("examples")));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setSpecVersion(SpecVersion.DRAFT_06).setExamplesPolicy(input -> {
            assertNotNull(input.getType());
            return null;
          }).build();
      inferrer.inferForSamples(IntStream.range(0, 5).mapToObj(Integer::toString).map(jnf::textNode)
          .collect(Collectors.toList()));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_06)
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(3)).build();
      final ObjectNode sample = jnf.objectNode();
      sample.set("foo", jnf.arrayNode());
      final ObjectNode schema = inferrer.inferForSample(sample);
      assertNull(schema.path("properties").path("foo").get("examples"));
    }
  }

  @Test
  public void testMultipleOf() {
    final List<JsonNode> samples =
        ImmutableList.of(jnf.numberNode(2), jnf.numberNode(4), jnf.numberNode(6));
    final List<JsonNode> samples2 =
        ImmutableList.of(jnf.numberNode(2), jnf.numberNode(4), jnf.numberNode(6.0));
    final List<JsonNode> samples3 =
        ImmutableList.of(jnf.numberNode(0), jnf.numberNode(0), jnf.numberNode(0));
    final List<JsonNode> samples4 =
        ImmutableList.of(jnf.numberNode(2), jnf.numberNode(4), jnf.numberNode(6.5));
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setMultipleOfPolicy(input -> {
            assertNotNull(input.getSamples());
            assertNotNull(input.getSpecVersion());
            return null;
          }).setIntegerTypePreference(IntegerTypePreference.NEVER).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("multipleOf"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setMultipleOfPolicy(MultipleOfPolicies.gcd())
              .setIntegerTypePreference(IntegerTypePreference.NEVER).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertEquals(2, schema.path("multipleOf").intValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setMultipleOfPolicy(MultipleOfPolicies.gcd())
              .setIntegerTypePreference(IntegerTypePreference.NEVER).build();
      final ObjectNode schema = inferrer.inferForSamples(samples2);
      assertEquals(2, schema.path("multipleOf").intValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setMultipleOfPolicy(MultipleOfPolicies.gcd())
              .setIntegerTypeCriterion(IntegerTypeCriteria.mathematicalInteger()).build();
      final ObjectNode schema = inferrer.inferForSamples(samples2);
      assertEquals(2, schema.path("multipleOf").intValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setMultipleOfPolicy(MultipleOfPolicies.gcd())
              .setIntegerTypeCriterion(IntegerTypeCriteria.mathematicalInteger()).build();
      final ObjectNode schema = inferrer.inferForSamples(samples3);
      assertNull(schema.get("multipleOf"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setMultipleOfPolicy(MultipleOfPolicies.gcd())
              .setIntegerTypePreference(IntegerTypePreference.NEVER).build();
      final ObjectNode schema = inferrer.inferForSamples(samples4);
      assertNull(schema.get("multipleOf"));
    }
  }

  @Test
  public void testObjectSizeFeatures() {
    final List<JsonNode> samples = ImmutableList.of(jnf.objectNode(),
        jnf.objectNode().put("1", 1).put("2", (String) null).put("3", 3),
        jnf.objectNode().put("4", 4).put("1", "one"));
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setObjectSizeFeatures(EnumSet.of(ObjectSizeFeature.MIN_PROPERTIES))
          .setObjectSizeFeatures(EnumSet.noneOf(ObjectSizeFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minProperties"));
      assertNull(schema.get("maxProperties"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setObjectSizeFeatures(EnumSet.allOf(ObjectSizeFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertEquals(0, schema.path("minProperties").intValue());
      assertEquals(3, schema.path("maxProperties").intValue());
    }
  }

  @Test
  public void testArrayLengthFeatures() {
    final List<JsonNode> samples =
        ImmutableList.of(jnf.arrayNode(), jnf.arrayNode().add(1).add("2").add(true),
            jnf.arrayNode().add(1).add("2").add(true).add(true),
            jnf.arrayNode().add(1).add("2").add(true).add((String) null).add((String) null));
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setArrayLengthFeatures(EnumSet.of(ArrayLengthFeature.MAX_ITEMS))
          .setArrayLengthFeatures(EnumSet.noneOf(ArrayLengthFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minItems"));
      assertNull(schema.get("maxItems"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setArrayLengthFeatures(EnumSet.allOf(ArrayLengthFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertEquals(0, schema.path("minItems").intValue());
      assertEquals(5, schema.path("maxItems").intValue());
    }
  }

  @Test
  public void testStringLengthFeatures() {
    final List<JsonNode> samples =
        ImmutableList.of(jnf.textNode(""), jnf.textNode("a"), jnf.textNode("foobar"));
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setStringLengthFeatures(EnumSet.of(StringLengthFeature.MIN_LENGTH))
          .setStringLengthFeatures(EnumSet.noneOf(StringLengthFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minLength"));
      assertNull(schema.get("maxLength"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setStringLengthFeatures(EnumSet.allOf(StringLengthFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertEquals(0, schema.path("minLength").intValue());
      assertEquals(6, schema.path("maxLength").intValue());
    }
    for (JsonNode shouldBeText : Arrays.asList(jnf.numberNode(Double.NaN),
        jnf.numberNode(Double.NEGATIVE_INFINITY), jnf.numberNode(Double.POSITIVE_INFINITY),
        jnf.numberNode(Float.NaN), jnf.numberNode(Float.NEGATIVE_INFINITY),
        jnf.numberNode(Float.POSITIVE_INFINITY), jnf.binaryNode("abc".getBytes(UTF_8)))) {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setStringLengthFeatures(EnumSet.allOf(StringLengthFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSample(shouldBeText);
      assertEquals(shouldBeText.asText().length(), schema.path("minLength").intValue());
      assertEquals(shouldBeText.asText().length(), schema.path("maxLength").intValue());
    }
  }

  @Test
  public void testStringLengthAndFormat() {
    final String dateTimeString = Instant.now().toString();
    final List<JsonNode> samples = ImmutableList.of(jnf.textNode(dateTimeString));
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
        .setStringLengthFeatures(EnumSet.allOf(StringLengthFeature.class))
        .setFormatInferrer(FormatInferrers.dateTime()).build();
    final ObjectNode schema = inferrer.inferForSamples(samples);
    assertEquals(dateTimeString.length(), schema.path("minLength").intValue());
    assertEquals(dateTimeString.length(), schema.path("maxLength").intValue());
    assertEquals("date-time", schema.path("format").textValue());
  }

  @Test
  public void testNumberRangeFeatures() {
    final List<JsonNode> samples = ImmutableList.of(jnf.numberNode(BigDecimal.valueOf(1L)),
        jnf.numberNode(BigInteger.valueOf(2L)), jnf.numberNode(3L), jnf.numberNode(4.0f),
        jnf.numberNode((byte) 5), jnf.numberNode(7.5));
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setNumberRangeFeatures(EnumSet.of(NumberRangeFeature.MAXIMUM))
          .setNumberRangeFeatures(EnumSet.noneOf(NumberRangeFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minimum"));
      assertNull(schema.get("maximum"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setNumberRangeFeatures(EnumSet.allOf(NumberRangeFeature.class)).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertEquals(BigDecimal.valueOf(1L), schema.path("minimum").decimalValue());
      assertEquals(BigDecimal.valueOf(7.5), schema.path("maximum").decimalValue());
    }
  }

  @Test
  public void testFakeGenericFeature() {
    // Fake feature that always attaches {"foo":"bar"}
    final GenericSchemaFeature fakeFeature = new GenericSchemaFeature() {
      @Override
      public ObjectNode getFeatureResult(GenericSchemaFeatureInput input) {
        final ObjectNode result = jnf.objectNode();
        result.put("foo", "bar");
        return result;
      }
    };
    final JsonSchemaInferrer inferrer =
        JsonSchemaInferrer.newBuilder().addAdditionalSchemaFeatures(fakeFeature).build();
    final ObjectNode schema = inferrer.inferForSample(null);
    assertEquals("bar", schema.path("foo").textValue());
  }

}
