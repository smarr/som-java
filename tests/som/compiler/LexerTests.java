package som.compiler;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;


/**
 * A small set of tests for the SOM lexer to demonstrate it's basic functionality.
 */
public class LexerTests {
  @Test
  public void emptyClass() {
    Lexer l = new Lexer(new StringReader("Foo = ()"));

    assertEquals(Symbol.Identifier, l.getSym());
    assertEquals("Foo", l.getText());

    assertEquals(Symbol.Equal, l.getSym());
    assertEquals("=", l.getText());

    assertEquals(Symbol.NewTerm, l.getSym());
    assertEquals("(", l.getText());

    assertEquals(Symbol.EndTerm, l.getSym());
    assertEquals(")", l.getText());

    assertEquals(Symbol.NONE, l.getSym());
  }

  @Test
  public void keywordSymbol() {
    Lexer l = new Lexer(new StringReader("#key:word:"));

    assertEquals(Symbol.Pound, l.getSym());
    assertEquals("#", l.getText());

    assertEquals(Symbol.KeywordSequence, l.getSym());
    assertEquals("key:word:", l.getText());

    assertEquals(Symbol.NONE, l.getSym());
  }

  @Test
  public void assignDouble() {
    Lexer l = new Lexer(new StringReader("var := 3.14."));

    assertEquals(Symbol.Identifier, l.getSym());
    assertEquals("var", l.getText());

    assertEquals(Symbol.Assign, l.getSym());
    assertEquals(":=", l.getText());

    assertEquals(Symbol.Double, l.getSym());
    assertEquals("3.14", l.getText());

    assertEquals(Symbol.Period, l.getSym());
    assertEquals(".", l.getText());

    assertEquals(Symbol.NONE, l.getSym());
  }

  @Test
  public void string() {
    Lexer l = new Lexer(new StringReader("'some string with new\nline'"));

    assertEquals(Symbol.STString, l.getSym());
    assertEquals("some string with new\nline", l.getText());

    assertEquals(Symbol.NONE, l.getSym());
  }
}
