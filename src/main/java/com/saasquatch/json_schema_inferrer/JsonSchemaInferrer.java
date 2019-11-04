package com.saasquatch.json_schema_inferrer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
  private final FormatInferrer formatInferrer;

  private JsonSchemaInferrer(@Nonnull SpecVersion specVersion, boolean includeMetaSchemaUrl,
      @Nonnull FormatInferrer formatInferrer) {
    this.specVersion = specVersion;
    this.includeMetaSchemaUrl = includeMetaSchemaUrl;
    this.formatInferrer = formatInferrer;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * @param input the sample JSON
   * @return the inferred JSON schema
   */
  @Nonnull
  public ObjectNode infer(@Nullable JsonNode input) {
    final ObjectNode result = newObject();
    if (includeMetaSchemaUrl) {
      result.put(Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    }
    if (input instanceof ObjectNode) {
      result.setAll(processObjects(Collections.singleton((ObjectNode) input)));
    } else if (input instanceof ArrayNode) {
      result.setAll(processArray((ArrayNode) input));
    } else {
      // input is null or a ValueNode
      result.setAll(processPrimitive((ValueNode) input));
    }
    return result;
  }

  @Nonnull
  private static String inferType(@Nullable JsonNode value) {
    if (value == null) {
      return Types.NULL;
    }
    final JsonNodeType type = value.getNodeType();
    switch (type) {
      case ARRAY:
        return Types.ARRAY;
      case BINARY:
        return Types.STRING;
      case BOOLEAN:
        return Types.BOOLEAN;
      case MISSING:
        return Types.NULL;
      case NULL:
        return Types.NULL;
      case NUMBER:
        return value.isIntegralNumber() ? Types.INTEGER : Types.NUMBER;
      case OBJECT:
        return Types.OBJECT;
      case POJO:
        throw new IllegalArgumentException(POJONode.class.getSimpleName() + " not supported");
      case STRING:
        return Types.STRING;
      default:
        break;
    }
    throw new IllegalArgumentException(
        String.format(Locale.ROOT, "Unrecognized %s: %s", type.getClass().getSimpleName(), type));
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
    result.put(Fields.TYPE, inferType(valueNode));
    final String format = inferFormat(valueNode);
    if (format != null) {
      result.put(Fields.FORMAT, format);
    }
    return result;
  }

  @Nonnull
  private ObjectNode processObjects(@Nonnull Collection<ObjectNode> allObjectNodes) {
    if (allObjectNodes.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    final Set<String> allFieldNames = new HashSet<>();
    for (ObjectNode objectNode : allObjectNodes) {
      objectNode.fieldNames().forEachRemaining(allFieldNames::add);
    }
    final ObjectNode properties = newObject();
    for (String key : allFieldNames) {
      final Collection<ObjectNode> anyOfs = new LinkedList<>();
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
        final ArrayNode arrayToProcess = newArray();
        arrayNodes.forEach(arrayToProcess::addAll);
        addAnyOf(anyOfs, processArray(arrayToProcess));
      }
      switch (anyOfs.size()) {
        case 0:
          // anyOfs cannot be empty here
          throw new AssertionError();
        case 1: {
          properties.set(key, anyOfs.iterator().next());
          break;
        }
        default: {
          final ObjectNode newProp = newObject();
          newProp.set(Fields.ANY_OF, newArray().addAll(anyOfs));
          properties.set(key, newProp);
          break;
        }
      }
    }
    final ObjectNode result = newObject().put(Fields.TYPE, Types.OBJECT);
    if (properties.size() > 0) {
      result.set(Fields.PROPERTIES, properties);
    }
    return result;
  }

  @Nonnull
  private ObjectNode processArray(@Nonnull ArrayNode arrayNode) {
    // Using LinkedList on purpose here since we do a lot of add and remove
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
        items.set(Fields.ANY_OF, newArray().addAll(anyOfs));
      }
    }
    final ObjectNode result = newObject().put(Fields.TYPE, Types.ARRAY);
    result.set(Fields.ITEMS, items);
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
        final String path = diff.path(DiffConsts.PATH).textValue();
        if (path != null && path.endsWith('/' + Fields.FORMAT)) {
          if (newAnyOf.at(path.substring(0, path.lastIndexOf('/'))).path(Fields.TYPE).isTextual()) {
            // If any of the diffs is caused by a format change, we'll want to add it
            break anyOfsLoop;
          }
        }
      }
      final Set<String> ops = StreamSupport.stream(diffs.spliterator(), false)
          .map(j -> j.path(DiffConsts.OP).textValue())
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      if (ops.equals(DiffConsts.SINGLETON_ADD)) {
        /*
         * The new anyOf is a superset of one of the existing anyOfs. Discard the existing one and
         * add the new one.
         */
        anyOfIter.remove();
        break;
      } else if (ops.isEmpty() || ops.equals(DiffConsts.SINGLETON_REMOVE)) {
        // The new anyOf is the same or a subset of one of the existing anyOfs. Do nothing.
        return;
      }
    }
    anyOfs.add(newAnyOf);
  }

  public static final class Builder {

    private SpecVersion specVersion = SpecVersion.DRAFT_04;
    private boolean includeMetaSchemaUrl = true;
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
      return new JsonSchemaInferrer(specVersion, includeMetaSchemaUrl, formatInferrer);
    }

  }

  private static interface Fields {
    String TYPE = "type", ITEMS = "items", ANY_OF = "anyOf", PROPERTIES = "properties",
        FORMAT = "format", DOLLAR_SCHEMA = "$schema";
  }

  private static interface Types {
    String OBJECT = "object", ARRAY = "array", STRING = "string", BOOLEAN = "boolean",
        INTEGER = "integer", NUMBER = "number", NULL = "null";
  }

  private static interface DiffConsts {
    String PATH = "path", OP = "op", ADD = "add", REMOVE = "remove";
    Set<String> SINGLETON_ADD = Collections.singleton(ADD),
        SINGLETON_REMOVE = Collections.singleton(REMOVE);
  }

  private static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  private static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

}
