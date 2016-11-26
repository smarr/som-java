/**
 * Copyright (c) 2016 Michael Haupt, github@haupz.de
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

package som.vm;

import som.compiler.Disassembler;
import som.interpreter.Bytecodes;
import som.interpreter.Frame;
import som.interpreter.Interpreter;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SBigInteger;
import som.vmobjects.SBlock;
import som.vmobjects.SClass;
import som.vmobjects.SDouble;
import som.vmobjects.SInteger;
import som.vmobjects.SInvokable;
import som.vmobjects.SMethod;
import som.vmobjects.SObject;
import som.vmobjects.SString;
import som.vmobjects.SSymbol;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class Universe {

  public static void main(String[] arguments) {
    // Create Universe
    Universe u = new Universe();

    // Start interpretation
    u.interpret(arguments);

    // Exit with error code 0
    u.exit(0);
  }

  public SAbstractObject interpret(String[] arguments) {
    // Check for command line switches
    arguments = handleArguments(arguments);

    // Initialize the known universe
    return initialize(arguments);
  }

  static { /* static initializer */
    pathSeparator = System.getProperty("path.separator");
    fileSeparator = System.getProperty("file.separator");
  }

  public Universe() {
    this.interpreter  = new Interpreter(this);
    this.symbolTable  = new HashMap<String, SSymbol>();
    this.avoidExit    = false;
    this.lastExitCode = 0;

    current = this;
  }

  public Universe(boolean avoidExit) {
    this.interpreter  = new Interpreter(this);
    this.symbolTable  = new HashMap<String, SSymbol>();
    this.avoidExit    = avoidExit;
    this.lastExitCode = 0;

    current = this;
  }

  public static Universe current() {
    return current;
  }

  public Interpreter getInterpreter() {
    return interpreter;
  }

  public void exit(long errorCode) {
    // Exit from the Java system
    if (!avoidExit) {
      System.exit((int) errorCode);
    } else {
      lastExitCode = (int) errorCode;
    }
  }

  public int lastExitCode() {
    return lastExitCode;
  }

  public void errorExit(String message) {
    errorPrintln("Runtime Error: " + message);
    exit(1);
  }

  private String[] handleArguments(String[] arguments) {
    boolean gotClasspath = false;
    String[] remainingArgs = new String[arguments.length];
    int cnt = 0;

    for (int i = 0; i < arguments.length; i++) {
      if (arguments[i].equals("-cp")) {
        if (i + 1 >= arguments.length) {
          printUsageAndExit();
        }
        setupClassPath(arguments[i + 1]);
        // Checkstyle: stop
        ++i; // skip class path
        // Checkstyle: resume
        gotClasspath = true;
      } else if (arguments[i].equals("-d")) {
        dumpBytecodes = true;
      } else {
        remainingArgs[cnt++] = arguments[i];
      }
    }

    if (!gotClasspath) {
      // Get the default class path of the appropriate size
      classPath = setupDefaultClassPath(0);
    }

    // Copy the remaining elements from the original array into the new
    // array
    arguments = new String[cnt];
    System.arraycopy(remainingArgs, 0, arguments, 0, cnt);

    // check remaining args for class paths, and strip file extension
    for (int i = 0; i < arguments.length; i++) {
      String[] split = getPathClassExt(arguments[i]);

      if (!("".equals(split[0]))) { // there was a path
        String[] tmp = new String[classPath.length + 1];
        System.arraycopy(classPath, 0, tmp, 1, classPath.length);
        tmp[0] = split[0];
        classPath = tmp;
      }
      arguments[i] = split[1];
    }

    return arguments;
  }

  // take argument of the form "../foo/Test.som" and return
  // "../foo", "Test", "som"
  private String[] getPathClassExt(String arg) {
    // Create a new tokenizer to split up the string of dirs
    StringTokenizer tokenizer = new StringTokenizer(arg,
        fileSeparator, true);

    String cp = "";

    while (tokenizer.countTokens() > 2) {
      cp = cp + tokenizer.nextToken();
    }
    if (tokenizer.countTokens() == 2) {
      tokenizer.nextToken(); // throw out delimiter
    }

    String file = tokenizer.nextToken();

    tokenizer = new StringTokenizer(file, ".");

    if (tokenizer.countTokens() > 2) {
      println("Class with . in its name?");
      exit(1);
    }

    String[] result = new String[3];
    result[0] = cp;
    result[1] = tokenizer.nextToken();
    result[2] = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
    return result;
  }

  public void setupClassPath(String cp) {
    // Create a new tokenizer to split up the string of directories
    StringTokenizer tokenizer = new StringTokenizer(cp,
        pathSeparator);

    // Get the default class path of the appropriate size
    classPath = setupDefaultClassPath(tokenizer.countTokens());

    // Get the directories and put them into the class path array
    for (int i = 0; tokenizer.hasMoreTokens(); i++) {
      classPath[i] = tokenizer.nextToken();
    }
  }

  private String[] setupDefaultClassPath(int directories) {
    // Get the default system class path
    String systemClassPath = System.getProperty("system.class.path");

    // Compute the number of defaults
    int defaults = (systemClassPath != null) ? 2 : 1;

    // Allocate an array with room for the directories and the defaults
    String[] result = new String[directories + defaults];

    // Insert the system class path into the defaults section
    if (systemClassPath != null) {
      result[directories] = systemClassPath;
    }

    // Insert the current directory into the defaults section
    result[directories + defaults - 1] = ".";

    // Return the class path
    return result;
  }

  private void printUsageAndExit() {
    // Print the usage
    println("Usage: som [-options] [args...]                          ");
    println("                                                         ");
    println("where options include:                                   ");
    println("    -cp <directories separated by " + pathSeparator
        + ">");
    println("                  set search path for application classes");
    println("    -d            enable disassembling");

    // Exit
    System.exit(0);
  }

  /**
   * Start interpretation by sending the selector to the given class.
   * This is mostly meant for testing currently.
   *
   * @param className
   * @param selector
   * @return
   */
  public SAbstractObject interpret(final String className,
      final String selector) {
    initializeObjectSystem();

    SClass clazz = loadClass(symbolFor(className));

    // Lookup the initialize invokable on the system class
    SMethod initialize = (SMethod) clazz.getSOMClass(this).
                                        lookupInvokable(symbolFor(selector));

    return interpretMethod(clazz, initialize, newArray(0));
  }

  private SAbstractObject initialize(String[] arguments) {
    SAbstractObject systemObject = initializeObjectSystem();

    // Start the shell if no filename is given
    if (arguments.length == 0) {
      Shell shell = new Shell(this, interpreter);
      SMethod bootstrapMethod = createBootstrapMethod();
      shell.setBootstrapMethod(bootstrapMethod);
      return shell.start();
    }

    // Lookup the initialize invokable on the system class
    SInvokable initialize = systemClass.lookupInvokable(symbolFor("initialize:"));

    // Convert the arguments into an array
    SArray argumentsArray = newArray(arguments);

    return interpretMethod(systemObject, initialize,
        argumentsArray);
  }

  private SMethod createBootstrapMethod() {
    // Create a fake bootstrap method to simplify later frame traversal
    SMethod bootstrapMethod = newMethod(symbolFor("bootstrap"), 1, 0, newInteger(0), newInteger(2), null);
    bootstrapMethod.setBytecode(0, Bytecodes.halt);
    bootstrapMethod.setHolder(systemClass);
    return bootstrapMethod;
  }

  private SAbstractObject interpretMethod(SAbstractObject receiver,
      SInvokable invokable, SArray arguments) {
    SMethod bootstrapMethod = createBootstrapMethod();

    // Create a fake bootstrap frame with the system object on the stack
    Frame bootstrapFrame = interpreter.pushNewFrame(bootstrapMethod);
    bootstrapFrame.push(receiver);
    bootstrapFrame.push(arguments);

    // Invoke the initialize invokable
    invokable.invoke(bootstrapFrame, interpreter);

    // Start the interpreter
    return interpreter.start();
  }

  private SAbstractObject initializeObjectSystem() {
    // Allocate the nil object
    nilObject = new SObject(null);

    // Allocate the Metaclass classes
    metaclassClass = newMetaclassClass();

    // Allocate the rest of the system classes
    objectClass     = newSystemClass();
    nilClass        = newSystemClass();
    classClass      = newSystemClass();
    arrayClass      = newSystemClass();
    symbolClass     = newSystemClass();
    methodClass     = newSystemClass();
    integerClass    = newSystemClass();
    primitiveClass  = newSystemClass();
    stringClass     = newSystemClass();
    doubleClass     = newSystemClass();

    // Setup the class reference for the nil object
    nilObject.setClass(nilClass);

    // Initialize the system classes.
    initializeSystemClass(objectClass,            null, "Object");
    initializeSystemClass(classClass,      objectClass, "Class");
    initializeSystemClass(metaclassClass,   classClass, "Metaclass");
    initializeSystemClass(nilClass,        objectClass, "Nil");
    initializeSystemClass(arrayClass,      objectClass, "Array");
    initializeSystemClass(methodClass,      arrayClass, "Method");
    initializeSystemClass(symbolClass,     objectClass, "Symbol");
    initializeSystemClass(integerClass,    objectClass, "Integer");
    initializeSystemClass(primitiveClass,  objectClass, "Primitive");
    initializeSystemClass(stringClass,     objectClass, "String");
    initializeSystemClass(doubleClass,     objectClass, "Double");

    // Load methods and fields into the system classes
    loadSystemClass(objectClass);
    loadSystemClass(classClass);
    loadSystemClass(metaclassClass);
    loadSystemClass(nilClass);
    loadSystemClass(arrayClass);
    loadSystemClass(methodClass);
    loadSystemClass(symbolClass);
    loadSystemClass(integerClass);
    loadSystemClass(primitiveClass);
    loadSystemClass(stringClass);
    loadSystemClass(doubleClass);

    // Fix up objectClass
    objectClass.setSuperClass(nilObject);

    // Load the generic block class
    blockClass = loadClass(symbolFor("Block"));

    // Setup the true and false objects
    SSymbol trueSymbol = symbolFor("True");
    trueClass   = loadClass(trueSymbol);
    trueObject  = newInstance(trueClass);

    SSymbol falseSymbol = symbolFor("False");
    falseClass  = loadClass(falseSymbol);
    falseObject = newInstance(falseClass);

    // Load the system class and create an instance of it
    systemClass = loadClass(symbolFor("System"));
    SAbstractObject systemObject = newInstance(systemClass);

    // Put special objects and classes into the dictionary of globals
    setGlobal(symbolFor("nil"),    nilObject);
    setGlobal(symbolFor("true"),   trueObject);
    setGlobal(symbolFor("false"),  falseObject);
    setGlobal(symbolFor("system"), systemObject);
    setGlobal(symbolFor("System"), systemClass);
    setGlobal(symbolFor("Block"),  blockClass);

    setGlobal(trueSymbol,  trueClass);
    setGlobal(falseSymbol, falseClass);
    return systemObject;
  }

  public SSymbol symbolFor(String string) {
    // Lookup the symbol in the symbol table
    SSymbol result = symbolTable.get(string);
    if (result != null) { return result; }

    // Create a new symbol and return it
    result = newSymbol(string);
    return result;
  }

  public SArray newArray(long length) {
    return new SArray(nilObject, length);
  }

  public SArray newArray(List<?> list) {
    // Allocate a new array with the same length as the list
    SArray result = newArray(list.size());

    // Copy all elements from the list into the array
    for (int i = 0; i < list.size(); i++) {
      result.setIndexableField(i, (SAbstractObject) list.get(i));
    }

    // Return the allocated and initialized array
    return result;
  }

  public SArray newArray(String[] stringArray) {
    // Allocate a new array with the same length as the string array
    SArray result = newArray(stringArray.length);

    // Copy all elements from the string array into the array
    for (int i = 0; i < stringArray.length; i++) {
      result.setIndexableField(i, newString(stringArray[i]));
    }

    // Return the allocated and initialized array
    return result;
  }

  public SBlock newBlock(SMethod method, Frame context, int arguments) {
    // Allocate a new block and set its class to be the block class
    SBlock result = new SBlock(method, context, getBlockClass(arguments));
    return result;
  }

  public SClass newClass(SClass classClass) {
    // Allocate a new class and set its class to be the given class class
    SClass result = new SClass(classClass.getNumberOfInstanceFields(), this);
    result.setClass(classClass);

    // Return the freshly allocated class
    return result;
  }

  public Frame newFrame(final Frame previousFrame, final SMethod method,
      final Frame context) {

    // Compute the maximum number of stack locations (including arguments,
    // locals and extra buffer to support doesNotUnderstand) and set the number
    // of indexable fields accordingly
    long length = method.getNumberOfArguments()
        + method.getNumberOfLocals().getEmbeddedInteger()
        + method.getMaximumNumberOfStackElements().getEmbeddedInteger() + 2;

    Frame result = new Frame(nilObject, previousFrame, context, method, length);

    // Reset the stack pointer and the bytecode index
    result.resetStackPointer();
    result.setBytecodeIndex(0);

    // Return the freshly allocated frame
    return result;
  }

  public SMethod newMethod(SSymbol signature, int numberOfBytecodes,
      int numberOfLiterals, final SInteger numberOfLocals,
      final SInteger maxNumStackElements, final List<SAbstractObject> literals) {
    // Allocate a new method and set its class to be the method class
    SMethod result = new SMethod(nilObject, signature, numberOfBytecodes,
        numberOfLocals, maxNumStackElements, numberOfLiterals, literals);
    return result;
  }

  public SObject newInstance(SClass instanceClass) {
    // Allocate a new instance and set its class to be the given class
    SObject result = new SObject(instanceClass.getNumberOfInstanceFields(),
        nilObject);
    result.setClass(instanceClass);

    // Return the freshly allocated instance
    return result;
  }

  public SInteger newInteger(long value) {
    SInteger result = SInteger.getInteger(value);
    return result;
  }

  public SBigInteger newBigInteger(BigInteger value) {
    SBigInteger result = new SBigInteger(value);
    return result;
  }

  public SDouble newDouble(double value) {
    SDouble result = new SDouble(value);
    return result;
  }

  public SClass newMetaclassClass() {
    // Allocate the metaclass classes
    SClass result = new SClass(this);
    result.setClass(new SClass(this));

    // Setup the metaclass hierarchy
    result.getSOMClass().setClass(result);

    // Return the freshly allocated metaclass class
    return result;
  }

  public SString newString(String embeddedString) {
    // Allocate a new string and set its class to be the string class
    SString result = new SString(embeddedString);

    // Return the freshly allocated string
    return result;
  }

  public SSymbol newSymbol(String string) {
    // Allocate a new symbol and set its class to be the symbol class
    SSymbol result = new SSymbol(string);

    // Insert the new symbol into the symbol table
    symbolTable.put(string, result);

    // Return the freshly allocated symbol
    return result;
  }

  public SClass newSystemClass() {
    // Allocate the new system class
    SClass systemClass = new SClass(this);

    // Setup the metaclass hierarchy
    systemClass.setClass(new SClass(this));
    systemClass.getSOMClass().setClass(metaclassClass);

    // Return the freshly allocated system class
    return systemClass;
  }

  public void initializeSystemClass(SClass systemClass, SClass superClass,
      String name) {
    // Initialize the superclass hierarchy
    if (superClass != null) {
      systemClass.setSuperClass(superClass);
      systemClass.getSOMClass().setSuperClass(superClass.getSOMClass());
    } else {
      systemClass.getSOMClass().setSuperClass(classClass);
    }

    // Initialize the array of instance fields
    systemClass.setInstanceFields(newArray(0));
    systemClass.getSOMClass().setInstanceFields(newArray(0));

    // Initialize the array of instance invokables
    systemClass.setInstanceInvokables(newArray(0));
    systemClass.getSOMClass().setInstanceInvokables(newArray(0));

    // Initialize the name of the system class
    systemClass.setName(symbolFor(name));
    systemClass.getSOMClass().setName(symbolFor(name + " class"));

    // Insert the system class into the dictionary of globals
    setGlobal(systemClass.getName(), systemClass);
  }

  public SAbstractObject getGlobal(SSymbol name) {
    // Return the global with the given name if it's in the dictionary of
    // globals
    if (hasGlobal(name)) { return globals.get(name); }

    // Global not found
    return null;
  }

  public void setGlobal(SSymbol name, SAbstractObject value) {
    // Insert the given value into the dictionary of globals
    globals.put(name, value);
  }

  public boolean hasGlobal(SSymbol name) {
    // Returns if the universe has a value for the global of the given name
    return globals.containsKey(name);
  }

  public SClass getBlockClass() {
    // Get the generic block class
    return blockClass;
  }

  public SClass getBlockClass(int numberOfArguments) {
    // Compute the name of the block class with the given number of
    // arguments
    SSymbol name = symbolFor("Block"
        + Integer.toString(numberOfArguments));

    // Lookup the specific block class in the dictionary of globals and
    // return it
    if (hasGlobal(name)) { return (SClass) getGlobal(name); }

    // Get the block class for blocks with the given number of arguments
    SClass result = loadClass(name, null);

    // Add the appropriate value primitive to the block class
    result.addInstancePrimitive(SBlock.getEvaluationPrimitive(numberOfArguments,
        this));

    // Insert the block class into the dictionary of globals
    setGlobal(name, result);

    // Return the loaded block class
    return result;
  }

  public SClass loadClass(SSymbol name) {
    // Check if the requested class is already in the dictionary of globals
    if (hasGlobal(name)) { return (SClass) getGlobal(name); }

    // Load the class
    SClass result = loadClass(name, null);

    // Load primitives (if necessary) and return the resulting class
    if (result != null && result.hasPrimitives()) { result.loadPrimitives(); }

    setGlobal(name, result);

    return result;
  }

  public void loadSystemClass(SClass systemClass) {
    // Load the system class
    SClass result = loadClass(systemClass.getName(), systemClass);

    // Load primitives if necessary
    if (result.hasPrimitives()) { result.loadPrimitives(); }
  }

  private SClass loadClass(SSymbol name, SClass systemClass) {
    // Try loading the class from all different paths
    for (String cpEntry : classPath) {
      try {
        // Load the class from a file and return the loaded class
        SClass result = som.compiler.SourcecodeCompiler.compileClass(cpEntry,
            name.getEmbeddedString(), systemClass, this);
        if (dumpBytecodes) {
          Disassembler.dump(result.getSOMClass());
          Disassembler.dump(result);
        }
        return result;

      } catch (IOException e) {
        // Continue trying different paths
      }
    }

    // The class could not be found.
    return null;
  }

  public SClass loadShellClass(String stmt) throws IOException {
    // java.io.ByteArrayInputStream in = new
    // java.io.ByteArrayInputStream(stmt.getBytes());

    // Load the class from a stream and return the loaded class
    SClass result = som.compiler.SourcecodeCompiler.compileClass(stmt, null,
        this);
    if (dumpBytecodes) { Disassembler.dump(result); }
    return result;
  }

  public static void errorPrint(String msg) {
    // Checkstyle: stop
    System.err.print(msg);
    // Checkstyle: resume
  }

  public static void errorPrintln(String msg) {
    // Checkstyle: stop
    System.err.println(msg);
    // Checkstyle: resume
  }

  public static void errorPrintln() {
    // Checkstyle: stop
    System.err.println();
    // Checkstyle: resume
  }

  public static void print(String msg) {
    // Checkstyle: stop
    System.err.print(msg);
    // Checkstyle: resume
  }

  public static void println(String msg) {
    // Checkstyle: stop
    System.err.println(msg);
    // Checkstyle: resume
  }

  public static void println() {
    // Checkstyle: stop
    System.err.println();
    // Checkstyle: resume
  }

  public SObject                                 nilObject;
  public SObject                                 trueObject;
  public SObject                                 falseObject;

  public SClass                                  objectClass;
  public SClass                                  classClass;
  public SClass                                  metaclassClass;

  public SClass                                  nilClass;
  public SClass                                  integerClass;
  public SClass                                  arrayClass;
  public SClass                                  methodClass;
  public SClass                                  symbolClass;
  public SClass                                  primitiveClass;
  public SClass                                  stringClass;
  public SClass                                  systemClass;
  public SClass                                  blockClass;
  public SClass                                  doubleClass;

  public SClass                                  trueClass;
  public SClass                                  falseClass;

  private final HashMap<SSymbol, SAbstractObject> globals = new HashMap<SSymbol, SAbstractObject>();
  private String[]                              classPath;
  private boolean                               dumpBytecodes;

  public static final String                    pathSeparator;
  public static final String                    fileSeparator;
  private final Interpreter                     interpreter;
  private final HashMap<String, SSymbol>        symbolTable;

  // TODO: this is not how it is supposed to be... it is just a hack to cope
  //       with the use of system.exit in SOM to enable testing
  private final boolean                         avoidExit;
  private int                                   lastExitCode;

  private static Universe current;
}
