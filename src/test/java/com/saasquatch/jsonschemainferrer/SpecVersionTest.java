package com.saasquatch.jsonschemainferrer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class SpecVersionTest {

  @Test
  public void testNonNull() {
    for (SpecVersion specVersion : SpecVersion.values()) {
      assertNotNull(specVersion.getMetaSchemaUrl());
      assertNotNull(specVersion.getMetaSchemaIdentifier());
      assertNotNull(specVersion.getCommonName());
    }
  }

}
