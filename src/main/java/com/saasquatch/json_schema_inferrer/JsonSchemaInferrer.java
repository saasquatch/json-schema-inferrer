package com.saasquatch.json_schema_inferrer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

/**
 * Infer JSON schema based on a sample JSON
 *
 * @author sli
 * @see #newBuilder()
 * @see #infer(JsonNode)
 */
public final class JsonSchemaInferrer {

  @Nullable
  private final String title;
  @Nonnull
  private final Draft draft;
  private final boolean outputDollarSchema;

  JsonSchemaInferrer(String title, Draft draft, boolean outputDollarSchema) {
    this.title = title;
    this.draft = draft;
    this.outputDollarSchema = outputDollarSchema;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * @param input the sample JSON
   * @return the inferred JSON schema
   */
  public ObjectNode infer(@Nullable JsonNode input) {
    if (input == null) {
      input = JsonNodeFactory.instance.nullNode();
    }
    final ObjectNode result = newObject();
    if (draft != null && outputDollarSchema) {
      result.put(Fields.SCHEMA, draft.url);
    }
    if (title != null) {
      result.put(Fields.TITLE, title);
    }
    if (input instanceof ObjectNode) {
      result.setAll(processObject((ObjectNode) input));
    } else if (input instanceof ArrayNode) {
      result.setAll(processArray((ArrayNode) input));
    } else {
      result.setAll(processPrimitive((ValueNode) input));
    }
    return result;
  }

  @Nullable
  private String getPropertyFormat(@Nonnull JsonNode value) {
    if (value.textValue() != null) {
      final String textValue = value.textValue();
      try {
        Instant.parse(textValue);
        return "date-time";
      } catch (Exception e) {
        // Ignore
      }
      if (draft.sameOrNewerThan(Draft.V7)) {
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
  private static String getPropertyType(@Nonnull JsonNode value) {
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
      case NUMBER: {
        if (value.isIntegralNumber()) {
          return Types.INTEGER;
        }
        return Types.NUMBER;
      }
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

  private ObjectNode processPrimitive(ValueNode valueNode) {
    final ObjectNode result = newObject();
    result.put(Fields.TYPE, getPropertyType(valueNode));
    final String format = getPropertyFormat(valueNode);
    if (format != null) {
      result.put(Fields.FORMAT, format);
    }
    return result;
  }

  private ObjectNode processObject(ObjectNode objectNode) {
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

  private ObjectNode processArray(ArrayNode arrayNode) {
    final ObjectNode items;
    final Set<ObjectNode> oneOfs = new HashSet<>();
    for (JsonNode val : arrayNode) {
      if (val instanceof ObjectNode) {
        oneOfs.add(processObject((ObjectNode) val));
      } else if (val instanceof ArrayNode) {
        oneOfs.add(processArray((ArrayNode) val));
      } else {
        oneOfs.add(processPrimitive((ValueNode) val));
      }
    }
    processOneOfs(oneOfs);
    switch (oneOfs.size()) {
      case 0:
        items = newObject();
        break;
      case 1:
        items = oneOfs.iterator().next();
        break;
      default: {
        items = newObject();
        final ArrayNode oneOfArray = newArray();
        oneOfs.forEach(oneOfArray::add);
        items.set(Fields.ONE_OF, oneOfArray);
      }
    }
    final ObjectNode result = newObject().put(Fields.TYPE, Types.ARRAY);
    result.set(Fields.ITEMS, items);
    return result;
  }

  private void processOneOfs(Set<ObjectNode> oneOfs) {
    final Set<String> types = oneOfs.stream()
        .map(j -> j.path(Fields.TYPE).textValue())
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (types.contains(Types.INTEGER) && types.contains(Types.NUMBER)) {
      oneOfs.removeIf(j -> Types.INTEGER.equals(j.path(Fields.TYPE).textValue()));
    }
  }

  public static final class Builder {

    private String title;
    private Draft draft = Draft.V4;
    private boolean outputDollarSchema = true;

    private Builder() {}

    public Builder withTitle(@Nullable String title) {
      this.title = title;
      return this;
    }

    public Builder draft04() {
      return withDraft(Draft.V4);
    }

    public Builder draft06() {
      return withDraft(Draft.V6);
    }

    public Builder draft07() {
      return withDraft(Draft.V7);
    }

    private Builder withDraft(@Nonnull Draft draft) {
      this.draft = Objects.requireNonNull(draft);
      return this;
    }

    public Builder outputDollarSchema(boolean outputDollarSchema) {
      this.outputDollarSchema = outputDollarSchema;
      return this;
    }

    public JsonSchemaInferrer build() {
      return new JsonSchemaInferrer(title, draft, outputDollarSchema);
    }

  }

  private static enum Draft {

    V4("http://json-schema.org/draft-04/schema#"),
    V6("http://json-schema.org/draft-06/schema#"),
    V7("http://json-schema.org/draft-07/schema#"),
    ;

    @Nonnull
    private final String url;

    Draft(String url) {
      this.url = url;
    }

    public boolean sameOrNewerThan(Draft other) {
      return this.compareTo(other) >= 0;
    }

  }

  private static interface Fields {
    String TYPE = "type", ITEMS = "items", ONE_OF = "oneOf", /* REQUIRED = "required", */
        PROPERTIES = "properties", FORMAT = "format", SCHEMA = "$schema", TITLE = "title";
  }

  private static interface Types {
    String OBJECT = "object", ARRAY = "array", STRING = "string", BOOLEAN = "boolean",
        INTEGER = "integer", NUMBER = "number", NULL = "null";
  }

//  private static boolean nonNull(@Nullable JsonNode j) {
//    return j != null && !j.isNull() && !j.isMissingNode();
//  }

  private static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  private static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

  // Visible for testing
  static List<String> toStringList(@Nonnull JsonNode arrayNode) {
    final List<String> list = new ArrayList<>();
    for (JsonNode node : arrayNode) {
      list.add(node.textValue());
    }
    return list;
  }

}
