package com.saasquatch.jsonschemainferrer;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;

public final class TestJunkDrawer {

  private TestJunkDrawer() {}

  public static final JsonNodeFactory jnf = JsonNodeFactory.instance;
  public static final ObjectMapper mapper = new ObjectMapper();
  public static final com.fasterxml.jackson.databind.ObjectMapper mapper2 =
      new com.fasterxml.jackson.databind.ObjectMapper();

  public static Set<String> toStringSet(JsonNode arrayNode) {
    return arrayNode.valueStream()
        .filter(Objects::nonNull)
        .map(JsonNode::stringValueOpt)
        .flatMap(Optional::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  public static JsonNode loadJson(String resourceName) {
    try (InputStream in = TestJunkDrawer.class.getResourceAsStream(resourceName)) {
      return mapper.readTree(in);
    } catch (IOException e) {
      System.out.printf(Locale.ROOT, "Exception encountered loading JSON from resource[%s]. "
          + "Error message: [%s].%n", resourceName, e.getMessage());
      //noinspection CallToPrintStackTrace
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

  public static com.fasterxml.jackson.databind.JsonNode jackson3To2(JsonNode j) {
    if (j == null) {
      return null;
    }
    try {
      return mapper2.readTree(mapper.writeValueAsString(j));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
