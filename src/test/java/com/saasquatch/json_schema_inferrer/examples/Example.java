package com.saasquatch.json_schema_inferrer.examples;

import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.json_schema_inferrer.JsonSchemaInferrer;
import com.saasquatch.json_schema_inferrer.SpecVersion;

public class Example {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer =
      JsonSchemaInferrer.newBuilder().withSpecVersion(SpecVersion.DRAFT_06).build();

  public static void main(String[] args) throws Exception {
    final JsonNode sample1 = mapper.readTree(
        "{\"one\":1,\"two\":\"https://saasquatch.com\",\"three\":[-1.5,\"127.0.0.1\"]}");
    final JsonNode sample2 = mapper.readTree(
        "{\"one\":\"-1111-11-11T11:11:11.111Z\",\"two\":\"hello@saasquat.ch\",\"three\":[{\"true\":true},[\"1234:abcd::1234\"]]}");
    final ObjectNode resultForSample1 = inferrer.infer(sample1);
    final ObjectNode resultForSample1And2 = inferrer.inferMulti(Arrays.asList(sample1, sample2));
    for (JsonNode j : Arrays.asList(sample1, sample2, resultForSample1, resultForSample1And2)) {
      System.out.println(mapper.writeValueAsString(j));
    }
  }

}
