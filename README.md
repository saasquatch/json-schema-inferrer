# json-schema-inferrer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/saasquatch/json-schema-inferrer.svg?branch=master)](https://travis-ci.org/saasquatch/json-schema-inferrer)
[![codecov](https://codecov.io/gh/saasquatch/json-schema-inferrer/branch/master/graph/badge.svg)](https://codecov.io/gh/saasquatch/json-schema-inferrer)
[ ![Download](https://api.bintray.com/packages/saasquatch/java-libs/json-schema-inferrer/images/download.svg) ](https://bintray.com/saasquatch/java-libs/json-schema-inferrer/_latestVersion)

Java library for inferring JSON schema based on sample JSONs.

## Sample usage

```java
import java.util.Arrays;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.saasquatch.jsonschemainferrer.AdditionalPropertiesPolicies;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.RequiredPolicies;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import com.saasquatch.jsonschemainferrer.TitleGenerators;

public class Example {

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final JsonSchemaInferrer inferrer =
      JsonSchemaInferrer.newBuilder()
          .setSpecVersion(SpecVersion.DRAFT_06)
          .setAdditionalPropertiesPolicy(AdditionalPropertiesPolicies.notAllowed())
          .setRequiredPolicy(RequiredPolicies.nonNullCommonFields())
          .setTitleGenerator(TitleGenerators.useFieldNames())
          .build();

  public static void main(String[] args) throws Exception {
    final JsonNode sample1 = mapper.readTree(
        "{\"one\":\"https://saasquatch.com\",\"two\":[-1.5,2,\"hello@saasquat.ch\",false],\"three\":3}");
    final JsonNode sample2 = mapper.readTree(
        "{\"one\":1,\"two\":{\"four\":true,\"five\":[2,\"2\"],\"six\":null},\"three\":null}");
    final ObjectNode resultForSample1 = inferrer.inferForSample(sample1);
    final ObjectNode resultForSample1And2 =
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
  "one": "https://saasquatch.com",
  "two": [-1.5, 2, "hello@saasquat.ch", false],
  "three": 3
}
```

`sample2` is:

```json
{
  "one": 1,
  "two": { "four": true, "five": [2, "2"], "six": null },
  "three": null
}
```

`resultForSample1` is:

```json
{
  "$schema": "http://json-schema.org/draft-06/schema#",
  "type": "object",
  "properties": {
    "one": { "title": "one", "type": "string" },
    "two": {
      "title": "two",
      "type": "array",
      "items": { "type": ["number", "boolean", "string"] }
    },
    "three": { "title": "three", "type": "integer" }
  },
  "additionalProperties": false,
  "required": ["one", "two", "three"]
}
```

And `resultForSample1And2` is:

```json
{
  "$schema": "http://json-schema.org/draft-06/schema#",
  "type": "object",
  "properties": {
    "one": { "title": "one", "type": ["string", "integer"] },
    "two": {
      "title": "two",
      "anyOf": [
        {
          "type": "object",
          "properties": {
            "six": { "title": "six", "type": "null" },
            "four": { "title": "four", "type": "boolean" },
            "five": {
              "title": "five",
              "type": "array",
              "items": { "type": ["string", "integer"] }
            }
          },
          "additionalProperties": false,
          "required": ["four", "five"]
        },
        {
          "type": "array",
          "items": { "type": ["number", "boolean", "string"] }
        }
      ]
    },
    "three": { "title": "three", "type": ["null", "integer"] }
  },
  "additionalProperties": false,
  "required": ["one", "two"]
}
```

For more examples, see package [`com.saasquatch.jsonschemainferrer.examples`](https://github.com/saasquatch/json-schema-inferrer/tree/master/src/test/java/com/saasquatch/jsonschemainferrer/examples).

## Adding it to your project

### Add the repository

Maven

```xml
<repositories>
  <repository>
    <id>bintray-saasquatch-java-libs</id>
    <url>https://dl.bintray.com/saasquatch/java-libs</url>
  </repository>
</repositories>
```

Gradle

```gradle
repositories {
  maven {
    url  "https://dl.bintray.com/saasquatch/java-libs"
  }
}
```

### Add the dependency

Maven

```xml
<dependency>
  <groupId>com.saasquatch</groupId>
  <artifactId>json-schema-inferrer</artifactId>
  <version>0.1.1-alpha-28</version>
</dependency>
```

Gradle

```gradle
compile 'com.saasquatch:json-schema-inferrer:0.1.1-alpha-28'
```

### Transitive Dependencies

The only required transitive dependencies are [Jackson](https://github.com/FasterXML/jackson) and [FindBugs (JSR305)](http://findbugs.sourceforge.net/). If you opt into using some of the built-in [`FormatInferrers`](https://github.com/saasquatch/json-schema-inferrer/blob/master/src/main/java/com/saasquatch/jsonschemainferrer/FormatInferrers.java), [Commons Validator](https://commons.apache.org/proper/commons-validator/) will also be needed.

## Development

Compile with `mvn compile`. Run tests with `mvn test`. Deploy with `mvn deploy`.

## LICENSE

```
Copyright 2019 ReferralSaaSquatch.com Inc.

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
