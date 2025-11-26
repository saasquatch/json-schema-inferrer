# json-schema-inferrer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![JavaCI](https://github.com/saasquatch/json-schema-inferrer/actions/workflows/JavaCI.yml/badge.svg?branch=master)](https://github.com/saasquatch/json-schema-inferrer/actions/workflows/JavaCI.yml)
[![](https://jitpack.io/v/saasquatch/json-schema-inferrer.svg)](https://jitpack.io/#saasquatch/json-schema-inferrer)

Java library for inferring JSON schema based on sample JSONs.

## Demo site

[Here is a simple demo site](https://json-schema-inferrer.herokuapp.com/) for this library that showcases some of the things it's capable of.

## Sample usage

```java
import com.saasquatch.jsonschemainferrer.*;
import java.util.Arrays;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class Example {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer = JsonSchemaInferrer.newBuilder()
      .setSpecVersion(SpecVersion.DRAFT_06)
      // Requires commons-validator
      .addFormatInferrers(FormatInferrers.email(), FormatInferrers.ip())
      .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
      .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
      .addEnumExtractors(EnumExtractors.validEnum(java.time.Month.class),
          EnumExtractors.validEnum(java.time.DayOfWeek.class))
      .build();

  public static void main(String[] args) throws Exception {
    final JsonNode sample1 = mapper.readTree(
        "{\"🙈\":\"https://saasquatch.com\",\"🙉\":[-1.5,2,\"hello@saasquat.ch\",false],\"🙊\":3,\"weekdays\":[\"MONDAY\",\"TUESDAY\"]}");
    final JsonNode sample2 = mapper.readTree(
        "{\"🙈\":1,\"🙉\":{\"🐒\":true,\"🐵\":[2,\"1234:5678::\"],\"🍌\":null},\"🙊\":null,\"months\":[\"JANUARY\",\"FEBRUARY\"]}");
    final JsonNode resultForSample1 = inferrer.inferForSample(sample1);
    final JsonNode resultForSample1And2 =
        inferrer.inferForSamples(Arrays.asList(sample1, sample2));
    for (JsonNode j : Arrays.asList(sample1, sample2, resultForSample1, resultForSample1And2)) {
      System.out.println(mapper.writeValueAsString(j));
    }
  }

}
```

In the code above, `sample1` is:

```json
{
  "🙈": "https://saasquatch.com",
  "🙉": [-1.5, 2, "hello@saasquat.ch", false],
  "🙊": 3,
  "weekdays": ["MONDAY", "TUESDAY"]
}
```

`sample2` is:

```json
{
  "🙈": 1,
  "🙉": { "🐒": true, "🐵": [2, "1234:5678::"], "🍌": null },
  "🙊": null,
  "months": ["JANUARY", "FEBRUARY"]
}
```

`resultForSample1` is:

```json
{
  "$schema": "http://json-schema.org/draft-06/schema#",
  "type": "object",
  "properties": {
    "🙈": { "type": "string" },
    "🙊": { "type": "integer" },
    "weekdays": { "type": "array", "items": { "enum": ["MONDAY", "TUESDAY"] } },
    "🙉": {
      "type": "array",
      "items": {
        "anyOf": [
          { "type": ["number", "boolean"] },
          { "type": "string", "format": "email" }
        ]
      }
    }
  },
  "additionalProperties": false,
  "required": ["🙈", "🙊", "weekdays", "🙉"]
}
```

And `resultForSample1And2` is:

```json
{
  "$schema": "http://json-schema.org/draft-06/schema#",
  "type": "object",
  "properties": {
    "🙈": { "type": ["string", "integer"] },
    "months": { "type": "array", "items": { "enum": ["JANUARY", "FEBRUARY"] } },
    "🙊": { "type": ["null", "integer"] },
    "weekdays": { "type": "array", "items": { "enum": ["MONDAY", "TUESDAY"] } },
    "🙉": {
      "anyOf": [
        {
          "type": "object",
          "properties": {
            "🐵": {
              "type": "array",
              "items": {
                "anyOf": [
                  { "type": "integer" },
                  { "type": "string", "format": "ipv6" }
                ]
              }
            },
            "🍌": { "type": "null" },
            "🐒": { "type": "boolean" }
          },
          "additionalProperties": false,
          "required": ["🐵", "🐒"]
        },
        {
          "type": "array",
          "items": {
            "anyOf": [
              { "type": ["number", "boolean"] },
              { "type": "string", "format": "email" }
            ]
          }
        }
      ]
    }
  },
  "additionalProperties": false,
  "required": ["🙈", "🙉"]
}
```

For more examples, see package [`com.saasquatch.jsonschemainferrer.examples`](https://github.com/saasquatch/json-schema-inferrer/tree/master/src/test/java/com/saasquatch/jsonschemainferrer/examples).

## Adding it to your project

### Add the repository

Maven

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Gradle

```gradle
repositories {
  maven { url 'https://jitpack.io' }
}
```

### Add the dependency

Maven

```xml
<dependency>
  <groupId>com.github.saasquatch</groupId>
  <artifactId>json-schema-inferrer</artifactId>
  <version>0.2.1</version>
</dependency>
```

Gradle

```gradle
implementation 'com.github.saasquatch:json-schema-inferrer:0.2.1'
```

### Transitive Dependencies

This project requires Java 8. The only required transitive dependencies are [Jackson](https://github.com/FasterXML/jackson) and [FindBugs (JSR305)](http://findbugs.sourceforge.net/). If you opt into using some of the built-in [`FormatInferrers`](https://github.com/saasquatch/json-schema-inferrer/blob/master/src/main/java/com/saasquatch/jsonschemainferrer/FormatInferrers.java), [Commons Validator](https://commons.apache.org/proper/commons-validator/) will also be needed. This is encapsulated with a [Gradle feature variant](https://docs.gradle.org/current/userguide/feature_variants.html) named `builtInFormatInferrerSupport`.

### Pre-release Versions

Pre-release versions and snapshots (as well as stable releases) can be obtained through [JitPack](https://jitpack.io/#saasquatch/json-schema-inferrer).

## License

Unless explicitly stated otherwise all files in this repository are licensed under the Apache License 2.0.

License boilerplate:

```
Copyright 2023 ReferralSaaSquatch.com, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
