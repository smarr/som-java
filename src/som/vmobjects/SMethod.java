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

package som.vmobjects;

import java.util.ArrayList;

import som.interpreter.Bytecodes;
import som.interpreter.Interpreter;

public class SMethod extends SArray implements SInvokable {

  public SMethod(final SAbstractObject nilObject) {
    super(nilObject);
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  public SInteger getNumberOfLocals() {
    // Get the number of locals (converted to a Java integer)
    return numberOfLocals;
  }

  public void setNumberOfLocals(SInteger value) {
    // Set the number of locals
    numberOfLocals = value;
  }

  public SInteger getMaximumNumberOfStackElements() {
    // Get the maximum number of stack elements (converted to a Java
    // integer)
    return maximumNumberOfStackElements;
  }

  public void setMaximumNumberOfStackElements(SInteger value) {
    // Set the maximum number of stack elements
    maximumNumberOfStackElements = value;
  }

  @Override
  public SSymbol getSignature() {
    // Get the signature of this method by reading the field with signature
    // index
    return (SSymbol) getField(signatureIndex);
  }

  public void setSignature(SSymbol value) {
    // Set the signature of this method by writing to the field with
    // signature index
    setField(signatureIndex, value);
  }

  @Override
  public SClass getHolder() {
    // Get the holder of this method by reading the field with holder index
    return (SClass) getField(holderIndex);
  }

  @Override
  public void setHolder(SClass value) {
    // Set the holder of this method by writing to the field with holder index
    setField(holderIndex, value);

    // Make sure all nested invokables have the same holder
    for (int i = 0; i < getNumberOfIndexableFields(); i++) {
      if (getIndexableField(i) instanceof SInvokable) {
        ((SInvokable) getIndexableField(i)).setHolder(value);
      }
    }
  }

  public SAbstractObject getConstant(int bytecodeIndex) {
    // Get the constant associated to a given bytecode index
    return getIndexableField(getBytecode(bytecodeIndex + 1));
  }

  public int getNumberOfArguments() {
    // Get the number of arguments of this method
    return getSignature().getNumberOfSignatureArguments();
  }

  @Override
  public int getDefaultNumberOfFields() {
    // Return the default number of fields in a method
    return numberOfMethodFields;
  }

  public int getNumberOfBytecodes() {
    // Get the number of bytecodes in this method
    return bytecodes.length;
  }

  public void setNumberOfBytecodes(int value) {
    // Set the number of bytecodes in this method
    bytecodes            = new byte[value];
    inlineCacheClass     = new SClass[value];
    inlineCacheInvokable = new SInvokable[value];
  }

  public byte getBytecode(int index) {
    // Get the bytecode at the given index
    return bytecodes[index];
  }

  public void setBytecode(int index, byte value) {
    // Set the bytecode at the given index to the given value
    bytecodes[index] = value;
  }

  public void increaseInvocationCounter() {
    invocationCount++;
  }

  public long getInvocationCount() {
    return invocationCount;
  }

  @Override
  public void invoke(final SFrame frame, final Interpreter interpreter) {
    // Increase the invocation counter
    invocationCount++;
    // Allocate and push a new frame on the interpreter stack
    SFrame newFrame = interpreter.pushNewFrame(this);
    newFrame.copyArgumentsFrom(frame);
  }

  public void replaceBytecodes() {
    byte[] newbc = new byte[bytecodes.length];
    int idx = 0;

    int i = 0;
    while (i < bytecodes.length) {
      byte bc1 = bytecodes[i];
      int len1 = Bytecodes.getBytecodeLength(bc1);

      if (i + len1 >= bytecodes.length) {
        // we're over target, so just copy bc1
        for (int j = i; j < i + len1; ++j) {
          newbc[idx++] = bytecodes[j];
        }
        break;
      }

      newbc[idx++] = bc1;

      // copy args to bc1
      for (int j = i + 1; j < i + len1; ++j) {
        newbc[idx++] = bytecodes[j];
      }

      i += len1; // update i to point on bc2
    }

    // we copy the new array because it may be shorter, and we don't
    // want to upset whatever dependence there is on the length
    bytecodes = new byte[idx];
    for (i = 0; i < idx; ++i) {
      bytecodes[i] = newbc[i];
    }
  }

  public SClass getReceiverClass(byte index) {
    return receiverClassTable.get(index);
  }

  public SInvokable getInvokedMethod(byte index) {
    // return the last invoked method for a particular send
    return invokedMethods.get(index);
  }

  public byte addReceiverClassAndMethod(SClass recClass, SInvokable invokable) {
    receiverClassTable.add(receiverClassIndex, recClass);
    invokedMethods.add(receiverClassIndex, invokable);
    receiverClassIndex++;

    return (byte) (receiverClassIndex - 1);
  }

  public boolean isReceiverClassTableFull() {
    return receiverClassIndex == 255;
  }

  @Override
  public java.lang.String toString() {
    return "Method(" + getHolder().getName().getString() + ">>" + getSignature().toString() + ")";
  }

  public SClass getInlineCacheClass(int bytecodeIndex) {
    return inlineCacheClass[bytecodeIndex];
  }

  public SInvokable getInlineCacheInvokable(int bytecodeIndex) {
    return inlineCacheInvokable[bytecodeIndex];
  }

  public void setInlineCache(int bytecodeIndex, SClass receiverClass, SInvokable invokable) {
    inlineCacheClass[bytecodeIndex]     = receiverClass;
    inlineCacheInvokable[bytecodeIndex] = invokable;
  }


  // Private variables for holding the last receiver class and invoked method
  private final ArrayList<SClass>         receiverClassTable                = new ArrayList<SClass>();
  private final ArrayList<SInvokable>     invokedMethods                    = new ArrayList<SInvokable>();
  private int                            receiverClassIndex                = 0;

  // Private variable holding number of invocations and back edges
  private long                           invocationCount;

  // Private variable holding byte array of bytecodes
  private byte[]                         bytecodes;
  private SClass[]                        inlineCacheClass;
  private SInvokable[]                    inlineCacheInvokable;

  // Meta information
  private SInteger                        numberOfLocals;
  private SInteger                        maximumNumberOfStackElements;

  // Static field indices and number of method fields
  static final int                       signatureIndex               = numberOfObjectFields;
  static final int                       holderIndex                  = 1 + signatureIndex;
  static final int                       numberOfMethodFields         = 1 + holderIndex;

}
