package com.saasquatch.json_schema_inferrer;

import static org.junit.jupiter.api.Assertions.assertSame;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class LambdaSingletonTest {

  @Test
  public void test() {
    doTest(AdditionalPropertiesPolicies::noOp);
    doTest(AdditionalPropertiesPolicies::allowed);
    doTest(AdditionalPropertiesPolicies::notAllowed);
    doTest(AdditionalPropertiesPolicies::existingTypes);
    doTest(FormatInferrers::noOp);
    doTest(FormatInferrers::dateTime);
    doTest(RequiredPolicies::noOp);
    doTest(RequiredPolicies::commonFields);
    doTest(RequiredPolicies::nonNullCommonFields);
    doTest(TitleGenerators::noOp);
    doTest(TitleGenerators::useFieldNames);
  }

  private static void doTest(Supplier<? extends Object> supplier) {
    assertSame(supplier.get(), supplier.get());
  }

}
