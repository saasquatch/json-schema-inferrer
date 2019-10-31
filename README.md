# json-schema-inferrer

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/saasquatch/json-schema-inferrer.svg?branch=master)](https://travis-ci.org/saasquatch/json-schema-inferrer)

Java library for inferring JSON schema based on a sample JSON.

## Sample usage

```java
final ObjectNode sample = JsonNodeFactory.instance.objectNode()
    .put("one", 1).put("two", "1234:abcd::1234").put("three", "hello@saasquat.ch");
sample.set("four",
    JsonNodeFactory.instance.arrayNode().add(1).add("two")
        .add(JsonNodeFactory.instance.arrayNode()
            .add(JsonNodeFactory.instance.objectNode().put("true", true))
            .add("https://saasquatch.com")));
final ObjectNode inferredSchema =
    JsonSchemaInferrer.newBuilder().withSpecVersion(SpecVersion.DRAFT_06).build().infer(sample);
```

In the code above, the `sample` JSON is:

```json
{
  "one" : 1,
  "two" : "1234:abcd::1234",
  "three" : "hello@saasquat.ch",
  "four" : [ 1, "two", [ {
    "true" : true
  }, "https://saasquatch.com" ] ]
}
```

And the result `inferredSchema` is:

```json
{
  "$schema" : "http://json-schema.org/draft-06/schema#",
  "type" : "object",
  "properties" : {
    "one" : {
      "type" : "integer"
    },
    "two" : {
      "type" : "string",
      "format" : "ipv6"
    },
    "three" : {
      "type" : "string",
      "format" : "email"
    },
    "four" : {
      "type" : "array",
      "items" : {
        "anyOf" : [ {
          "type" : "integer"
        }, {
          "type" : "string"
        }, {
          "type" : "array",
          "items" : {
            "anyOf" : [ {
              "type" : "string",
              "format" : "uri"
            }, {
              "type" : "object",
              "properties" : {
                "true" : {
                  "type" : "boolean"
                }
              }
            } ]
          }
        } ]
      }
    }
  }
}
```

## Adding it to your project

### Add the repository

Maven

```xml
<repositories>
  <repository>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <id>bintray-saasquatch-java-libs</id>
    <name>bintray</name>
    <url>https://dl.bintray.com/saasquatch/java-libs</url>
  </repository>
</repositories>
<pluginRepositories>
  <pluginRepository>
    <snapshots>
      <enabled>false</enabled>
    </snapshots>
    <id>bintray-saasquatch-java-libs</id>
    <name>bintray-plugins</name>
    <url>https://dl.bintray.com/saasquatch/java-libs</url>
  </pluginRepository>
</pluginRepositories>
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
  <version>0.1.1-alpha-1</version>
  <type>pom</type>
</dependency>
```

Gradle

```gradle
compile 'com.saasquatch:json-schema-inferrer:0.1.1-alpha-1'
```

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
