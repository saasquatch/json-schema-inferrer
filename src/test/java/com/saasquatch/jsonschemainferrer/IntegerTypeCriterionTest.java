package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class IntegerTypeCriterionTest {

  @Test
  public void testNonFloatingPoint() {
    final IntegerTypeCriterion cr = IntegerTypeCriterion.NON_FLOATING_POINT;
    assertTrue(cr.isInteger(jnf.numberNode(1)));
    assertTrue(cr.isInteger(jnf.numberNode(1L)));
    assertFalse(cr.isInteger(jnf.numberNode(BigDecimal.valueOf(0L))));
    assertTrue(cr.isInteger(jnf.numberNode(BigInteger.valueOf(1L))));
    assertFalse(cr.isInteger(jnf.numberNode(1.0f)));
    assertFalse(cr.isInteger(jnf.numberNode(1.0)));
    assertFalse(cr.isInteger(jnf.numberNode(BigDecimal.valueOf(1.0))));
    assertFalse(cr.isInteger(jnf.numberNode(1.5f)));
    assertFalse(cr.isInteger(jnf.numberNode(1.5)));
    assertFalse(cr.isInteger(jnf.numberNode(BigDecimal.valueOf(1.5))));
    assertFalse(cr.isInteger(jnf.numberNode(Float.NaN)));
    assertFalse(cr.isInteger(jnf.numberNode(Double.NaN)));
    assertFalse(cr.isInteger(jnf.numberNode(Float.NEGATIVE_INFINITY)));
    assertFalse(cr.isInteger(jnf.numberNode(Double.NEGATIVE_INFINITY)));
  }

  @Test
  public void testMathematicalInteger() {
    final IntegerTypeCriterion cr = IntegerTypeCriterion.MATHEMATICAL_INTEGER;
    assertTrue(cr.isInteger(jnf.numberNode(1)));
    assertTrue(cr.isInteger(jnf.numberNode(1L)));
    assertTrue(cr.isInteger(jnf.numberNode(BigDecimal.valueOf(0L))));
    assertTrue(cr.isInteger(jnf.numberNode(BigInteger.valueOf(1L))));
    assertTrue(cr.isInteger(jnf.numberNode(1.0f)));
    assertTrue(cr.isInteger(jnf.numberNode(1.0)));
    assertTrue(cr.isInteger(jnf.numberNode(BigDecimal.valueOf(1.0))));
    assertFalse(cr.isInteger(jnf.numberNode(1.5f)));
    assertFalse(cr.isInteger(jnf.numberNode(1.5)));
    assertFalse(cr.isInteger(jnf.numberNode(BigDecimal.valueOf(1.5))));
    assertFalse(cr.isInteger(jnf.numberNode(Float.NaN)));
    assertFalse(cr.isInteger(jnf.numberNode(Double.NaN)));
    assertFalse(cr.isInteger(jnf.numberNode(Float.NEGATIVE_INFINITY)));
    assertFalse(cr.isInteger(jnf.numberNode(Double.NEGATIVE_INFINITY)));
  }

}
