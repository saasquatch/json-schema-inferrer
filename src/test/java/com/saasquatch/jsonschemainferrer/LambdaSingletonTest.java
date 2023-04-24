package com.saasquatch.jsonschemainferrer;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

public class LambdaSingletonTest {

  @Test
  public void test() {
    doTest(IntegerTypeCriteria::nonFloatingPoint);
    doTest(IntegerTypeCriteria::mathematicalInteger);
    doTest(AdditionalPropertiesPolicies::noOp);
    doTest(AdditionalPropertiesPolicies::allowed);
    doTest(AdditionalPropertiesPolicies::notAllowed);
    doTest(AdditionalPropertiesPolicies::existingTypes);
    doTest(FormatInferrers::noOp);
    doTest(FormatInferrers::dateTime);
    doTest(RequiredPolicies::noOp);
    doTest(RequiredPolicies::commonFields);
    doTest(RequiredPolicies::nonNullCommonFields);
    doTest(TitleDescriptionGenerators::noOp);
    doTest(TitleDescriptionGenerators::useFieldNamesAsTitles);
    doTest(DefaultPolicies::noOp);
    doTest(DefaultPolicies::useFirstSamples);
    doTest(DefaultPolicies::useLastSamples);
    doTest(ExamplesPolicies::noOp);
    doTest(MultipleOfPolicies::noOp);
    doTest(MultipleOfPolicies::gcd);
    doTest(EnumExtractors::noOp);
  }

  private static void doTest(Supplier<?> supplier) {
    assertSame(supplier.get(), supplier.get());
  }

}
