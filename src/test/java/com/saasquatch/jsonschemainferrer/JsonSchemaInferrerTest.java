package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.mapper;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.toStringSet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;

public class JsonSchemaInferrerTest {

  private JsonNode loadJson(String fileName) {
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  public void testBasic() {
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder().build();
    assertDoesNotThrow(() -> inferrer.inferForSample(null));
    assertThrows(IllegalArgumentException.class,
        () -> inferrer.inferForSamples(Collections.emptyList()));
    assertDoesNotThrow(() -> inferrer.inferForSamples(Collections.singleton(jnf.missingNode())));
    assertDoesNotThrow(() -> inferrer.inferForSamples(Collections.singleton(null)));
    assertDoesNotThrow(() -> inferrer.inferForSamples(Collections.singleton(jnf.nullNode())));
    assertThrows(IllegalArgumentException.class, () -> inferrer.inferForSample(jnf.pojoNode("")));
    assertThrows(IllegalArgumentException.class,
        () -> inferrer.inferForSample(jnf.arrayNode().add(jnf.pojoNode(""))));
    assertThrows(IllegalArgumentException.class, () -> {
      final ObjectNode sampleObj = jnf.objectNode();
      sampleObj.set("foo",
          jnf.arrayNode().add(jnf.arrayNode().add(jnf.arrayNode().add(jnf.pojoNode("")))));
      inferrer.inferForSample(sampleObj);
    });
    assertThrows(IllegalArgumentException.class,
        () -> JsonSchemaInferrer.newBuilder().setExamplesLimit(-1));
    assertThrows(IllegalArgumentException.class, () -> JsonSchemaInferrer.newBuilder()
        .setSpecVersion(SpecVersion.DRAFT_04).setExamplesLimit(1).build());
  }

  @Test
  public void testSimpleExample() throws Exception {
    final JsonNode simple = loadJson("simple.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().inferForSample(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.path("$schema").textValue().contains("-04"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_06)
          .build().inferForSample(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.path("$schema").textValue().contains("-06"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder()
          .setFormatInferrer(FormatInferrers.dateTime()).build().inferForSample(simple);
      assertTrue(schema.hasNonNull("properties"));
      assertTrue(schema.path("properties").isObject());
      assertEquals("integer", schema.path("properties").path("id").path("type").textValue());
      assertEquals("string", schema.path("properties").path("slug").path("type").textValue());
      assertEquals("boolean", schema.path("properties").path("admin").path("type").textValue());
      assertEquals("null", schema.path("properties").path("avatar").path("type").textValue());
      assertEquals("string", schema.path("properties").path("date").path("type").textValue());
      assertEquals("date-time", schema.path("properties").path("date").path("format").textValue());
      assertEquals("object", schema.path("properties").path("article").path("type").textValue());
      assertTrue(schema.path("properties").path("article").isObject());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("title").path("type").textValue());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("description").path("type").textValue());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("body").path("type").textValue());
      assertEquals("array", schema.path("properties").path("comments").path("type").textValue());
      assertTrue(schema.path("properties").path("comments").path("items").isObject());
      assertEquals(ImmutableSet.of("string", "null"), toStringSet(schema.path("properties")
          .path("comments").path("items").path("properties").path("body").path("type")));
    }
  }

  @Test
  public void testAdvancedExample() throws Exception {
    final JsonNode advanced = loadJson("advanced.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().inferForSample(advanced);
      assertTrue(schema.path("items").isObject());
      assertTrue(schema.path("items").path("properties").path("tags").isObject());
      assertEquals("integer",
          schema.path("items").path("properties").path("id").path("type").textValue());
      assertEquals("number",
          schema.path("items").path("properties").path("price").path("type").textValue());
      assertEquals("number", schema.path("items").path("properties").path("dimensions")
          .path("properties").path("length").path("type").textValue());
    }
  }

}
