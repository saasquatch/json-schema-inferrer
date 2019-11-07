package com.saasquatch.json_schema_inferrer;

import static com.saasquatch.json_schema_inferrer.JunkDrawer.combineArraysDistinct;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.format;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.getAllFieldNames;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.getAllValuesForFieldName;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.newArray;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.newObject;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stream;
import static com.saasquatch.json_schema_inferrer.JunkDrawer.stringColToArrayDistinct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.fasterxml.jackson.databind.node.POJONode;
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
  private final AdditionalPropertiesPolicy additionalPropertiesPolicy;
  private final RequiredPolicy requiredPolicy;
  private final FormatInferrer formatInferrer;
  private final TitleGenerator titleGenerator;

  private JsonSchemaInferrer(@Nonnull SpecVersion specVersion, @Nonnegative int examplesLimit,
      @Nonnull AdditionalPropertiesPolicy additionalPropertiesPolicy,
      @Nonnull RequiredPolicy requiredPolicy, @Nonnull FormatInferrer formatInferrer,
      @Nonnull TitleGenerator titleGenerator) {
    this.specVersion = specVersion;
    this.examplesLimit = examplesLimit;
    this.additionalPropertiesPolicy = additionalPropertiesPolicy;
    this.requiredPolicy = requiredPolicy;
    this.formatInferrer = formatInferrer;
    this.titleGenerator = titleGenerator;
  }

  public static Builder newBuilder() {
    return new Builder();
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
    final Set<JsonNode> processedSamples = preProcessJsonNodes(samples);
    if (processedSamples.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    final ObjectNode schema = newObject();
    schema.put(Consts.Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(processedSamples);
    switch (anyOfs.size()) {
      case 0:
        // anyOfs cannot be empty here, since we force inputs to be non empty
        throw new AssertionError();
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
   * Pre-process a {@link JsonNode} input. Note that {@code null}s produced by this method should be
   * discarded.
   */
  @Nullable
  private JsonNode preProcessJsonNode(@Nullable JsonNode value) {
    if (value == null) {
      // Treat null as NullNode
      return JsonNodeFactory.instance.nullNode();
    } else if (value.isPojo()) {
      throw new IllegalArgumentException(POJONode.class.getSimpleName() + " not supported");
    }
    return value;
  }

  @Nonnull
  private Set<JsonNode> preProcessJsonNodes(@Nonnull Iterable<? extends JsonNode> values) {
    return stream(values).map(this::preProcessJsonNode).filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @Nonnull
  private Collection<ObjectNode> processPrimitives(@Nonnull Collection<ValueNode> valueNodes) {
    final Collection<ObjectNode> anyOfs = new HashSet<>();
    // Whether all the numbers in the samples are integers. Used for inferring number types.
    final boolean allNumbersAreIntegers =
        valueNodes.stream().filter(JsonNode::isNumber).allMatch(JsonNode::isIntegralNumber);
    /*
     * Map to keep track of examples. The keys are pairs of [type, format] stored in Lists, and the
     * vales are examples for that type/format combo.
     */
    final Map<List<String>, Set<ValueNode>> examplesMap =
        examplesLimit > 0 ? new HashMap<>() : null;
    for (ValueNode valueNode : valueNodes) {
      final ObjectNode newAnyOf = newObject();
      final String type = inferPrimitiveType(valueNode, allNumbersAreIntegers);
      newAnyOf.put(Consts.Fields.TYPE, type);
      final String format = inferFormat(valueNode);
      if (format != null) {
        newAnyOf.put(Consts.Fields.FORMAT, format);
      }
      // Keep track of examples if examples is enabled
      if (examplesMap != null) {
        examplesMap.compute(Arrays.asList(type, format), (typeFormatPair, originalExamples) -> {
          final Set<ValueNode> newExamples =
              originalExamples == null ? new HashSet<>() : originalExamples;
          if (newExamples.size() < examplesLimit) {
            newExamples.add(valueNode);
          }
          return newExamples;
        });
      }
      anyOfs.add(newAnyOf);
    }
    // Put the combined examples back into the result schema
    if (examplesMap != null) {
      for (ObjectNode anyOf : anyOfs) {
        final String type = anyOf.path(Consts.Fields.TYPE).textValue();
        final String format = anyOf.path(Consts.Fields.FORMAT).textValue();
        final Set<ValueNode> examples = examplesMap.get(Arrays.asList(type, format));
        if (examples != null && !examples.isEmpty()) {
          anyOf.set(Consts.Fields.EXAMPLES, newArray().addAll(examples));
        }
      }
    }
    return anyOfs;
  }

  @Nonnull
  private ObjectNode processObjects(@Nonnull Collection<ObjectNode> objectNodes) {
    if (objectNodes.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    // All the field names across all samples combined
    final Set<String> allFieldNames = getAllFieldNames(objectNodes);
    final ObjectNode properties = newObject();
    for (String fieldName : allFieldNames) {
      // Get the vals from samples that have the field name. vals cannot be empty.
      final Set<JsonNode> samples =
          preProcessJsonNodes(getAllValuesForFieldName(objectNodes, fieldName));
      final ObjectNode newProperty = newObject();
      final String title = generateTitle(fieldName);
      if (title != null) {
        newProperty.put(Consts.Fields.TITLE, title);
      }
      final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(samples);
      switch (anyOfs.size()) {
        case 0:
          // anyOfs cannot be empty here, since we should have at least one match of the fieldName
          throw new AssertionError();
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
    return schema;
  }

  @Nonnull
  private ObjectNode processArray(@Nonnull ArrayNode arrayNode) {
    final Set<JsonNode> samples = preProcessJsonNodes(arrayNode);
    final ObjectNode items;
    final Collection<ObjectNode> anyOfs = getAnyOfsFromSamples(samples);
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
    return schema;
  }

  /**
   * Build {@code anyOf} from sample JSONs. Note that all the arrays and objects will be combined.
   *
   * @param samples the <em>processed</em> samples that have gone through
   *        {@link #preProcessJsonNode(JsonNode)}
   */
  @Nonnull
  private Collection<ObjectNode> getAnyOfsFromSamples(@Nonnull Set<JsonNode> samples) {
    final Collection<ObjectNode> anyOfs = new HashSet<>();
    final Set<ObjectNode> objectNodes = new HashSet<>();
    final Set<ArrayNode> arrayNodes = new HashSet<>();
    final Set<ValueNode> valueNodes = new HashSet<>();
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
      anyOfs.add(processArray(combineArraysDistinct(arrayNodes)));
    }
    if (!valueNodes.isEmpty()) {
      // Not using addAnyOf on purpose
      anyOfs.addAll(processPrimitives(valueNodes));
    }
    postProcessAnyOfs(anyOfs);
    return Collections.unmodifiableCollection(anyOfs);
  }

  private void postProcessAnyOfs(@Nonnull Collection<ObjectNode> anyOfs) {
    // Combine all the "simple" anyOfs, i.e. anyOfs that only have the "type" field
    final Set<String> simpleTypes = new HashSet<>();
    final Collection<ObjectNode> simpleAnyOfs = new ArrayList<>();
    for (ObjectNode anyOf : anyOfs) {
      final Set<String> fieldNames = stream(anyOf.fieldNames()).collect(Collectors.toSet());
      if (fieldNames.equals(Consts.Fields.SINGLETON_TYPE)) {
        simpleAnyOfs.add(anyOf);
        simpleTypes.add(anyOf.path(Consts.Fields.TYPE).textValue());
      }
    }
    if (simpleAnyOfs.size() <= 1) {
      return;
    }
    // Combine all the simple types into an array
    anyOfs.removeAll(simpleAnyOfs);
    final ObjectNode combinedSimpleAnyOf = newObject();
    combinedSimpleAnyOf.set(Consts.Fields.TYPE, stringColToArrayDistinct(simpleTypes));
    anyOfs.add(combinedSimpleAnyOf);
  }

  @Nonnull
  private static String inferPrimitiveType(@Nonnull JsonNode value, boolean allNumbersAreIntegers) {
    // Marker for whether the error is caused by a known type
    boolean knownType = false;
    final JsonNodeType type = value.getNodeType();
    switch (type) {
      case ARRAY:
      case POJO:
      case OBJECT:
        knownType = true;
        break;
      case BINARY:
        return Consts.Types.STRING;
      case BOOLEAN:
        return Consts.Types.BOOLEAN;
      case MISSING:
        return Consts.Types.NULL;
      case NULL:
        return Consts.Types.NULL;
      case NUMBER:
        return allNumbersAreIntegers ? Consts.Types.INTEGER : Consts.Types.NUMBER;
      case STRING:
        return Consts.Types.STRING;
      default:
        break;
    }
    final String adj = knownType ? "Unexpected" : "Unrecognized";
    throw new IllegalArgumentException(format("%s %s[%s] encountered with value[%s]", adj,
        type.getClass().getSimpleName(), type, value));
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

  @Nullable
  private String generateTitle(@Nonnull String fieldName) {
    return titleGenerator.generate(new TitleGeneratorInput() {

      @Override
      public String getFieldName() {
        return fieldName;
      }

      @Override
      public SpecVersion getSpecVersion() {
        return specVersion;
      }

    });
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

  public static final class Builder {

    private SpecVersion specVersion = SpecVersion.DRAFT_04;
    private int examplesLimit = 0;
    private AdditionalPropertiesPolicy additionalPropertiesPolicy =
        AdditionalPropertiesPolicies.noOp();
    private RequiredPolicy requiredPolicy = RequiredPolicies.noOp();
    private FormatInferrer formatInferrer = FormatInferrers.noOp();
    private TitleGenerator titleGenerator = TitleGenerators.noOp();

    private Builder() {}

    /**
     * Set the specification version. The default is draft-04.
     */
    public Builder setSpecVersion(@Nonnull SpecVersion specVersion) {
      this.specVersion = Objects.requireNonNull(specVersion);
      return this;
    }

    /**
     * Set the max size for {@code examples}. 0 to disable {@code examples}. By default it is 0.
     *
     * @throws IllegalArgumentException if the input is negative
     */
    public Builder setExamplesLimit(@Nonnegative int examplesLimit) {
      if (examplesLimit < 0) {
        throw new IllegalArgumentException("Invalid examplesLimit");
      }
      this.examplesLimit = examplesLimit;
      return this;
    }

    /**
     * Set the {@link AdditionalPropertiesPolicy}. By default it is
     * {@link AdditionalPropertiesPolicies#noOp()}.
     *
     * @see AdditionalPropertiesPolicy
     * @see AdditionalPropertiesPolicies
     */
    public Builder setAdditionalPropertiesPolicy(
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
    public Builder setRequiredPolicy(@Nonnull RequiredPolicy requiredPolicy) {
      this.requiredPolicy = Objects.requireNonNull(requiredPolicy);
      return this;
    }

    /**
     * Set the {@link FormatInferrer} for inferring the <a href=
     * "https://json-schema.org/understanding-json-schema/reference/string.html#format">format</a>
     * of strings. By default it uses {@link FormatInferrers#noOp()}. An example of a possible
     * custom implementation is available at {@link FormatInferrers#dateTime()}, which you can
     * potentially use or use it combined with your own implementations with
     * {@link FormatInferrers#chained(FormatInferrer...)}.<br>
     * Note that if your JSON samples have large nested arrays, it's recommended to set this to
     * false to prevent confusing outputs.
     *
     * @see FormatInferrer
     * @see FormatInferrers
     */
    public Builder setFormatInferrer(@Nonnull FormatInferrer formatInferrer) {
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
    public Builder setTitleGenerator(@Nonnull TitleGenerator titleGenerator) {
      this.titleGenerator = Objects.requireNonNull(titleGenerator);
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
      return new JsonSchemaInferrer(specVersion, examplesLimit, additionalPropertiesPolicy,
          requiredPolicy, formatInferrer, titleGenerator);
    }

  }

}
