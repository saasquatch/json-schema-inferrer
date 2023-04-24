package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exactly what it sounds like. NOT PUBLIC!!!
 *
 * @author sli
 */
final class JunkDrawer {

  private JunkDrawer() {}

  /**
   * String.format with {@link Locale#ROOT}
   */
  static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }

  static <E> Stream<E> stream(@Nonnull Iterator<E> iter) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, 0), false);
  }

  static <E> Stream<E> stream(@Nonnull Iterable<E> iter) {
    if (iter instanceof Collection) {
      return ((Collection<E>) iter).stream();
    }
    return StreamSupport.stream(iter.spliterator(), false);
  }

  /**
   * Convenience method for creating an immutable {@link Map.Entry}.
   */
  static <K, V> Map.Entry<K, V> entryOf(K k, V v) {
    return new AbstractMap.SimpleImmutableEntry<>(k, v);
  }

  /**
   * Create an unmodifiable {@link Set} with the given elements. Note that this method is meant for
   * creating constants. If you want to create constant {@link Set}s of size 0 or 1, you should be
   * using {@link Collections#emptySet()} or {@link Collections#singleton(Object)} instead.
   */
  @SafeVarargs
  static <E> Set<E> unmodifiableSetOf(E... elements) {
    return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(elements)));
  }

  /**
   * Check if the given String is a valid enum name for the given enum class
   */
  static <E extends Enum<E>> boolean isValidEnum(@Nonnull Class<E> enumClass,
      @Nullable String name) {
    if (name == null) {
      return false;
    }
    try {
      Enum.valueOf(enumClass, name);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Check if the given String is a valid enum name for the given enum class ignoring case.
   */
  static <E extends Enum<E>> boolean isValidEnumIgnoreCase(@Nonnull Class<E> enumClass,
      @Nullable String name) {
    if (name == null) {
      return false;
    }
    for (E enumConst : enumClass.getEnumConstants()) {
      if (enumConst.name().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

  static ArrayNode newArray(@Nonnull Collection<? extends JsonNode> elements) {
    return newArray().addAll(elements);
  }

  /**
   * Create a {@link NumericNode} with the given {@link BigInteger} while attempting to fit the
   * input into an int or a long.
   */
  static ValueNode numberNode(@Nonnull BigInteger v) {
    // Attempt to fit the result into an int or long
    try {
      return JsonNodeFactory.instance.numberNode(v.intValueExact());
    } catch (ArithmeticException e) {
      // Ignore
    }
    try {
      return JsonNodeFactory.instance.numberNode(v.longValueExact());
    } catch (ArithmeticException e) {
      // Ignore
    }
    return JsonNodeFactory.instance.numberNode(v);
  }

  /**
   * Build an {@link ArrayNode} with distinct strings
   */
  static ArrayNode stringColToArrayDistinct(@Nonnull Collection<String> strings) {
    final ArrayNode result = newArray();
    if (strings instanceof Set) {
      strings.forEach(result::add);
    } else {
      strings.stream().distinct().forEach(result::add);
    }
    return result;
  }

  /**
   * Get all the unique field names across multiple {@link ObjectNode}s
   */
  static Set<String> getAllFieldNames(@Nonnull Iterable<? extends JsonNode> objectNodes) {
    return stream(objectNodes)
        .flatMap(j -> stream(j.fieldNames()))
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Get all the unique values for a field name across multiple {@link ObjectNode}s
   */
  static Stream<JsonNode> getAllValuesForFieldName(
      @Nonnull Iterable<? extends JsonNode> objectNodes, @Nonnull String fieldName) {
    return stream(objectNodes)
        .map(j -> j.get(fieldName))
        .filter(Objects::nonNull);
  }

  /**
   * @param samples Assumed to be {@link ObjectNode}s
   * @return The field names common to the given samples
   */
  @Nonnull
  static Set<String> getCommonFieldNames(@Nonnull Iterable<? extends JsonNode> samples,
      boolean requireNonNull) {
    Set<String> commonFieldNames = null;
    for (JsonNode sample : samples) {
      final Set<String> fieldNames = stream(sample.fieldNames())
          .filter(Objects::nonNull)
          .filter(requireNonNull
              ? fieldName -> nonNull(sample.get(fieldName))
              : fieldName -> true)
          .collect(Collectors.toCollection(LinkedHashSet::new));
      if (commonFieldNames == null) {
        commonFieldNames = new LinkedHashSet<>(fieldNames);
      } else {
        commonFieldNames.retainAll(fieldNames);
      }
    }
    return commonFieldNames == null || commonFieldNames.isEmpty() ? Collections.emptySet()
        : Collections.unmodifiableSet(commonFieldNames);
  }

  /**
   * Get the length of the Base64 String for the given number of bytes
   */
  static int getBase64Length(int bytesLength) {
    return (bytesLength + 2) / 3 * 4;
  }

  /**
   * Get the serialized text length, or -1 if the input cannot be serialized as text.
   */
  static int getSerializedTextLength(@Nonnull JsonNode jsonNode) {
    if (jsonNode instanceof BinaryNode) {
      final byte[] binaryValue = ((BinaryNode) jsonNode).binaryValue();
      return getBase64Length(binaryValue.length);
    } else if (isTextualFloat(jsonNode)) {
      // Handle NaN and infinity
      return jsonNode.asText().length();
    }
    final String textValue = jsonNode.textValue();
    if (textValue == null) {
      return -1;
    }
    // DO NOT use String.length()
    return textValue.codePointCount(0, textValue.length());
  }

  /**
   * @return Whether the input is a floating point node that is to be serialized as text, i.e. NaN
   *         and infinity
   */
  static boolean isTextualFloat(@Nonnull JsonNode jsonNode) {
    if (jsonNode.isFloat() || jsonNode.isDouble()) {
      final double doubleValue = jsonNode.doubleValue();
      return Double.isNaN(doubleValue) || Double.isInfinite(doubleValue);
    }
    return false;
  }

  /**
   * @return Whether the given number is a whole number
   */
  static boolean isMathematicalInteger(double val) {
    return !Double.isNaN(val) && !Double.isInfinite(val) && val == Math.rint(val);
  }

  /**
   * @return Whether the given number is a whole number
   */
  static boolean isMathematicalInteger(@Nonnull BigDecimal val) {
    return val.compareTo(BigDecimal.ZERO) == 0 || val.stripTrailingZeros().scale() <= 0;
  }

  /**
   * @return Whether the given {@link JsonNode} represents a mathematical integer
   */
  static boolean isMathematicalIntegerNode(@Nonnull JsonNode numberNode) {
    if (!numberNode.isNumber()) {
      return false;
    } else if (numberNode.isIntegralNumber()) {
      return true;
    } else if (numberNode.isFloat() || numberNode.isDouble()) {
      return isMathematicalInteger(numberNode.doubleValue());
    } else {
      return isMathematicalInteger(numberNode.decimalValue());
    }
  }

  /**
   * @return Whether the input {@link JsonNode} is null or is to be serialized as null, like a
   *         {@link TextNode} with a null String.
   */
  static boolean isNull(@Nullable JsonNode j) {
    if (j == null || j.isNull() || j.isMissingNode()) {
      return true;
    } else if (j instanceof TextNode) {
      return j.textValue() == null;
    } else if (j instanceof BinaryNode) {
      return ((BinaryNode) j).binaryValue() == null;
    } else if (j instanceof BigIntegerNode) {
      return j.bigIntegerValue() == null;
    } else if (j instanceof DecimalNode) {
      return j.decimalValue() == null;
    } else if (j instanceof POJONode) {
      return ((POJONode) j).getPojo() == null;
    }
    return false;
  }

  static boolean nonNull(@Nullable JsonNode j) {
    return !isNull(j);
  }

}
