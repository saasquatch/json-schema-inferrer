package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.escapeSingleQuoteString;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getBase64Length;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getCommonFieldNames;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.getSerializedTextLength;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isNull;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnum;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnumIgnoreCase;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.numberNode;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stringColToArrayDistinct;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class JunkDrawerTest {

  @Test
  public void testStringColToArrayDistinct() {
    final List<String> stringCol = Arrays.asList("a", "a", "b");
    final ArrayNode arrayNode = stringColToArrayDistinct(stringCol);
    assertEquals(2, arrayNode.size());
  }

  @Test
  public void testGetCommonFieldNames() {
    assertTrue(getCommonFieldNames(Collections.emptyList(), false).isEmpty());
    assertTrue(getCommonFieldNames(Arrays.asList(jnf.objectNode(), jnf.objectNode().put("a", "a"),
        jnf.objectNode().put("b", "b")), false).isEmpty());
    assertEquals(Collections.emptySet(), getCommonFieldNames(
        Arrays.asList(jnf.objectNode().put("a", "a"), jnf.objectNode().put("b", "b")), false));
    assertEquals(Collections.singleton("a"),
        getCommonFieldNames(Arrays.asList(jnf.objectNode().put("a", 1).put("b", 2),
            jnf.objectNode().put("a", 1).put("b", (String) null)), true));
    {
      final TextNode nullTextNode = new TextNode(null);
      final ObjectNode j1 = jnf.objectNode();
      j1.set("a", nullTextNode);
      assertEquals(Collections.emptySet(),
          getCommonFieldNames(Arrays.asList(j1, jnf.objectNode().put("a", "a")), true));
    }
  }

  @Test
  public void testValidEnum() {
    assertTrue(isValidEnum(TimeUnit.class, "SECONDS"));
    assertFalse(isValidEnum(TimeUnit.class, "seconds"));
    assertTrue(isValidEnumIgnoreCase(TimeUnit.class, "seconds"));
    assertFalse(isValidEnumIgnoreCase(TimeUnit.class, "second"));
    assertFalse(isValidEnum(TimeUnit.class, null));
    assertFalse(isValidEnumIgnoreCase(TimeUnit.class, null));
  }

  @Test
  public void testNumberNode() {
    {
      final int num = 1;
      final NumericNode numberNode = jnf.numberNode(num);
      assertEquals(numberNode, numberNode(BigInteger.valueOf(num)));
    }
    {
      final long num = 2L * Integer.MAX_VALUE;
      final NumericNode numberNode = jnf.numberNode(num);
      assertEquals(numberNode, numberNode(BigInteger.valueOf(num)));
    }
    {
      final BigInteger num = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2));
      final ValueNode numberNode = jnf.numberNode(num);
      assertEquals(numberNode, numberNode(num));
    }
  }

  @Test
  public void testBse64Length() {
    for (int i = 0; i < 1024; i++) {
      final byte[] bytes = new byte[i];
      ThreadLocalRandom.current().nextBytes(bytes);
      assertEquals(getBase64Length(i), Base64.getEncoder().encodeToString(bytes).length());
      assertEquals(getBase64Length(i), jnf.binaryNode(bytes).asText().length());
    }
  }

  @Test
  public void testSerializedTextLength() {
    assertEquals(8, getSerializedTextLength(jnf.binaryNode(new byte[4])));
    assertEquals(-1, getSerializedTextLength(jnf.numberNode(1)));
    assertEquals(3, getSerializedTextLength(jnf.numberNode(Float.NaN)));
    assertEquals("infinity".length(),
        getSerializedTextLength(jnf.numberNode(Float.POSITIVE_INFINITY)));
    assertEquals("-infinity".length(),
        getSerializedTextLength(jnf.numberNode(Float.NEGATIVE_INFINITY)));
    assertEquals(3, getSerializedTextLength(jnf.numberNode(Double.NaN)));
    assertEquals("infinity".length(),
        getSerializedTextLength(jnf.numberNode(Double.POSITIVE_INFINITY)));
    assertEquals("-infinity".length(),
        getSerializedTextLength(jnf.numberNode(Double.NEGATIVE_INFINITY)));
    assertEquals(-1, getSerializedTextLength(jnf.objectNode().put("1", "1")));
    assertEquals(0, getSerializedTextLength(jnf.textNode("")));
    assertEquals(1, getSerializedTextLength(jnf.textNode("😂")));
  }

  @Test
  public void testIsNull() {
    assertTrue(isNull(null));
    assertTrue(isNull(jnf.nullNode()));
    assertTrue(isNull(jnf.missingNode()));
    assertTrue(isNull(new TextNode(null)));
    assertFalse(isNull(new TextNode("")));
    assertTrue(isNull(new BinaryNode(null)));
    assertFalse(isNull(new BinaryNode(new byte[0])));
    assertTrue(isNull(new BigIntegerNode(null)));
    assertFalse(isNull(new BigIntegerNode(BigInteger.ZERO)));
    assertTrue(isNull(new DecimalNode(null)));
    assertFalse(isNull(new DecimalNode(BigDecimal.ZERO)));
    assertTrue(isNull(new POJONode(null)));
    assertFalse(isNull(new POJONode(0)));
  }

  @Test
  public void testEscapeSingleQuoteString() {
    assertEquals("", escapeSingleQuoteString(""));
    // a\bb\fc\nd\re\tf\'"\\\r\n\n\rg
    assertEquals(" a\\bb\\fc\\nd\\re\\tf\\'\"\\\\\\r\\n\\n\\rg",
        escapeSingleQuoteString(" a\bb\fc\nd\re\tf'\"\\\r\n\n\rg"));
  }

}
