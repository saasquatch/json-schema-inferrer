package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.TestJunkDrawer.jnf;
import static com.saasquatch.json_schema_inferrer.TestJunkDrawer.toStringSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class JsonSchemaInferrerOptionsTest {

  @Test
  public void testFormatInferrers() {
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
      final ObjectNode schema = inferrer.inferForSample(jnf.objectNode().put("1", 1).put("2", "2"));
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

}
