package com.saasquatch.jsonschemainferrer;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

  @Nullable
  static String getSerializedTextValue(@Nonnull JsonNode jsonNode) {
    if (jsonNode.isBinary()) {
      // Jackson serializes BinaryNode as Base64
      return jsonNode.asText();
    } else {
      return jsonNode.textValue();
    }
  }

  static boolean allNumbersAreIntegers(@Nonnull Iterable<? extends JsonNode> jsonNodes) {
    return stream(jsonNodes).filter(JsonNode::isNumber).allMatch(JsonNode::isIntegralNumber);
  }

}
