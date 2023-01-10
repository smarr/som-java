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
import som.vmobjects.SPrimitive;
import som.vmobjects.SString;
import som.vmobjects.SSymbol;


public class SymbolPrimitives extends Primitives {

  public SymbolPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("asString", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SSymbol self = (SSymbol) frame.pop();
        frame.push(universe.newString(self.getEmbeddedString()));
      }
    });

    installInstancePrimitive(new SPrimitive("=", universe) {

      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        SAbstractObject op1 = frame.pop();
        SSymbol op2 = (SSymbol) frame.pop(); // self
        if (op1 == op2) {
          frame.push(universe.trueObject);
          return;
        }

        if (op1 instanceof SString) {
          SString s = (SString) op1;
          if (s.getEmbeddedString().equals(op2.getEmbeddedString())) {
            frame.push(universe.trueObject);
            return;
          }
        }
        frame.push(universe.falseObject);
      }
    }, true);
  }
}
