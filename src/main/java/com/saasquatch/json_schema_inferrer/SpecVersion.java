package com.saasquatch.json_schema_inferrer;

import javax.annotation.Nonnull;

/**
 * Supported JSON Schema specification versions
 *
 * @author sli
 */
public enum SpecVersion {

  DRAFT_04("http://json-schema.org/draft-04/schema#"),
  DRAFT_06("http://json-schema.org/draft-06/schema#"),
  DRAFT_07("http://json-schema.org/draft-07/schema#"),
  ;

  @Nonnull
  final String metaSchemaUrl;

  SpecVersion(String metaSchemaUrl) {
    this.metaSchemaUrl = metaSchemaUrl;
  }

}
