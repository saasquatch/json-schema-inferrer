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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
import com.flipkart.zjsonpatch.JsonDiff;

/**
 * Infer JSON schema based on sample JSONs
 *
 * @author sli
 * @see #newBuilder()
 * @see #inferForSample(JsonNode)
 */
@Immutable
public final class JsonSchemaInferrer {

  private final SpecVersion specVersion;
  private final boolean includeMetaSchemaUrl;
  private final AdditionalPropertiesPolicy additionalPropertiesPolicy;
  private final RequiredPolicy requiredPolicy;
  private final FormatInferrer formatInferrer;
  private final TitleGenerator titleGenerator;

  private JsonSchemaInferrer(@Nonnull SpecVersion specVersion, boolean includeMetaSchemaUrl,
      @Nonnull AdditionalPropertiesPolicy additionalPropertiesPolicy,
      @Nonnull RequiredPolicy requiredPolicy, @Nonnull FormatInferrer formatInferrer,
      @Nonnull TitleGenerator titleGenerator) {
    this.specVersion = specVersion;
    this.includeMetaSchemaUrl = includeMetaSchemaUrl;
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
  public ObjectNode inferForSamples(@Nonnull Collection<JsonNode> samples) {
    final Set<JsonNode> processedSamples = preProcessJsonNodes(samples);
    if (processedSamples.isEmpty()) {
      throw new IllegalArgumentException("Unable to process empty Collection");
    }
    final ObjectNode schema = newObject();
    if (includeMetaSchemaUrl) {
      schema.put(Consts.Fields.DOLLAR_SCHEMA, specVersion.getMetaSchemaUrl());
    }
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
    } else if (value.isMissingNode()) {
      // Treat MissingNode as non-existent
      return null;
    } else if (value.isPojo()) {
      throw new IllegalArgumentException(POJONode.class.getSimpleName() + " not supported");
    }
    return value;
  }

  @Nonnull
  private Set<JsonNode> preProcessJsonNodes(@Nonnull Iterable<JsonNode> values) {
    return stream(values)
        .map(this::preProcessJsonNode)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @Nonnull
  private ObjectNode processPrimitive(@Nonnull ValueNode valueNode) {
    final ObjectNode schema = newObject();
    schema.put(Consts.Fields.TYPE, inferType(valueNode));
    final String format = inferFormat(valueNode);
    if (format != null) {
      schema.put(Consts.Fields.FORMAT, format);
    }
    return schema;
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
    // Using LinkedList on purpose here since we do a lot of add and remove
    final Collection<ObjectNode> anyOfs = new LinkedList<>();
    final Set<ObjectNode> objectNodes = new HashSet<>();
    final Set<ArrayNode> arrayNodes = new HashSet<>();
    for (JsonNode sample : samples) {
      if (sample instanceof ObjectNode) {
        objectNodes.add((ObjectNode) sample);
      } else if (sample instanceof ArrayNode) {
        arrayNodes.add((ArrayNode) sample);
      } else {
        // input is null or a ValueNode
        addAnyOf(anyOfs, processPrimitive((ValueNode) sample));
      }
    }
    if (!objectNodes.isEmpty()) {
      addAnyOf(anyOfs, processObjects(objectNodes));
    }
    if (!arrayNodes.isEmpty()) {
      addAnyOf(anyOfs, processArray(combineArraysDistinct(arrayNodes)));
    }
    postProcessAnyOfs(anyOfs);
    return Collections.unmodifiableCollection(anyOfs);
  }

  private void addAnyOf(@Nonnull Collection<ObjectNode> anyOfs, @Nonnull ObjectNode newAnyOf) {
    if (anyOfs.isEmpty()) {
      anyOfs.add(newAnyOf);
      return;
    }
    final Iterator<ObjectNode> anyOfsIterator = anyOfs.iterator();
    anyOfsLoop: while (anyOfsIterator.hasNext()) {
      final ObjectNode anyOf = anyOfsIterator.next();
      if (anyOf.equals(newAnyOf)) {
        return; // Low hanging fruit
      }
      final JsonNode diffs = JsonDiff.asJson(anyOf, newAnyOf);
      for (JsonNode diff : diffs) {
        final String path = diff.path(Consts.Diff.PATH).textValue();
        if (path != null && path.endsWith('/' + Consts.Fields.FORMAT)) {
          if (newAnyOf.at(path.substring(0, path.lastIndexOf('/'))).path(Consts.Fields.TYPE)
              .isTextual()) {
            // If any of the diffs is caused by a format change, we'll want to add it
            break anyOfsLoop;
          }
        }
      }
      final Set<String> ops = stream(diffs)
          .map(j -> j.path(Consts.Diff.OP).textValue())
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      if (ops.equals(Consts.Diff.SINGLETON_ADD)) {
        /*
         * The new anyOf is a superset of one of the existing anyOfs. Discard the existing one and
         * add the new one.
         */
        anyOfsIterator.remove();
        break;
      } else if (ops.isEmpty() || ops.equals(Consts.Diff.SINGLETON_REMOVE)) {
        // The new anyOf is the same or a subset of one of the existing anyOfs. Do nothing.
        return;
      }
    }
    anyOfs.add(newAnyOf);
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
  private static String inferType(@Nonnull JsonNode value) {
    // Marker for whether the error is caused by a known type
    boolean knownType = false;
    final JsonNodeType type = value.getNodeType();
    switch (type) {
      case ARRAY:
        return Consts.Types.ARRAY;
      case BINARY:
        return Consts.Types.STRING;
      case BOOLEAN:
        return Consts.Types.BOOLEAN;
      case MISSING:
        knownType = true;
        break;
      case NULL:
        return Consts.Types.NULL;
      case NUMBER:
        return value.isIntegralNumber() ? Consts.Types.INTEGER : Consts.Types.NUMBER;
      case OBJECT:
        return Consts.Types.OBJECT;
      case POJO:
        knownType = true;
        break;
      case STRING:
        return Consts.Types.STRING;
      default:
        break;
    }
    if (knownType) {
      throw new IllegalArgumentException(
          format("Unexpected %s: %s encountered", type.getClass().getSimpleName(), type));
    } else {
      throw new IllegalArgumentException(
          format("Unrecognized %s: %s", type.getClass().getSimpleName(), type));
    }
  }

  @Nullable
  private String inferFormat(@Nonnull JsonNode value) {
    return formatInferrer.infer(new FormatInferrerInput() {

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
    private boolean includeMetaSchemaUrl = true;
    private AdditionalPropertiesPolicy additionalPropertiesPolicy =
        AdditionalPropertiesPolicies.noOp();
    private RequiredPolicy requiredPolicy = RequiredPolicies.noOp();
    private FormatInferrer formatInferrer = FormatInferrers.noOp();
    private TitleGenerator titleGenerator = TitleGenerators.noOp();

    private Builder() {}

    /**
     * Set the specification version. The default is draft-04.
     */
    public Builder withSpecVersion(@Nonnull SpecVersion specVersion) {
      this.specVersion = Objects.requireNonNull(specVersion);
      return this;
    }

    /**
     * Set whether {@code $schema} should be included in the output. It is true by default.
     */
    public Builder includeMetaSchemaUrl(boolean includeMetaSchemaUrl) {
      this.includeMetaSchemaUrl = includeMetaSchemaUrl;
      return this;
    }

    /**
     * Set the {@link AdditionalPropertiesPolicy}. By default it is
     * {@link AdditionalPropertiesPolicies#noOp()}.
     *
     * @see AdditionalPropertiesPolicy
     * @see AdditionalPropertiesPolicies
     */
    public Builder withAdditionalPropertiesPolicy(
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
    public Builder withRequiredPolicy(@Nonnull RequiredPolicy requiredPolicy) {
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
    public Builder withFormatInferrer(@Nonnull FormatInferrer formatInferrer) {
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
    public Builder withTitleGenerator(@Nonnull TitleGenerator titleGenerator) {
      this.titleGenerator = Objects.requireNonNull(titleGenerator);
      return this;
    }

    /**
     * @return the {@link JsonSchemaInferrer} built
     * @throws IllegalArgumentException if the spec version and features don't match up
     */
    public JsonSchemaInferrer build() {
      return new JsonSchemaInferrer(specVersion, includeMetaSchemaUrl, additionalPropertiesPolicy,
          requiredPolicy, formatInferrer, titleGenerator);
    }

  }

}
