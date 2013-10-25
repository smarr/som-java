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

import som.interpreter.Interpreter;
import som.vm.Universe;
import som.vmobjects.Array;
import som.vmobjects.Class;
import som.vmobjects.Frame;
import som.vmobjects.Integer;
import som.vmobjects.Invokable;
import som.vmobjects.Object;
import som.vmobjects.Primitive;
import som.vmobjects.Symbol;

public class ObjectPrimitives extends Primitives {

  public ObjectPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {

    installInstancePrimitive(new Primitive("==", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object op1 = frame.pop();
        Object op2 = frame.pop();
        if (op1 == op2) {
          frame.push(universe.trueObject);
        } else {
          frame.push(universe.falseObject);
        }
      }
    });

    installInstancePrimitive(new Primitive("hashcode", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object self = frame.pop();
        frame.push(universe.newInteger(self.hashCode()));
      }
    });

    installInstancePrimitive(new Primitive("objectSize", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object self = frame.pop();
        int size = self.getNumberOfFields();
        if (self instanceof Array) {
          size += ((Array) self).getNumberOfIndexableFields();
        }
        frame.push(universe.newInteger(size));
      }
    });

    installInstancePrimitive(new Primitive("perform:", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object arg  = frame.pop();
        Object self = frame.getStackElement(0);
        Symbol selector = (Symbol) arg;

        Invokable invokable = self.getSOMClass().lookupInvokable(selector);
        invokable.invoke(frame, interpreter);
      }
    });

    installInstancePrimitive(new Primitive("perform:inSuperclass:", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object arg2 = frame.pop();
        Object arg  = frame.pop();
        // Object self = frame.getStackElement(0);

        Symbol selector = (Symbol) arg;
        Class  clazz    = (Class) arg2;

        Invokable invokable = clazz.lookupInvokable(selector);
        invokable.invoke(frame, interpreter);
      }
    });

    installInstancePrimitive(new Primitive("perform:withArguments:", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object arg2 = frame.pop();
        Object arg  = frame.pop();
        Object self = frame.getStackElement(0);

        Symbol selector = (Symbol) arg;
        Array  args     = (Array) arg2;

        for (int i = 0; i < args.getNumberOfIndexableFields(); i++) {
          frame.push(args.getIndexableField(i));
        }

        Invokable invokable = self.getSOMClass().lookupInvokable(selector);
        invokable.invoke(frame, interpreter);
      }
    });

    installInstancePrimitive(new Primitive("instVarAt:", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object arg  = frame.pop();
        Object self = frame.pop();

        Integer idx = (Integer) arg;

        frame.push(self.getField(idx.getEmbeddedInteger() - 1));
      }
    });

    installInstancePrimitive(new Primitive("instVarAt:put:", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object val  = frame.pop();
        Object arg  = frame.pop();
        Object self = frame.getStackElement(0);

        Integer idx = (Integer) arg;

        self.setField(idx.getEmbeddedInteger() - 1, val);
      }
    });

    installInstancePrimitive(new Primitive("class", universe) {
      @Override
      public void invoke(final Frame frame, final Interpreter interpreter) {
        Object self  = frame.pop();
        frame.push(self.getSOMClass());
      }
    });
  }
}
