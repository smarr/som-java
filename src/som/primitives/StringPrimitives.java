/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
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
import som.vmobjects.SInteger;
import som.vmobjects.SPrimitive;
import som.vmobjects.SString;


public class StringPrimitives extends Primitives {

  public StringPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("concatenate:", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString argument = (SString) frame.pop();
        SString self = (SString) frame.pop();
        frame.push(universe.newString(self.getEmbeddedString()
            + argument.getEmbeddedString()));
      }
    });

    installInstancePrimitive(new SPrimitive("asSymbol", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString self = (SString) frame.pop();
        frame.push(universe.symbolFor(self.getEmbeddedString()));
      }
    });

    installInstancePrimitive(new SPrimitive("length", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString self = (SString) frame.pop();
        frame.push(universe.newInteger(self.getEmbeddedString().length()));
      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject op1 = frame.pop();
        SString op2 = (SString) frame.pop(); // self
        if (op1.getSOMClass(universe) == universe.stringClass) {
          SString s = (SString) op1;
          if (s.getEmbeddedString().equals(op2.getEmbeddedString())) {
            frame.push(universe.trueObject);
            return;
          }
        }

        frame.push(universe.falseObject);
      }
    });

    installInstancePrimitive(new SPrimitive("primSubstringFrom:to:", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SInteger end = (SInteger) frame.pop();
        SInteger start = (SInteger) frame.pop();

        SString self = (SString) frame.pop();

        try {
          frame.push(universe.newString(self.getEmbeddedString().substring(
              (int) start.getEmbeddedInteger() - 1,
              (int) end.getEmbeddedInteger())));
        } catch (IndexOutOfBoundsException e) {
          frame.push(universe.newString(new java.lang.String(
              "Error - index out of bounds")));
        }
      }
    });

    installInstancePrimitive(new SPrimitive("hashcode", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString self = (SString) frame.pop();
        frame.push(universe.newInteger(self.getEmbeddedString().hashCode()));
      }
    });

    installInstancePrimitive(new SPrimitive("isWhiteSpace", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString self = (SString) frame.pop();
        String embedded = self.getEmbeddedString();

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isWhitespace(embedded.charAt(i))) {
            frame.push(universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {
          frame.push(universe.trueObject);
        } else {
          frame.push(universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("isLetters", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString self = (SString) frame.pop();
        String embedded = self.getEmbeddedString();

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isLetter(embedded.charAt(i))) {
            frame.push(universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {
          frame.push(universe.trueObject);
        } else {
          frame.push(universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("isDigits", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SString self = (SString) frame.pop();
        String embedded = self.getEmbeddedString();

        for (int i = 0; i < embedded.length(); i++) {
          if (!Character.isDigit(embedded.charAt(i))) {
            frame.push(universe.falseObject);
            return;
          }
        }

        if (embedded.length() > 0) {
          frame.push(universe.trueObject);
        } else {
          frame.push(universe.falseObject);
        }
      }
    });
  }
}
