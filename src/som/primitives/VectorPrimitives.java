package som.primitives;

import som.interpreter.Frame;
import som.interpreter.Interpreter;
import som.vm.Universe;
import som.vmobjects.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

public class VectorPrimitives extends Primitives {

  /*
   * This value must be recomputed and updated whenever the source to Smalltalk/Vector.som changes
   */
  public static final byte[] VECTOR_CHECKSUM = new byte[] {-30, -108, -99, -57, -36, 65, 119, -44, 33, -90, -2, -39, 106, 1, -85, 59};

  public VectorPrimitives(final Universe universe) {
    super(universe);
  }

  @Override
  public void installPrimitives() {
    byte[] digest;
    try {
      final MessageDigest md = MessageDigest.getInstance("MD5");
      try (final InputStream is = Files.newInputStream(Paths.get("Smalltalk/Vector.som"));
           final DigestInputStream dis = new DigestInputStream(is, md)) {
        dis.readAllBytes();
      }

      digest = md.digest();

      if (!Arrays.equals(VECTOR_CHECKSUM, digest)) {
        System.err.println("Warning: Vector checksum mismatch. Primitives not loaded.");
        return; // Only load primitives if source matches known hash
      }
    } catch (final Exception e) {
      System.err.println("Warning: Cannot determine Vector checksum. Primitives not loaded.");
      return; // If the checksum cannot be determined due to an exception, then do not load the primitives
    }

    installInstancePrimitive(new SPrimitive("initialize:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SInteger length = (SInteger) frame.pop();
        SVector vector = universe.newVector(length.getEmbeddedInteger());
        vector.setClass(frame.pop().getSOMClass(universe));
        frame.push(vector);
      }
    });

    installInstancePrimitive(new SPrimitive("append:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SAbstractObject value = frame.pop();
        SVector self = (SVector) frame.pop();
        self.setLastIndexableField(value);
        frame.push(self);
      }
    });

    installInstancePrimitive(new SPrimitive("at:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SInteger index = (SInteger) frame.pop();
        SVector self = (SVector) frame.pop();
        SAbstractObject value = self.getIndexableField(index.getEmbeddedInteger(), universe.nilObject);
        if (value == null) {
          SString error = new SString("Vector[" + self.getFirstIndex() + ".." + self.getLastIndex() + "]: Index " + index.getEmbeddedInteger() + " out of bounds");
          self.send("error", new SAbstractObject[] {error}, universe, universe.getInterpreter());
        }
        else {
          frame.push(value);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("at:put:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SAbstractObject value = frame.pop();
        SInteger index = (SInteger) frame.pop();
        SVector self = (SVector) frame.getStackElement(0);
        if (!self.setIndexableField(index.getEmbeddedInteger(), value)) {
          SString error = new SString("Vector[" + self.getFirstIndex() + ".." + self.getLastIndex() + "]: Index " + index.getEmbeddedInteger() + " out of bounds");
          self.send("error", new SAbstractObject[] {error}, universe, universe.getInterpreter());
        }
      }
    });

    installInstancePrimitive(new SPrimitive("first", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        frame.push(self.getFirstIndexableField(universe.nilObject));
      }
    });

    installInstancePrimitive(new SPrimitive("last", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        frame.push(self.getLastIndexableField(universe.nilObject));
      }
    });

    installInstancePrimitive(new SPrimitive("do:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SBlock block = (SBlock) frame.pop();
        SVector self = (SVector) frame.pop();

        for (int i = self.getLastIndex() - 1; i >= self.getFirstIndex(); i--) {
          block.send("value:", new SAbstractObject[] {self.getIndexableField(i, universe.nilObject)}, universe, universe.getInterpreter());
        }
      }
    });

    installInstancePrimitive(new SPrimitive("doIndexes:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SBlock block = (SBlock) frame.pop();
        SVector self = (SVector) frame.pop();
        SInteger.getInteger(1).send("to:do:", new SAbstractObject[] {SInteger.getInteger(self.getLastIndex() - self.getFirstIndex()), block}, universe, universe.getInterpreter());
      }
    });

    installInstancePrimitive(new SPrimitive("remove", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        SAbstractObject value = self.removeLastElement(universe.nilObject);
        if (value == null) {
          SString error = new SString("Vector: Attempting to remove the last element from an empty Vector");
          self.send("error", new SAbstractObject[] {error}, universe, universe.getInterpreter());
        } else {
          frame.push(value);
        }
      }
    });

    installInstancePrimitive(new SPrimitive("remove:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SAbstractObject object = frame.pop();
        SVector self = (SVector) frame.pop();
        frame.push(universe.newBoolean(self.removeElement(object)));
      }
    });

    installInstancePrimitive(new SPrimitive("contains:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SAbstractObject object = frame.pop();
        SVector self = (SVector) frame.pop();
        frame.push(universe.newBoolean(self.containsElement(object)));
      }
    });

    installInstancePrimitive(new SPrimitive("indexOf:", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SAbstractObject object = frame.pop();
        SVector self = (SVector) frame.pop();
        frame.push(SInteger.getInteger(self.getIndexOfElement(object)));
      }
    });

    installInstancePrimitive(new SPrimitive("isEmpty", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        frame.push(universe.newBoolean(self.isEmpty()));
      }
    });

    installInstancePrimitive(new SPrimitive("size", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        frame.push(SInteger.getInteger(self.getSize()));
      }
    });

    installInstancePrimitive(new SPrimitive("capacity", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        frame.push(SInteger.getInteger(self.getCapacity()));
      }
    });

    installInstancePrimitive(new SPrimitive("asArray", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        frame.push(self.asArray(universe.nilObject));
      }
    });

    installInstancePrimitive(new SPrimitive("removeFirst", universe) {
      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        SVector self = (SVector) frame.pop();
        SAbstractObject value = self.removeFirstElement(universe.nilObject);
        if (value == null) {
          SString error = new SString("Vector: Attempting to remove the first element from an empty Vector");
          self.send("error", new SAbstractObject[] {error}, universe, universe.getInterpreter());
        } else {
          frame.push(value);
        }
      }
    });
  }

}
