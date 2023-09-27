/**
 * Copyright (c) 2017 Michael Haupt, github@haupz.de
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

import static som.interpreter.Bytecodes.*;

import som.vmobjects.SAbstractObject;
import som.vmobjects.SMethod;
import som.vmobjects.SSymbol;


public class BytecodeGenerator {

  public void emitPOP(final MethodGenerationContext mgenc, final int[] coord) {
    emit1(mgenc, POP, coord);
  }

  public void emitPUSHARGUMENT(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx, final int[] coord) {
    emit3(mgenc, PUSH_ARGUMENT, idx, ctx, coord);
  }

  public void emitRETURNLOCAL(final MethodGenerationContext mgenc, final int[] coord) {
    emit1(mgenc, RETURN_LOCAL, coord);
  }

  public void emitRETURNNONLOCAL(final MethodGenerationContext mgenc, final int[] coord) {
    emit1(mgenc, RETURN_NON_LOCAL, coord);
  }

  public void emitDUP(final MethodGenerationContext mgenc, final int[] coord) {
    emit1(mgenc, DUP, coord);
  }

  public void emitPUSHBLOCK(final MethodGenerationContext mgenc, final SMethod blockMethod,
      final int[] coord) {
    emit2(mgenc, PUSH_BLOCK, mgenc.findLiteralIndex(blockMethod), coord);
  }

  public void emitPUSHLOCAL(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx, final int[] coord) {
    assert idx >= 0;
    emit3(mgenc, PUSH_LOCAL, idx, ctx, coord);
  }

  public void emitPUSHFIELD(final MethodGenerationContext mgenc, final SSymbol fieldName,
      final int[] coord) {
    assert mgenc.hasField(fieldName);
    emit2(mgenc, PUSH_FIELD, mgenc.getFieldIndex(fieldName), coord);
  }

  public void emitPUSHGLOBAL(final MethodGenerationContext mgenc, final SSymbol global,
      final int[] coord) {
    emit2(mgenc, PUSH_GLOBAL, mgenc.findLiteralIndex(global), coord);
  }

  public void emitPOPARGUMENT(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx, final int[] coord) {
    emit3(mgenc, POP_ARGUMENT, idx, ctx, coord);
  }

  public void emitPOPLOCAL(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx, final int[] coord) {
    emit3(mgenc, POP_LOCAL, idx, ctx, coord);
  }

  public void emitPOPFIELD(final MethodGenerationContext mgenc, final SSymbol fieldName,
      final int[] coord) {
    assert mgenc.hasField(fieldName);
    emit2(mgenc, POP_FIELD, mgenc.getFieldIndex(fieldName), coord);
  }

  public void emitSUPERSEND(final MethodGenerationContext mgenc, final SSymbol msg,
      final int[] coord) {
    emit2(mgenc, SUPER_SEND, mgenc.findLiteralIndex(msg), coord);
  }

  public void emitSEND(final MethodGenerationContext mgenc, final SSymbol msg, final int[] coord) {
    emit2(mgenc, SEND, mgenc.findLiteralIndex(msg), coord);
  }

  public void emitPUSHCONSTANT(final MethodGenerationContext mgenc,
      final SAbstractObject lit, final int[] coord) {
    emit2(mgenc, PUSH_CONSTANT, mgenc.findLiteralIndex(lit), coord);
  }

  public void emitPUSHCONSTANT(final MethodGenerationContext mgenc, final byte literalIndex,
      final int[] coord) {
    emit2(mgenc, PUSH_CONSTANT, literalIndex, coord);
  }

  private void emit1(final MethodGenerationContext mgenc, final byte code, final int[] coord) {
    mgenc.addBytecode(code);
    mgenc.addCoord(coord);
  }

  private void emit2(final MethodGenerationContext mgenc, final byte code, final byte idx,
      final int[] coord) {
    mgenc.addBytecode(code);
    mgenc.addCoord(coord);
    mgenc.addBytecode(idx);
    mgenc.addCoord(null);
  }

  private void emit3(final MethodGenerationContext mgenc, final byte code, final byte idx,
      final byte ctx, final int[] coord) {
    mgenc.addBytecode(code);
    mgenc.addCoord(coord);
    mgenc.addBytecode(idx);
    mgenc.addCoord(null);
    mgenc.addBytecode(ctx);
    mgenc.addCoord(null);
  }

}
