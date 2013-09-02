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

package som.compiler;

import som.interpreter.Bytecodes;
import som.vmobjects.Class;
import som.vmobjects.Invokable;
import som.vmobjects.Method;
import som.vmobjects.Object;
import som.vmobjects.Symbol;

public class Disassembler {

  public static void dump(Class cl) {
    for (int i = 0; i < cl.getNumberOfInstanceInvokables(); i++) {
      Invokable inv = cl.getInstanceInvokable(i);
      // Checkstyle: stop
      // output header and skip if the Invokable is a Primitive
      System.err.print(cl.getName().toString() + ">>"
          + inv.getSignature().toString() + " = ");
      // Checkstyle: resume
      if (inv.isPrimitive()) {
        // Checkstyle: stop
        System.err.println("<primitive>");
        // Checkstyle: resume
        continue;
      }
      // output actual method
      dumpMethod((Method) inv, "\t");
    }
  }

  public static void dumpMethod(Method m, java.lang.String indent) {
    // Checkstyle: stop
    System.err.println("(");
    // Checkstyle: resume
    // output stack information
    // Checkstyle: stop
    System.err.println(indent + "<" + m.getNumberOfLocals() + " locals, "
        + m.getMaximumNumberOfStackElements() + " stack, "
        + m.getNumberOfBytecodes() + " bc_count>");
    // Checkstyle: resume
    // output bytecodes
    for (int b = 0;
         b < m.getNumberOfBytecodes();
         b += Bytecodes.getBytecodeLength(m.getBytecode(b))) {
      // Checkstyle: stop
      System.err.print(indent);
      // Checkstyle: resume

      // bytecode index
      // Checkstyle: stop
      if (b < 10) System.err.print(' ');
      if (b < 100) System.err.print(' ');
      System.err.print(" " + b + ":");
      // Checkstyle: resume

      // mnemonic
      byte bytecode = m.getBytecode(b);
      // Checkstyle: stop
      System.err.print(Bytecodes.bytecodeNames[bytecode] + "  ");
      // Checkstyle: resume
      // parameters (if any)
      if (Bytecodes.getBytecodeLength(bytecode) == 1) {
        // Checkstyle: stop
        System.err.println();
        // Checkstyle: resume
        continue;
      }
      switch (bytecode) {
        case Bytecodes.push_local:
          // Checkstyle: stop
          System.err.println("local: " + m.getBytecode(b + 1) + ", context: "
              + m.getBytecode(b + 2));
          // Checkstyle: resume
          break;
        case Bytecodes.push_argument:
          // Checkstyle: stop
          System.err.println("argument: " + m.getBytecode(b + 1) + ", context "
              + m.getBytecode(b + 2));
          // Checkstyle: resume
          break;
        case Bytecodes.push_field:
          // Checkstyle: stop
          System.err.println("(index: " + m.getBytecode(b + 1) + ") field: "
              + ((Symbol) m.getConstant(b)).toString());
          // Checkstyle: resume
          break;
        case Bytecodes.push_block:
          // Checkstyle: stop
          System.err.print("block: (index: " + m.getBytecode(b + 1) + ") ");
          // Checkstyle: resume
          dumpMethod((Method) m.getConstant(b), indent + "\t");
          break;
        case Bytecodes.push_constant:
          Object constant = m.getConstant(b);
          System.err.println("(index: " + m.getBytecode(b + 1) + ") value: "
              + "(" + constant.getSOMClass().getName().toString() + ") "
              + constant.toString());
          break;
        case Bytecodes.push_global:
          System.err.println("(index: " + m.getBytecode(b + 1) + ") value: "
              + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.pop_local:
          System.err.println("local: " + m.getBytecode(b + 1) + ", context: "
              + m.getBytecode(b + 2));
          break;
        case Bytecodes.pop_argument:
          System.err.println("argument: " + m.getBytecode(b + 1)
              + ", context: " + m.getBytecode(b + 2));
          break;
        case Bytecodes.pop_field:
          System.err.println("(index: " + m.getBytecode(b + 1) + ") field: "
              + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.send:
          System.err.println("(index: " + m.getBytecode(b + 1)
              + ") signature: " + ((Symbol) m.getConstant(b)).toString());
          break;
        case Bytecodes.super_send:
          System.err.println("(index: " + m.getBytecode(b + 1)
              + ") signature: " + ((Symbol) m.getConstant(b)).toString());
          break;
        default:
          System.err.println("<incorrect bytecode>");
      }
    }
    System.err.println(indent + ")");
  }

}

