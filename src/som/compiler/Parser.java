/**
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

import static som.compiler.Symbol.And;
import static som.compiler.Symbol.Assign;
import static som.compiler.Symbol.At;
import static som.compiler.Symbol.Colon;
import static som.compiler.Symbol.Comma;
import static som.compiler.Symbol.Div;
import static som.compiler.Symbol.Double;
import static som.compiler.Symbol.EndBlock;
import static som.compiler.Symbol.EndTerm;
import static som.compiler.Symbol.Equal;
import static som.compiler.Symbol.Exit;
import static som.compiler.Symbol.Identifier;
import static som.compiler.Symbol.Integer;
import static som.compiler.Symbol.Keyword;
import static som.compiler.Symbol.KeywordSequence;
import static som.compiler.Symbol.Less;
import static som.compiler.Symbol.Minus;
import static som.compiler.Symbol.Mod;
import static som.compiler.Symbol.More;
import static som.compiler.Symbol.NONE;
import static som.compiler.Symbol.NewBlock;
import static som.compiler.Symbol.NewTerm;
import static som.compiler.Symbol.Not;
import static som.compiler.Symbol.OperatorSequence;
import static som.compiler.Symbol.Or;
import static som.compiler.Symbol.Per;
import static som.compiler.Symbol.Period;
import static som.compiler.Symbol.Plus;
import static som.compiler.Symbol.Pound;
import static som.compiler.Symbol.Primitive;
import static som.compiler.Symbol.STString;
import static som.compiler.Symbol.Separator;
import static som.compiler.Symbol.Star;

import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SClass;
import som.vmobjects.SInteger;
import som.vmobjects.SMethod;
import som.vmobjects.SString;
import som.vmobjects.SSymbol;


public class Parser {

  private final Universe universe;
  private final String   filename;

  private final Lexer             lexer;
  private final BytecodeGenerator bcGen;

  private Symbol sym;
  private String text;
  private Symbol nextSym;

  private static final List<Symbol> singleOpSyms        = new ArrayList<Symbol>();
  private static final List<Symbol> binaryOpSyms        = new ArrayList<Symbol>();
  private static final List<Symbol> keywordSelectorSyms = new ArrayList<Symbol>();

  static {
    for (Symbol s : new Symbol[] {Not, And, Or, Star, Div, Mod, Plus, Equal,
        More, Less, Comma, At, Per, NONE}) {
      singleOpSyms.add(s);
    }
    for (Symbol s : new Symbol[] {Or, Comma, Minus, Equal, Not, And, Or, Star,
        Div, Mod, Plus, Equal, More, Less, Comma, At, Per, NONE}) {
      binaryOpSyms.add(s);
    }
    for (Symbol s : new Symbol[] {Keyword, KeywordSequence}) {
      keywordSelectorSyms.add(s);
    }
  }

  public static class ParseError extends ProgramDefinitionError {
    private static final long serialVersionUID = 425390202979033628L;

    private final int line;
    private final int column;

    private final String text;
    private final String rawBuffer;
    private final String fileName;
    private final Symbol expected;
    private final Symbol found;

    ParseError(final String message, final Symbol expected, final Parser parser) {
      super(message);
      if (parser.lexer == null) {
        this.line = 0;
        this.column = 0;
        this.rawBuffer = "";
      } else {
        this.line = parser.lexer.getCurrentLineNumber();
        this.column = parser.lexer.getCurrentColumn();
        this.rawBuffer = parser.lexer.getRawBuffer();
      }
      this.text = parser.text;
      this.fileName = parser.filename;
      this.expected = expected;
      this.found = parser.sym;
    }

    protected String expectedSymbolAsString() {
      return expected.toString();
    }

    @Override
    public String getMessage() {
      String msg = super.getMessage();

      String foundStr;
      if (Parser.printableSymbol(found)) {
        foundStr = found + " (" + text + ")";
      } else {
        foundStr = found.toString();
      }
      String expectedStr = expectedSymbolAsString();

      msg = msg.replace("%(expected)s", expectedStr);
      msg = msg.replace("%(found)s", foundStr);

      return msg;
    }

    @Override
    public String toString() {
      String msg = "%(file)s:%(line)d:%(column)d: error: " + super.getMessage();
      String foundStr;
      if (Parser.printableSymbol(found)) {
        foundStr = found + " (" + text + ")";
      } else {
        foundStr = found.toString();
      }
      msg += ": " + rawBuffer;
      String expectedStr = expectedSymbolAsString();

      msg = msg.replace("%(file)s", fileName);
      msg = msg.replace("%(line)d", "" + line);
      msg = msg.replace("%(column)d", "" + column);
      msg = msg.replace("%(expected)s", expectedStr);
      msg = msg.replace("%(found)s", foundStr);
      return msg;
    }
  }

  public Parser(final Reader reader, final Universe universe, final String filename) {
    this.universe = universe;
    this.filename = filename;

    sym = NONE;
    lexer = new Lexer(reader);
    bcGen = new BytecodeGenerator();
    nextSym = NONE;
    getSymbolFromLexer();
  }

  public void classdef(final ClassGenerationContext cgenc) throws ProgramDefinitionError {
    cgenc.setName(universe.symbolFor(text));
    expect(Identifier);
    expect(Equal);

    superclass(cgenc);

    expect(NewTerm);
    instanceFields(cgenc);
    while (sym == Identifier || sym == Keyword || sym == OperatorSequence
        || symIn(binaryOpSyms)) {
      MethodGenerationContext mgenc = new MethodGenerationContext();
      mgenc.setHolder(cgenc);
      mgenc.addArgument("self");

      method(mgenc);

      if (mgenc.isPrimitive()) {
        cgenc.addInstanceMethod(mgenc.assemblePrimitive(universe));
      } else {
        cgenc.addInstanceMethod(mgenc.assemble(universe));
      }
    }

    if (accept(Separator)) {
      cgenc.setClassSide(true);
      classFields(cgenc);
      while (sym == Identifier || sym == Keyword || sym == OperatorSequence
          || symIn(binaryOpSyms)) {
        MethodGenerationContext mgenc = new MethodGenerationContext();
        mgenc.setHolder(cgenc);
        mgenc.addArgument("self");

        method(mgenc);

        if (mgenc.isPrimitive()) {
          cgenc.addClassMethod(mgenc.assemblePrimitive(universe));
        } else {
          cgenc.addClassMethod(mgenc.assemble(universe));
        }
      }
    }
    expect(EndTerm);
  }

  private void superclass(final ClassGenerationContext cgenc) throws ProgramDefinitionError {
    SSymbol superName;
    if (sym == Identifier) {
      superName = universe.symbolFor(text);
      accept(Identifier);
    } else {
      superName = universe.symbolFor("Object");
    }
    cgenc.setSuperName(superName);

    // Load the super class, if it is not nil (break the dependency cycle)
    if (!superName.getEmbeddedString().equals("nil")) {
      SClass superClass = universe.loadClass(superName);
      cgenc.setInstanceFieldsOfSuper(superClass.getInstanceFields());
      cgenc.setClassFieldsOfSuper(superClass.getSOMClass().getInstanceFields());
    }
  }

  private boolean symIn(final List<Symbol> ss) {
    return ss.contains(sym);
  }

  private boolean accept(final Symbol s) {
    if (sym == s) {
      getSymbolFromLexer();
      return true;
    }
    return false;
  }

  private boolean acceptOneOf(final List<Symbol> ss) {
    if (symIn(ss)) {
      getSymbolFromLexer();
      return true;
    }
    return false;
  }

  private boolean expect(final Symbol s) {
    if (accept(s)) {
      return true;
    }
    StringBuffer err = new StringBuffer("Error: " + filename + ":" +
        lexer.getCurrentLineNumber() +
        ": unexpected symbol, expected: " + s.toString()
        + ", but found: " + sym.toString());
    if (printableSymbol(sym)) {
      err.append(" (" + text + ")");
    }
    err.append(": " + lexer.getRawBuffer());
    throw new IllegalStateException(err.toString());
  }

  private boolean expectOneOf(final List<Symbol> ss) {
    if (acceptOneOf(ss)) {
      return true;
    }
    StringBuffer err = new StringBuffer("Error: " + filename + ":" +
        lexer.getCurrentLineNumber() + ": unexpected symbol, expected one of: ");
    for (Symbol s : ss) {
      err.append(s.toString() + ", ");
    }
    err.append("but found: " + sym.toString());
    if (printableSymbol(sym)) {
      err.append(" (" + text + ")");
    }
    err.append(": " + lexer.getRawBuffer());
    throw new IllegalStateException(err.toString());
  }

  private void instanceFields(final ClassGenerationContext cgenc) {
    if (accept(Or)) {
      while (sym == Identifier) {
        String var = variable();
        cgenc.addInstanceField(universe.symbolFor(var));
      }
      expect(Or);
    }
  }

  private void classFields(final ClassGenerationContext cgenc) {
    if (accept(Or)) {
      while (sym == Identifier) {
        String var = variable();
        cgenc.addClassField(universe.symbolFor(var));
      }
      expect(Or);
    }
  }

  private void method(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    pattern(mgenc);
    expect(Equal);
    if (sym == Primitive) {
      mgenc.setPrimitive(true);
      primitiveBlock();
    } else {
      methodBlock(mgenc);
    }
  }

  private void primitiveBlock() {
    expect(Primitive);
  }

  private void pattern(final MethodGenerationContext mgenc) {
    switch (sym) {
      case Identifier:
        unaryPattern(mgenc);
        break;
      case Keyword:
        keywordPattern(mgenc);
        break;
      default:
        binaryPattern(mgenc);
        break;
    }
  }

  private void unaryPattern(final MethodGenerationContext mgenc) {
    mgenc.setSignature(unarySelector());
  }

  private void binaryPattern(final MethodGenerationContext mgenc) {
    mgenc.setSignature(binarySelector());
    mgenc.addArgumentIfAbsent(argument());
  }

  private void keywordPattern(final MethodGenerationContext mgenc) {
    StringBuffer kw = new StringBuffer();
    do {
      kw.append(keyword());
      mgenc.addArgumentIfAbsent(argument());
    } while (sym == Keyword);

    mgenc.setSignature(universe.symbolFor(kw.toString()));
  }

  private void methodBlock(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    expect(NewTerm);
    blockContents(mgenc);
    // if no return has been generated so far, we can be sure there was no .
    // terminating the last expression, so the last expression's value must
    // be
    // popped off the stack and a ^self be generated
    if (!mgenc.isFinished()) {
      bcGen.emitPOP(mgenc);
      bcGen.emitPUSHARGUMENT(mgenc, (byte) 0, (byte) 0);
      bcGen.emitRETURNLOCAL(mgenc);
      mgenc.setFinished();
    }

    expect(EndTerm);
  }

  private SSymbol unarySelector() {
    return universe.symbolFor(identifier());
  }

  private SSymbol binarySelector() {
    String s = new String(text);

    // Checkstyle: stop @formatter:off
    if (accept(Or)) {
    } else if (accept(Comma)) {
    } else if (accept(Minus)) {
    } else if (accept(Equal)) {
    } else if (acceptOneOf(singleOpSyms)) {
    } else if (accept(OperatorSequence)) {
    } else { expect(NONE); }
    // Checkstyle: resume @formatter:on

    return universe.symbolFor(s);
  }

  private String identifier() {
    String s = new String(text);
    boolean isPrimitive = accept(Primitive);
    if (!isPrimitive) {
      expect(Identifier);
    }
    return s;
  }

  private String keyword() {
    String s = new String(text);
    expect(Keyword);

    return s;
  }

  private String argument() {
    return variable();
  }

  private void blockContents(final MethodGenerationContext mgenc)
      throws ProgramDefinitionError {
    if (accept(Or)) {
      locals(mgenc);
      expect(Or);
    }
    blockBody(mgenc, false);
  }

  private void locals(final MethodGenerationContext mgenc) {
    while (sym == Identifier) {
      mgenc.addLocalIfAbsent(variable());
    }
  }

  private void blockBody(final MethodGenerationContext mgenc, final boolean seenPeriod)
      throws ProgramDefinitionError {
    if (accept(Exit)) {
      result(mgenc);
    } else if (sym == EndBlock) {
      if (seenPeriod) {
        // a POP has been generated which must be elided (blocks always
        // return the value of the last expression, regardless of
        // whether it
        // was terminated with a . or not)
        mgenc.removeLastBytecode();
      }
      bcGen.emitRETURNLOCAL(mgenc);
      mgenc.setFinished();
    } else if (sym == EndTerm) {
      // it does not matter whether a period has been seen, as the end of
      // the
      // method has been found (EndTerm) - so it is safe to emit a "return
      // self"
      bcGen.emitPUSHARGUMENT(mgenc, (byte) 0, (byte) 0);
      bcGen.emitRETURNLOCAL(mgenc);
      mgenc.setFinished();
    } else {
      expression(mgenc);
      if (accept(Period)) {
        bcGen.emitPOP(mgenc);
        blockBody(mgenc, true);
      }
    }
  }

  private void result(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    expression(mgenc);

    if (mgenc.isBlockMethod()) {
      bcGen.emitRETURNNONLOCAL(mgenc);
    } else {
      bcGen.emitRETURNLOCAL(mgenc);
    }

    mgenc.setFinished(true);
    accept(Period);
  }

  private void expression(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    peekForNextSymbolFromLexer();

    if (nextSym == Assign) {
      assignation(mgenc);
    } else {
      evaluation(mgenc);
    }
  }

  private void assignation(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    List<String> l = new ArrayList<String>();

    assignments(mgenc, l);
    evaluation(mgenc);

    for (int i = 1; i <= l.size(); i++) {
      bcGen.emitDUP(mgenc);
    }
    for (String s : l) {
      genPopVariable(mgenc, s);
    }
  }

  private void assignments(final MethodGenerationContext mgenc, final List<String> l) {
    if (sym == Identifier) {
      l.add(assignment(mgenc));
      peekForNextSymbolFromLexer();
      if (nextSym == Assign) {
        assignments(mgenc, l);
      }
    }
  }

  private String assignment(final MethodGenerationContext mgenc) {
    String v = variable();
    SSymbol var = universe.symbolFor(v);
    mgenc.addLiteralIfAbsent(var);

    expect(Assign);

    return v;
  }

  private void evaluation(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    // single: superSend
    Single<Boolean> si = new Single<Boolean>(false);

    primary(mgenc, si);
    if (sym == Identifier || sym == Keyword || sym == OperatorSequence
        || symIn(binaryOpSyms)) {
      messages(mgenc, si);
    }
  }

  private void primary(final MethodGenerationContext mgenc, final Single<Boolean> superSend)
      throws ProgramDefinitionError {
    superSend.set(false);
    switch (sym) {
      case Identifier: {
        String v = variable();
        if (v.equals("super")) {
          superSend.set(true);
          // sends to super push self as the receiver
          v = "self";
        }

        genPushVariable(mgenc, v);
        break;
      }
      case NewTerm:
        nestedTerm(mgenc);
        break;
      case NewBlock: {
        MethodGenerationContext bgenc = new MethodGenerationContext();
        bgenc.setIsBlockMethod(true);
        bgenc.setHolder(mgenc.getHolder());
        bgenc.setOuter(mgenc);

        nestedBlock(bgenc);

        SMethod blockMethod = bgenc.assemble(universe);
        mgenc.addLiteral(blockMethod);
        bcGen.emitPUSHBLOCK(mgenc, blockMethod);
        break;
      }
      default:
        literal(mgenc);
        break;
    }
  }

  private String variable() {
    return identifier();
  }

  private void messages(final MethodGenerationContext mgenc, final Single<Boolean> superSend)
      throws ProgramDefinitionError {
    if (sym == Identifier) {
      do {
        // only the first message in a sequence can be a super send
        unaryMessage(mgenc, superSend);
        superSend.set(false);
      } while (sym == Identifier);

      while (sym == OperatorSequence || symIn(binaryOpSyms)) {
        binaryMessage(mgenc, new Single<Boolean>(false));
      }

      if (sym == Keyword) {
        keywordMessage(mgenc, new Single<Boolean>(false));
      }
    } else if (sym == OperatorSequence || symIn(binaryOpSyms)) {
      do {
        // only the first message in a sequence can be a super send
        binaryMessage(mgenc, superSend);
        superSend.set(false);
      } while (sym == OperatorSequence || symIn(binaryOpSyms));

      if (sym == Keyword) {
        keywordMessage(mgenc, new Single<Boolean>(false));
      }
    } else {
      keywordMessage(mgenc, superSend);
    }
  }

  private void unaryMessage(final MethodGenerationContext mgenc,
      final Single<Boolean> superSend) {
    SSymbol msg = unarySelector();
    mgenc.addLiteralIfAbsent(msg);

    if (superSend.get()) {
      bcGen.emitSUPERSEND(mgenc, msg);
    } else {
      bcGen.emitSEND(mgenc, msg);
    }
  }

  private void binaryMessage(final MethodGenerationContext mgenc,
      final Single<Boolean> superSend) throws ProgramDefinitionError {
    SSymbol msg = binarySelector();
    mgenc.addLiteralIfAbsent(msg);

    binaryOperand(mgenc, new Single<Boolean>(false));

    if (superSend.get()) {
      bcGen.emitSUPERSEND(mgenc, msg);
    } else {
      bcGen.emitSEND(mgenc, msg);
    }
  }

  private void binaryOperand(final MethodGenerationContext mgenc,
      final Single<Boolean> superSend) throws ProgramDefinitionError {
    primary(mgenc, superSend);

    while (sym == Identifier) {
      unaryMessage(mgenc, superSend);
    }
  }

  private void keywordMessage(final MethodGenerationContext mgenc,
      final Single<Boolean> superSend) throws ProgramDefinitionError {
    StringBuffer kw = new StringBuffer();
    do {
      kw.append(keyword());
      formula(mgenc);
    } while (sym == Keyword);

    SSymbol msg = universe.symbolFor(kw.toString());

    mgenc.addLiteralIfAbsent(msg);

    if (superSend.get()) {
      bcGen.emitSUPERSEND(mgenc, msg);
    } else {
      bcGen.emitSEND(mgenc, msg);
    }
  }

  private void formula(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    Single<Boolean> superSend = new Single<Boolean>(false);
    binaryOperand(mgenc, superSend);

    // only the first message in a sequence can be a super send
    if (sym == OperatorSequence || symIn(binaryOpSyms)) {
      binaryMessage(mgenc, superSend);
    }
    while (sym == OperatorSequence || symIn(binaryOpSyms)) {
      binaryMessage(mgenc, new Single<Boolean>(false));
    }
  }

  private void nestedTerm(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    expect(NewTerm);
    expression(mgenc);
    expect(EndTerm);
  }

  private void literal(final MethodGenerationContext mgenc) throws ParseError {
    switch (sym) {
      case Pound: {
        peekForNextSymbolFromLexerIfNecessary();
        if (nextSym == NewTerm) {
          literalArray(mgenc);
        } else {
          literalSymbol(mgenc);
        }
        break;
      }
      case STString: {
        literalString(mgenc);
        break;
      }
      default: {
        literalNumber(mgenc);
        break;
      }
    }
  }

  private void literalNumber(final MethodGenerationContext mgenc) throws ParseError {
    SAbstractObject lit;

    if (sym == Minus) {
      lit = negativeDecimal();
    } else {
      lit = literalDecimal(false);
    }
    mgenc.addLiteralIfAbsent(lit);
    bcGen.emitPUSHCONSTANT(mgenc, lit);
  }

  private SAbstractObject literalDecimal(final boolean isNegative) throws ParseError {
    if (sym == Integer) {
      return literalInteger(isNegative);
    } else {
      assert sym == Double;
      return literalDouble(isNegative);
    }
  }

  private SAbstractObject negativeDecimal() throws ParseError {
    expect(Minus);
    return literalDecimal(true);
  }

  private SAbstractObject literalInteger(final boolean isNegative) {
    try {
      long i = Long.parseLong(text);
      if (isNegative) {
        i = 0 - i;
      }
      expect(Integer);
      return universe.newInteger(i);
    } catch (NumberFormatException first) {
      try {
        BigInteger big = new BigInteger(text);
        if (isNegative) {
          big = big.negate();
        }
        expect(Integer);
        return universe.newBigInteger(big);
      } catch (NumberFormatException e) {
        StringBuffer err = new StringBuffer("Error: " + filename + ":" +
            lexer.getCurrentLineNumber() +
            ": parsing number literal failed: '" + text.toString()
            + "'");
        throw new IllegalStateException(err.toString());
      }
    }
  }

  private SAbstractObject literalDouble(final boolean isNegative) throws ParseError {
    try {
      double d = java.lang.Double.parseDouble(text);
      if (isNegative) {
        d = 0.0 - d;
      }
      expect(Double);
      return universe.newDouble(d);
    } catch (NumberFormatException e) {
      throw new ParseError("Could not parse double. Expected a number but " +
          "got '" + text + "'", NONE, this);
    }
  }

  private void literalSymbol(final MethodGenerationContext mgenc) {
    SSymbol symb;
    expect(Pound);
    if (sym == STString) {
      String s = string();
      symb = universe.symbolFor(s);
    } else {
      symb = selector();
    }

    mgenc.addLiteralIfAbsent(symb);
    bcGen.emitPUSHCONSTANT(mgenc, symb);
  }

  private void literalString(final MethodGenerationContext mgenc) {
    String s = string();

    SString str = universe.newString(s);
    mgenc.addLiteralIfAbsent(str);

    bcGen.emitPUSHCONSTANT(mgenc, str);
  }

  private void literalArray(final MethodGenerationContext mgenc) throws ParseError {
    expect(Pound);
    expect(NewTerm);

    SSymbol arrayClassName = universe.symbolFor("Array");
    SSymbol arraySizePlaceholder = universe.symbolFor("ArraySizeLiteralPlaceholder");
    SSymbol newMessage = universe.symbolFor("new:");
    SSymbol atPutMessage = universe.symbolFor("at:put:");

    mgenc.addLiteralIfAbsent(arrayClassName);
    mgenc.addLiteralIfAbsent(newMessage);
    mgenc.addLiteralIfAbsent(atPutMessage);
    final byte arraySizeLiteralIndex = mgenc.addLiteral(arraySizePlaceholder);

    // create empty array
    bcGen.emitPUSHGLOBAL(mgenc, arrayClassName);
    bcGen.emitPUSHCONSTANT(mgenc, arraySizeLiteralIndex);
    bcGen.emitSEND(mgenc, newMessage);

    int i = 1;

    while (sym != EndTerm) {
      SInteger pushIndex = universe.newInteger(i);
      mgenc.addLiteralIfAbsent(pushIndex);
      bcGen.emitPUSHCONSTANT(mgenc, pushIndex);
      literal(mgenc);
      bcGen.emitSEND(mgenc, atPutMessage);
      i += 1;
    }

    // replace the placeholder with the actual array size
    mgenc.updateLiteral(
        arraySizePlaceholder, arraySizeLiteralIndex, universe.newInteger(i - 1));
    expect(EndTerm);
  }

  private SSymbol selector() {
    if (sym == OperatorSequence || symIn(singleOpSyms)) {
      return binarySelector();
    } else if (sym == Keyword || sym == KeywordSequence) {
      return keywordSelector();
    } else {
      return unarySelector();
    }
  }

  private SSymbol keywordSelector() {
    String s = new String(text);
    expectOneOf(keywordSelectorSyms);
    SSymbol symb = universe.symbolFor(s);
    return symb;
  }

  private String string() {
    String s = new String(text);
    expect(STString);
    return s;
  }

  private void nestedBlock(final MethodGenerationContext mgenc) throws ProgramDefinitionError {
    mgenc.addArgumentIfAbsent("$block self");

    expect(NewBlock);
    if (sym == Colon) {
      blockPattern(mgenc);
    }

    // generate Block signature
    String blockSig = "$block method";
    int argSize = mgenc.getNumberOfArguments();
    for (int i = 1; i < argSize; i++) {
      blockSig += ":";
    }

    mgenc.setSignature(universe.symbolFor(blockSig));

    blockContents(mgenc);

    // if no return has been generated, we can be sure that the last
    // expression
    // in the block was not terminated by ., and can generate a return
    if (!mgenc.isFinished()) {
      bcGen.emitRETURNLOCAL(mgenc);
      mgenc.setFinished(true);
    }

    expect(EndBlock);
  }

  private void blockPattern(final MethodGenerationContext mgenc) {
    blockArguments(mgenc);
    expect(Or);
  }

  private void blockArguments(final MethodGenerationContext mgenc) {
    do {
      expect(Colon);
      mgenc.addArgumentIfAbsent(argument());
    } while (sym == Colon);
  }

  private void genPushVariable(final MethodGenerationContext mgenc,
      final String var) {
    // The purpose of this function is to find out whether the variable to be
    // pushed on the stack is a local variable, argument, or object field.
    // This is done by examining all available lexical contexts, starting with
    // the innermost (i.e., the one represented by mgenc).

    // triplet: index, context, isArgument
    Triplet<Byte, Byte, Boolean> tri = new Triplet<Byte, Byte, Boolean>(
        (byte) 0, (byte) 0, false);

    if (mgenc.findVar(var, tri)) {
      if (tri.getZ()) {
        bcGen.emitPUSHARGUMENT(mgenc, tri.getX(), tri.getY());
      } else {
        bcGen.emitPUSHLOCAL(mgenc, tri.getX(), tri.getY());
      }
    } else {
      SSymbol identifier = universe.symbolFor(var);
      if (mgenc.hasField(identifier)) {
        SSymbol fieldName = identifier;
        mgenc.addLiteralIfAbsent(fieldName);
        bcGen.emitPUSHFIELD(mgenc, fieldName);
      } else {
        SSymbol global = identifier;
        mgenc.addLiteralIfAbsent(global);
        bcGen.emitPUSHGLOBAL(mgenc, global);
      }
    }
  }

  private void genPopVariable(final MethodGenerationContext mgenc,
      final String var) throws ParseError {
    // The purpose of this function is to find out whether the variable to be
    // popped off the stack is a local variable, argument, or object field.
    // This is done by examining all available lexical contexts, starting with
    // the innermost (i.e., the one represented by mgenc).

    // triplet: index, context, isArgument
    Triplet<Byte, Byte, Boolean> tri = new Triplet<Byte, Byte, Boolean>(
        (byte) 0, (byte) 0, false);

    if (mgenc.findVar(var, tri)) {
      if (tri.getZ()) {
        bcGen.emitPOPARGUMENT(mgenc, tri.getX(), tri.getY());
      } else {
        bcGen.emitPOPLOCAL(mgenc, tri.getX(), tri.getY());
      }
    } else {
      SSymbol varName = universe.symbolFor(var);
      if (!mgenc.hasField(varName)) {
        throw new ParseError("Trying to write to field with the name '" + var + "', but field does not seem exist in class.", Symbol.NONE, this);
      }
      bcGen.emitPOPFIELD(mgenc, varName);
    }
  }

  private void getSymbolFromLexer() {
    sym = lexer.getSym();
    text = lexer.getText();
  }

  private void peekForNextSymbolFromLexerIfNecessary() {
    if (!lexer.getPeekDone()) {
      peekForNextSymbolFromLexer();
    }
  }

  private void peekForNextSymbolFromLexer() {
    nextSym = lexer.peek();
  }

  private static boolean printableSymbol(final Symbol sym) {
    return sym == Integer || sym == Double || sym.compareTo(STString) >= 0;
  }
}
