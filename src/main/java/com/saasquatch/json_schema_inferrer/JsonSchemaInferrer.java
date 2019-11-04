package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.combineArrays;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.format;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.toArrayNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.saasquatch.json_schema_inferrer.JunkDrawer.Consts;

/**
 * Infer JSON schema based on a sample JSON
 *
 * @author sli
 * @see #newBuilder()
 * @see #infer(JsonNode)
 */
@Immutable
public final class JsonSchemaInferrer {

  private final SpecVersion specVersion;
  private final boolean includeMetaSchemaUrl;
  private final boolean usePropertyNamesAsTitles;
  private final FormatInferrer formatInferrer;

  private JsonSchemaInferrer(@Nonnull SpecVersion specVersion, boolean includeMetaSchemaUrl,
      boolean usePropertyNamesAsTitles, @Nonnull FormatInferrer formatInferrer) {
    this.specVersion = specVersion;
    this.includeMetaSchemaUrl = includeMetaSchemaUrl;
    this.usePropertyNamesAsTitles = usePropertyNamesAsTitles;
    this.formatInferrer = formatInferrer;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Infer the JSON schema from a sample JSON.
   *
   * @param input the sample JSON
   * @return the inferred JSON schema
   */
  @Nonnull
  public ObjectNode infer(@Nullable JsonNode input) {
    return inferMulti(Collections.singleton(input));
  }

  /**
   * Infer the JSON schema from multiple sample JSONs.
   *
   * @param input the sample JSONs
   * @return the inferred JSON schema
   */
  @Nonnull
  public ObjectNode inferMulti(@Nonnull Collection<JsonNode> inputs) {
    if (inputs.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    // Using LinkedList on purpose here since we do a lot of add and remove
    final Collection<ObjectNode> anyOfs = new LinkedList<>();
    final Set<ObjectNode> objectNodes = new HashSet<>();
    final Set<ArrayNode> arrayNodes = new HashSet<>();
    for (JsonNode input : inputs) {
      if (input instanceof ObjectNode) {
        objectNodes.add((ObjectNode) input);
      } else if (input instanceof ArrayNode) {
        arrayNodes.add((ArrayNode) input);
      } else {
        // input is null or a ValueNode
        addAnyOf(anyOfs, processPrimitive((ValueNode) input));
      }
    }
    if (!objectNodes.isEmpty()) {
      addAnyOf(anyOfs, processObjects(objectNodes));
    }
    if (!arrayNodes.isEmpty()) {
      addAnyOf(anyOfs, processArray(combineArrays(arrayNodes)));
    }
    final ObjectNode result = newObject();
    if (includeMetaSchemaUrl) {
      result.put(Consts.Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    }
    processAnyOfs(anyOfs);
    switch (anyOfs.size()) {
      case 0:
        throw new AssertionError();
      case 1: {
        result.setAll(anyOfs.iterator().next());
        break;
      }
      default: {
        result.set(Consts.Fields.ANY_OF, newArray().addAll(anyOfs));
        break;
      }
    }
    return result;
  }

  @Nonnull
  private static String inferType(@Nullable JsonNode value) {
    if (value == null) {
      return Consts.Types.NULL;
    }
    final JsonNodeType type = value.getNodeType();
    switch (type) {
      case ARRAY:
        return Consts.Types.ARRAY;
      case BINARY:
        return Consts.Types.STRING;
      case BOOLEAN:
        return Consts.Types.BOOLEAN;
      case MISSING:
        return Consts.Types.NULL;
      case NULL:
        return Consts.Types.NULL;
      case NUMBER:
        return value.isIntegralNumber() ? Consts.Types.INTEGER : Consts.Types.NUMBER;
      case OBJECT:
        return Consts.Types.OBJECT;
      case POJO:
        throw new IllegalArgumentException(POJONode.class.getSimpleName() + " not supported");
      case STRING:
        return Consts.Types.STRING;
      default:
        break;
    }
    throw new IllegalArgumentException(
        format("Unrecognized %s: %s", type.getClass().getSimpleName(), type));
  }

  @Nullable
  private String inferFormat(@Nullable JsonNode value) {
    final JsonNode valueNodeToUse = value == null ? NullNode.getInstance() : value;
    return formatInferrer.infer(new FormatInferrerInput() {
      @Override
      public JsonNode getJsonNode() {
        return valueNodeToUse;
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }
    });
  }

  @Nonnull
  private ObjectNode processPrimitive(@Nullable ValueNode valueNode) {
    final ObjectNode result = newObject();
    result.put(Consts.Fields.TYPE, inferType(valueNode));
    final String format = inferFormat(valueNode);
    if (format != null) {
      result.put(Consts.Fields.FORMAT, format);
    }
    return result;
  }

  @Nonnull
  private ObjectNode processObjects(@Nonnull Collection<ObjectNode> allObjectNodes) {
    if (allObjectNodes.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    // All the field names across all samples combined
    final Set<String> allFieldNames = allObjectNodes.stream()
        .flatMap(j -> stream(j.fieldNames()))
        .collect(Collectors.toSet());
    final ObjectNode properties = newObject();
    for (String key : allFieldNames) {
      final Collection<ObjectNode> anyOfs = new LinkedList<>();
      // Get the vals from samples that have the key. vals cannot be empty.
      final Set<JsonNode> vals = allObjectNodes.stream()
          .map(j -> j.path(key))
          .filter(j -> !j.isMissingNode())
          .collect(Collectors.toSet());
      final Set<ObjectNode> objectNodes = new HashSet<>();
      final Set<ArrayNode> arrayNodes = new HashSet<>();
      for (JsonNode val : vals) {
        if (val instanceof ObjectNode) {
          objectNodes.add((ObjectNode) val);
        } else if (val instanceof ArrayNode) {
          arrayNodes.add((ArrayNode) val);
        } else {
          addAnyOf(anyOfs, processPrimitive((ValueNode) val));
        }
      }
      if (!objectNodes.isEmpty()) {
        addAnyOf(anyOfs, processObjects(objectNodes));
      }
      if (!arrayNodes.isEmpty()) {
        addAnyOf(anyOfs, processArray(combineArrays(arrayNodes)));
      }
      processAnyOfs(anyOfs);
      switch (anyOfs.size()) {
        case 0:
          // anyOfs cannot be empty here
          throw new AssertionError();
        case 1: {
          final ObjectNode newProp = newObject();
          if (usePropertyNamesAsTitles) {
            newProp.put(Consts.Fields.TITLE, key);
          }
          newProp.setAll(anyOfs.iterator().next());
          properties.set(key, newProp);
          break;
        }
        default: {
          final ObjectNode newProp = newObject();
          if (usePropertyNamesAsTitles) {
            newProp.put(Consts.Fields.TITLE, key);
          }
          newProp.set(Consts.Fields.ANY_OF, newArray().addAll(anyOfs));
          properties.set(key, newProp);
          break;
        }
      }
    }
    final ObjectNode result = newObject().put(Consts.Fields.TYPE, Consts.Types.OBJECT);
    if (properties.size() > 0) {
      result.set(Consts.Fields.PROPERTIES, properties);
    }
    return result;
  }

  @Nonnull
  private ObjectNode processArray(@Nonnull ArrayNode arrayNode) {
    final Collection<ObjectNode> anyOfs = new LinkedList<>();
    final Set<ObjectNode> objectNodes = new HashSet<>();
    final Set<ArrayNode> arrayNodes = new HashSet<>();
    for (JsonNode val : arrayNode) {
      if (val instanceof ObjectNode) {
        objectNodes.add((ObjectNode) val);
      } else if (val instanceof ArrayNode) {
        arrayNodes.add((ArrayNode) val);
      } else {
        addAnyOf(anyOfs, processPrimitive((ValueNode) val));
      }
    }
    if (!objectNodes.isEmpty()) {
      addAnyOf(anyOfs, processObjects(objectNodes));
    }
    if (!arrayNodes.isEmpty()) {
      final ArrayNode arrayToProcess = newArray();
      arrayNodes.forEach(arrayToProcess::addAll);
      addAnyOf(anyOfs, processArray(arrayToProcess));
    }
    processAnyOfs(anyOfs);
    final ObjectNode items;
    switch (anyOfs.size()) {
      case 0:
        items = newObject();
        break;
      case 1:
        items = anyOfs.iterator().next();
        break;
      default: {
        items = newObject();
        items.set(Consts.Fields.ANY_OF, newArray().addAll(anyOfs));
        break;
      }
    }
    final ObjectNode result = newObject().put(Consts.Fields.TYPE, Consts.Types.ARRAY);
    result.set(Consts.Fields.ITEMS, items);
    return result;
  }

  private void addAnyOf(@Nonnull Collection<ObjectNode> anyOfs, @Nonnull ObjectNode newAnyOf) {
    if (anyOfs.isEmpty()) {
      anyOfs.add(newAnyOf);
      return;
    }
    final Iterator<ObjectNode> anyOfIter = anyOfs.iterator();
    anyOfsLoop: while (anyOfIter.hasNext()) {
      final ObjectNode anyOf = anyOfIter.next();
      final JsonNode diffs = JsonDiff.asJson(anyOf, newAnyOf);
      for (JsonNode diff : diffs) {
        final String path = diff.path(Consts.Diff.PATH).textValue();
        if (path != null && path.endsWith('/' + Consts.Fields.FORMAT)) {
          if (newAnyOf.at(path.substring(0, path.lastIndexOf('/'))).path(Consts.Fields.TYPE)
              .isTextual()) {
            // If any of the diffs is caused by a format change, we'll want to add it
            break anyOfsLoop;
          }
        }
      }
      final Set<String> ops = stream(diffs)
          .map(j -> j.path(Consts.Diff.OP).textValue())
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      if (ops.equals(Consts.Diff.SINGLETON_ADD)) {
        /*
         * The new anyOf is a superset of one of the existing anyOfs. Discard the existing one and
         * add the new one.
         */
        anyOfIter.remove();
        break;
      } else if (ops.isEmpty() || ops.equals(Consts.Diff.SINGLETON_REMOVE)) {
        // The new anyOf is the same or a subset of one of the existing anyOfs. Do nothing.
        return;
      }
    }
    anyOfs.add(newAnyOf);
  }

  private void processAnyOfs(@Nonnull Collection<ObjectNode> anyOfs) {
    // Combine all the "simple" anyOfs, i.e. anyOfs that only have the "type" field
    final Set<String> simpleTypes = new HashSet<>();
    final Collection<ObjectNode> simpleAnyOfs = new ArrayList<>();
    for (ObjectNode anyOf : anyOfs) {
      final Set<String> fieldNames = stream(anyOf.fieldNames()).collect(Collectors.toSet());
      if (fieldNames.equals(Collections.singleton(Consts.Fields.TYPE))) {
        simpleAnyOfs.add(anyOf);
        simpleTypes.add(anyOf.path(Consts.Fields.TYPE).textValue());
      }
    }
    if (simpleAnyOfs.size() <= 1) {
      return;
    }
    // Combine all the simple types into an array
    anyOfs.removeAll(simpleAnyOfs);
    final ObjectNode combinedSimpleAnyOf = newObject();
    combinedSimpleAnyOf.set(Consts.Fields.TYPE, toArrayNode(simpleTypes));
    anyOfs.add(combinedSimpleAnyOf);
  }

  public static final class Builder {

    private SpecVersion specVersion = SpecVersion.DRAFT_04;
    private boolean includeMetaSchemaUrl = true;
    private boolean usePropertyNamesAsTitles = false;
    private FormatInferrer formatInferrer = DefaultFormatInferrer.INSTANCE;

    private Builder() {}

    /**
     * Set the specification version. The default is draft-04.
     */
    public Builder withSpecVersion(@Nonnull SpecVersion specVersion) {
      this.specVersion = Objects.requireNonNull(specVersion);
      return this;
    }

    /**
     * Set whether {@code $schema} should be included in the output. It is true by default.
     */
    public Builder includeMetaSchemaUrl(boolean includeMetaSchemaUrl) {
      this.includeMetaSchemaUrl = includeMetaSchemaUrl;
      return this;
    }

    /**
     * Set whether the {@code title} fields should be filled in with the property names. It is false
     * by default.
     */
    public Builder usePropertyNamesAsTitles(boolean usePropertyNamesAsTitles) {
      this.usePropertyNamesAsTitles = usePropertyNamesAsTitles;
      return this;
    }

    /**
     * Set the {@link FormatInferrer} for inferring the <a href=
     * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a>
     * of strings. By default it uses {@link DefaultFormatInferrer}, which implements a subset of
     * standard formats. To use custom formats, provide your own implementation. To disable string
     * format inference, use {@link FormatInferrer#noOp()}.<br>
     * Note that if your JSON samples have large nested arrays, it's recommended to set this to
     * false to prevent confusing outputs.
     *
     * @see FormatInferrer
     */
    public Builder withFormatInferrer(@Nonnull FormatInferrer formatInferrer) {
      this.formatInferrer = Objects.requireNonNull(formatInferrer);
      return this;
    }

    /**
     * @return the {@link JsonSchemaInferrer} built
     * @throws IllegalArgumentException if the spec version and features don't match up
     */
    public JsonSchemaInferrer build() {
      return new JsonSchemaInferrer(specVersion, includeMetaSchemaUrl, usePropertyNamesAsTitles,
          formatInferrer);
    }

  }

  private static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  private static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

}
