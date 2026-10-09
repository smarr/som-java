package som.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import som.compiler.ProgramDefinitionError;
import som.vm.Universe;
import som.vmobjects.SClass;
import som.vmobjects.SObject;


/**
 * Checks that classes report the expected fields.
 */
@RunWith(Parameterized.class)
public class ClassFieldsTests {

  private static final String[] NONE = new String[0];

  @Parameters(name = "{0} [{index}]")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][] {
        // system classes
        {"Object", NONE, NONE},
        {"Class", NONE, NONE},
        {"Metaclass", NONE, NONE},
        {"Nil", NONE, NONE},
        {"Array", NONE, NONE},
        {"Method", NONE, NONE},
        {"Symbol", NONE, NONE},
        {"Integer", NONE, NONE},
        {"Primitive", NONE, NONE},
        {"String", NONE, NONE},
        {"Double", NONE, NONE},
        {"Block", NONE, NONE},
        {"Boolean", NONE, NONE},
        {"True", NONE, NONE},
        {"False", NONE, NONE},
        {"System", NONE, NONE},

        // other library classes
        {"Pair", new String[] {"key", "value"}, NONE},
        {"Vector", new String[] {"first", "last", "storage"}, NONE},
        {"HashEntry", new String[] {"key", "value", "next", "hash"}, NONE},

        // test classes with fields in a class hierarchy
        {"ClassA", new String[] {"a", "b"}, new String[] {"c1", "c2", "c3"}},
        {"ClassB", new String[] {"a", "b", "c", "d"},
            new String[] {"c1", "c2", "c3", "c4", "c5", "c6"}},
        {"ClassC", new String[] {"a", "b", "c", "d", "e", "f"},
            new String[] {"c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9"}},
    });
  }

  private final String   className;
  private final String[] instanceFields;
  private final String[] classFields;

  public ClassFieldsTests(final String className, final String[] instanceFields,
      final String[] classFields) {
    this.className = className;
    this.instanceFields = instanceFields;
    this.classFields = classFields;
  }

  private Universe universe;

  private SClass loadClass() throws ProgramDefinitionError {
    Universe u = new Universe(true);
    universe = u;
    u.setupClassPath("Smalltalk:TestSuite");
    u.initializeObjectSystem();
    return u.loadClass(u.symbolFor(className));
  }

  private static void assertFields(final String[] expected, final SClass clazz) {
    assertEquals(expected.length, clazz.getNumberOfInstanceFields());

    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], clazz.getInstanceFieldName(i).getEmbeddedString());
      assertEquals(i, clazz.lookupFieldIndex(clazz.getInstanceFieldName(i)));
    }
  }

  @Test
  public void testInstanceSide() throws ProgramDefinitionError {
    SClass clazz = loadClass();
    assertFields(instanceFields, clazz);
  }

  @Test
  public void testClassSide() throws ProgramDefinitionError {
    SClass clazz = loadClass();
    assertFields(classFields, clazz.getSOMClass());
  }

  @Test
  public void testNumberOfFieldsOfClassObject() throws ProgramDefinitionError {
    SClass clazz = loadClass();
    assertEquals(classFields.length, clazz.getNumberOfFields());
  }

  @Test
  public void testNumberOfFieldsOfNewInstance() throws ProgramDefinitionError {
    SClass clazz = loadClass();
    SObject obj = universe.newInstance(clazz);
    assertEquals(instanceFields.length, obj.getNumberOfFields());
  }
}
