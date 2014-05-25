/**
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
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

package som.primitives;

import som.interpreter.Frame;
import som.interpreter.Interpreter;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SBigInteger;
import som.vmobjects.SDouble;
import som.vmobjects.SInteger;
import som.vmobjects.SPrimitive;
import som.vmobjects.SString;

public class IntegerPrimitives extends Primitives {

  public IntegerPrimitives(final Universe universe) {
    super(universe);
  }

  private void pushLongResult(final Frame frame, long result) {
    // Check with integer bounds and push:
    if (result > java.lang.Integer.MAX_VALUE
        || result < java.lang.Integer.MIN_VALUE) {
      frame.push(universe.newBigInteger(result));
    } else {
      frame.push(universe.newInteger((int) result));
    }
  }

  private void resendAsBigInteger(java.lang.String operator, SInteger left,
      SBigInteger right) {
    // Construct left value as BigInteger:
    SBigInteger leftBigInteger = universe.newBigInteger(
        left.getEmbeddedInteger());

    // Resend message:
    SAbstractObject[] operands = new SAbstractObject[1];
    operands[0] = right;

    leftBigInteger.send(operator, operands, universe, universe.getInterpreter());
  }

  void resendAsDouble(java.lang.String operator, SInteger left, SDouble right) {
    SDouble leftDouble = universe.newDouble(left.getEmbeddedInteger());
    SAbstractObject[] operands = new SAbstractObject[] {right};
    leftDouble.send(operator, operands, universe, universe.getInterpreter());
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("asString", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SInteger self = (SInteger) frame.pop();
        frame.push(universe.newString(
            java.lang.Integer.toString(self.getEmbeddedInteger())));
      }
    });

    installInstancePrimitive(new SPrimitive("sqrt", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SInteger self = (SInteger) frame.pop();

        double result = Math.sqrt(self.getEmbeddedInteger());

        if (result == Math.rint(result)) {
          pushLongResult(frame, (long) result);
        } else {
          frame.push(universe.newDouble(result));
        }
      }
    });

    installInstancePrimitive(new SPrimitive("atRandom", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SInteger self = (SInteger) frame.pop();
        frame.push(universe.newInteger(
            (int) (self.getEmbeddedInteger() * Math.random())));
      }
    });

    installInstancePrimitive(new SPrimitive("+", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("+", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("+", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          long result = ((long) left.getEmbeddedInteger())
              + right.getEmbeddedInteger();
          pushLongResult(frame, result);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("-", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("-", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("-", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          long result = ((long) left.getEmbeddedInteger())
              - right.getEmbeddedInteger();
          pushLongResult(frame, result);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("*", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("*", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("*", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          long result = ((long) left.getEmbeddedInteger())
              * right.getEmbeddedInteger();
          pushLongResult(frame, result);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("//", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        /*
         * Integer op1 = (Integer) frame.pop(); Integer op2 = (Integer)
         * frame.pop();
         * frame.push(universe.new_double((double)op2.get_embedded_integer () /
         * (double)op1.get_embedded_integer()));
         */
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("/", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("/", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          double result = ((double) left.getEmbeddedInteger())
              / right.getEmbeddedInteger();
          frame.push(universe.newDouble(result));
        }
      }
    });

    installInstancePrimitive(new SPrimitive("/", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("/", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("/", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          long result = ((long) left.getEmbeddedInteger())
              / right.getEmbeddedInteger();
          pushLongResult(frame, result);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("%", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("%", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("%", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          long l = left.getEmbeddedInteger();
          long r = right.getEmbeddedInteger();
          long result = l % r;

          if (l > 0 && r < 0) {
            result += r;
          }

          pushLongResult(frame, result);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("&", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("&", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("&", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          long result = ((long) left.getEmbeddedInteger())
              & right.getEmbeddedInteger();
          pushLongResult(frame, result);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger:
          resendAsBigInteger("=", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SInteger) {
          // Second operand was Integer:
          SInteger right = (SInteger) rightObj;

          if (left.getEmbeddedInteger() == right.getEmbeddedInteger()) {
            frame.push(universe.trueObject);
          } else {
            frame.push(universe.falseObject);
          }
        } else if (rightObj instanceof SDouble) {
          // Second operand was Integer:
          SDouble right = (SDouble) rightObj;

          if (left.getEmbeddedInteger() == right.getEmbeddedDouble()) {
            frame.push(universe.trueObject);
          } else {
            frame.push(universe.falseObject);
          }
        } else {
          frame.push(universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("<", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject rightObj = frame.pop();
        SInteger left = (SInteger) frame.pop();

        // Check second parameter type:
        if (rightObj instanceof SBigInteger) {
          // Second operand was BigInteger
          resendAsBigInteger("<", left, (SBigInteger) rightObj);
        } else if (rightObj instanceof SDouble) {
          resendAsDouble("<", left, (SDouble) rightObj);
        } else {
          // Do operation:
          SInteger right = (SInteger) rightObj;

          if (left.getEmbeddedInteger() < right.getEmbeddedInteger()) {
            frame.push(universe.trueObject);
          } else {
            frame.push(universe.falseObject);
          }
        }
      }
    });

    installInstancePrimitive(new SPrimitive("<<", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SInteger right = (SInteger) frame.pop();
        SInteger left  = (SInteger) frame.pop();

        assert  right.getEmbeddedInteger() > 0;

        long result = left.getEmbeddedInteger() << right.getEmbeddedInteger();

        pushLongResult(frame, result);
      }
    });

    installInstancePrimitive(new SPrimitive("bitXor:", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SInteger right = (SInteger) frame.pop();
        SInteger left  = (SInteger) frame.pop();

        int result = left.getEmbeddedInteger() ^ right.getEmbeddedInteger();
        frame.push(universe.newInteger(result));
      }
    });

    installClassPrimitive(new SPrimitive("fromString:", universe) {

      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SString param = (SString) frame.pop();
        frame.pop();

        long result = java.lang.Long.parseLong(param.getEmbeddedString());

        pushLongResult(frame, result);
      }
    });
  }
}
