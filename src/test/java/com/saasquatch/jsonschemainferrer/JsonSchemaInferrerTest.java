package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.loadJson;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.toStringSet;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.POJONode;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class JsonSchemaInferrerTest {

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
    assertThrows(IllegalArgumentException.class, () -> inferrer.inferForSample(new POJONode(null)));
    assertThrows(IllegalArgumentException.class, () -> {
      final ObjectNode sampleObj = jnf.objectNode();
      sampleObj.set("foo",
          jnf.arrayNode().add(jnf.arrayNode().add(jnf.arrayNode().add(jnf.pojoNode("")))));
      inferrer.inferForSample(sampleObj);
    });
  }

  @Test
  public void testSimpleExample() {
    final JsonNode simple = loadJson("simple.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().inferForSample(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.path("$schema").stringValue(null).contains("-04"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder()
          .setSpecVersion(SpecVersion.DRAFT_06)
          .build()
          .inferForSample(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.path("$schema").stringValue(null).contains("-06"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder()
          .addFormatInferrers(FormatInferrers.dateTime())
          .build()
          .inferForSample(simple);
      assertTrue(schema.hasNonNull("properties"));
      assertTrue(schema.path("properties").isObject());
      assertEquals("integer", schema.path("properties").path("id").path("type").stringValue(null));
      assertEquals("string", schema.path("properties").path("slug").path("type").stringValue(null));
      assertEquals("boolean", schema.path("properties").path("admin").path("type").stringValue(null));
      assertEquals("null", schema.path("properties").path("avatar").path("type").stringValue(null));
      assertEquals("string", schema.path("properties").path("date").path("type").stringValue(null));
      assertEquals("date-time", schema.path("properties").path("date").path("format").stringValue(null));
      assertEquals("object", schema.path("properties").path("article").path("type").stringValue(null));
      assertTrue(schema.path("properties").path("article").isObject());
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("title").path("type").stringValue(null));
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("description").path("type").stringValue(null));
      assertEquals("string", schema.path("properties").path("article").path("properties")
          .path("body").path("type").stringValue(null));
      assertEquals("array", schema.path("properties").path("comments").path("type").stringValue(null));
      assertTrue(schema.path("properties").path("comments").path("items").isObject());
      assertEquals(ImmutableSet.of("string", "null"), toStringSet(schema.path("properties")
          .path("comments").path("items").path("properties").path("body").path("type")));
    }
  }

  @Test
  public void testAdvancedExample() {
    final JsonNode advanced = loadJson("advanced.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().inferForSample(advanced);
      assertTrue(schema.path("items").isObject());
      assertTrue(schema.path("items").path("properties").path("tags").isObject());
      assertEquals("integer",
          schema.path("items").path("properties").path("id").path("type").stringValue(null));
      assertEquals("number",
          schema.path("items").path("properties").path("price").path("type").stringValue(null));
      assertEquals("number", schema.path("items").path("properties").path("dimensions")
          .path("properties").path("length").path("type").stringValue(null));
    }
  }

}
