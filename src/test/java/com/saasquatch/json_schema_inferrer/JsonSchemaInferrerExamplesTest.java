package com.saasquatch.json_schema_inferrer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
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

  private static final String quicktypeCommitHash = "f75f66bff3d1f812b61c481637c12173778a29b8";
  private static CloseableHttpClient httpClient;
  private static final ObjectMapper mapper = new ObjectMapper();

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
    for (String jsonUrl : getSampleJsonUrls()) {
      doTestForJsonUrl(jsonUrl);
    }
  }

  private static void doTestForJsonUrl(String jsonUrl) {
    final JsonNode sampleJson;
    // Not being able to load the sample JSON should not be considered a failure
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
    for (JsonSchemaInferrer inferrer : getTestInferrers()) {
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
        System.out.println(e.getClass().getSimpleName() + " encountered");
        System.out.println(schemaJson.toPrettyString());
        System.out.println("Error messages:");
        e.getAllMessages().forEach(System.out::println);
        fail(format("Inferred schema for url[%s] failed to validate", jsonUrl), e);
      }
    }
  }

  private static Iterable<String> getSampleJsonUrls() {
    return () -> getQuicktypeSampleJsonUrls().iterator();
  }

  private static Stream<String> getQuicktypeSampleJsonUrls() {
    return Stream
        .of("/test/inputs/json/misc", "/test/inputs/json/priority", "/test/inputs/json/samples")
        .map("https://api.github.com/repos/quicktype/quicktype/contents"::concat)
        .flatMap(dirBaseUrl -> getJsonDownloadUrls(dirBaseUrl, quicktypeCommitHash));
  }

  private static Stream<String> getJsonDownloadUrls(String dirBaseUrl, String commitHash) {
    final JsonNode respJson;
    try {
      final HttpGet request = new HttpGet(
          new URIBuilder(dirBaseUrl).addParameter("ref", commitHash).build());
      respJson = httpClient.execute(request, new AbstractHttpClientResponseHandler<JsonNode>() {
        @Override
        public JsonNode handleEntity(HttpEntity entity) throws IOException {
          return mapper.readTree(entity.getContent());
        }
      });
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return StreamSupport.stream(respJson.spliterator(), false)
        .filter(j -> "file".equals(j.path("type").textValue()))
        .filter(j -> j.path("name").textValue().endsWith(".json"))
        .map(j -> j.path("download_url").textValue())
        .filter(Objects::nonNull)
        .map(downloadUrl -> processGitHubDownloadUrl(downloadUrl, commitHash));
  }

  private static String processGitHubDownloadUrl(String url, String commitHash) {
    try {
      // Use jsdelivr to speed up the downloads
      final String host = new URL(url).getHost();
      url = url.replaceFirst(host, "cdn.jsdelivr.net/gh");
      url = url.replaceFirst('/' + commitHash, '@' + commitHash);
      return url;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Collection<JsonSchemaInferrer> getTestInferrers() {
    final List<JsonSchemaInferrer> inferrers = new ArrayList<>();
    for (SpecVersion specVersion : SpecVersion.values()) {
      for (boolean inferStringFormat : Arrays.asList(true, false)) {
        if (specVersion == SpecVersion.DRAFT_07 && inferStringFormat) {
          /*
           * Skip tests for inferring format with draft-07 due to a disagreement between Java time
           * and the schema library on what a valid time string is.
           */
          continue;
        }
        final JsonSchemaInferrer.Builder builder =
            JsonSchemaInferrer.newBuilder().withSpecVersion(specVersion);
        if (!inferStringFormat) {
          builder.withStringFormatInferrer(null);
        }
        try {
          inferrers.add(builder.build());
        } catch (IllegalArgumentException e) {
          // Ignore
        }
      }
    }
    return Collections.unmodifiableList(inferrers);
  }

  @Nullable
  private static JsonNode loadJsonFromUrl(String jsonUrl) throws IOException {
    final HttpGet request = new HttpGet(jsonUrl);
    request.setConfig(RequestConfig.custom()
        .setConnectTimeout(1, TimeUnit.SECONDS)
        .setConnectionRequestTimeout(1, TimeUnit.SECONDS)
        .setResponseTimeout(3, TimeUnit.SECONDS)
        .build());
    return httpClient.execute(request, new AbstractHttpClientResponseHandler<JsonNode>() {
      @Override
      public JsonNode handleEntity(HttpEntity entity) throws IOException {
        final byte[] byteArray = EntityUtils.toByteArray(entity);
        if (byteArray.length > 1 << 20) {
          System.out.printf(Locale.ROOT, "JSON at url[%s] is too large [%d].\n", jsonUrl,
              byteArray.length);
          return null;
        }
        return mapper.readTree(byteArray);
      }
    });
  }

  private static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }

  private static Map<String, Object> toMap(Object o) {
    return mapper.convertValue(o, new TypeReference<Map<String, Object>>() {});
  }

}
