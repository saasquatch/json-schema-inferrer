package com.saasquatch.json_schema_inferrer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSchemaInferrerExamplesTest {

  private static CloseableHttpClient httpClient;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Collection<JsonSchemaInferrer> testInferrers = getTestInferrers();
  private final Collection<String> jsonUrls = loadJsonUrls();

  @BeforeAll
  public static void beforeAll() {
    httpClient = HttpClients.custom().disableCookieManagement().build();
  }

  @AfterAll
  public static void afterAll() throws Exception {
    httpClient.close();
  }

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
      System.out.printf(Locale.ROOT, "Exception encountered loading JSON from url[%s]. "
          + "Error message: [%s]. Skipping tests.\n", jsonUrl, e.getMessage());
      return;
    }
    System.out.printf(Locale.ROOT, "Got valid JSON from url[%s]\n", jsonUrl);
    for (JsonSchemaInferrer inferrer : testInferrers) {
      final ObjectNode schemaJson = inferrer.infer(sampleJson);
      assertNotNull(schemaJson, format("Inferred schema for url[%s] is null", jsonUrl));
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

  private static Collection<String> loadJsonUrls() {
    final String url = "https://raw.githubusercontent.com/quicktype/quicktype/b37bd7ee621c7c78807e388507e631771da1f6e1/test/awesome-json-datasets";
    try (CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
      final int status = response.getCode();
      if (status >= 300) {
        throw new IllegalStateException(format("status[%d] received for url[%s]\n", status, url));
      }
      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(response.getEntity().getContent(), UTF_8))) {
        final Set<String> result = br.lines()
            .filter(jsonUrl -> !jsonUrl.contains(".gov/"))
            .filter(jsonUrl -> !jsonUrl.contains("vizgr.org"))
            .filter(UrlValidator.getInstance()::isValid)
            .collect(Collectors.toSet());
        System.out.printf(Locale.ROOT, "%d urls loaded\n", result.size());
        return result;
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static List<JsonSchemaInferrer> getTestInferrers() {
    final List<JsonSchemaInferrer> inferrers = new ArrayList<>();
    for (SpecVersion specVersion : SpecVersion.values()) {
      final JsonSchemaInferrer.Builder builder =
          JsonSchemaInferrer.newBuilder().inferFormat(false).withSpecVersion(specVersion);
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
    final HttpGet request = new HttpGet(jsonUrl);
    request.setConfig(RequestConfig.custom()
        .setConnectTimeout(1, TimeUnit.SECONDS)
        .setConnectionRequestTimeout(1, TimeUnit.SECONDS)
        .setResponseTimeout(2500, TimeUnit.MILLISECONDS)
        .build());
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      final int status = response.getCode();
      if (status >= 300) {
        System.out.printf(Locale.ROOT, "status[%d] received for url[%s]\n", status, jsonUrl);
        return null;
      }
      final byte[] byteArray = EntityUtils.toByteArray(response.getEntity());
      if (byteArray.length > 500_000) {
        System.out.printf(Locale.ROOT, "JSON at url[%s] is too large [%d].\n", jsonUrl,
            byteArray.length);
        return null;
      }
      return mapper.readTree(byteArray);
    }
  }

}
