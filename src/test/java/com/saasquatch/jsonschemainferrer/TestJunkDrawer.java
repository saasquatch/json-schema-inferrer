package com.saasquatch.jsonschemainferrer;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class TestJunkDrawer {

  private TestJunkDrawer() {}

  public static final JsonNodeFactory jnf = JsonNodeFactory.instance;
  public static final ObjectMapper mapper = new ObjectMapper();

  public static Set<String> toStringSet(JsonNode arrayNode) {
    return Streams.stream(arrayNode)
        .map(JsonNode::textValue)
        .collect(ImmutableSet.toImmutableSet());
  }

  public static JsonNode loadJson(String resourceName) {
    try (InputStream in = TestJunkDrawer.class.getResourceAsStream(resourceName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      System.out.printf(Locale.ROOT, "Exception encountered loading JSON from resource[%s]. "
          + "Error message: [%s].%n", resourceName, e.getMessage());
      e.printStackTrace();
      throw new UncheckedIOException(e);
    }
  }

  public static List<String> getResourceNamesUnderDir(String resourceDirName) {
    try (
        InputStream in = TestJunkDrawer.class.getResourceAsStream(resourceDirName);
        BufferedReader br = new BufferedReader(
            new InputStreamReader(Objects.requireNonNull(in), UTF_8))
    ) {
      return br.lines().collect(ImmutableList.toImmutableList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
