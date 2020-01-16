package com.saasquatch.jsonschemainferrer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import com.saasquatch.jsonschemainferrer.annotations.Beta;

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
  private List<EnumExtractor> enumExtractors;
  private TitleDescriptionGenerator titleDescriptionGenerator = TitleDescriptionGenerators.noOp();
  @Nullable
  private List<FormatInferrer> formatInferrers;
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
  private List<GenericSchemaFeature> genericSchemaFeatures;

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
   *
   * @see IntegerTypePreference
   */
  public JsonSchemaInferrerBuilder setIntegerTypePreference(
      @Nonnull IntegerTypePreference integerTypePreference) {
    this.integerTypePreference = Objects.requireNonNull(integerTypePreference);
    return this;
  }

  /**
   * Set the {@link IntegerTypeCriterionL}. The default is
   * {@link IntegerTypeCriteria#nonFloatingPoint()}.
   *
   * @see IntegerTypeCriterion
   * @see IntegerTypeCriteria
   */
  public JsonSchemaInferrerBuilder setIntegerTypeCriterion(
      @Nonnull IntegerTypeCriterion integerTypeCriterion) {
    this.integerTypeCriterion = Objects.requireNonNull(integerTypeCriterion);
    return this;
  }

  /**
   * Add {@link EnumExtractor}s.
   *
   * @see EnumExtractor
   * @see EnumExtractors
   */
  public JsonSchemaInferrerBuilder addEnumExtractors(@Nonnull EnumExtractor... enumExtractors) {
    if (this.enumExtractors == null) {
      this.enumExtractors = new ArrayList<>();
    }
    for (EnumExtractor enumExtractor : enumExtractors) {
      this.enumExtractors.add(Objects.requireNonNull(enumExtractor));
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
  @Beta
  public JsonSchemaInferrerBuilder setTitleDescriptionGenerator(
      @Nonnull TitleDescriptionGenerator titleDescriptionGenerator) {
    this.titleDescriptionGenerator = Objects.requireNonNull(titleDescriptionGenerator);
    return this;
  }

  /**
   * Add a {@link FormatInferrer} for inferring the <a href=
   * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a> of
   * strings. By default no {@link FormatInferrer} is enabled. An example of a possible custom
   * implementation is available at {@link FormatInferrers#dateTime()}, which you can potentially
   * use or use it combined with your own implementations.
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

  /**
   * Add custom implementations of {@link GenericSchemaFeature}s.
   */
  public JsonSchemaInferrerBuilder addGenericSchemaFeatures(
      @Nonnull GenericSchemaFeature... features) {
    if (this.genericSchemaFeatures == null) {
      this.genericSchemaFeatures = new ArrayList<>();
    }
    for (GenericSchemaFeature feature : features) {
      this.genericSchemaFeatures.add(Objects.requireNonNull(feature));
    }
    return this;
  }

  @Nonnull
  private EnumExtractor getCombinedEnumExtractor() {
    if (enumExtractors == null) {
      return EnumExtractors.noOp();
    }
    return EnumExtractors.chained(enumExtractors.toArray(new EnumExtractor[0]));
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
    if (genericSchemaFeatures != null) {
      features.addAll(genericSchemaFeatures);
    }
    return GenericSchemaFeatures.chained(features.toArray(new GenericSchemaFeature[0]));
  }

  /**
   * @return the {@link JsonSchemaInferrer} built
   * @throws IllegalArgumentException if the spec version and features don't match up
   */
  public JsonSchemaInferrer build() {
    return new JsonSchemaInferrer(specVersion, integerTypePreference, integerTypeCriterion,
        getCombinedEnumExtractor(), titleDescriptionGenerator, getCombinedFormatInferrer(),
        getCombinedGenericSchemaFeature());
  }

}
