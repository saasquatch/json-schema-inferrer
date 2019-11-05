package com.saasquatch.json_schema_inferrer;

import java.util.Collection;
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

/**
 * Exactly what it sounds like. NOT PUBLIC!!!
 *
 * @author sli
 */
interface JunkDrawer {

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

  static ArrayNode combineArrays(@Nonnull Collection<ArrayNode> arrays) {
    final ArrayNode result = JsonNodeFactory.instance.arrayNode();
    arrays.stream().flatMap(JunkDrawer::stream).distinct().forEach(result::add);
    return result;
  }

  static ArrayNode stringColToArrayNode(@Nonnull Collection<String> strings) {
    final ArrayNode result = JsonNodeFactory.instance.arrayNode();
    strings.forEach(result::add);
    return result;
  }

  static Set<String> getAllFieldNames(@Nonnull Iterable<? extends JsonNode> objectNodes) {
    return stream(objectNodes)
        .flatMap(j -> stream(j.fieldNames()))
        .collect(Collectors.toSet());
  }

  static Set<JsonNode> getAllValuesForFieldName(@Nonnull Iterable<? extends JsonNode> objectNodes,
      @Nonnull String fieldName) {
    return stream(objectNodes)
        .map(j -> j.path(fieldName))
        .filter(j -> !j.isMissingNode())
        .collect(Collectors.toSet());
  }

}
