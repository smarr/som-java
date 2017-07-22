/**
 * Copyright (c) 2017 Michael Haupt, github@haupz.de
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

package som.compiler;

import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SInvokable;
import som.vmobjects.SMethod;
import som.vmobjects.SSymbol;

import static som.interpreter.Bytecodes.*;


public class Disassembler {

  public static void dump(final SClass cl) {
    for (int i = 0; i < cl.getNumberOfInstanceInvokables(); i++) {
      SInvokable inv = cl.getInstanceInvokable(i);

      // output header and skip if the Invokable is a Primitive
      Universe.errorPrint(cl.getName().toString() + ">>"
          + inv.getSignature().toString() + " = ");

      if (inv.isPrimitive()) {
        Universe.errorPrintln("<primitive>");
        continue;
      }
      // output actual method
      dumpMethod((SMethod) inv, "\t");
    }
  }

  public static void dumpMethod(final SMethod m, final String indent) {
    Universe.errorPrintln("(");

    // output stack information
    Universe.errorPrintln(indent + "<" + m.getNumberOfLocals() + " locals, "
        + m.getMaximumNumberOfStackElements() + " stack, "
        + m.getNumberOfBytecodes() + " bc_count>");

    // output bytecodes
    for (int b = 0; b < m.getNumberOfBytecodes(); b +=
        getBytecodeLength(m.getBytecode(b))) {

      Universe.errorPrint(indent);

      // bytecode index
      if (b < 10) {
        Universe.errorPrint(" ");
      }
      if (b < 100) {
        Universe.errorPrint(" ");
      }
      Universe.errorPrint(" " + b + ":");

      // mnemonic
      byte bytecode = m.getBytecode(b);
      Universe.errorPrint(getPaddedBytecodeName(bytecode) + "  ");

      // parameters (if any)
      if (getBytecodeLength(bytecode) == 1) {
        Universe.errorPrintln();
        continue;
      }
      switch (bytecode) {
        case PUSH_LOCAL:
          Universe.errorPrintln("local: " + m.getBytecode(b + 1) + ", context: "
              + m.getBytecode(b + 2));
          break;
        case PUSH_ARGUMENT:
          Universe.errorPrintln("argument: " + m.getBytecode(b + 1) + ", context "
              + m.getBytecode(b + 2));
          break;
        case PUSH_FIELD: {
          int idx = m.getBytecode(b + 1);
          String fieldName = ((SSymbol) m.getHolder().getInstanceFields()
                                         .getIndexableField(idx)).getEmbeddedString();
          Universe.errorPrintln("(index: " + idx + ") field: " + fieldName);
          break;
        }
        case PUSH_BLOCK:
          Universe.errorPrint("block: (index: " + m.getBytecode(b + 1) + ") ");
          dumpMethod((SMethod) m.getConstant(b), indent + "\t");
          break;
        case PUSH_CONSTANT:
          SAbstractObject constant = m.getConstant(b);
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1) + ") value: "
              + "(" + constant.getSOMClass(Universe.current()).getName().toString() + ") "
              + constant.toString());
          break;
        case PUSH_GLOBAL:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1) + ") value: "
              + ((SSymbol) m.getConstant(b)).toString());
          break;
        case POP_LOCAL:
          Universe.errorPrintln("local: " + m.getBytecode(b + 1) + ", context: "
              + m.getBytecode(b + 2));
          break;
        case POP_ARGUMENT:
          Universe.errorPrintln("argument: " + m.getBytecode(b + 1)
              + ", context: " + m.getBytecode(b + 2));
          break;
        case POP_FIELD: {
          int idx = m.getBytecode(b + 1);
          String fieldName = ((SSymbol) m.getHolder().getInstanceFields()
                                         .getIndexableField(idx)).getEmbeddedString();
          Universe.errorPrintln("(index: " + idx + ") field: " + fieldName);
          break;
        }
        case SEND:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1)
              + ") signature: " + ((SSymbol) m.getConstant(b)).toString());
          break;
        case SUPER_SEND:
          Universe.errorPrintln("(index: " + m.getBytecode(b + 1)
              + ") signature: " + ((SSymbol) m.getConstant(b)).toString());
          break;
        default:
          Universe.errorPrintln("<incorrect bytecode>");
      }
    }
    Universe.errorPrintln(indent + ")");
  }

}
