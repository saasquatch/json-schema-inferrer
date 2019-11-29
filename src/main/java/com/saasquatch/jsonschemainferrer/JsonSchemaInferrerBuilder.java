package com.saasquatch.jsonschemainferrer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  private IntegerTypeCriterion integerTypeCriterion = IntegerTypeCriteria.nonFloatingPoint();
  @Nullable
  private Collection<FormatInferrer> formatInferrers;
  private TitleDescriptionGenerator titleDescriptionGenerator = TitleDescriptionGenerators.noOp();
  private AdditionalPropertiesPolicy additionalPropertiesPolicy =
      AdditionalPropertiesPolicies.noOp();
  private RequiredPolicy requiredPolicy = RequiredPolicies.noOp();
  private DefaultPolicy defaultPolicy = DefaultPolicies.noOp();
  private ExamplesPolicy examplesPolicy = ExamplesPolicies.noOp();
  private MultipleOfPolicy multipleOfPolicy = MultipleOfPolicies.noOp();
  private Set<ObjectSizeFeature> objectSizeFeatures = Collections.emptySet();
  private Set<ArrayLengthFeature> arrayLengthFeatures = Collections.emptySet();
  private Set<StringLengthFeature> stringLengthFeatures = Collections.emptySet();
  private Set<NumberRangeFeature> numberRangeFeatures = Collections.emptySet();
  @Nullable
  private Collection<GenericSchemaFeature> additionalSchemaFeatures;

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
   * Set the {@link IntegerTypeCriterionL}. The default is
   * {@link IntegerTypeCriteria#nonFloatingPoint()}.
   */
  public JsonSchemaInferrerBuilder setIntegerTypeCriterion(
      @Nonnull IntegerTypeCriterion integerTypeCriterion) {
    this.integerTypeCriterion = Objects.requireNonNull(integerTypeCriterion);
    return this;
  }

  /**
   * Add a {@link FormatInferrer} for inferring the <a href=
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
  public JsonSchemaInferrerBuilder addFormatInferrers(@Nonnull FormatInferrer... formatInferrers) {
    if (this.formatInferrers == null) {
      this.formatInferrers = new ArrayList<>();
    }
    for (FormatInferrer formatInferrer : formatInferrers) {
      this.formatInferrers.add(Objects.requireNonNull(formatInferrer));
    }
    return this;
  }

  /**
   * Set the {@link TitleDescriptionGenerator} for this inferrer. By default it is
   * {@link TitleDescriptionGenerators#noOp()}. You can provide your custom implementations and
   * transform the field names however you see fit.
   *
   * @see TitleDescriptionGenerator
   * @see TitleDescriptionGenerators
   */
  public JsonSchemaInferrerBuilder setTitleDescriptionGenerator(
      @Nonnull TitleDescriptionGenerator titleDescriptionGenerator) {
    this.titleDescriptionGenerator = Objects.requireNonNull(titleDescriptionGenerator);
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
   * Set the {@link MultipleOfPolicy}. By default it is {@link MultipleOfPolicies#noOp()}.
   *
   * @see MultipleOfPolicy
   * @see MultipleOfPolicies
   */
  public JsonSchemaInferrerBuilder setMultipleOfPolicy(@Nonnull MultipleOfPolicy multipleOfPolicy) {
    this.multipleOfPolicy = Objects.requireNonNull(multipleOfPolicy);
    return this;
  }

  /**
   * Set the {@link ObjectSizeFeature}s enabled
   */
  public JsonSchemaInferrerBuilder setObjectSizeFeatures(
      @Nonnull EnumSet<ObjectSizeFeature> objectSizeFeatures) {
    this.objectSizeFeatures = Objects.requireNonNull(objectSizeFeatures);
    return this;
  }

  /**
   * Set the {@link ArrayLengthFeature}s enabled
   */
  public JsonSchemaInferrerBuilder setArrayLengthFeatures(
      @Nonnull EnumSet<ArrayLengthFeature> arrayLengthFeatures) {
    this.arrayLengthFeatures = Objects.requireNonNull(arrayLengthFeatures);
    return this;
  }

  /**
   * Set the {@link StringLengthFeature}s enabled
   */
  public JsonSchemaInferrerBuilder setStringLengthFeatures(
      @Nonnull EnumSet<StringLengthFeature> stringLengthFeatures) {
    this.stringLengthFeatures = Objects.requireNonNull(stringLengthFeatures);
    return this;
  }

  /**
   * Set the {@link NumberRangeFeature}s enabled
   */
  public JsonSchemaInferrerBuilder setNumberRangeFeatures(
      @Nonnull EnumSet<NumberRangeFeature> numberRangeFeatures) {
    this.numberRangeFeatures = Objects.requireNonNull(numberRangeFeatures);
    return this;
  }

  public JsonSchemaInferrerBuilder addAdditionalSchemaFeatures(
      @Nonnull GenericSchemaFeature... features) {
    if (this.additionalSchemaFeatures == null) {
      this.additionalSchemaFeatures = new ArrayList<>();
    }
    for (GenericSchemaFeature feature : features) {
      this.additionalSchemaFeatures.add(Objects.requireNonNull(feature));
    }
    return this;
  }

  @Nonnull
  private FormatInferrer getCombinedFormatInferrer() {
    if (formatInferrers == null) {
      return FormatInferrers.noOp();
    }
    return FormatInferrers.chained(formatInferrers.toArray(new FormatInferrer[0]));
  }

  @Nonnull
  private GenericSchemaFeature getCombinedGenericSchemaFeature() {
    final List<GenericSchemaFeature> features = new ArrayList<>();
    if (additionalPropertiesPolicy != AdditionalPropertiesPolicies.noOp()) {
      features.add(additionalPropertiesPolicy);
    }
    if (requiredPolicy != RequiredPolicies.noOp()) {
      features.add(requiredPolicy);
    }
    if (defaultPolicy != DefaultPolicies.noOp()) {
      features.add(defaultPolicy);
    }
    if (examplesPolicy != ExamplesPolicies.noOp()) {
      features.add(examplesPolicy);
    }
    if (multipleOfPolicy != MultipleOfPolicies.noOp()) {
      features.add(multipleOfPolicy);
    }
    features.addAll(objectSizeFeatures);
    features.addAll(arrayLengthFeatures);
    features.addAll(stringLengthFeatures);
    features.addAll(numberRangeFeatures);
    if (additionalSchemaFeatures != null) {
      features.addAll(additionalSchemaFeatures);
    }
    final GenericSchemaFeature[] featuresArray = features.toArray(new GenericSchemaFeature[0]);
    return GenericSchemaFeatures.chained(featuresArray);
  }

  /**
   * @return the {@link JsonSchemaInferrer} built
   * @throws IllegalArgumentException if the spec version and features don't match up
   */
  public JsonSchemaInferrer build() {
    return new JsonSchemaInferrer(specVersion, integerTypePreference, integerTypeCriterion,
        getCombinedFormatInferrer(), titleDescriptionGenerator, getCombinedGenericSchemaFeature());
  }

}
