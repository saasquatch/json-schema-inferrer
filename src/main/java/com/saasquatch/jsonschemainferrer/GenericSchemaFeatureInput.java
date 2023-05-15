package com.saasquatch.jsonschemainferrer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Input for {@link GenericSchemaFeature}
 *
 * @author sli
 */
public final class GenericSchemaFeatureInput {

  private final ObjectNode schema;
  private final Collection<? extends JsonNode> samples;
  private final String type;
  private final SpecVersion specVersion;
  private final String path;

  GenericSchemaFeatureInput(@Nonnull ObjectNode schema,
      @Nonnull Collection<? extends JsonNode> samples, @Nullable String type,
      @Nonnull SpecVersion specVersion, @Nonnull String path) {
    this.schema = schema;
    this.samples = samples;
    this.type = type;
    this.specVersion = specVersion;
    this.path = path;
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

  /**
   * @return The JSON path of the current traversal.
   */
  @Nonnull
  public String getPath() {
    return path;
  }

}
