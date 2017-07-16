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

package som.interpreter;

public class Bytecodes {

  // Bytecodes used by the simple object machine
  public static final byte HALT             = 0;
  public static final byte DUP              = 1;
  public static final byte PUSH_LOCAL       = 2;
  public static final byte PUSH_ARGUMENT    = 3;
  public static final byte PUSH_FIELD       = 4;
  public static final byte PUSH_BLOCK       = 5;
  public static final byte PUSH_CONSTANT    = 6;
  public static final byte PUSH_GLOBAL      = 7;
  public static final byte POP              = 8;
  public static final byte POP_LOCAL        = 9;
  public static final byte POP_ARGUMENT     = 10;
  public static final byte POP_FIELD        = 11;
  public static final byte SEND             = 12;
  public static final byte SUPER_SEND       = 13;
  public static final byte RETURN_LOCAL     = 14;
  public static final byte RETURN_NON_LOCAL = 15;

  public static final java.lang.String[] bytecodeNames = new java.lang.String[] {
      "HALT            ", "DUP             ", "PUSH_LOCAL      ",
      "PUSH_ARGUMENT   ", "PUSH_FIELD      ", "PUSH_BLOCK      ",
      "PUSH_CONSTANT   ", "PUSH_GLOBAL     ", "POP             ",
      "POP_LOCAL       ", "POP_ARGUMENT    ", "POP_FIELD       ",
      "SEND            ", "SUPER_SEND      ", "RETURN_LOCAL    ",
      "RETURN_NON_LOCAL"};

  private static final byte numBytecodes = 16;

  public static int getBytecodeLength(byte bytecode) {
    // Return the length of the given bytecode
    return bytecodeLength[bytecode];
  }

  // Static array holding lengths of each bytecode
  private static int[] bytecodeLength = new int[numBytecodes];

  static {
    // set up the lengths of the "native" bytecodes
    bytecodeLength[HALT] = 1;
    bytecodeLength[DUP] = 1;
    bytecodeLength[PUSH_LOCAL] = 3;
    bytecodeLength[PUSH_ARGUMENT] = 3;
    bytecodeLength[PUSH_FIELD] = 2;
    bytecodeLength[PUSH_BLOCK] = 2;
    bytecodeLength[PUSH_CONSTANT] = 2;
    bytecodeLength[PUSH_GLOBAL] = 2;
    bytecodeLength[POP] = 1;
    bytecodeLength[POP_LOCAL] = 3;
    bytecodeLength[POP_ARGUMENT] = 3;
    bytecodeLength[POP_FIELD] = 2;
    bytecodeLength[SEND] = 2;
    bytecodeLength[SUPER_SEND] = 2;
    bytecodeLength[RETURN_LOCAL] = 1;
    bytecodeLength[RETURN_NON_LOCAL] = 1;
  }

}
