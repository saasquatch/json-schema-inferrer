package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;

public class IntegerTypeCriterionTest {

  @Test
  public void testNonFloatingPoint() {
    final Predicate<JsonNode> cr = j -> IntegerTypeCriteria.nonFloatingPoint()
        .isInteger(new IntegerTypeCriterionInput(j, SpecVersion.DRAFT_06));
    assertFalse(cr.test(jnf.textNode("")));
    assertTrue(cr.test(jnf.numberNode(1)));
    assertTrue(cr.test(jnf.numberNode(1L)));
    assertFalse(cr.test(jnf.numberNode(BigDecimal.valueOf(0L))));
    assertTrue(cr.test(jnf.numberNode(BigInteger.valueOf(1L))));
    assertFalse(cr.test(jnf.numberNode(1.0f)));
    assertFalse(cr.test(jnf.numberNode(1.0)));
    assertFalse(cr.test(jnf.numberNode(BigDecimal.valueOf(1.0))));
    assertFalse(cr.test(jnf.numberNode(1.5f)));
    assertFalse(cr.test(jnf.numberNode(1.5)));
    assertFalse(cr.test(jnf.numberNode(BigDecimal.valueOf(1.5))));
    assertFalse(cr.test(jnf.numberNode(Float.NaN)));
    assertFalse(cr.test(jnf.numberNode(Double.NaN)));
    assertFalse(cr.test(jnf.numberNode(Float.NEGATIVE_INFINITY)));
    assertFalse(cr.test(jnf.numberNode(Double.NEGATIVE_INFINITY)));
  }

  @Test
  public void testMathematicalInteger() {
    final Predicate<JsonNode> cr = j -> IntegerTypeCriteria.mathematicalInteger()
        .isInteger(new IntegerTypeCriterionInput(j, SpecVersion.DRAFT_06));
    assertFalse(cr.test(jnf.textNode("")));
    assertTrue(cr.test(jnf.numberNode(1)));
    assertTrue(cr.test(jnf.numberNode(1L)));
    assertTrue(cr.test(jnf.numberNode(BigDecimal.valueOf(0L))));
    assertTrue(cr.test(jnf.numberNode(BigInteger.valueOf(1L))));
    assertTrue(cr.test(jnf.numberNode(1.0f)));
    assertTrue(cr.test(jnf.numberNode(1.0)));
    assertTrue(cr.test(jnf.numberNode(BigDecimal.valueOf(1.0))));
    assertFalse(cr.test(jnf.numberNode(1.5f)));
    assertFalse(cr.test(jnf.numberNode(1.5)));
    assertFalse(cr.test(jnf.numberNode(BigDecimal.valueOf(1.5))));
    assertFalse(cr.test(jnf.numberNode(Float.NaN)));
    assertFalse(cr.test(jnf.numberNode(Double.NaN)));
    assertFalse(cr.test(jnf.numberNode(Float.NEGATIVE_INFINITY)));
    assertFalse(cr.test(jnf.numberNode(Double.NEGATIVE_INFINITY)));
  }

}
