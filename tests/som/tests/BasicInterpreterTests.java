/**
 * Copyright (c) 2013 Stefan Marr, stefan.marr@vub.ac.be
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package som.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import som.compiler.ProgramDefinitionError;
import som.vm.Universe;
import som.vmobjects.SArray;
import som.vmobjects.SClass;
import som.vmobjects.SDouble;
import som.vmobjects.SInteger;
import som.vmobjects.SSymbol;


@RunWith(Parameterized.class)
public class BasicInterpreterTests {

  @Parameters(name = "{0}.{1} [{index}]")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        // {"Self", "assignSuper", 42, ProgramDefinitionError.class},

        {"MethodCall", "test", 42, SInteger.class},
        {"MethodCall", "test2", 42, SInteger.class},

        {"NonLocalReturn", "test1", 42, SInteger.class},
        {"NonLocalReturn", "test2", 43, SInteger.class},
        {"NonLocalReturn", "test3", 3, SInteger.class},
        {"NonLocalReturn", "test4", 42, SInteger.class},
        {"NonLocalReturn", "test5", 22, SInteger.class},

        {"Blocks", "testArg1", 42, SInteger.class},
        {"Blocks", "testArg2", 77, SInteger.class},
        {"Blocks", "testArgAndLocal", 8, SInteger.class},
        {"Blocks", "testArgAndContext", 8, SInteger.class},
        {"Blocks", "testEmptyZeroArg", 1, SInteger.class},
        {"Blocks", "testEmptyOneArg", 1, SInteger.class},
        {"Blocks", "testEmptyTwoArg", 1, SInteger.class},

        {"Return", "testReturnSelf", "Return", SClass.class},
        {"Return", "testReturnSelfImplicitly", "Return", SClass.class},
        {"Return", "testNoReturnReturnsSelf", "Return", SClass.class},
        {"Return", "testBlockReturnsImplicitlyLastValue", 4, SInteger.class},

        {"IfTrueIfFalse", "test", 42, SInteger.class},
        {"IfTrueIfFalse", "test2", 33, SInteger.class},
        {"IfTrueIfFalse", "test3", 4, SInteger.class},

        {"IfTrueIfFalse", "testIfTrueTrueResult", "Integer", SClass.class},
        {"IfTrueIfFalse", "testIfTrueFalseResult", "Nil", SClass.class},
        {"IfTrueIfFalse", "testIfFalseTrueResult", "Nil", SClass.class},
        {"IfTrueIfFalse", "testIfFalseFalseResult", "Integer", SClass.class},

        {"CompilerSimplification", "testReturnConstantSymbol", "constant", SSymbol.class},
        {"CompilerSimplification", "testReturnConstantInt", 42, SInteger.class},
        {"CompilerSimplification", "testReturnSelf", "CompilerSimplification", SClass.class},
        {"CompilerSimplification", "testReturnSelfImplicitly", "CompilerSimplification",
            SClass.class},
        {"CompilerSimplification", "testReturnArgumentN", 55, SInteger.class},
        {"CompilerSimplification", "testReturnArgumentA", 44, SInteger.class},
        {"CompilerSimplification", "testSetField", "foo", SSymbol.class},
        {"CompilerSimplification", "testGetField", 40, SInteger.class},

        {"Hash", "testHash", 444, SInteger.class},

        {"Arrays", "testEmptyToInts", 3, SInteger.class},
        {"Arrays", "testPutAllInt", 5, SInteger.class},
        {"Arrays", "testPutAllNil", "Nil", SClass.class},
        {"Arrays", "testPutAllBlock", 3, SInteger.class},
        {"Arrays", "testNewWithAll", 1, SInteger.class},
        {"Arrays", "testAtPut", 1, SInteger.class},
        {"Arrays", "testPutDifferentTypes", "[a True]", SArray.class},
        {"Arrays", "testFirstLast", 1, SInteger.class},
        {"Arrays", "testEmpty", 0, SInteger.class},
        {"Arrays", "testAccessEmpty", "Nil", SClass.class},
        {"Arrays", "testEmptyToFull", 1, SInteger.class},

        {"BlockInlining", "testNoInlining", 1, SInteger.class},
        {"BlockInlining", "testOneLevelInlining", 1, SInteger.class},
        {"BlockInlining", "testOneLevelInliningWithLocalShadowTrue", 2, SInteger.class},
        {"BlockInlining", "testOneLevelInliningWithLocalShadowFalse", 1, SInteger.class},

        {"BlockInlining", "testShadowDoesntStoreWrongLocal", 33, SInteger.class},
        {"BlockInlining", "testShadowDoesntReadUnrelated", "Nil", SClass.class},

        {"BlockInlining", "testBlockNestedInIfTrue", 2, SInteger.class},
        {"BlockInlining", "testBlockNestedInIfFalse", 42, SInteger.class},

        {"BlockInlining", "testStackDisciplineTrue", 1, SInteger.class},
        {"BlockInlining", "testStackDisciplineFalse", 2, SInteger.class},

        {"BlockInlining", "testDeepNestedInlinedIfTrue", 3, SInteger.class},
        {"BlockInlining", "testDeepNestedInlinedIfFalse", 42, SInteger.class},

        {"BlockInlining", "testDeepNestedBlocksInInlinedIfTrue", 5, SInteger.class},
        {"BlockInlining", "testDeepNestedBlocksInInlinedIfFalse", 43, SInteger.class},

        {"BlockInlining", "testDeepDeepNestedTrue", 9, SInteger.class},
        {"BlockInlining", "testDeepDeepNestedFalse", 43, SInteger.class},

        {"BlockInlining", "testToDoNestDoNestIfTrue", 2, SInteger.class},

        {"NonLocalVars", "testWriteDifferentTypes", 3.75, SDouble.class},

        {"ObjectCreation", "test", 1000000, SInteger.class},

        {"Regressions", "testSymbolEquality", 1, SInteger.class},
        {"Regressions", "testSymbolReferenceEquality", 1, SInteger.class},
        {"Regressions", "testUninitializedLocal", 1, SInteger.class},
        {"Regressions", "testUninitializedLocalInBlock", 1, SInteger.class},

        {"BinaryOperation", "test", 3 + 8, SInteger.class},

        {"NumberOfTests", "numberOfTests", 65, SInteger.class}
    });
  }

  private final String   testClass;
  private final String   testSelector;
  private final Object   expectedResult;
  private final Class<?> resultType;

  public BasicInterpreterTests(final String testClass,
      final String testSelector,
      final Object expectedResult,
      final Class<?> resultType) {
    this.testClass = testClass;
    this.testSelector = testSelector;
    this.expectedResult = expectedResult;
    this.resultType = resultType;
  }

  protected void assertExpectedEqualsSOMValue(final Object actualResult) {
    if (resultType == SInteger.class) {
      long expected = (int) expectedResult;
      long actual = ((SInteger) actualResult).getEmbeddedInteger();
      assertEquals(expected, actual);
      return;
    }

    if (resultType == SDouble.class) {
      double expected = (double) expectedResult;
      double actual = ((SDouble) actualResult).getEmbeddedDouble();
      assertEquals(expected, actual, 1e-15);
      return;
    }

    if (resultType == SClass.class) {
      String expected = (String) expectedResult;
      String actual = ((SClass) actualResult).getName().getEmbeddedString();
      assertEquals(expected, actual);
      return;
    }

    if (resultType == SSymbol.class) {
      String expected = (String) expectedResult;
      String actual = ((SSymbol) actualResult).getEmbeddedString();
      assertEquals(expected, actual);
      return;
    }

    if (resultType == SArray.class) {
      String expected = (String) expectedResult;
      final SArray theArray = (SArray) actualResult;
      final int numberOfIndexableFields = theArray.getNumberOfIndexableFields();
      String[] actual = new String[numberOfIndexableFields];

      for (int i = 0; i < numberOfIndexableFields; i++) {
        actual[i] = theArray.getIndexableField(i).toString();
      }

      assertEquals(expected, Arrays.toString(actual));
      return;
    }

    fail("SOM Value handler missing for " + resultType);
  }

  @Test
  public void testBasicInterpreterBehavior() throws ProgramDefinitionError {
    Universe u = new Universe(true);
    u.setupClassPath("Smalltalk:TestSuite/BasicInterpreterTests");

    try {
      Object actualResult = u.interpret(testClass, testSelector);
      assertExpectedEqualsSOMValue(actualResult);
    } catch (ProgramDefinitionError e) {
      if (resultType != ProgramDefinitionError.class) {
        throw e;
      }
    }
  }
}
