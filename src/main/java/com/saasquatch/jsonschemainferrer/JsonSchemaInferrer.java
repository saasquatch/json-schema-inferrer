package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.format;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getAllFieldNames;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getAllValuesForFieldName;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isNull;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isTextualFloat;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.newArray;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stream;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * Infer JSON schema based on sample JSONs. This class is immutable and thread safe.
 *
 * @author sli
 * @see #newBuilder()
 * @see #inferForSample(JsonNode)
 * @see #inferForSamples(Collection)
 */
@Immutable
public final class JsonSchemaInferrer {

  // All the fields are non-null
  private final SpecVersion specVersion;
  private final IntegerTypePreference integerTypePreference;
  private final IntegerTypeCriterion integerTypeCriterion;
  private final EnumExtractor enumExtractor;
  private final TitleDescriptionGenerator titleDescriptionGenerator;
  private final FormatInferrer formatInferrer;
  private final GenericSchemaFeature genericSchemaFeature;

  JsonSchemaInferrer(@Nonnull SpecVersion specVersion,
      @Nonnull IntegerTypePreference integerTypePreference,
      @Nonnull IntegerTypeCriterion integerTypeCriterion, @Nonnull EnumExtractor enumExtractor,
      @Nonnull TitleDescriptionGenerator titleDescriptionGenerator,
      @Nonnull FormatInferrer formatInferrer, @Nonnull GenericSchemaFeature genericSchemaFeature) {
    this.specVersion = specVersion;
    this.integerTypePreference = integerTypePreference;
    this.integerTypeCriterion = integerTypeCriterion;
    this.enumExtractor = enumExtractor;
    this.titleDescriptionGenerator = titleDescriptionGenerator;
    this.formatInferrer = formatInferrer;
    this.genericSchemaFeature = genericSchemaFeature;
  }

  /**
   * @return A new instance of {@link JsonSchemaInferrerBuilder} with default options.
   */
  @Nonnull
  public static JsonSchemaInferrerBuilder newBuilder() {
    return new JsonSchemaInferrerBuilder();
  }

  /**
   * Infer the JSON schema from a sample JSON.
   *
   * @param sample the sample JSON
   * @return the inferred JSON schema
   */
  @Nonnull
  public ObjectNode inferForSample(@Nullable JsonNode sample) {
    return inferForSamples(Collections.singleton(sample));
  }

  /**
   * Infer the JSON schema from multiple sample JSONs.
   *
   * @param samples the sample JSONs
   * @return the inferred JSON schema
   */
  @Nonnull
  public ObjectNode inferForSamples(@Nonnull Collection<? extends JsonNode> samples) {
    if (samples.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty samples");
    }
    final Collection<JsonNode> processedSamples =
        samples.stream().map(this::preProcessSample).collect(Collectors.toList());
    final ObjectNode schema = newObject();
    schema.put(Consts.Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    final Set<ObjectNode> anyOfs = getAnyOfsFromSamples(processedSamples);
    // anyOfs cannot be empty here, since we force inputs to be non empty
    assert !anyOfs.isEmpty() : "empty anyOfs encountered in inferForSamples";
    switch (anyOfs.size()) {
      case 1:
        schema.setAll(anyOfs.iterator().next());
        // No need to call processGenericSchemaFeature since this is an existing schema
        break;
      default: {
        schema.set(Consts.Fields.ANY_OF, newArray(anyOfs));
        // This is an anyOf schema. No type available.
        processGenericSchemaFeature(schema, processedSamples, null);
        break;
      }
    }
    return schema;
  }

  /**
   * Pre-process a {@link JsonNode} input.
   */
  @Nonnull
  private JsonNode preProcessSample(@Nullable JsonNode sample) {
    if (sample == null) {
      return JsonNodeFactory.instance.nullNode();
    } else if (sample.isPojo()) {
      throw new IllegalArgumentException(sample.getClass().getSimpleName() + " not supported");
    } else if (isNull(sample)) {
      /*
       * Treat JsonNodes that are to be serialized as null as NullNode. Turn NullNode into the
       * singleton NullNode because NullNode is not a final class and may break equals further down
       * the logic. Treat MissingNode as NullNode so we don't end up with duplicate nulls.
       */
      return JsonNodeFactory.instance.nullNode();
    }
    return sample;
  }

  /**
   * Handle object samples
   */
  @Nullable
  private ObjectNode processObjects(@Nonnull Collection<ObjectNode> objectNodes) {
    if (objectNodes.isEmpty()) {
      return null;
    }
    // All the field names across all samples combined
    final Set<String> allFieldNames = getAllFieldNames(objectNodes);
    final ObjectNode properties = newObject();
    for (String fieldName : allFieldNames) {
      // Get the vals from samples that have the field name. vals cannot be empty.
      final Collection<JsonNode> processedSamples = getAllValuesForFieldName(objectNodes, fieldName)
          .map(this::preProcessSample).collect(Collectors.toList());
      final ObjectNode newProperty = newObject();
      handleDescriptionGeneration(newProperty, fieldName);
      final Set<ObjectNode> anyOfs = getAnyOfsFromSamples(processedSamples);
      // anyOfs cannot be empty here, since we should have at least one match of the fieldName
      assert !anyOfs.isEmpty() : "empty anyOfs encountered";
      switch (anyOfs.size()) {
        case 1:
          newProperty.setAll(anyOfs.iterator().next());
          // No need to call processGenericSchemaFeature since this is an existing schema
          break;
        default: {
          newProperty.set(Consts.Fields.ANY_OF, newArray(anyOfs));
          // This is an anyOf schema. No type available.
          processGenericSchemaFeature(newProperty, processedSamples, null);
          break;
        }
      }
      properties.set(fieldName, newProperty);
    }
    final ObjectNode schema = newObject().put(Consts.Fields.TYPE, Consts.Types.OBJECT);
    if (properties.size() > 0) {
      schema.set(Consts.Fields.PROPERTIES, properties);
    }
    processGenericSchemaFeature(schema, objectNodes, Consts.Types.OBJECT);
    return schema;
  }

  /**
   * Handle array samples
   */
  @Nullable
  private ObjectNode processArrays(@Nonnull Collection<ArrayNode> arrayNodes) {
    if (arrayNodes.isEmpty()) {
      return null;
    }
    // Note that samples can be empty here if the sample arrays are empty
    final Collection<JsonNode> processedSamples = arrayNodes.stream().flatMap(j -> stream(j))
        .map(this::preProcessSample).collect(Collectors.toList());
    final ObjectNode items;
    final Set<ObjectNode> anyOfs = getAnyOfsFromSamples(processedSamples);
    switch (anyOfs.size()) {
      case 0:
        // anyOfs can be empty here, since the original array can be empty
        items = newObject();
        break;
      case 1:
        items = anyOfs.iterator().next();
        break;
      default:
        items = newObject();
        items.set(Consts.Fields.ANY_OF, newArray(anyOfs));
        break;
    }
    final ObjectNode schema = newObject().put(Consts.Fields.TYPE, Consts.Types.ARRAY);
    if (items.size() > 0) {
      schema.set(Consts.Fields.ITEMS, items);
    }
    processGenericSchemaFeature(schema, arrayNodes, Consts.Types.ARRAY);
    return schema;
  }

  /**
   * Handle primitive samples
   */
  @Nonnull
  private Set<ObjectNode> processPrimitives(@Nonnull Collection<ValueNode> valueNodes) {
    if (valueNodes.isEmpty()) {
      return Collections.emptySet();
    }
    final Set<ObjectNode> anyOfs = new HashSet<>();
    // Whether all the numbers in the samples are integers. Used for inferring number types.
    final boolean allNumbersAreIntegers =
        valueNodes.stream().filter(JsonNode::isNumber).allMatch(this::isInteger);
    /*
     * Map to keep track of examples. The keys are pairs of [type, format] stored in Lists, and the
     * vales are examples for that type/format combo.
     */
    final PrimitivesSummaryMap primitivesSummaryMap = new PrimitivesSummaryMap();
    for (ValueNode valueNode : valueNodes) {
      final ObjectNode newAnyOf = newObject();
      final String type = inferPrimitiveType(valueNode, allNumbersAreIntegers);
      newAnyOf.put(Consts.Fields.TYPE, type);
      final String format = inferFormat(valueNode);
      if (format != null) {
        newAnyOf.put(Consts.Fields.FORMAT, format);
      }
      primitivesSummaryMap.addSample(type, format, valueNode);
      anyOfs.add(newAnyOf);
    }
    // Put the combined examples and default back into the result schema
    for (ObjectNode anyOf : anyOfs) {
      final String type = anyOf.path(Consts.Fields.TYPE).textValue();
      final String format = anyOf.path(Consts.Fields.FORMAT).textValue();
      @Nonnull
      final PrimitivesSummary primitivesSummary =
          primitivesSummaryMap.getPrimitivesSummary(type, format);
      processGenericSchemaFeature(anyOf, primitivesSummary.getSamples(), type);
    }
    return anyOfs;
  }

  private Stream<ObjectNode> handleEnumExtractionResults(
      @Nonnull Set<Set<? extends JsonNode>> enumExtractionResults) {
    return enumExtractionResults.stream().map(enumExtractionResult -> {
      final ObjectNode schema = newObject();
      schema.set(Consts.Fields.ENUM, newArray(enumExtractionResult));
      processGenericSchemaFeature(schema, enumExtractionResult, null);
      return schema;
    });
  }

  /**
   * Build {@code anyOf} from sample JSONs. Note that all the arrays and objects will be combined.
   *
   * @param processedSamples The stream of samples that have gone through
   *        {@link #preProcessSample(JsonNode)}
   */
  @Nonnull
  private Set<ObjectNode> getAnyOfsFromSamples(
      @Nonnull Collection<? extends JsonNode> processedSamples) {
    final Set<Set<? extends JsonNode>> enumExtractionResults =
        getEnumExtractionResults(processedSamples);
    final Collection<ObjectNode> objectNodes = new ArrayList<>();
    final Collection<ArrayNode> arrayNodes = new ArrayList<>();
    final Collection<ValueNode> valueNodes = new ArrayList<>();
    for (JsonNode sample : processedSamples) {
      if (enumExtractionResults.stream()
          .anyMatch(enumExtractionResult -> enumExtractionResult.contains(sample))) {
        continue;
      }
      if (sample instanceof ObjectNode) {
        objectNodes.add((ObjectNode) sample);
      } else if (sample instanceof ArrayNode) {
        arrayNodes.add((ArrayNode) sample);
      } else {
        valueNodes.add((ValueNode) sample);
      }
    }
    final Set<ObjectNode> anyOfs = new HashSet<>();
    // Enums
    for (Set<? extends JsonNode> enumExtractionResult : enumExtractionResults) {
      final ObjectNode anyOf = newObject();
      anyOf.set(Consts.Fields.ENUM, newArray(enumExtractionResult));
      processGenericSchemaFeature(anyOf, enumExtractionResult, null);
      anyOfs.add(anyOf);
    }
    handleEnumExtractionResults(enumExtractionResults).forEach(anyOfs::add);
    // Objects
    final ObjectNode resultForObjects = processObjects(objectNodes);
    if (resultForObjects != null) {
      anyOfs.add(resultForObjects);
    }
    // Arrays
    final ObjectNode resultForArrays = processArrays(arrayNodes);
    if (resultForArrays != null) {
      anyOfs.add(resultForArrays);
    }
    // Primitives
    anyOfs.addAll(processPrimitives(valueNodes));
    postProcessAnyOfs(anyOfs);
    return Collections.unmodifiableSet(anyOfs);
  }

  private void postProcessAnyOfs(@Nonnull Collection<ObjectNode> anyOfs) {
    // Combine all the "simple" anyOfs, i.e. anyOfs that only have the "type" field
    final Set<String> simpleTypes = new HashSet<>();
    final Collection<ObjectNode> simpleAnyOfs = new ArrayList<>();
    for (ObjectNode anyOf : anyOfs) {
      final Set<String> anyOfSchemaFieldNames =
          stream(anyOf.fieldNames()).collect(Collectors.toSet());
      if (anyOfSchemaFieldNames.equals(Consts.Fields.SINGLETON_TYPE)) {
        simpleAnyOfs.add(anyOf);
        simpleTypes.add(anyOf.path(Consts.Fields.TYPE).textValue());
      }
    }
    // Combine all the simple types into an array
    if (simpleAnyOfs.size() <= 1) {
      // If we only have 1 simple anyOf, there's nothing to do.
      return;
    }
    anyOfs.removeAll(simpleAnyOfs);
    final ObjectNode combinedSimpleAnyOf = newObject();
    combinedSimpleAnyOf.set(Consts.Fields.TYPE, stringColToArrayDistinct(simpleTypes));
    anyOfs.add(combinedSimpleAnyOf);
  }

  // Visible for testing
  @Nonnull
  String inferPrimitiveType(@Nonnull JsonNode sample, boolean allNumbersAreIntegers) {
    // Marker for whether the error is caused by a known type
    final JsonNodeType type = sample.getNodeType();
    switch (type) {
      case STRING:
      case BINARY:
        return Consts.Types.STRING;
      case BOOLEAN:
        return Consts.Types.BOOLEAN;
      case NULL:
        return Consts.Types.NULL;
      case NUMBER: {
        if (isTextualFloat(sample)) {
          // This covers NaN and infinity
          return Consts.Types.STRING;
        }
        return integerTypePreference.shouldUseInteger(() -> isInteger(sample),
            allNumbersAreIntegers) ? Consts.Types.INTEGER : Consts.Types.NUMBER;
      }
      // We shouldn't encounter other types here
      default:
        break;
    }
    throw new IllegalStateException(format("Unexpected %s[%s] encountered with value[%s]",
        type.getClass().getSimpleName(), type, sample));
  }

  private boolean isInteger(@Nonnull JsonNode sample) {
    final IntegerTypeCriterionInput input = new IntegerTypeCriterionInput(sample, specVersion);
    return integerTypeCriterion.isInteger(input);
  }

  private Set<Set<? extends JsonNode>> getEnumExtractionResults(
      @Nonnull Collection<? extends JsonNode> samples) {
    final EnumExtractorInput input = new EnumExtractorInput(samples, specVersion);
    final Set<Set<? extends JsonNode>> enumExtractionResults = enumExtractor.extractEnums(input);
    return Objects.requireNonNull(enumExtractionResults);
  }

  private void handleDescriptionGeneration(@Nonnull ObjectNode schema, @Nullable String fieldName) {
    final TitleDescriptionGeneratorInput input =
        new TitleDescriptionGeneratorInput(fieldName, specVersion);
    final String title = titleDescriptionGenerator.generateTitle(input);
    if (title != null) {
      schema.put(Consts.Fields.TITLE, title);
    }
    final String description = titleDescriptionGenerator.generateDescription(input);
    if (description != null) {
      schema.put(Consts.Fields.DESCRIPTION, description);
    }
    final String comment = titleDescriptionGenerator.generateComment(input);
    if (comment != null) {
      schema.put(Consts.Fields.DOLLAR_COMMENT, comment);
    }
  }

  @Nullable
  private String inferFormat(@Nonnull JsonNode sample) {
    final FormatInferrerInput input = new FormatInferrerInput(sample, specVersion);
    return formatInferrer.inferFormat(input);
  }

  private void processGenericSchemaFeature(@Nonnull ObjectNode schema,
      @Nonnull Collection<? extends JsonNode> samples, @Nullable String type) {
    final GenericSchemaFeatureInput input =
        new GenericSchemaFeatureInput(schema, samples, type, specVersion);
    final ObjectNode featureResult = genericSchemaFeature.getFeatureResult(input);
    if (featureResult != null) {
      schema.setAll(featureResult);
    }
  }

}
