package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Input for {@link GenericSchemaFeature}
 *
 * @author sli
 */
public final class GenericSchemaFeatureInput {

  private ObjectNode schema;
  private Collection<? extends JsonNode> samples;
  private String type;
  private SpecVersion specVersion;

  GenericSchemaFeatureInput(@Nonnull ObjectNode schema,
      @Nonnull Collection<? extends JsonNode> samples, @Nullable String type,
      @Nonnull SpecVersion specVersion) {
    this.schema = schema;
    this.samples = samples;
    this.type = type;
    this.specVersion = specVersion;
  }

  /**
   * @return The current schema. Note that {@link GenericSchemaFeature}s are not supposed to mutate
   *         the schema.
   */
  @Nonnull
  public ObjectNode getSchema() {
    return schema;
  }

  /**
   * @return The current samples
   */
  @Nonnull
  public Collection<? extends JsonNode> getSamples() {
    return samples;
  }

  /**
   * @return The current type, if available
   */
  @Nullable
  public String getType() {
    return type;
  }

  /**
   * @return The current {@link SpecVersion}
   */
  @Nonnull
  public SpecVersion getSpecVersion() {
    return specVersion;
  }

}
