package com.saasquatch.json_schema_inferrer;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSchemaInferrer {

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

  public ObjectNode infer(JsonNode input) {
    final ObjectNode processOutput;
    final ObjectNode output = newObject();
    if (draft != null && outputDollarSchema) {
      output.put(Fields.SCHEMA, draft.url);
    }
    if (title != null) {
      output.put(Fields.TITLE, title);
    }

    // Process object
    if (input.isObject()) {
      processOutput = processObject(input, null, false);
      output.put(Fields.TYPE, processOutput.path(Fields.TYPE).textValue());
      output.set(Fields.PROPERTIES, processOutput.get(Fields.PROPERTIES));
    } else if (input.isArray()) {
      processOutput = processArray(input, null, false);
      output.put(Fields.TYPE, processOutput.path(Fields.TYPE).textValue());
      output.set(Fields.ITEMS, processOutput.get(Fields.ITEMS));

      if (nonNull(output.get(Fields.TITLE))) {
        final String outputTitle = output.get(Fields.TITLE).textValue();
        ((ObjectNode) output.get(Fields.ITEMS)).put(Fields.TITLE, outputTitle);
        output.put(Fields.TITLE, outputTitle + " Set");
      }
    } else {
      output.put(Fields.TYPE, getPropertyType(input));
      final String format = getPropertyFormat(input);
      if (format != null && !format.isEmpty()) {
        output.put(Fields.FORMAT, format);
      }
    }

    return output;
  }

  @Nullable
  private String getPropertyFormat(JsonNode value) {
    if (value.isTextual()) {
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
      try {
        Objects.requireNonNull(new URI(textValue));
        return "uri";
      } catch (Exception e) {
        // Ignore
      }
    }
    return null;
  }

  @Nonnull
  private static String getPropertyType(JsonNode value) {
    final JsonNodeType type = value.getNodeType();
    switch (type) {
    case ARRAY: return Types.ARRAY;
    case BINARY: return Types.STRING;
    case BOOLEAN: return Types.BOOLEAN;
    case MISSING: return Types.NULL;
    case NULL: return Types.NULL;
    case NUMBER: return Types.NUMBER;
    case OBJECT: return Types.OBJECT;
    case POJO: throw new IllegalArgumentException("POJONode not supported");
    case STRING: return Types.STRING;
    default: break;
    }
    throw new IllegalArgumentException(String.format(Locale.ROOT, "Unrecognized %s: %s",
        type.getClass().getSimpleName(), type));
  }

  private ArrayNode getUniqueKeys(JsonNode aInput, JsonNode bInput, JsonNode cInput) {
    final List<String> a = iteratorToList(aInput.fieldNames());
    final List<String> b = iteratorToList(bInput.fieldNames());
    final List<String> c;
    if (cInput == null) {
      c = new ArrayList<>();
    } else {
      c = toStringList(cInput);
    }

    String value;
    int aIndex, cIndex;

    for (int keyIndex = 0, keyLength = b.size(); keyIndex < keyLength; keyIndex++) {
      value = b.get(keyIndex);
      aIndex = a.indexOf(value);
      cIndex = c.indexOf(value);

      if (aIndex == -1) {
        if (cIndex != -1) {
          // Value is optional, it doesn't exist in A but exists in B(n)
          c.remove(cIndex);
        }
      } else if (cIndex == -1) {
        // Value is required, it exists in both B and A, and is not yet present in C
        c.add(value);
      }
    }
    final ArrayNode resultArray = newArray();
    for (String _c : c) {
      resultArray.add(_c);
    }
    return resultArray;
  }

  private ObjectNode processArray(JsonNode array, ObjectNode output, boolean nested) {
    String format = null;
    boolean oneOf = false;
    String type = null;
    if (nested && nonNull(output)) {
      final ObjectNode output2 = newObject();
      output2.set(Fields.ITEMS, output);
      output = output2;
    } else {
      if (output == null || !output.isObject()) {
        output = newObject();
      }
      output.put(Fields.TYPE, getPropertyType(array));
      if (!output.path(Fields.ITEMS).isObject()) {
        output.set(Fields.ITEMS, newObject());
      }
      type = output.path(Fields.ITEMS).path(Fields.TYPE).textValue();
    }

    // Determine whether each item is different
    for (int arrIndex = 0, arrLength = array.size(); arrIndex < arrLength; arrIndex++) {
      final String elementType = getPropertyType(array.get(arrIndex));
      final String elementFormat = getPropertyFormat(array.get(arrIndex));
      if (type != null && !type.isEmpty() && !elementType.equals(type)) {
        ((ObjectNode) output.get(Fields.ITEMS)).set(Fields.ONE_OF, newArray());
        oneOf = true;
        break;
      } else {
        type = elementType;
        format = elementFormat;
      }
    }

    // Setup type otherwise
    if (!oneOf && (type != null && !type.isEmpty())) {
      ((ObjectNode) output.path(Fields.ITEMS)).put(Fields.TYPE, type);
      if (format != null && !format.isEmpty()) {
        ((ObjectNode) output.path(Fields.ITEMS)).put(Fields.FORMAT, format);
      }
    } else if (oneOf && !Types.OBJECT.equals(type)) {
      final ObjectNode outputItems = newObject();
      outputItems.set(Fields.ONE_OF, arrayNodeOf(newObject().put(Fields.TYPE, type)));
      outputItems.set(Fields.REQUIRED, output.path(Fields.ITEMS).get(Fields.REQUIRED));
      (output).set(Fields.ITEMS, outputItems);
    }

    // Process each item depending
    if (output.path(Fields.ITEMS).get(Fields.ONE_OF) != null || Types.OBJECT.equals(type)) {
      for (int itemIndex = 0, itemLength = array.size(); itemIndex < itemLength; itemIndex++) {
        final JsonNode value = array.get(itemIndex);
        final String itemType = getPropertyType(value);
        final String itemFormat = getPropertyFormat(value);
        ObjectNode arrayItem;
        if (Types.OBJECT.equals(itemType)) {
          if (output.path(Fields.ITEMS).get(Fields.PROPERTIES) != null) {
            ((ObjectNode) output.path(Fields.ITEMS)).set(Fields.REQUIRED,
                getUniqueKeys(output.path(Fields.ITEMS).get(Fields.PROPERTIES), value,
                    output.path(Fields.ITEMS).get(Fields.REQUIRED)));
          }
          arrayItem = processObject(value,
              oneOf ? newObject() : (ObjectNode) output.path(Fields.ITEMS).get(Fields.PROPERTIES),
              true);
        } else if (Types.ARRAY.equals(itemType)) {
          arrayItem = processObject(value,
              oneOf ? newObject() : (ObjectNode) output.path(Fields.ITEMS).get(Fields.PROPERTIES),
              true);
        } else {
          arrayItem = newObject()
              .put(Fields.TYPE, itemType);
          if (itemFormat != null && !itemFormat.isEmpty()) {
            arrayItem.put(Fields.FORMAT, itemFormat);
          }
        }
        if (oneOf) {
          final String childType = getPropertyType(value);
          final ObjectNode tempObj = newObject();
          if (arrayItem.path(Fields.TYPE).textValue() == null
              && Types.OBJECT.equals(childType)) {
            tempObj.set(Fields.PROPERTIES, arrayItem);
            tempObj.put(Fields.TYPE, Types.OBJECT);
            arrayItem = tempObj;
          }
          final ArrayNode itemsOneOf = (ArrayNode) output.path(Fields.ITEMS).get(Fields.ONE_OF);
          if (!contains(itemsOneOf, arrayItem)) {
            itemsOneOf.add(arrayItem);
          }
        } else {
          if (!Types.OBJECT.equals(output.path(Fields.ITEMS).path(Fields.TYPE).textValue())) {
            continue;
          }
          ((ObjectNode) output.get(Fields.ITEMS)).set(Fields.PROPERTIES, arrayItem);
        }
      }
    }
    return nested ? (ObjectNode) output.get(Fields.ITEMS) : output;
  }

  private ObjectNode processObject(JsonNode object, ObjectNode output, boolean nested) {
    if (nested && nonNull(output)) {
      final ObjectNode output2 = newObject();
      output2.set(Fields.PROPERTIES, output);
      output = output2;
    } else {
      if (output == null || !output.isObject()) {
        output = newObject();
      }
      output.put(Fields.TYPE, getPropertyType(object));
      if (!output.path(Fields.PROPERTIES).isObject()) {
        output.set(Fields.PROPERTIES, newObject());
      }
    }

    for (String key : iteratorToList(object.fieldNames())) {
      final JsonNode value = object.get(key);
      String type = getPropertyType(value);
      final String format = getPropertyFormat(value);

      if (type.equals(Types.OBJECT)) {
        ((ObjectNode) output.get(Fields.PROPERTIES)).set(key,
            processObject(value, (ObjectNode) output.path(Fields.PROPERTIES).get(key), false));
        continue;
      }

      if (type.equals(Types.ARRAY)) {
        ((ObjectNode) output.get(Fields.PROPERTIES)).set(key,
            processArray(value, (ObjectNode) output.path(Fields.PROPERTIES).get(key), false));
        continue;
      }

      if (nonNull(output.path(Fields.PROPERTIES).get(key))) {
        final ObjectNode entry = (ObjectNode) output.path(Fields.PROPERTIES).get(key);
        final boolean hasTypeArray = entry.path(Fields.TYPE).isArray();

        // When an array already exists, we check the existing
        // type array to see if it contains our current property
        // type, if not, we add it to the array and continue
        if (hasTypeArray) {
          if (toStringList(entry.get(Fields.TYPE)).contains(type)) {
            ((ArrayNode) entry.get(Fields.TYPE)).add(type);
          }
        }

        // When multiple fields of differing types occur,
        // json schema states that the field must specify the
        // primitive types the field allows in array format.
        if (!hasTypeArray && !type.equals(entry.path(Fields.TYPE).textValue())) {
          final ArrayNode newEntryType = newArray();
          newEntryType.add(entry.get(Fields.TYPE));
          newEntryType.add(type);
          entry.set(Fields.TYPE, newEntryType);
        }

        continue;
      }

      ((ObjectNode) output.get(Fields.PROPERTIES)).set(key, newObject().put(Fields.TYPE, type));

      if (format != null && !format.isEmpty()) {
        ((ObjectNode) output.path(Fields.PROPERTIES).get(key)).put(Fields.FORMAT, format);
      }
    }
    return nested ? (ObjectNode) output.get(Fields.PROPERTIES) : output;
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
    String TYPE = "type", ITEMS = "items", ONE_OF = "oneOf", REQUIRED = "required",
        PROPERTIES = "properties", FORMAT = "format", SCHEMA = "$schema", TITLE = "title";
  }

  private static interface Types {
    String OBJECT = "object", ARRAY = "array", STRING = "string", BOOLEAN = "boolean",
        NUMBER = "number", NULL = "null";
  }

  private static ArrayNode arrayNodeOf(JsonNode... nodes) {
    final ArrayNode result = JsonNodeFactory.instance.arrayNode();
    for (JsonNode node : nodes) {
      result.add(node);
    }
    return result;
  }

  private static boolean nonNull(@Nullable JsonNode j) {
    return j != null && !j.isNull() && !j.isMissingNode();
  }

  private static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  private static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

  private static <T> List<T> iteratorToList(Iterator<T> iterator) {
    final List<T> list = new ArrayList<>();
    while (iterator.hasNext()) {
      list.add(iterator.next());
    }
    return list;
  }

  private static List<String> toStringList(JsonNode arrayNode) {
    final List<String> list = new ArrayList<>();
    for (JsonNode node : arrayNode) {
      list.add(node.textValue());
    }
    return list;
  }

  private static boolean contains(JsonNode arrayNode, JsonNode target) {
    for (JsonNode node : arrayNode) {
      if (node.equals(target)) {
        return true;
      }
    }
    return false;
  }

}
