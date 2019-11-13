package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.unmodifiableEnumSet;
import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Builder for {@link JsonSchemaInferrer}.
 *
 * @author sli
 * @see JsonSchemaInferrer#newBuilder()
 */
public final class JsonSchemaInferrerBuilder {

  private SpecVersion specVersion = SpecVersion.DRAFT_04;
  private int examplesLimit = 0;
  private IntegerTypePreference integerTypePreference = IntegerTypePreference.IF_ALL;
  private SimpleUnionTypePreference simpleUnionTypePreference =
      SimpleUnionTypePreference.TYPE_AS_ARRAY;
  private AdditionalPropertiesPolicy additionalPropertiesPolicy =
      AdditionalPropertiesPolicies.noOp();
  private RequiredPolicy requiredPolicy = RequiredPolicies.noOp();
  private DefaultPolicy defaultPolicy = DefaultPolicies.noOp();
  private FormatInferrer formatInferrer = FormatInferrers.noOp();
  private TitleGenerator titleGenerator = TitleGenerators.noOp();
  private final EnumSet<ObjectSizeFeature> objectSizeFeatures =
      EnumSet.noneOf(ObjectSizeFeature.class);
  private final EnumSet<ArrayLengthFeature> arrayLengthFeatures =
      EnumSet.noneOf(ArrayLengthFeature.class);
  private final EnumSet<StringLengthFeature> stringLengthFeatures =
      EnumSet.noneOf(StringLengthFeature.class);

  JsonSchemaInferrerBuilder() {}

  /**
   * Set the specification version. The default is draft-04.
   */
  public JsonSchemaInferrerBuilder setSpecVersion(@Nonnull SpecVersion specVersion) {
    this.specVersion = Objects.requireNonNull(specVersion);
    return this;
  }

  /**
   * Set the max size for {@code examples}. 0 to disable {@code examples}. By default it is 0.
   *
   * @throws IllegalArgumentException if the input is negative
   */
  public JsonSchemaInferrerBuilder setExamplesLimit(@Nonnegative int examplesLimit) {
    if (examplesLimit < 0) {
      throw new IllegalArgumentException("Invalid examplesLimit");
    }
    this.examplesLimit = examplesLimit;
    return this;
  }

  /**
   * Set the {@link IntegerTypePreference}. The default is {@link IntegerTypePreference#IF_ALL}.
   */
  public JsonSchemaInferrerBuilder setIntegerTypePreference(
      @Nonnull IntegerTypePreference integerTypePreference) {
    this.integerTypePreference = Objects.requireNonNull(integerTypePreference);
    return this;
  }

  /**
   * Set the {@link SimpleUnionTypePreference}. The default is
   * {@link SimpleUnionTypePreference#TYPE_AS_ARRAY}.
   */
  public JsonSchemaInferrerBuilder setSimpleUnionTypePreference(
      @Nonnull SimpleUnionTypePreference simpleUnionTypePreference) {
    this.simpleUnionTypePreference = Objects.requireNonNull(simpleUnionTypePreference);
    return this;
  }

  /**
   * Set the {@link AdditionalPropertiesPolicy}. By default it is
   * {@link AdditionalPropertiesPolicies#noOp()}.
   *
   * @see AdditionalPropertiesPolicy
   * @see AdditionalPropertiesPolicies
   */
  public JsonSchemaInferrerBuilder setAdditionalPropertiesPolicy(
      @Nonnull AdditionalPropertiesPolicy additionalPropertiesPolicy) {
    this.additionalPropertiesPolicy = Objects.requireNonNull(additionalPropertiesPolicy);
    return this;
  }

  /**
   * Set the {@link RequiredPolicy}. By default it is {@link RequiredPolicies#noOp()}.
   *
   * @see RequiredPolicy
   * @see RequiredPolicies
   */
  public JsonSchemaInferrerBuilder setRequiredPolicy(@Nonnull RequiredPolicy requiredPolicy) {
    this.requiredPolicy = Objects.requireNonNull(requiredPolicy);
    return this;
  }

  /**
   * Set the {@link DefaultPolicy}. By default it is {@link DefaultPolicies#noOp()}.
   *
   * @see DefaultPolicy
   * @see DefaultPolicies
   */
  public JsonSchemaInferrerBuilder setDefaultPolicy(@Nonnull DefaultPolicy defaultPolicy) {
    this.defaultPolicy = Objects.requireNonNull(defaultPolicy);
    return this;
  }

  /**
   * Set the {@link FormatInferrer} for inferring the <a href=
   * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a> of
   * strings. By default it uses {@link FormatInferrers#noOp()}. An example of a possible custom
   * implementation is available at {@link FormatInferrers#dateTime()}, which you can potentially
   * use or use it combined with your own implementations with
   * {@link FormatInferrers#chained(FormatInferrer...)}.<br>
   * Note that if your JSON samples have large nested arrays, it's recommended to set this to false
   * to prevent confusing outputs.
   *
   * @see FormatInferrer
   * @see FormatInferrers
   */
  public JsonSchemaInferrerBuilder setFormatInferrer(@Nonnull FormatInferrer formatInferrer) {
    this.formatInferrer = Objects.requireNonNull(formatInferrer);
    return this;
  }

  /**
   * Set the {@link TitleGenerator} for this inferrer. By default it is
   * {@link TitleGenerators#noOp()}. You can provide your custom implementations and transform the
   * field names however you see fit.
   *
   * @see TitleGenerator
   * @see TitleGenerators
   */
  public JsonSchemaInferrerBuilder setTitleGenerator(@Nonnull TitleGenerator titleGenerator) {
    this.titleGenerator = Objects.requireNonNull(titleGenerator);
    return this;
  }

  /**
   * Enable {@link ObjectSizeFeature}s
   */
  public JsonSchemaInferrerBuilder enable(@Nonnull ObjectSizeFeature... features) {
    for (ObjectSizeFeature feature : features) {
      this.objectSizeFeatures.add(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * Disable {@link ObjectSizeFeature}s.
   */
  public JsonSchemaInferrerBuilder disable(@Nonnull ObjectSizeFeature... features) {
    for (ObjectSizeFeature feature : features) {
      this.objectSizeFeatures.remove(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * Enable {@link ArrayLengthFeature}s
   */
  public JsonSchemaInferrerBuilder enable(@Nonnull ArrayLengthFeature... features) {
    for (ArrayLengthFeature feature : features) {
      this.arrayLengthFeatures.add(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * Disable {@link ArrayLengthFeature}s
   */
  public JsonSchemaInferrerBuilder disable(@Nonnull ArrayLengthFeature... features) {
    for (ArrayLengthFeature feature : features) {
      this.arrayLengthFeatures.remove(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * Enable {@link StringLengthFeature}s
   */
  public JsonSchemaInferrerBuilder enable(@Nonnull StringLengthFeature... features) {
    for (StringLengthFeature feature : features) {
      this.stringLengthFeatures.add(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * Disable {@link StringLengthFeature}s.
   */
  public JsonSchemaInferrerBuilder disable(@Nonnull StringLengthFeature... features) {
    for (StringLengthFeature feature : features) {
      this.stringLengthFeatures.remove(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * @return the {@link JsonSchemaInferrer} built
   * @throws IllegalArgumentException if the spec version and features don't match up
   */
  public JsonSchemaInferrer build() {
    if (specVersion.compareTo(SpecVersion.DRAFT_06) < 0 && examplesLimit > 0) {
      throw new IllegalArgumentException(
          "examples not supported with " + specVersion.getMetaSchemaIdentifier());
    }
    return new JsonSchemaInferrer(specVersion, examplesLimit, integerTypePreference,
        simpleUnionTypePreference, additionalPropertiesPolicy, requiredPolicy, defaultPolicy,
        formatInferrer, titleGenerator, unmodifiableEnumSet(objectSizeFeatures),
        unmodifiableEnumSet(arrayLengthFeatures), unmodifiableEnumSet(stringLengthFeatures));
  }

}
