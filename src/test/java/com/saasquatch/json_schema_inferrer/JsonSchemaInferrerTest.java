package com.saasquatch.json_schema_inferrer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSchemaInferrerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  private JsonNode loadJson(String fileName) {
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  public void testBasic() {
    assertDoesNotThrow(() -> JsonSchemaInferrer.newBuilder().build().infer(null));
    assertThrows(IllegalArgumentException.class,
        () -> JsonSchemaInferrer.newBuilder().draft04().includeExamples(true).build());
  }

  @Test
  public void testSimpleExample() throws Exception {
    final JsonNode simple = loadJson("simple.json");
//    {
//      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(simple);
//      assertTrue(schema.hasNonNull("$schema"));
//      assertTrue(schema.path("$schema").textValue().contains("-04"));
//      assertTrue(schema.hasNonNull("type"));
//    }
//    {
//      final ObjectNode schema = JsonSchemaInferrer.newBuilder().draft06().build().infer(simple);
//      assertTrue(schema.hasNonNull("$schema"));
//      assertTrue(schema.path("$schema").textValue().contains("-06"));
//      assertTrue(schema.hasNonNull("type"));
//    }
//    {
//      final ObjectNode schema =
//          JsonSchemaInferrer.newBuilder().draft06().includeDollarSchema(false).build().infer(simple);
//      assertFalse(schema.hasNonNull("$schema"));
//      assertTrue(schema.hasNonNull("type"));
//    }
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(simple);
      System.out.println(schema.toPrettyString());
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
      assertTrue(
          StreamSupport
              .stream(schema.path("properties").path("comments").path("items").path("oneOf")
                  .spliterator(), false)
              .anyMatch(
                  j -> j.path("properties").path("body").path("type").asText("").equals("string")));
      assertTrue(
          StreamSupport
              .stream(schema.path("properties").path("comments").path("items").path("oneOf")
                  .spliterator(), false)
              .anyMatch(
                  j -> j.path("properties").path("body").path("type").asText("").equals("null")));
    }
  }

  @Test
  public void testAdvancedExample() throws Exception {
    final JsonNode advanced = loadJson("advanced.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(advanced);
      System.out.println(schema.toPrettyString());
      assertTrue(schema.path("items").isObject());
      assertTrue(StreamSupport.stream(schema.path("items").path("oneOf").spliterator(), false)
          .anyMatch(j -> j.path("properties").path("tags").isObject()));
      assertTrue(
          StreamSupport.stream(schema.path("items").path("oneOf").spliterator(), false).anyMatch(
              j -> j.path("properties").path("id").path("type").asText("").equals("integer")));
      assertTrue(
          StreamSupport.stream(schema.path("items").path("oneOf").spliterator(), false).anyMatch(
              j -> j.path("properties").path("price").path("type").asText("").equals("number")));
      assertEquals(new HashSet<>(Arrays.asList("integer", "number")),
          StreamSupport.stream(schema.path("items").path("oneOf").spliterator(), false)
              .map(j -> j.path("properties").path("dimensions").path("properties").path("length")
                  .path("type"))
              .map(j -> j.textValue()).filter(Objects::nonNull).collect(Collectors.toSet()));
    }
  }

}
