package com.saasquatch.json_schema_inferrer;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for inferring the <a href=
 * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a> of
 * strings
 *
 * @author sli
 * @see #noOp()
 * @see #chained(StringFormatInferrer...)
 */
@FunctionalInterface
public interface StringFormatInferrer {

  @Nullable
  String infer(@Nonnull StringFormatInferrerInput input);

  public static StringFormatInferrer noOp() {
    return NoOpStringFormatInferrer.INSTANCE;
  }

  /**
   * Convenience method for {@link #chained(List)}
   */
  public static StringFormatInferrer chained(StringFormatInferrer... inferrers) {
    // Not passed in directly to the constructor to make a defensive copy
    return chained(Arrays.asList(inferrers));
  }

  /**
   * Create a chained {@link StringFormatInferrer} with multiple inferrers. The result
   * {@link StringFormatInferrer} will attempt to use the inferrers in the original order and will
   * return the first available result.
   */
  public static StringFormatInferrer chained(@Nonnull List<StringFormatInferrer> inferrers) {
    switch (inferrers.size()) {
      case 0:
        return noOp();
      case 1:
        return inferrers.get(0);
      default:
        break;
    }
    return new ChainedStringFormatInferrer(inferrers.toArray(new StringFormatInferrer[0]));
  }

}
