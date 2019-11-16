package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.format;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getAllFieldNames;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getAllValuesForFieldName;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.newArray;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.newObject;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stream;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public final class JsonSchemaInferrer {

  private final SpecVersion specVersion;
  private final IntegerTypePreference integerTypePreference;
  private final AdditionalPropertiesPolicy additionalPropertiesPolicy;
  private final RequiredPolicy requiredPolicy;
  private final DefaultPolicy defaultPolicy;
  private final ExamplesPolicy examplesPolicy;
  private final FormatInferrer formatInferrer;
  private final TitleGenerator titleGenerator;
  private final DescriptionGenerator descriptionGenerator;
  private final Set<ObjectSizeFeature> objectSizeFeatures;
  private final Set<ArrayLengthFeature> arrayLengthFeatures;
  private final Set<StringLengthFeature> stringLengthFeatures;

  JsonSchemaInferrer(@Nonnull SpecVersion specVersion,
      @Nonnull IntegerTypePreference integerTypePreference,
      @Nonnull AdditionalPropertiesPolicy additionalPropertiesPolicy,
      @Nonnull RequiredPolicy requiredPolicy, @Nonnull DefaultPolicy defaultPolicy,
      @Nonnull ExamplesPolicy examplesPolicy, @Nonnull FormatInferrer formatInferrer,
      @Nonnull TitleGenerator titleGenerator, @Nonnull DescriptionGenerator descriptionGenerator,
      @Nonnull Set<ObjectSizeFeature> objectSizeFeatures,
      @Nonnull Set<ArrayLengthFeature> arrayLengthFeatures,
      @Nonnull Set<StringLengthFeature> stringLengthFeatures) {
    this.specVersion = specVersion;
    this.integerTypePreference = integerTypePreference;
    this.additionalPropertiesPolicy = additionalPropertiesPolicy;
    this.requiredPolicy = requiredPolicy;
    this.defaultPolicy = defaultPolicy;
    this.examplesPolicy = examplesPolicy;
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
      throw new IllegalArgumentException("Unable to process empty samples");
    }
    final ObjectNode schema = newObject();
    schema.put(Consts.Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(samples.stream());
    // anyOfs cannot be empty here, since we force inputs to be non empty
    assert !anyOfs.isEmpty() : "empty anyOfs encountered in inferForSamples";
    switch (anyOfs.size()) {
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
  private JsonNode preProcessSample(@Nullable JsonNode sample) {
    if (sample == null || sample.isNull() || sample.isMissingNode()) {
      /*
       * Treat null as NullNode for obvious reasons. Treat NullNode as the singleton NullNode
       * because NullNode is not a final class and may break equals in the future. Treat MissingNode
       * as NullNode so we don't end up with duplicate nulls in examples.
       */
      return JsonNodeFactory.instance.nullNode();
    } else if (sample.isPojo()) {
      throw new IllegalArgumentException(sample.getClass().getSimpleName() + " not supported");
    }
    return sample;
  }

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
      final Stream<JsonNode> samplesStream = getAllValuesForFieldName(objectNodes, fieldName);
      final ObjectNode newProperty = newObject();
      handleTitleGeneration(newProperty, fieldName);
      handleDescriptionGeneration(newProperty, fieldName);
      final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(samplesStream);
      // anyOfs cannot be empty here, since we should have at least one match of the fieldName
      assert !anyOfs.isEmpty() : "empty anyOfs encountered";
      switch (anyOfs.size()) {
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

  @Nullable
  private ObjectNode processArrays(@Nonnull Collection<ArrayNode> arrayNodes) {
    if (arrayNodes.isEmpty()) {
      return null;
    }
    // Note that samples can be empty here if the sample arrays are empty
    final Stream<JsonNode> samplesStream = arrayNodes.stream().flatMap(j -> stream(j));
    final ObjectNode items;
    final Set<ObjectNode> anyOfs = getAnyOfsFromSamples(samplesStream);
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
  private Set<ObjectNode> processPrimitives(@Nonnull Collection<ValueNode> valueNodes) {
    if (valueNodes.isEmpty()) {
      return Collections.emptySet();
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
              primitiveSummary = new PrimitivesSummary();
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
      processExamples(anyOf, primitivesSummary, type, format);
      processStringLengthFeatures(anyOf, primitivesSummary);
    }
    return anyOfs;
  }

  /**
   * Build {@code anyOf} from sample JSONs. Note that all the arrays and objects will be combined.
   */
  @Nonnull
  private Set<ObjectNode> getAnyOfsFromSamples(@Nonnull Stream<? extends JsonNode> samplesStream) {
    final Set<ObjectNode> anyOfs = new HashSet<>();
    final Collection<ObjectNode> objectNodes = new ArrayList<>();
    final Collection<ArrayNode> arrayNodes = new ArrayList<>();
    final Collection<ValueNode> valueNodes = new ArrayList<>();
    samplesStream.map(this::preProcessSample).forEach(sample -> {
      if (sample instanceof ObjectNode) {
        objectNodes.add((ObjectNode) sample);
      } else if (sample instanceof ArrayNode) {
        arrayNodes.add((ArrayNode) sample);
      } else {
        valueNodes.add((ValueNode) sample);
      }
    });
    Optional.ofNullable(processObjects(objectNodes)).ifPresent(anyOfs::add);
    Optional.ofNullable(processArrays(arrayNodes)).ifPresent(anyOfs::add);
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
    boolean knownType = false;
    final JsonNodeType type = sample.getNodeType();
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
        return integerTypePreference.shouldUseInteger(sample, allNumbersAreIntegers)
            ? Consts.Types.INTEGER
            : Consts.Types.NUMBER;
      }
      default:
        break;
    }
    throw new IllegalStateException(format("%s %s[%s] encountered with value[%s]",
        knownType ? "Unexpected" : "Unrecognized", type.getClass().getSimpleName(), type, sample));
  }

  @Nullable
  private String inferFormat(@Nonnull JsonNode sample) {
    return formatInferrer.inferFormat(new FormatInferrerInput() {

      @Override
      public JsonNode getSample() {
        return sample;
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

  private void processExamples(@Nonnull ObjectNode schema,
      @Nonnull PrimitivesSummary primitivesSummary, @Nonnull String type, @Nullable String format) {
    final JsonNode examples = examplesPolicy.getExamples(new ExamplesPolicyInput() {

      @Override
      public Collection<JsonNode> getSamples() {
        return primitivesSummary.getSamples();
      }

      @Override
      public String getType() {
        return type;
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    });
    if (examples != null) {
      schema.set(Consts.Fields.EXAMPLES, examples);
    }
  }

  private void processObjectSizeFeatures(@Nonnull ObjectNode schema,
      @Nonnull Collection<ObjectNode> objectNodes) {
    for (ObjectSizeFeature objectSizeFeature : objectSizeFeatures) {
      objectSizeFeature.process(schema, objectNodes, this);
    }
  }

  private void processArrayLengthFeatures(@Nonnull ObjectNode schema,
      @Nonnull Collection<ArrayNode> arrayNodes) {
    for (ArrayLengthFeature arrayLengthFeature : arrayLengthFeatures) {
      arrayLengthFeature.process(schema, arrayNodes, this);
    }
  }

  private void processStringLengthFeatures(@Nonnull ObjectNode schema,
      @Nonnull PrimitivesSummary primitivesSummary) {
    for (StringLengthFeature stringLengthFeature : stringLengthFeatures) {
      stringLengthFeature.process(schema, primitivesSummary, this);
    }
  }

}