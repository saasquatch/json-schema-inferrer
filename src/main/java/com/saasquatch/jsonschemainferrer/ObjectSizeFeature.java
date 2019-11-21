package com.saasquatch.jsonschemainferrer;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Features for object size validation
 *
 * @author sli
 */
public enum ObjectSizeFeature {

  /**
   * {@code minProperties}
   */
  MIN_PROPERTIES {
    @Override
    void process(ObjectNode schema, Collection<ObjectNode> samples) {
      samples.stream().mapToInt(JsonNode::size).min()
          .ifPresent(minProps -> schema.put(Consts.Fields.MIN_PROPERTIES, minProps));
    }
  },
  /**
   * {@code maxProperties}
   */
  MAX_PROPERTIES {
    @Override
    void process(ObjectNode schema, Collection<ObjectNode> samples) {
      samples.stream().mapToInt(JsonNode::size).max()
          .ifPresent(maxProps -> schema.put(Consts.Fields.MAX_PROPERTIES, maxProps));
    }
  },;

  abstract void process(@Nonnull ObjectNode schema, @Nonnull Collection<ObjectNode> samples);

}
