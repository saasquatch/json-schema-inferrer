package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.unmodifiableEnumSet;
import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Builder for {@link JsonSchemaInferrer}. This class is mutable and not thread safe.
 *
 * @author sli
 * @see JsonSchemaInferrer#newBuilder()
 */
@NotThreadSafe
public final class JsonSchemaInferrerBuilder {

  private SpecVersion specVersion = SpecVersion.DRAFT_04;
  private IntegerTypePreference integerTypePreference = IntegerTypePreference.IF_ALL;
  private AdditionalPropertiesPolicy additionalPropertiesPolicy =
      AdditionalPropertiesPolicies.noOp();
  private RequiredPolicy requiredPolicy = RequiredPolicies.noOp();
  private DefaultPolicy defaultPolicy = DefaultPolicies.noOp();
  private ExamplesPolicy examplesPolicy = ExamplesPolicies.noOp();
  private FormatInferrer formatInferrer = FormatInferrers.noOp();
  private TitleGenerator titleGenerator = TitleGenerators.noOp();
  private DescriptionGenerator descriptionGenerator = DescriptionGenerators.noOp();
  private MultipleOfPolicy multipleOfPolicy = MultipleOfPolicies.noOp();
  private final EnumSet<ObjectSizeFeature> objectSizeFeatures =
      EnumSet.noneOf(ObjectSizeFeature.class);
  private final EnumSet<ArrayLengthFeature> arrayLengthFeatures =
      EnumSet.noneOf(ArrayLengthFeature.class);
  private final EnumSet<StringLengthFeature> stringLengthFeatures =
      EnumSet.noneOf(StringLengthFeature.class);
  private final EnumSet<NumberRangeFeature> numberRangeFeatures =
      EnumSet.noneOf(NumberRangeFeature.class);

  JsonSchemaInferrerBuilder() {}

  /**
   * Set the specification version. The default is draft-04.
   */
  public JsonSchemaInferrerBuilder setSpecVersion(@Nonnull SpecVersion specVersion) {
    this.specVersion = Objects.requireNonNull(specVersion);
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
   * Set the {@link ExamplesPolicy}. By default is {@link ExamplesPolicies#noOp()}.
   *
   * @see ExamplesPolicy
   * @see ExamplesPolicies
   */
  public JsonSchemaInferrerBuilder setExamplesPolicy(@Nonnull ExamplesPolicy examplesPolicy) {
    this.examplesPolicy = Objects.requireNonNull(examplesPolicy);
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
   * Set the {@link DescriptionGenerator}. By default it is {@link DescriptionGenerators#noOp()}.
   *
   * @see DescriptionGenerator
   */
  public JsonSchemaInferrerBuilder setDescriptionGenerator(
      @Nonnull DescriptionGenerator descriptionGenerator) {
    this.descriptionGenerator = Objects.requireNonNull(descriptionGenerator);
    return this;
  }

  /**
   * Set the {@link MultipleOfPolicy}. By default it is {@link MultipleOfPolicies#noOp()}.
   *
   * @see MultipleOfPolicy
   */
  public JsonSchemaInferrerBuilder setMultipleOfPolicy(@Nonnull MultipleOfPolicy multipleOfPolicy) {
    this.multipleOfPolicy = Objects.requireNonNull(multipleOfPolicy);
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
   * Enable {@link NumberRangeFeature}s
   */
  public JsonSchemaInferrerBuilder enable(@Nonnull NumberRangeFeature... features) {
    for (NumberRangeFeature feature : features) {
      this.numberRangeFeatures.add(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * Disable {@link NumberRangeFeature}s.
   */
  public JsonSchemaInferrerBuilder disable(@Nonnull NumberRangeFeature... features) {
    for (NumberRangeFeature feature : features) {
      this.numberRangeFeatures.remove(Objects.requireNonNull(feature));
    }
    return this;
  }

  /**
   * @return the {@link JsonSchemaInferrer} built
   * @throws IllegalArgumentException if the spec version and features don't match up
   */
  public JsonSchemaInferrer build() {
    return new JsonSchemaInferrer(specVersion, integerTypePreference, additionalPropertiesPolicy,
        requiredPolicy, defaultPolicy, examplesPolicy, formatInferrer, titleGenerator,
        descriptionGenerator, multipleOfPolicy, unmodifiableEnumSet(objectSizeFeatures),
        unmodifiableEnumSet(arrayLengthFeatures), unmodifiableEnumSet(stringLengthFeatures),
        unmodifiableEnumSet(numberRangeFeatures));
  }

}
