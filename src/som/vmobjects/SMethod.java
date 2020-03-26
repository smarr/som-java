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

import java.util.List;

import som.interpreter.Frame;
import som.interpreter.Interpreter;
import som.vm.Universe;


public class SMethod extends SAbstractObject implements SInvokable {

  public SMethod(SObject nilObject, SSymbol signature, int numberOfBytecodes,
      SInteger numberOfLocals, SInteger maxNumStackElements,
      int numberOfLiterals, List<SAbstractObject> literals) {
    this.signature = signature;
    this.numberOfLocals = numberOfLocals;
    this.bytecodes = new byte[numberOfBytecodes];
    inlineCacheClass = new SClass[numberOfBytecodes];
    inlineCacheInvokable = new SInvokable[numberOfBytecodes];
    maximumNumberOfStackElements = maxNumStackElements;
    this.literals = new SAbstractObject[numberOfLiterals];

    // copy literals into the method
    if (numberOfLiterals > 0) {
      int i = 0;
      for (SAbstractObject l : literals) {
        this.literals[i++] = l;
      }
    }
  }

  @Override
  public boolean isPrimitive() {
    return false;
  }

  public SInteger getNumberOfLocals() {
    // Get the number of locals (converted to a Java integer)
    return numberOfLocals;
  }

  public SInteger getMaximumNumberOfStackElements() {
    // Get the maximum number of stack elements (converted to a Java
    // integer)
    return maximumNumberOfStackElements;
  }

  @Override
  public SSymbol getSignature() {
    return signature;
  }

  @Override
  public SClass getHolder() {
    // Get the holder of this method by reading the field with holder index
    return holder;
  }

  @Override
  public void setHolder(final SClass value) {
    holder = value;

    // Make sure all nested invokables have the same holder
    for (int i = 0; i < literals.length; i++) {
      if (literals[i] instanceof SInvokable) {
        ((SInvokable) literals[i]).setHolder(value);
      }
    }
  }

  public SAbstractObject getConstant(final int bytecodeIndex) {
    // Get the constant associated to a given bytecode index
    return literals[getBytecode(bytecodeIndex + 1)];
  }

  public int getNumberOfArguments() {
    // Get the number of arguments of this method
    return getSignature().getNumberOfSignatureArguments();
  }

  public int getNumberOfBytecodes() {
    // Get the number of bytecodes in this method
    return bytecodes.length;
  }

  public byte getBytecode(final int index) {
    // Get the bytecode at the given index
    return bytecodes[index];
  }

  public void setBytecode(final int index, final byte value) {
    // Set the bytecode at the given index to the given value
    bytecodes[index] = value;
  }

  @Override
  public void invoke(final Frame frame, final Interpreter interpreter) {
    // Allocate and push a new frame on the interpreter stack
    Frame newFrame = interpreter.pushNewFrame(this);
    newFrame.copyArgumentsFrom(frame);
  }

  @Override
  public String toString() {
    return "Method(" + getHolder().getName().getEmbeddedString() + ">>"
        + getSignature().toString() + ")";
  }

  public SClass getInlineCacheClass(final int bytecodeIndex) {
    return inlineCacheClass[bytecodeIndex];
  }

  public SInvokable getInlineCacheInvokable(final int bytecodeIndex) {
    return inlineCacheInvokable[bytecodeIndex];
  }

  public void setInlineCache(final int bytecodeIndex, final SClass receiverClass, final SInvokable invokable) {
    inlineCacheClass[bytecodeIndex] = receiverClass;
    inlineCacheInvokable[bytecodeIndex] = invokable;
  }

  @Override
  public SClass getSOMClass(final Universe universe) {
    return universe.methodClass;
  }

  // Private variable holding byte array of bytecodes
  private final byte[]       bytecodes;
  private final SClass[]     inlineCacheClass;
  private final SInvokable[] inlineCacheInvokable;

  private final SAbstractObject[] literals;

  private final SSymbol signature;
  private SClass        holder;

  // Meta information
  private final SInteger numberOfLocals;
  private final SInteger maximumNumberOfStackElements;
}
