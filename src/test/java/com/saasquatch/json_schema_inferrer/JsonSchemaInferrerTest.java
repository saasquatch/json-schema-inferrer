package com.saasquatch.json_schema_inferrer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSchemaInferrerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  private JsonNode loadJson(String fileName) {
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
