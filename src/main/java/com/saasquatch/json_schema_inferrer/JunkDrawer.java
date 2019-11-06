package com.saasquatch.json_schema_inferrer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Exactly what it sounds like. NOT PUBLIC!!!
 *
 * @author sli
 */
interface JunkDrawer {

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

  static ObjectNode newObject() {
    return JsonNodeFactory.instance.objectNode();
  }

  static ArrayNode newArray() {
    return JsonNodeFactory.instance.arrayNode();
  }

  /**
   * Combine multiple {@link ArrayNode} with their unique elements
   */
  static ArrayNode combineArraysDistinct(@Nonnull Collection<ArrayNode> arrays) {
    final ArrayNode result = newArray();
    arrays.stream().flatMap(j -> stream(j)).distinct().forEach(result::add);
    return result;
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
  static Set<JsonNode> getAllValuesForFieldName(@Nonnull Iterable<? extends JsonNode> objectNodes,
      @Nonnull String fieldName) {
    return stream(objectNodes)
        .map(j -> j.path(fieldName))
        .filter(j -> !j.isMissingNode())
        .collect(Collectors.toSet());
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

}
