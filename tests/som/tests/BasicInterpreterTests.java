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
import som.vmobjects.SClass;
import som.vmobjects.SDouble;
import som.vmobjects.SInteger;
import som.vmobjects.SSymbol;


@RunWith(Parameterized.class)
public class BasicInterpreterTests {

  @Parameters(name = "{0}.{1} [{index}]")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"Self", "assignSuper", 42, ProgramDefinitionError.class},

        {"MethodCall", "test", 42, SInteger.class},
        {"MethodCall", "test2", 42, SInteger.class},

        {"NonLocalReturn", "test", "NonLocalReturn", SClass.class},
        {"NonLocalReturn", "test1", 42, SInteger.class},
        {"NonLocalReturn", "test2", 43, SInteger.class},
        {"NonLocalReturn", "test3", 3, SInteger.class},
        {"NonLocalReturn", "test4", 42, SInteger.class},
        {"NonLocalReturn", "test5", 22, SInteger.class},

        {"Blocks", "arg1", 42, SInteger.class},
        {"Blocks", "arg2", 77, SInteger.class},
        {"Blocks", "argAndLocal", 8, SInteger.class},
        {"Blocks", "argAndContext", 8, SInteger.class},

        {"Return", "returnSelf", "Return", SClass.class},
        {"Return", "returnSelfImplicitly", "Return", SClass.class},
        {"Return", "noReturnReturnsSelf", "Return", SClass.class},
        {"Return", "blockReturnsImplicitlyLastValue", 4, SInteger.class},

        {"IfTrueIfFalse", "test", 42, SInteger.class},
        {"IfTrueIfFalse", "test2", 33, SInteger.class},
        {"IfTrueIfFalse", "test3", 4, SInteger.class},

        {"CompilerSimplification", "returnConstantSymbol", "constant", SSymbol.class},
        {"CompilerSimplification", "returnConstantInt", 42, SInteger.class},
        {"CompilerSimplification", "returnSelf", "CompilerSimplification", SClass.class},
        {"CompilerSimplification", "returnSelfImplicitly", "CompilerSimplification",
            SClass.class},
        {"CompilerSimplification", "testReturnArgumentN", 55, SInteger.class},
        {"CompilerSimplification", "testReturnArgumentA", 44, SInteger.class},
        {"CompilerSimplification", "testSetField", "foo", SSymbol.class},
        {"CompilerSimplification", "testGetField", 40, SInteger.class},

        {"Arrays", "testEmptyToInts", 3, SInteger.class},
        {"Arrays", "testPutAllInt", 5, SInteger.class},
        {"Arrays", "testPutAllNil", "Nil", SClass.class},
        {"Arrays", "testNewWithAll", 1, SInteger.class},

        {"BlockInlining", "testNoInlining", 1, SInteger.class},
        {"BlockInlining", "testOneLevelInlining", 1, SInteger.class},
        {"BlockInlining", "testOneLevelInliningWithLocalShadowTrue", 2, SInteger.class},
        {"BlockInlining", "testOneLevelInliningWithLocalShadowFalse", 1, SInteger.class},

        {"BlockInlining", "testBlockNestedInIfTrue", 2, SInteger.class},
        {"BlockInlining", "testBlockNestedInIfFalse", 42, SInteger.class},

        {"BlockInlining", "testDeepNestedInlinedIfTrue", 3, SInteger.class},
        {"BlockInlining", "testDeepNestedInlinedIfFalse", 42, SInteger.class},

        {"BlockInlining", "testDeepNestedBlocksInInlinedIfTrue", 5, SInteger.class},
        {"BlockInlining", "testDeepNestedBlocksInInlinedIfFalse", 43, SInteger.class},

        {"BlockInlining", "testDeepDeepNestedTrue", 9, SInteger.class},
        {"BlockInlining", "testDeepDeepNestedFalse", 43, SInteger.class},

        {"BlockInlining", "testToDoNestDoNestIfTrue", 2, SInteger.class},

        {"NonLocalVars", "writeDifferentTypes", 3.75, SDouble.class}
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

  protected void assertEqualsSOMValue(final Object expectedResult, final Object actualResult) {
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
    fail("SOM Value handler missing");
  }

  @Test
  public void testBasicInterpreterBehavior() throws ProgramDefinitionError {
    Universe u = new Universe(true);
    u.setupClassPath("Smalltalk:TestSuite/BasicInterpreterTests");

    try {
      Object actualResult = u.interpret(testClass, testSelector);
      assertEqualsSOMValue(expectedResult, actualResult);
    } catch (ProgramDefinitionError e) {
      if (resultType != ProgramDefinitionError.class) {
        throw e;
      }
    }
  }
}
