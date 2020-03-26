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

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import som.vm.Universe;
import som.vmobjects.SClass;
import som.vmobjects.SSymbol;


public class SourcecodeCompiler {

  private Parser parser;

  public static SClass compileClass(final String path, final String file,
      final SClass systemClass, final Universe universe)
      throws IOException, ProgramDefinitionError {
    return new SourcecodeCompiler().compile(path, file, systemClass, universe);
  }

  public static SClass compileClass(final String stmt, final SClass systemClass,
      final Universe universe) throws ProgramDefinitionError {
    return new SourcecodeCompiler().compileClassString(stmt, systemClass,
        universe);
  }

  private SClass compile(final String path, final String file,
      final SClass systemClass, final Universe universe)
      throws IOException, ProgramDefinitionError {
    String fname = path + Universe.fileSeparator + file + ".som";

    parser = new Parser(new FileReader(fname), universe, fname);

    SClass result = compile(systemClass);

    SSymbol cname = result.getName();
    String cnameC = cname.getEmbeddedString();

    if (file != cnameC) {
      throw new ProgramDefinitionError("File name " + fname
          + " does not match class name (" + cnameC + ") in it.");
    }

    return result;
  }

  private SClass compileClassString(final String stream,
      final SClass systemClass, final Universe universe)
      throws ProgramDefinitionError {
    parser = new Parser(new StringReader(stream), universe, "$string$");

    SClass result = compile(systemClass);
    return result;
  }

  private SClass compile(final SClass systemClass) throws ProgramDefinitionError {
    ClassGenerationContext cgc = parser.classdef();

    if (systemClass == null) {
      return cgc.assemble();
    } else {
      return cgc.assembleSystemClass(systemClass);
    }
  }
}
