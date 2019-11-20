package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.format;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.mapper;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class JsonSchemaInferrerExamplesTest {

  private static final String QUICKTYPE_REPO_BASE_URL =
      "https://cdn.jsdelivr.net/gh/quicktype/quicktype@f75f66bff3d1f812b61c481637c12173778a29b8";
  // private static final String CONST_BASE = "com.saasquatch.jsonschemainferrer.test.";
  private static CloseableHttpClient httpClient;
  private static final Collection<JsonSchemaInferrer> testInferrers = getTestInferrers();
  private static final LoadingCache<String, JsonNode> testJsonCache =
      CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, JsonNode>() {
        @Override
        public JsonNode load(String url) throws Exception {
          return loadJsonFromUrl(url);
        }
      });

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

  @Test
  public void testMulti() {
    for (List<String> jsonUrls : Iterables.partition(getSampleJsonUrls(), 4)) {
      doTestForJsonUrls(jsonUrls);
    }
  }

  private static void doTestForJsonUrl(String jsonUrl) {
    final JsonNode sampleJson;
    // Not being able to load the sample JSON should not be considered a failure
    try {
      sampleJson = testJsonCache.get(jsonUrl);
      if (sampleJson == null) {
        return;
      }
    } catch (Exception e) {
      System.out.printf(Locale.ROOT, "Exception encountered loading JSON from url[%s]. "
          + "Error message: [%s]. Skipping tests.\n", jsonUrl, e.getMessage());
      return;
    }
    System.out.printf(Locale.ROOT, "Got valid JSON from url[%s]\n", jsonUrl);
    for (JsonSchemaInferrer inferrer : testInferrers) {
      final ObjectNode schemaJson = inferrer.inferForSample(sampleJson);
      assertNotNull(schemaJson, format("Inferred schema for url[%s] is null", jsonUrl));
      final Schema schema;
      try {
        schema = SchemaLoader.load(new JSONObject(schemaJson.toString()));
      } catch (RuntimeException e) {
        fail(format("Unable to parse the inferred schema for url[%s]", jsonUrl), e);
        throw e;
      }
      try {
        if (sampleJson.isObject()) {
          schema.validate(new JSONObject(sampleJson.toString()));
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

  private static void doTestForJsonUrls(Collection<String> jsonUrls) {
    final List<JsonNode> sampleJsons = jsonUrls.stream().map(jsonUrl -> {
      try {
        return testJsonCache.get(jsonUrl);
      } catch (Exception e) {
        System.out.printf(Locale.ROOT, "Exception encountered loading JSON from url[%s]. "
            + "Error message: [%s]. Skipping tests.\n", jsonUrl, e.getMessage());
        return null;
      }
    }).collect(Collectors.toList());
    System.out.printf(Locale.ROOT, "Got valid JSONs from urls%s\n", jsonUrls);
    for (JsonSchemaInferrer inferrer : testInferrers) {
      final ObjectNode schemaJson = inferrer.inferForSamples(sampleJsons);
      assertNotNull(schemaJson, format("Inferred schema for urls%s is null", jsonUrls));
      final Schema schema;
      try {
        schema = SchemaLoader.load(new JSONObject(schemaJson.toString()));
      } catch (RuntimeException e) {
        fail(format("Unable to parse the inferred schema for urls%s", jsonUrls), e);
        throw e;
      }
      for (JsonNode sampleJson : sampleJsons) {
        try {
          if (sampleJson.isObject()) {
            schema.validate(new JSONObject(sampleJson.toString()));
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
          fail(format("Inferred schema for urls%s failed to validate", jsonUrls), e);
        }
      }
    }
  }

  private static Iterable<String> getSampleJsonUrls() {
    final List<String> urls =
        getQuicktypeSampleJsonUrls().collect(Collectors.toCollection(ArrayList::new));
    Collections.shuffle(urls, ThreadLocalRandom.current());
    System.out.println("Running tests for all samples");
    return urls;
  }

  private static Stream<String> getQuicktypeSampleJsonUrls() {
    final Stream<String> misc = Stream
        .of("00c36.json", "00ec5.json", "010b1.json", "016af.json", "033b1.json", "050b0.json",
            "06bee.json", "07540.json", "0779f.json", "07c75.json", "09f54.json", "0a358.json",
            "0a91a.json", "0b91a.json", "0cffa.json", "0e0c2.json", "0fecf.json", "10be4.json",
            "112b5.json", "127a1.json", "13d8d.json", "14d38.json", "167d6.json", "16bc5.json",
            "176f1.json", "1a7f5.json", "1b28c.json", "1b409.json", "2465e.json", "24f52.json",
            "262f0.json", "26b49.json", "26c9c.json", "27332.json", "29f47.json", "2d4e2.json",
            "2df80.json", "31189.json", "32431.json", "32d5c.json", "337ed.json", "33d2e.json",
            "34702.json", "3536b.json", "3659d.json", "36d5d.json", "3a6b3.json", "3e9a3.json",
            "3f1ce.json", "421d4.json", "437e7.json", "43970.json", "43eaf.json", "458db.json",
            "4961a.json", "4a0d7.json", "4a455.json", "4c547.json", "4d6fb.json", "4e336.json",
            "54147.json", "54d32.json", "570ec.json", "5dd0d.json", "5eae5.json", "5eb20.json",
            "5f3a1.json", "5f7fe.json", "617e8.json", "61b66.json", "6260a.json", "65dec.json",
            "66121.json", "6617c.json", "67c03.json", "68c30.json", "6c155.json", "6de06.json",
            "6dec6.json", "6eb00.json", "70c77.json", "734ad.json", "75912.json", "7681c.json",
            "76ae1.json", "77392.json", "7d397.json", "7d722.json", "7df41.json", "7dfa6.json",
            "7eb30.json", "7f568.json", "7fbfb.json", "80aff.json", "82509.json", "8592b.json",
            "88130.json", "8a62c.json", "908db.json", "9617f.json", "96f7c.json", "9847b.json",
            "9929c.json", "996bd.json", "9a503.json", "9ac3b.json", "9eed5.json", "a0496.json",
            "a1eca.json", "a3d8c.json", "a45b0.json", "a71df.json", "a9691.json", "ab0d1.json",
            "abb4b.json", "ac944.json", "ad8be.json", "ae7f0.json", "ae9ca.json", "af2d1.json",
            "b4865.json", "b6f2c.json", "b6fe5.json", "b9f64.json", "bb1ec.json", "be234.json",
            "c0356.json", "c0a3a.json", "c3303.json", "c6cfd.json", "c8c7e.json", "cb0cc.json",
            "cb81e.json", "ccd18.json", "cd238.json", "cd463.json", "cda6c.json", "cf0d8.json",
            "cfbce.json", "d0908.json", "d23d5.json", "dbfb3.json", "dc44f.json", "dd1ce.json",
            "dec3a.json", "df957.json", "e0ac7.json", "e2915.json", "e2a58.json", "e324e.json",
            "e53b5.json", "e64a0.json", "e8a0b.json", "e8b04.json", "ed095.json", "f22f5.json",
            "f3139.json", "f3edf.json", "f466a.json", "f6a65.json", "f74d5.json", "f82d9.json",
            "f974d.json", "faff5.json", "fcca3.json", "fd329.json")
        .map("/test/inputs/json/misc/"::concat);
    final Stream<String> priority = Stream.of("blns-object.json", "bug427.json", "bug790.json",
        "bug855-short.json", "bug863.json", "coin-pairs.json", "combinations1.json",
        "combinations2.json", "combinations3.json", "combinations4.json", "combined-enum.json",
        "direct-recursive.json", "empty-enum.json", "identifiers.json", "keywords.json",
        "list.json", "name-style.json", "nbl-stats.json", "no-classes.json", "nst-test-suite.json",
        "number-map.json", "optional-union.json", "recursive.json", "simple-identifiers.json",
        "union-constructor-clash.json", "unions.json", "url.json")
        .map("/test/inputs/json/priority/"::concat);
    final Stream<String> samples = Stream
        .of("bitcoin-block.json", "getting-started.json", "github-events.json", "kitchen-sink.json",
            "pokedex.json", "reddit.json", "simple-object.json", "spotify-album.json",
            "us-avg-temperatures.json", "us-senators.json")
        .map("/test/inputs/json/samples/"::concat);
    return Stream.of(misc, priority, samples).flatMap(Function.identity())
        .map(QUICKTYPE_REPO_BASE_URL::concat);
  }

  private static Collection<JsonSchemaInferrer> getTestInferrers() {
    final List<JsonSchemaInferrer> inferrers = new ArrayList<>();
    final Iterator<DefaultPolicy> defaultPolicyIter =
        Iterators.cycle(DefaultPolicies.useFirstSamples(), DefaultPolicies.useLastSamples());
    final Iterator<RequiredPolicy> requiredPolicyIter =
        Iterators.cycle(RequiredPolicies.commonFields(), RequiredPolicies.nonNullCommonFields());
    final Iterator<AdditionalPropertiesPolicy> additionalPropPolicyIter = Iterators.cycle(
        AdditionalPropertiesPolicies.existingTypes(), AdditionalPropertiesPolicies.notAllowed());
    final List<SpecVersion> specVersions = Arrays.asList(SpecVersion.values());
    Collections.shuffle(specVersions, ThreadLocalRandom.current());
    for (SpecVersion specVersion : specVersions) {
      for (boolean extraFeatures : Arrays.asList(true, false)) {
        final JsonSchemaInferrerBuilder builder =
            JsonSchemaInferrer.newBuilder().setSpecVersion(specVersion);
        if (extraFeatures) {
          if (specVersion != SpecVersion.DRAFT_07) {
            /*
             * Skip tests for inferring format with draft-07 due to a disagreement between Java time
             * and the schema library on what a valid time string is.
             */
            builder.setFormatInferrer(FormatInferrers.chained(FormatInferrers.dateTime(),
                FormatInferrers.email(), FormatInferrers.ip()));
          }
          builder.enable(ArrayLengthFeature.values()).enable(ObjectSizeFeature.values())
              .enable(StringLengthFeature.values()).enable(NumberRangeFeature.values())
              .setExamplesPolicy(ExamplesPolicies.useFirstSamples(10))
              .setDefaultPolicy(defaultPolicyIter.next())
              .setTitleGenerator(TitleGenerators.useFieldNames())
              .setDescriptionGenerator(DescriptionGeneratorInput::getFieldName)
              .setAdditionalPropertiesPolicy(additionalPropPolicyIter.next())
              .setRequiredPolicy(requiredPolicyIter.next())
              .setMultipleOfPolicy(MultipleOfPolicies.gcd());
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
    request.setConfig(RequestConfig.custom().setConnectTimeout(1, TimeUnit.SECONDS)
        .setConnectionRequestTimeout(1, TimeUnit.SECONDS).setResponseTimeout(5, TimeUnit.SECONDS)
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

}
