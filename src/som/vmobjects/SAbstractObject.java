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

import som.interpreter.Interpreter;
import som.vm.Universe;

public class SAbstractObject {

  public SAbstractObject(final SAbstractObject nilObject) {
    // Set the number of fields to the default value
    setNumberOfFieldsAndClear(getDefaultNumberOfFields(), nilObject);
  }

  public SAbstractObject(int numberOfFields, final SAbstractObject nilObject) {
    // Set the number of fields to the given value
    setNumberOfFieldsAndClear(numberOfFields, nilObject);
  }

  public SClass getSOMClass() {
    // Get the class of this object by reading the field with class index
    return clazz;
  }

  public void setClass(SClass value) {
    // Set the class of this object by writing to the field with class index
    clazz = value;
  }

  public SSymbol getFieldName(int index) {
    // Get the name of the field with the given index
    return getSOMClass().getInstanceFieldName(index);
  }

  public int getFieldIndex(SSymbol name) {
    // Get the index for the field with the given name
    return getSOMClass().lookupFieldIndex(name);
  }

  public int getNumberOfFields() {
    // Get the number of fields in this object
    return fields.length;
  }

  public void setNumberOfFieldsAndClear(int value, final SAbstractObject nilObject) {
    // Allocate a new array of fields
    fields = new SAbstractObject[value];

    // Clear each and every field by putting nil into them
    for (int i = 0; i < getNumberOfFields(); i++) {
      setField(i, nilObject);
    }
  }

  public int getDefaultNumberOfFields() {
    // Return the default number of fields in an object
    return numberOfObjectFields;
  }

  public void send(java.lang.String selectorString, SAbstractObject[] arguments,
      final Universe universe, final Interpreter interpreter) {
    // Turn the selector string into a selector
    SSymbol selector = universe.symbolFor(selectorString);

    // Push the receiver onto the stack
    interpreter.getFrame().push(this);

    // Push the arguments onto the stack
    for (SAbstractObject arg : arguments) {
      interpreter.getFrame().push(arg);
    }

    // Lookup the invokable
    SInvokable invokable = getSOMClass().lookupInvokable(selector);

    // Invoke the invokable
    invokable.invoke(interpreter.getFrame(), interpreter);
  }

  public void sendDoesNotUnderstand(final SSymbol selector,
      final Universe universe, final Interpreter interpreter) {
    // Compute the number of arguments
    int numberOfArguments = selector.getNumberOfSignatureArguments();

    SFrame frame = interpreter.getFrame();

    // Allocate an array with enough room to hold all arguments
    SArray argumentsArray = universe.newArray(numberOfArguments);

    // Remove all arguments and put them in the freshly allocated array
    for (int i = numberOfArguments - 1; i >= 0; i--) {
      argumentsArray.setIndexableField(i, frame.pop());
    }

    SAbstractObject[] args = {selector, argumentsArray};
    send("doesNotUnderstand:arguments:", args, universe, interpreter);
  }

  public void sendUnknownGlobal(final SSymbol globalName,
      final Universe universe, final Interpreter interpreter) {
    SAbstractObject[] arguments = {globalName};
    send("unknownGlobal:", arguments, universe, interpreter);
  }

  public void sendEscapedBlock(final SBlock block, final Universe universe,
      final Interpreter interpreter) {
    SAbstractObject[] arguments = {block};
    send("escapedBlock:", arguments, universe, interpreter);
  }

  public SAbstractObject getField(int index) {
    // Get the field with the given index
    return fields[index];
  }

  public void setField(int index, SAbstractObject value) {
    // Set the field with the given index to the given value
    fields[index] = value;
  }

  @Override
  public java.lang.String toString() {
    return "a " + getSOMClass().getName().getString();
  }

  // Private array of fields
  private SAbstractObject[] fields;
  private SClass    clazz;

  // Static field indices and number of object fields
  static final int numberOfObjectFields = 0;
}
