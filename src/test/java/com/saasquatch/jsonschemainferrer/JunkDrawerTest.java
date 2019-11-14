package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.getCommonFieldNames;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.stringColToArrayDistinct;
import static com.saasquatch.jsonschemainferrer.JunkDrawer.unrecognizedEnumError;
import static com.saasquatch.jsonschemainferrer.TestJunkDrawer.jnf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JunkDrawerTest {

  @Test
  public void testStringColToArrayDistinct() {
    final List<String> stringCol = Arrays.asList("a", "a", "b");
    final ArrayNode arrayNode = stringColToArrayDistinct(stringCol);
    assertEquals(2, arrayNode.size());
  }

  @Test
  public void testGetCommonFieldNames() {
    assertEquals(0, getCommonFieldNames(Collections.emptyList(), false).size());
    assertEquals(0,
        getCommonFieldNames(Arrays.asList(jnf.objectNode(), jnf.objectNode().put("a", "a")), false)
            .size());
    assertEquals(Collections.singleton("a"),
        getCommonFieldNames(Arrays.asList(jnf.objectNode().put("a", 1).put("b", 2),
            jnf.objectNode().put("a", 1).put("b", (String) null)), true));
  }

  @Test
  public void testUnrecognizedEnum() {
    assertThrows(IllegalStateException.class, () -> unrecognizedEnumError(TimeUnit.DAYS));
  }

}
