package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.format;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.getAllFieldNames;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.getAllValuesForFieldName;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.newArray;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.newObject;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayDistinct;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.unrecognizedEnumError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnegative;
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
 * Infer JSON schema based on sample JSONs
 *
 * @author sli
 * @see #newBuilder()
 * @see #inferForSample(JsonNode)
 * @see #inferForSamples(Collection)
 */
@Immutable
public final class JsonSchemaInferrer {

  private final SpecVersion specVersion;
  private final int examplesLimit;
  private final IntegerTypePreference integerTypePreference;
  private final AdditionalPropertiesPolicy additionalPropertiesPolicy;
  private final RequiredPolicy requiredPolicy;
  private final DefaultPolicy defaultPolicy;
  private final FormatInferrer formatInferrer;
  private final TitleGenerator titleGenerator;
  private final DescriptionGenerator descriptionGenerator;
  private final Set<ObjectSizeFeature> objectSizeFeatures;
  private final Set<ArrayLengthFeature> arrayLengthFeatures;
  private final Set<StringLengthFeature> stringLengthFeatures;

  JsonSchemaInferrer(@Nonnull SpecVersion specVersion, @Nonnegative int examplesLimit,
      @Nonnull IntegerTypePreference integerTypePreference,
      @Nonnull AdditionalPropertiesPolicy additionalPropertiesPolicy,
      @Nonnull RequiredPolicy requiredPolicy, @Nonnull DefaultPolicy defaultPolicy,
      @Nonnull FormatInferrer formatInferrer, @Nonnull TitleGenerator titleGenerator,
      @Nonnull DescriptionGenerator descriptionGenerator,
      @Nonnull Set<ObjectSizeFeature> objectSizeFeatures,
      @Nonnull Set<ArrayLengthFeature> arrayLengthFeatures,
      @Nonnull Set<StringLengthFeature> stringLengthFeatures) {
    this.specVersion = specVersion;
    this.examplesLimit = examplesLimit;
    this.integerTypePreference = integerTypePreference;
    this.additionalPropertiesPolicy = additionalPropertiesPolicy;
    this.requiredPolicy = requiredPolicy;
    this.defaultPolicy = defaultPolicy;
    this.formatInferrer = formatInferrer;
    this.titleGenerator = titleGenerator;
    this.descriptionGenerator = descriptionGenerator;
    this.objectSizeFeatures = objectSizeFeatures;
    this.arrayLengthFeatures = arrayLengthFeatures;
    this.stringLengthFeatures = stringLengthFeatures;
  }

  /**
   * @return A new instance of {@link JsonSchemaInferrerBuilder} with default options.
   */
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
   * @param input the sample JSONs
   * @return the inferred JSON schema
   */
  @Nonnull
  public ObjectNode inferForSamples(@Nonnull Collection<? extends JsonNode> samples) {
    if (samples.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    final ObjectNode schema = newObject();
    schema.put(Consts.Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(samples);
    switch (anyOfs.size()) {
      case 0:
        // anyOfs cannot be empty here, since we force inputs to be non empty
        throw new IllegalStateException("empty anyOfs encountered in inferForSamples");
      case 1:
        schema.setAll(anyOfs.iterator().next());
        break;
      default:
        schema.set(Consts.Fields.ANY_OF, newArray().addAll(anyOfs));
        break;
    }
    return schema;
  }

  /**
   * Pre-process a {@link JsonNode} input.
   */
  @Nonnull
  private JsonNode preProcessJsonNode(@Nullable JsonNode value) {
    if (value == null || value.isNull() || value.isMissingNode()) {
      /*
       * Treat null as NullNode for obvious reasons. Treat NullNode as the singleton NullNode
       * because NullNode is not a final class and may break equals in the future. Treat MissingNode
       * as NullNode so we don't end up with duplicate nulls in examples.
       */
      return JsonNodeFactory.instance.nullNode();
    } else if (value.isPojo()) {
      throw new IllegalArgumentException(value.getClass().getSimpleName() + " not supported");
    }
    return value;
  }

  @Nonnull
  private ObjectNode processObjects(@Nonnull Collection<ObjectNode> objectNodes) {
    if (objectNodes.isEmpty()) {
      throw new IllegalStateException("Unable to process empty Collection of objects");
    }
    // All the field names across all samples combined
    final Set<String> allFieldNames = getAllFieldNames(objectNodes);
    final ObjectNode properties = newObject();
    for (String fieldName : allFieldNames) {
      // Get the vals from samples that have the field name. vals cannot be empty.
      final Collection<JsonNode> samples = getAllValuesForFieldName(objectNodes, fieldName);
      final ObjectNode newProperty = newObject();
      handleTitleGeneration(newProperty, fieldName);
      handleDescriptionGeneration(newProperty, fieldName);
      final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(samples);
      switch (anyOfs.size()) {
        case 0:
          // anyOfs cannot be empty here, since we should have at least one match of the fieldName
          throw new IllegalStateException("empty anyOfs encountered");
        case 1:
          newProperty.setAll(anyOfs.iterator().next());
          break;
        default:
          newProperty.set(Consts.Fields.ANY_OF, newArray().addAll(anyOfs));
          break;
      }
      properties.set(fieldName, newProperty);
    }
    final ObjectNode schema = newObject().put(Consts.Fields.TYPE, Consts.Types.OBJECT);
    if (properties.size() > 0) {
      schema.set(Consts.Fields.PROPERTIES, properties);
    }
    processAdditionalProperties(schema);
    processRequired(schema, objectNodes);
    processObjectSizeFeatures(schema, objectNodes);
    return schema;
  }

  @Nonnull
  private ObjectNode processArrays(@Nonnull Collection<ArrayNode> arrayNodes) {
    if (arrayNodes.isEmpty()) {
      throw new IllegalStateException("Unable to process empty Collection of arrays");
    }
    // Note that samples can be empty here if the sample arrays are empty
    final Collection<JsonNode> samples =
        arrayNodes.stream().flatMap(j -> stream(j)).collect(Collectors.toList());
    final ObjectNode items;
    final Set<ObjectNode> anyOfs = getAnyOfsFromSamples(samples);
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
        items.set(Consts.Fields.ANY_OF, newArray().addAll(anyOfs));
        break;
    }
    final ObjectNode schema = newObject().put(Consts.Fields.TYPE, Consts.Types.ARRAY);
    if (items.size() > 0) {
      schema.set(Consts.Fields.ITEMS, items);
    }
    processArrayLengthFeatures(schema, arrayNodes);
    return schema;
  }

  @Nonnull
  private Set<ObjectNode> processPrimitives(@Nonnull Set<ValueNode> valueNodes) {
    if (valueNodes.isEmpty()) {
      throw new IllegalStateException("Unable to process empty Collection of primitive");
    }
    final Set<ObjectNode> anyOfs = new HashSet<>();
    // Whether all the numbers in the samples are integers. Used for inferring number types.
    final boolean allNumbersAreIntegers =
        valueNodes.stream().filter(JsonNode::isNumber).allMatch(JsonNode::isIntegralNumber);
    /*
     * Map to keep track of examples. The keys are pairs of [type, format] stored in Lists, and the
     * vales are examples for that type/format combo.
     */
    final Map<List<String>, PrimitivesSummary> primitivesSummaryMap = new HashMap<>();
    for (ValueNode valueNode : valueNodes) {
      final ObjectNode newAnyOf = newObject();
      final String type = inferPrimitiveType(valueNode, allNumbersAreIntegers);
      newAnyOf.put(Consts.Fields.TYPE, type);
      final String format = inferFormat(valueNode);
      if (format != null) {
        newAnyOf.put(Consts.Fields.FORMAT, format);
      }
      // Keep track of examples even if examples is disabled
      primitivesSummaryMap.compute(Arrays.asList(type, format),
          (typeFormatPair, primitiveSummary) -> {
            if (primitiveSummary == null) {
              primitiveSummary = new PrimitivesSummary(examplesLimit);
            }
            primitiveSummary.addSample(valueNode);
            return primitiveSummary;
          });
      anyOfs.add(newAnyOf);
    }
    // Put the combined examples and default back into the result schema
    for (ObjectNode anyOf : anyOfs) {
      final String type = anyOf.path(Consts.Fields.TYPE).textValue();
      final String format = anyOf.path(Consts.Fields.FORMAT).textValue();
      @Nonnull
      final PrimitivesSummary primitivesSummary =
          primitivesSummaryMap.get(Arrays.asList(type, format));
      processDefault(anyOf, primitivesSummary);
      final Collection<JsonNode> examples = primitivesSummary.getExamples();
      if (!examples.isEmpty()) {
        anyOf.set(Consts.Fields.EXAMPLES, newArray().addAll(examples));
      }
      processStringLengthFeatures(anyOf, primitivesSummary);
    }
    return anyOfs;
  }

  /**
   * Build {@code anyOf} from sample JSONs. Note that all the arrays and objects will be combined.
   */
  @Nonnull
  private Set<ObjectNode> getAnyOfsFromSamples(
      @Nonnull Collection<? extends JsonNode> samplesInput) {
    final Collection<JsonNode> samples =
        samplesInput.stream().map(this::preProcessJsonNode).collect(Collectors.toList());
    final Set<ObjectNode> anyOfs = new HashSet<>();
    final Collection<ObjectNode> objectNodes = new ArrayList<>();
    final Collection<ArrayNode> arrayNodes = new ArrayList<>();
    final Set<ValueNode> valueNodes = new LinkedHashSet<>();
    for (JsonNode sample : samples) {
      if (sample instanceof ObjectNode) {
        objectNodes.add((ObjectNode) sample);
      } else if (sample instanceof ArrayNode) {
        arrayNodes.add((ArrayNode) sample);
      } else {
        valueNodes.add((ValueNode) sample);
      }
    }
    if (!objectNodes.isEmpty()) {
      anyOfs.add(processObjects(objectNodes));
    }
    if (!arrayNodes.isEmpty()) {
      anyOfs.add(processArrays(arrayNodes));
    }
    if (!valueNodes.isEmpty()) {
      anyOfs.addAll(processPrimitives(valueNodes));
    }
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

  @Nonnull
  private String inferPrimitiveType(@Nonnull JsonNode value, boolean allNumbersAreIntegers) {
    // Marker for whether the error is caused by a known type
    boolean knownType = false;
    final JsonNodeType type = value.getNodeType();
    switch (type) {
      // We shouldn't encounter these types here
      case ARRAY:
      case POJO:
      case OBJECT:
      case MISSING:
        knownType = true;
        break;
      case STRING:
      case BINARY:
        return Consts.Types.STRING;
      case BOOLEAN:
        return Consts.Types.BOOLEAN;
      case NULL:
        return Consts.Types.NULL;
      case NUMBER: {
        final boolean useInteger;
        switch (integerTypePreference) {
          case IF_ALL:
            useInteger = allNumbersAreIntegers;
            break;
          case IF_ANY:
            useInteger = value.isIntegralNumber();
            break;
          case NEVER:
            useInteger = false;
            break;
          default:
            return unrecognizedEnumError(integerTypePreference);
        }
        return useInteger ? Consts.Types.INTEGER : Consts.Types.NUMBER;
      }
      default:
        break;
    }
    throw new IllegalStateException(format("%s %s[%s] encountered with value[%s]",
        knownType ? "Unexpected" : "Unrecognized", type.getClass().getSimpleName(), type, value));
  }

  @Nullable
  private String inferFormat(@Nonnull JsonNode value) {
    return formatInferrer.inferFormat(new FormatInferrerInput() {

      @Override
      public JsonNode getSample() {
        return value;
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    });
  }

  private void handleTitleGeneration(@Nonnull ObjectNode schema, @Nullable String fieldName) {
    final String title = titleGenerator.generateTitle(new TitleGeneratorInput() {

      @Override
      public String getFieldName() {
        return fieldName;
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    });
    if (title != null) {
      schema.put(Consts.Fields.TITLE, title);
    }
  }

  private void handleDescriptionGeneration(@Nonnull ObjectNode schema, @Nullable String fieldName) {
    final String description =
        descriptionGenerator.generateDescription(new DescriptionGeneratorInput() {

          @Override
          public String getFieldName() {
            return fieldName;
          }

          @Override
          public SpecVersion getSpecVersion() {
            return specVersion;
          }

        });
    if (description != null) {
      schema.put(Consts.Fields.DESCRIPTION, description);
    }
  }

  private void processAdditionalProperties(@Nonnull ObjectNode schema) {
    final JsonNode additionalProps =
        additionalPropertiesPolicy.getAdditionalProperties(new AdditionalPropertiesPolicyInput() {

          @Override
          public ObjectNode getSchema() {
            return schema;
          }

          @Override
          public SpecVersion getSpecVersion() {
            return specVersion;
          }

        });
    if (additionalProps != null) {
      schema.set(Consts.Fields.ADDITIONAL_PROPERTIES, additionalProps);
    }
  }

  private void processRequired(@Nonnull ObjectNode schema,
      @Nonnull Collection<ObjectNode> objectNodes) {
    final JsonNode required = requiredPolicy.getRequired(new RequiredPolicyInput() {

      @Override
      public ObjectNode getSchema() {
        return schema;
      }

      @Override
      public Collection<JsonNode> getSamples() {
        return Collections.unmodifiableCollection(objectNodes);
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    });
    if (required != null) {
      schema.set(Consts.Fields.REQUIRED, required);
    }
  }

  private void processObjectSizeFeatures(@Nonnull ObjectNode schema,
      @Nonnull Collection<ObjectNode> objectNodes) {
    for (ObjectSizeFeature objectSizeFeature : objectSizeFeatures) {
      switch (objectSizeFeature) {
        case MIN_PROPERTIES: {
          objectNodes.stream().mapToInt(JsonNode::size).min()
              .ifPresent(minProps -> schema.put(Consts.Fields.MIN_PROPERTIES, minProps));
          break;
        }
        case MAX_PROPERTIES: {
          objectNodes.stream().mapToInt(JsonNode::size).max()
              .ifPresent(maxProps -> schema.put(Consts.Fields.MAX_PROPERTIES, maxProps));
          break;
        }
        default:
          unrecognizedEnumError(objectSizeFeature);
      }
    }
  }

  private void processArrayLengthFeatures(@Nonnull ObjectNode schema,
      @Nonnull Collection<ArrayNode> arrayNodes) {
    for (ArrayLengthFeature arrayLengthFeature : arrayLengthFeatures) {
      switch (arrayLengthFeature) {
        case MIN_ITEMS: {
          arrayNodes.stream().mapToInt(JsonNode::size).min()
              .ifPresent(minItems -> schema.put(Consts.Fields.MIN_ITEMS, minItems));
          break;
        }
        case MAX_ITEMS: {
          arrayNodes.stream().mapToInt(JsonNode::size).max()
              .ifPresent(maxItems -> schema.put(Consts.Fields.MAX_ITEMS, maxItems));
          break;
        }
        default:
          unrecognizedEnumError(arrayLengthFeature);
      }
    }
  }

  private void processDefault(@Nonnull ObjectNode schema,
      @Nonnull PrimitivesSummary primitivesSummary) {
    final JsonNode defaultNode = defaultPolicy.getDefault(new DefaultPolicyInput() {

      @Override
      public JsonNode getFirstSample() {
        return primitivesSummary.getFirstSample();
      }

      @Override
      public JsonNode getLastSample() {
        return primitivesSummary.getLastSample();
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    });
    if (defaultNode != null) {
      schema.set(Consts.Fields.DEFAULT, defaultNode);
    }
  }

  private void processStringLengthFeatures(@Nonnull ObjectNode schema,
      @Nonnull PrimitivesSummary primitivesSummary) {
    for (StringLengthFeature stringLengthFeature : stringLengthFeatures) {
      switch (stringLengthFeature) {
        case MIN_LENGTH: {
          primitivesSummary.getMinStringLength()
              .ifPresent(minLength -> schema.put(Consts.Fields.MIN_LENGTH, minLength));
          break;
        }
        case MAX_LENGTH: {
          primitivesSummary.getMaxStringLength()
              .ifPresent(maxLength -> schema.put(Consts.Fields.MAX_LENGTH, maxLength));
          break;
        }
        default:
          unrecognizedEnumError(stringLengthFeature);
      }
    }
  }

}
