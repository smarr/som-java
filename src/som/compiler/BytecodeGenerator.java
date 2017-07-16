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

import static som.interpreter.Bytecodes.*;

import som.vmobjects.SAbstractObject;
import som.vmobjects.SMethod;
import som.vmobjects.SSymbol;


public class BytecodeGenerator {

  public void emitPOP(final MethodGenerationContext mgenc) {
    emit1(mgenc, POP);
  }

  public void emitPUSHARGUMENT(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx) {
    emit3(mgenc, PUSH_ARGUMENT, idx, ctx);
  }

  public void emitRETURNLOCAL(final MethodGenerationContext mgenc) {
    emit1(mgenc, RETURN_LOCAL);
  }

  public void emitRETURNNONLOCAL(final MethodGenerationContext mgenc) {
    emit1(mgenc, RETURN_NON_LOCAL);
  }

  public void emitDUP(final MethodGenerationContext mgenc) {
    emit1(mgenc, DUP);
  }

  public void emitPUSHBLOCK(final MethodGenerationContext mgenc, final SMethod blockMethod) {
    emit2(mgenc, PUSH_BLOCK, mgenc.findLiteralIndex(blockMethod));
  }

  public void emitPUSHLOCAL(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx) {
    assert idx >= 0;
    emit3(mgenc, PUSH_LOCAL, idx, ctx);
  }

  public void emitPUSHFIELD(final MethodGenerationContext mgenc, final SSymbol fieldName) {
    assert mgenc.hasField(fieldName);
    emit2(mgenc, PUSH_FIELD, mgenc.getFieldIndex(fieldName));
  }

  public void emitPUSHGLOBAL(final MethodGenerationContext mgenc, final SSymbol global) {
    emit2(mgenc, PUSH_GLOBAL, mgenc.findLiteralIndex(global));
  }

  public void emitPOPARGUMENT(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx) {
    emit3(mgenc, POP_ARGUMENT, idx, ctx);
  }

  public void emitPOPLOCAL(final MethodGenerationContext mgenc, final byte idx,
      final byte ctx) {
    emit3(mgenc, POP_LOCAL, idx, ctx);
  }

  public void emitPOPFIELD(final MethodGenerationContext mgenc, final SSymbol fieldName) {
    assert mgenc.hasField(fieldName);
    emit2(mgenc, POP_FIELD, mgenc.getFieldIndex(fieldName));
  }

  public void emitSUPERSEND(final MethodGenerationContext mgenc, final SSymbol msg) {
    emit2(mgenc, SUPER_SEND, mgenc.findLiteralIndex(msg));
  }

  public void emitSEND(final MethodGenerationContext mgenc, final SSymbol msg) {
    emit2(mgenc, SEND, mgenc.findLiteralIndex(msg));
  }

  public void emitPUSHCONSTANT(final MethodGenerationContext mgenc,
      final SAbstractObject lit) {
    emit2(mgenc, PUSH_CONSTANT, mgenc.findLiteralIndex(lit));
  }

  public void emitPUSHCONSTANT(final MethodGenerationContext mgenc, final byte literalIndex) {
    emit2(mgenc, PUSH_CONSTANT, literalIndex);
  }

  private void emit1(final MethodGenerationContext mgenc, final byte code) {
    mgenc.addBytecode(code);
  }

  private void emit2(final MethodGenerationContext mgenc, final byte code, final byte idx) {
    mgenc.addBytecode(code);
    mgenc.addBytecode(idx);
  }

  private void emit3(final MethodGenerationContext mgenc, final byte code, final byte idx,
      final byte ctx) {
    mgenc.addBytecode(code);
    mgenc.addBytecode(idx);
    mgenc.addBytecode(ctx);
  }

}
