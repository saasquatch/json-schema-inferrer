package com.saasquatch.jsonschemainferrer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

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
    return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(elements)));
  }

  /**
   * Create an unmodifiable {@link Set} possibly backed by a defensive copy of the given
   * {@link EnumSet}.
   */
  static <E extends Enum<E>> Set<E> unmodifiableEnumSet(@Nonnull EnumSet<E> enumSet) {
    return enumSet.isEmpty() ? Collections.emptySet()
        : Collections.unmodifiableSet(EnumSet.copyOf(enumSet));
  }

  static <E extends Enum<E>, R> R unrecognizedEnumError(E enumVal) {
    throw new IllegalStateException(
        format("Unrecognized %s[%s] encountered", enumVal.getClass().getSimpleName(), enumVal));
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
  static ValueNode numberNode(@Nonnull BigInteger gcd) {
    // Attempt to fit the result into a int or long
    try {
      return JsonNodeFactory.instance.numberNode(gcd.intValueExact());
    } catch (ArithmeticException e) {
      // Ignore
    }
    try {
      return JsonNodeFactory.instance.numberNode(gcd.longValueExact());
    } catch (ArithmeticException e) {
      // Ignore
    }
    return JsonNodeFactory.instance.numberNode(gcd);
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
        .collect(Collectors.toSet());
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

  @Nonnull
  static Set<String> getCommonFieldNames(@Nonnull Iterable<? extends JsonNode> samples,
      boolean requireNonNull) {
    Set<String> commonFieldNames = null;
    for (JsonNode sample : samples) {
      final Set<String> fieldNames = stream(sample.fieldNames())
          .filter(requireNonNull
              ? fieldName -> !sample.path(fieldName).isNull()
              : fieldName -> true)
          .collect(Collectors.toSet());
      if (commonFieldNames == null) {
        commonFieldNames = new HashSet<>(fieldNames);
      } else {
        commonFieldNames.retainAll(fieldNames);
      }
    }
    return commonFieldNames == null ? Collections.emptySet()
        : Collections.unmodifiableSet(commonFieldNames);
  }

  /**
   * Get the length of the Base64 String for the given number of bytes
   */
  static int getBase64Length(@Nonnull int bytesLength) {
    return (bytesLength + 2) / 3 * 4;
  }

  /**
   * Get the serialized text length, or -1 if the input cannot be serialized as text.
   */
  static int getSerializedTextLength(@Nonnull JsonNode jsonNode) {
    if (jsonNode instanceof BinaryNode) {
      final byte[] binaryValue = ((BinaryNode) jsonNode).binaryValue();
      if (binaryValue == null) {
        return -1;
      }
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

  static boolean allNumbersAreIntegers(@Nonnull Iterable<? extends JsonNode> jsonNodes,
      @Nonnull IntegerTypeCriterion integerTypeCriterion) {
    return stream(jsonNodes).filter(JsonNode::isNumber)
        .allMatch(j -> integerTypeCriterion.isInteger(j));
  }

  /**
   * @return Whether the input is a floating point node that is to be serialized as text, i.e. NaN
   *         and infinity
   */
  static boolean isTextualFloat(@Nonnull JsonNode jsonNode) {
    if (jsonNode.isFloat()) {
      final float floatValue = jsonNode.floatValue();
      if (Float.isNaN(floatValue) || Float.isInfinite(floatValue)) {
        return true;
      }
    } else if (jsonNode.isDouble()) {
      final double doubleValue = jsonNode.doubleValue();
      if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
        return true;
      }
    }
    return false;
  }

  static boolean isMathematicalInteger(float val) {
    return !Double.isNaN(val) && !Double.isInfinite(val) && val == Math.rint(val);
  }

  static boolean isMathematicalInteger(double val) {
    return !Double.isNaN(val) && !Double.isInfinite(val) && val == Math.rint(val);
  }

  static boolean isMathematicalInteger(BigDecimal val) {
    return val.stripTrailingZeros().scale() <= 0;
  }

}
