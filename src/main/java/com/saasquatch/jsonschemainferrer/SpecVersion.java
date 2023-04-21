package com.saasquatch.jsonschemainferrer;

import javax.annotation.Nonnull;

/**
 * Supported JSON Schema specification versions ordered from old to new, which means
 * {@link #compareTo(SpecVersion)} can be used.
 *
 * @author sli
 */
public enum SpecVersion {

  DRAFT_04("http://json-schema.org/draft-04/schema#", "draft-04", "Draft 4"),
  DRAFT_06("http://json-schema.org/draft-06/schema#", "draft-06", "Draft 6"),
  DRAFT_07("http://json-schema.org/draft-07/schema#", "draft-07", "Draft 7"),
  DRAFT_2019_09("https://json-schema.org/draft/2019-09/schema#", "2019-09", "2019-09"),
  DRAFT_2020_12("https://json-schema.org/draft/2020-12/schema#", "2020-12", "2020-12"),
  ;

  @Nonnull
  private final String metaSchemaUrl;
  @Nonnull
  private final String metaSchemaIdentifier;
  @Nonnull
  private final String commonName;

  SpecVersion(@Nonnull String metaSchemaUrl, @Nonnull String metaSchemaIdentifier,
      @Nonnull String commonName) {
    this.metaSchemaUrl = metaSchemaUrl;
    this.metaSchemaIdentifier = metaSchemaIdentifier;
    this.commonName = commonName;
  }

  @Nonnull
  String getMetaSchemaUrl() {
    return metaSchemaUrl;
  }

  @Nonnull
  String getMetaSchemaIdentifier() {
    return metaSchemaIdentifier;
  }

  @Nonnull
  String getCommonName() {
    return commonName;
  }

}
