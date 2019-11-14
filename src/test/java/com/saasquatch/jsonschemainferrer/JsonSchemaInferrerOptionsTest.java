package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.toStringSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.saasquatch.jsonschemainferrer.AdditionalPropertiesPolicies;
import com.saasquatch.jsonschemainferrer.ArrayLengthFeature;
import com.saasquatch.jsonschemainferrer.DefaultPolicies;
import com.saasquatch.jsonschemainferrer.FormatInferrer;
import com.saasquatch.jsonschemainferrer.FormatInferrers;
import com.saasquatch.jsonschemainferrer.IntegerTypePreference;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.ObjectSizeFeature;
import com.saasquatch.jsonschemainferrer.RequiredPolicies;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import com.saasquatch.jsonschemainferrer.StringLengthFeature;
import com.saasquatch.jsonschemainferrer.TitleGenerators;

public class JsonSchemaInferrerOptionsTest {

  @Test
  public void testFormatInferrers() {
    // Fake format inferrer that always uses the string length as the format
    final FormatInferrer testStrLenFormatInferrer = input -> {
      final String textValue = input.getSample().textValue();
      if (textValue == null) {
        return null;
      }
      return textValue.length() + "";
    };
    assertSame(FormatInferrers.noOp(), FormatInferrers.chained());
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
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setFormatInferrer(FormatInferrers.chained(FormatInferrers.noOp(), FormatInferrers.noOp(),
              FormatInferrers.noOp()))
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
  public void testRequired() {
    final List<ObjectNode> samples = ImmutableList.of(jnf.objectNode().put("1", 1).put("2", 2),
        jnf.objectNode().put("1", "1").put("2", (String) null));
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setRequiredPolicy(RequiredPolicies.noOp()).build();
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
  public void testTitleGenerator() {
    final JsonNode sample = jnf.objectNode().put("fieldName", "value");
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setTitleGenerator(TitleGenerators.noOp()).build();
      assertNull(inferrer.inferForSample(sample).path("properties").path("fieldName").get("title"));
    }
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .setTitleGenerator(TitleGenerators.useFieldNames()).build();
      assertEquals("fieldName", inferrer.inferForSample(sample).path("properties").path("fieldName")
          .get("title").textValue());
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setTitleGenerator(input -> {
            return Optional.ofNullable(input.getFieldName()).map(String::toUpperCase).orElse(null);
          }).build();
      assertEquals("FIELDNAME", inferrer.inferForSample(sample).path("properties").path("fieldName")
          .get("title").textValue());
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
  public void testObjectSizeFeatures() {
    final List<JsonNode> samples = ImmutableList.of(jnf.objectNode(),
        jnf.objectNode().put("1", 1).put("2", (String) null).put("3", 3),
        jnf.objectNode().put("4", 4).put("1", "one"));
    {
      final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
          .enable(ObjectSizeFeature.MIN_PROPERTIES).disable(ObjectSizeFeature.values()).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minProperties"));
      assertNull(schema.get("maxProperties"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().enable(ObjectSizeFeature.values()).build();
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
          .enable(ArrayLengthFeature.MAX_ITEMS).disable(ArrayLengthFeature.values()).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minItems"));
      assertNull(schema.get("maxItems"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().enable(ArrayLengthFeature.values()).build();
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
          .enable(StringLengthFeature.MIN_LENGTH).disable(StringLengthFeature.values()).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertNull(schema.get("minLength"));
      assertNull(schema.get("maxLength"));
    }
    {
      final JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().enable(StringLengthFeature.values()).build();
      final ObjectNode schema = inferrer.inferForSamples(samples);
      assertEquals(0, schema.path("minLength").intValue());
      assertEquals(6, schema.path("maxLength").intValue());
    }
  }

  @Test
  public void testStringLengthAndFormat() {
    final String dateTimeString = Instant.now().toString();
    final List<JsonNode> samples = ImmutableList.of(jnf.textNode(dateTimeString));
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
        .enable(StringLengthFeature.values()).setFormatInferrer(FormatInferrers.dateTime()).build();
    final ObjectNode schema = inferrer.inferForSamples(samples);
    assertEquals(dateTimeString.length(), schema.path("minLength").intValue());
    assertEquals(dateTimeString.length(), schema.path("maxLength").intValue());
    assertEquals("date-time", schema.path("format").textValue());
  }

}
