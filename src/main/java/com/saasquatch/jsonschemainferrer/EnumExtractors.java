package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;

public final class EnumExtractors {

  private EnumExtractors() {}

  public static EnumExtractor noOp() {
    return input -> Collections.emptySet();
  }

  public static <E extends Enum<E>> EnumExtractor validEnum(@Nonnull Class<E> enumClass) {
    Objects.requireNonNull(enumClass);
    return input -> {
      final Set<? extends JsonNode> enumNodes = input.getSamples().stream()
          .filter(j -> isValidEnum(enumClass, j.textValue()))
          .collect(Collectors.toSet());
      return enumNodes.isEmpty() ? Collections.emptySet() : Collections.singleton(enumNodes);
    };
  }

  public static EnumExtractor chained(@Nonnull EnumExtractor... enumExtrators) {
    for (EnumExtractor enumExtrator : enumExtrators) {
      Objects.requireNonNull(enumExtrator);
    }
    switch (enumExtrators.length) {
      case 0:
        throw new IllegalArgumentException("Empty criteria");
      case 1:
        return enumExtrators[0];
      default:
        break;
    }
    // Defensive copy
    return input -> {
      return Arrays.stream(enumExtrators)
          .flatMap(enumExtrator -> enumExtrator.extractEnums(input).stream())
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toSet());
    };
  }

}
