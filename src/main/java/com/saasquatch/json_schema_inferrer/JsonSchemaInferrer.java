package com.saasquatch.json_schema_inferrer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
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
  private final boolean inferFormat;

  private JsonSchemaInferrer(@Nonnull SpecVersion specVersion, boolean includeMetaSchemaUrl,
      boolean inferFormat) {
    this.specVersion = specVersion;
    this.includeMetaSchemaUrl = includeMetaSchemaUrl;
    this.inferFormat = inferFormat;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * @param input the sample JSON
   * @return the inferred JSON schema
   */
  public ObjectNode infer(@Nullable JsonNode input) {
    final ObjectNode result = newObject();
    if (includeMetaSchemaUrl) {
      result.put(Fields.DOLLAR_SCHEMA, specVersion.metaSchemaUrl);
    }
    if (input instanceof ObjectNode) {
      result.setAll(processObject((ObjectNode) input));
    } else if (input instanceof ArrayNode) {
      result.setAll(processArray((ArrayNode) input));
    } else {
      // input is null or a ValueNode
      result.setAll(processPrimitive((ValueNode) input));
    }
    return result;
  }

  @Nullable
  private String inferFormat(@Nullable JsonNode value) {
    if (!inferFormat || value == null) {
      return null;
    }
    if (value.textValue() != null) {
      final String textValue = value.textValue();
      try {
        ZonedDateTime.parse(textValue);
        return "date-time";
      } catch (Exception e) {
        // Ignore
      }
      if (specVersion.sameOrNewerThan(SpecVersion.DRAFT_07)) {
        try {
          LocalTime.parse(textValue);
          return "time";
        } catch (Exception e) {
          // Ignore
        }
        try {
          LocalDate.parse(textValue);
          return "date";
        } catch (Exception e) {
          // Ignore
        }
      }
      if (EmailValidator.getInstance().isValid(textValue)) {
        return "email";
      }
      if (InetAddressValidator.getInstance().isValidInet4Address(textValue)) {
        return "ipv4";
      }
      if (InetAddressValidator.getInstance().isValidInet6Address(textValue)) {
        return "ipv6";
      }
      if (UrlValidator.getInstance().isValid(textValue)) {
        return "uri";
      }
    }
    return null;
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

  private ObjectNode processPrimitive(@Nullable ValueNode valueNode) {
    final ObjectNode result = newObject();
    result.put(Fields.TYPE, inferType(valueNode));
    final String format = inferFormat(valueNode);
    if (format != null) {
      result.put(Fields.FORMAT, format);
    }
    return result;
  }

  private ObjectNode processObject(@Nonnull ObjectNode objectNode) {
    final ObjectNode properties = newObject();
    final Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
    while (fields.hasNext()) {
      final Map.Entry<String, JsonNode> field = fields.next();
      final String key = field.getKey();
      final JsonNode val = field.getValue();
      if (val instanceof ObjectNode) {
        properties.set(key, processObject((ObjectNode) val));
      } else if (val instanceof ArrayNode) {
        properties.set(key, processArray((ArrayNode) val));
      } else {
        properties.set(key, processPrimitive((ValueNode) val));
      }
    }
    final ObjectNode result = newObject().put(Fields.TYPE, Types.OBJECT);
    if (properties.size() > 0) {
      result.set(Fields.PROPERTIES, properties);
    }
    return result;
  }

  private ObjectNode processArray(@Nonnull ArrayNode arrayNode) {
    final ObjectNode items;
    final Set<ObjectNode> anyOfs = new HashSet<>();
    for (JsonNode val : arrayNode) {
      if (val instanceof ObjectNode) {
        addAnyOf(anyOfs, processObject((ObjectNode) val));
      } else if (val instanceof ArrayNode) {
        addAnyOf(anyOfs, processArray((ArrayNode) val));
      } else {
        addAnyOf(anyOfs, processPrimitive((ValueNode) val));
      }
    }
    processAnyOfs(anyOfs);
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

  private void addAnyOf(Set<ObjectNode> anyOfs, ObjectNode newAnyOf) {
    if (anyOfs.isEmpty()) {
      anyOfs.add(newAnyOf);
      return;
    }
    final Iterator<ObjectNode> anyOfIter = anyOfs.iterator();
    while (anyOfIter.hasNext()) {
      final ObjectNode anyOf = anyOfIter.next();
      final JsonNode diff = JsonDiff.asJson(anyOf, newAnyOf);
      final Set<String> ops = StreamSupport.stream(diff.spliterator(), false)
          .map(j -> j.path("op").textValue())
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      if (ops.equals(Collections.singleton("add"))) {
        /*
         * The new anyOf is a superset of one of the existing anyOfs. Discard the existing one and
         * add the new one.
         */
        anyOfIter.remove();
        break;
      } else if (ops.isEmpty() || ops.equals(Collections.singleton("remove"))) {
        // The new anyOf is the same or a subset of one of the existing anyOfs. Do nothing.
        return;
      }
    }
    anyOfs.add(newAnyOf);
  }

  private void processAnyOfs(Set<ObjectNode> anyOfs) {
    final Set<String> types = anyOfs.stream()
        .map(j -> j.path(Fields.TYPE).textValue())
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (types.contains(Types.INTEGER) && types.contains(Types.NUMBER)) {
      anyOfs.removeIf(j -> Types.INTEGER.equals(j.path(Fields.TYPE).textValue()));
    }
  }

  public static final class Builder {

    private SpecVersion specVersion = SpecVersion.DRAFT_04;
    private boolean includeMetaSchemaUrl = true;
    private boolean inferFormat = true;

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
     * Set whether we should infer the {@code format} of the input, i.e. email, ipv4, ipv6, etc. It
     * is true by default.
     */
    public Builder inferFormat(boolean inferFormat) {
      this.inferFormat = inferFormat;
      return this;
    }

    /**
     * @return the {@link JsonSchemaInferrer} built
     * @throws IllegalArgumentException if the spec version and features don't match up
     */
    public JsonSchemaInferrer build() {
      return new JsonSchemaInferrer(specVersion, includeMetaSchemaUrl, inferFormat);
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

  private static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  private static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

}
