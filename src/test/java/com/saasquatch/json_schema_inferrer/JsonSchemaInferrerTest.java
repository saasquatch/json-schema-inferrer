package com.saasquatch.json_schema_inferrer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
  public void testSimple() {
    final JsonNode simple = loadJson("simple.json");
    {
      final ObjectNode schema = JsonSchemaInferrer.newBuilder().build().infer(simple);
      assertTrue(schema.hasNonNull("$schema"));
      assertTrue(schema.get("$schema").textValue().contains("-04"));
      assertTrue(schema.hasNonNull("type"));
    }
    {
      final ObjectNode schema =
          JsonSchemaInferrer.newBuilder().draft06().outputDollarSchema(false).build().infer(simple);
      assertFalse(schema.hasNonNull("$schema"));
      assertTrue(schema.hasNonNull("type"));
    }
  }

}
