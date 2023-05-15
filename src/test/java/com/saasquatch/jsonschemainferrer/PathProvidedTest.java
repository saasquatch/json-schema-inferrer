package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.mapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

/**
 * @author sbroekhuis
 */
public class PathProvidedTest {

  @SuppressWarnings("SameParameterValue")
  private JsonNode loadJson(String fileName) {
    try (InputStream in = this.getClass().getResourceAsStream(fileName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  public void genericFeatureTest() {
    final Queue<String> expected = new LinkedList<>(ImmutableList.of("$[\"id\"]",
        "$[\"slug\"]",
        "$[\"admin\"]",
        "$[\"avatar\"]",
        "$[\"date\"]",
        "$[\"article\"][\"title\"]",
        "$[\"article\"][\"description\"]",
        "$[\"article\"][\"body\"]",
        "$[\"article\"]",
        "$[\"comments\"][*][\"body\"]",
        "$[\"comments\"][*][\"body\"]",
        "$[\"comments\"][*][\"tags\"][*]",
        "$[\"comments\"][*][\"tags\"][*]",
        "$[\"comments\"][*][\"tags\"]",
        "$[\"comments\"][*]",
        "$[\"comments\"]",
        "$"));
    final JsonNode jsonNode = loadJson("simple.json");
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
        .addGenericSchemaFeatures(input -> {
          assertEquals(expected.poll(), input.getPath());
          return null;
        }).build();
    inferrer.inferForSample(jsonNode);
  }

  @Test
  public void testSpecialCharacters() {
    final Queue<String> expected = new LinkedList<>(ImmutableList.of("$[\"foo[\\\"bar\\\"]\"]", "$"));
    final String fieldName = "foo[\"bar\"]";
    final JsonNode root = JsonNodeFactory.instance.objectNode()
        .put(fieldName, 42);
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
        .addGenericSchemaFeatures(input -> {
          assertEquals(expected.poll(), input.getPath());
          return null;
        }).build();
    inferrer.inferForSample(root);
  }

  @Test
  public void enumExtractorTest() {
    final Queue<String> expected = new LinkedList<>(ImmutableList.of("$",
        "$[\"id\"]",
        "$[\"slug\"]",
        "$[\"admin\"]",
        "$[\"avatar\"]",
        "$[\"date\"]",
        "$[\"article\"]",
        "$[\"article\"][\"title\"]",
        "$[\"article\"][\"description\"]",
        "$[\"article\"][\"body\"]",
        "$[\"comments\"]",
        "$[\"comments\"][*]",
        "$[\"comments\"][*][\"body\"]",
        "$[\"comments\"][*][\"tags\"]",
        "$[\"comments\"][*][\"tags\"][*]"));
    final JsonNode jsonNode = loadJson("simple.json");
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder().addEnumExtractors(input -> {
      assertEquals(expected.poll(), input.getPath());
      return Collections.emptySet();
    }).build();
    inferrer.inferForSample(jsonNode);
  }

  @Test
  public void formatInferTest() {
    final Queue<String> expected = new LinkedList<>(List.of("$[\"id\"]",
        "$[\"slug\"]",
        "$[\"admin\"]",
        "$[\"avatar\"]",
        "$[\"date\"]",
        "$[\"article\"][\"title\"]",
        "$[\"article\"][\"description\"]",
        "$[\"article\"][\"body\"]",
        "$[\"comments\"][*][\"body\"]",
        "$[\"comments\"][*][\"body\"]",
        "$[\"comments\"][*][\"body\"]",
        "$[\"comments\"][*][\"tags\"][*]",
        "$[\"comments\"][*][\"tags\"][*]",
        "$[\"comments\"][*][\"tags\"][*]",
        "$[\"comments\"][*]",
        "$[\"comments\"]",
        "$"));
    final JsonNode jsonNode = loadJson("simple.json");
    final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
        .addFormatInferrers(input -> {
          assertEquals(expected.poll(), input.getPath());
          return null;
        }).build();
    inferrer.inferForSample(jsonNode);
  }

}
