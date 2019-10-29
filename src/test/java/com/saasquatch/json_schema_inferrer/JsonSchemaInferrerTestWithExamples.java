package com.saasquatch.json_schema_inferrer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nullable;
import org.apache.commons.validator.routines.UrlValidator;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteStreams;

public class JsonSchemaInferrerTestWithExamples {

  private final ObjectMapper mapper = new ObjectMapper();
  private final List<JsonSchemaInferrer> testInferrers = getTestInferrers();
  private final List<String> jsonUrls = loadJsonUrls();

  @Test
  public void test() {
    for (String jsonUrl : jsonUrls) {
      doTestForJsonUrl(jsonUrl);
    }
  }

  private void doTestForJsonUrl(String jsonUrl) {
    final JsonNode sampleJson;
    try {
      sampleJson = loadJsonFromUrl(jsonUrl);
      if (sampleJson == null) {
        return;
      }
    } catch (IOException e) {
      System.out.printf(Locale.ROOT,
          "Exception encountered loading JSON from URL[%s]. Skipping tests.\n", jsonUrl);
      e.printStackTrace(System.out);
      return;
    }
    System.out.printf(Locale.ROOT, "Got valid JSON from url[%s]\n", jsonUrl);
    for (JsonSchemaInferrer inferrer : testInferrers) {
      final ObjectNode schemaJson = inferrer.infer(sampleJson);
      assertNotNull(schemaJson, format("Inferred schema for URL[%s] is null", jsonUrl));
      final Schema schema;
      try {
        schema = SchemaLoader.load(new JSONObject(toMap(schemaJson)));
      } catch (RuntimeException e) {
        fail(format("Unable to parse the inferred schema for url[%s]", jsonUrl), e);
        throw e;
      }
      try {
        if (sampleJson.isObject()) {
          schema.validate(new JSONObject(toMap(sampleJson)));
        } else if (sampleJson.isArray()) {
          schema.validate(new JSONArray(sampleJson.toString()));
        } else {
          schema.validate(mapper.convertValue(sampleJson, Object.class));
        }
      } catch (ValidationException e) {
        System.out.println(schemaJson.toPrettyString());
        System.out.println("Error messages:");
        e.getAllMessages().forEach(System.out::println);
        fail(format("Inferred schema for url[%s] failed to validate", jsonUrl), e);
      }
    }
  }

  private static List<String> loadJsonUrls() {
    try {
      final URL url = new URL("https://raw.githubusercontent.com/quicktype/quicktype/"
          + "b37bd7ee621c7c78807e388507e631771da1f6e1/test/awesome-json-datasets");
      try (InputStream in = url.openStream();
          BufferedReader br = new BufferedReader(new InputStreamReader(in, UTF_8))) {
        final List<String> result = br.lines().filter(jsonUrl -> !jsonUrl.contains(".gov/"))
            .filter(jsonUrl -> !jsonUrl.contains("vizgr.org"))
            .filter(UrlValidator.getInstance()::isValid).collect(Collectors.toList());
        System.out.printf(Locale.ROOT, "%d urls loaded\n", result.size());
        return result;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static List<JsonSchemaInferrer> getTestInferrers() {
    final List<JsonSchemaInferrer> inferrers = new ArrayList<>();
    for (String draft : Arrays.asList("04", "06", "07")) {
      final JsonSchemaInferrer.Builder builder = JsonSchemaInferrer.newBuilder().inferFormat(false);
      switch (draft) {
        case "04":
          builder.draft04();
          break;
        case "06":
          builder.draft06();
          break;
        case "07":
          builder.draft07();
          break;
        default:
          throw new AssertionError("Unknown draft: " + draft);
      }
      try {
        inferrers.add(builder.build());
      } catch (IllegalArgumentException e) {
        // Ignore
      }
    }
    return Collections.unmodifiableList(inferrers);
  }

  private static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }

  private Map<String, Object> toMap(Object o) {
    return mapper.convertValue(o, new TypeReference<Map<String, Object>>() {});
  }

  @Nullable
  private JsonNode loadJsonFromUrl(String jsonUrl) throws IOException {
    final URL url = new URL(jsonUrl);
    final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setInstanceFollowRedirects(true);
    conn.setConnectTimeout(1000);
    conn.setReadTimeout(2500);
    conn.addRequestProperty("Accept-Encoding", "gzip");
    final int status = conn.getResponseCode();
    if (status >= 300) {
      System.out.printf(Locale.ROOT, "status[%d] received\n", status);
      return null;
    }
    final boolean gzip = "gzip".equals(conn.getContentEncoding());
    try (InputStream in =
        gzip ? new GZIPInputStream(conn.getInputStream()) : conn.getInputStream()) {
      final byte[] byteArray = ByteStreams.toByteArray(in);
      if (byteArray.length > 500_000) {
        System.out.printf("JSON at url[%s] is too large [%d].\n", jsonUrl, byteArray.length);
        return null;
      }
      return mapper.readTree(byteArray);
    }
  }

}
