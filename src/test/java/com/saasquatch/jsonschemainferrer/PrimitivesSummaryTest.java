package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class PrimitivesSummaryTest {

  @Test
  public void testStringLengthForNonStrings() {
    final PrimitivesSummary primitivesSummary = new PrimitivesSummary();
    primitivesSummary.addSample(jnf.numberNode(1));
    assertFalse(primitivesSummary.getMaxStringLength().isPresent());
    assertFalse(primitivesSummary.getMinStringLength().isPresent());
  }

}
