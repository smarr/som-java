package som.primitives;

import som.interpreter.Interpreter;
import som.vm.Universe;
import som.vmobjects.Frame;
import som.vmobjects.Primitive;

public class FramePrimitives extends Primitives {

  public FramePrimitives(final Universe universe) {
    super(universe);
  }

  public void installPrimitives() {
    installInstancePrimitive(new Primitive("method", universe) {

      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        Frame self = (Frame) frame.pop();
        frame.push(self.getMethod());
      }
    });

    installInstancePrimitive(new Primitive("previousFrame", universe) {

      @Override
      public void invoke(Frame frame, Interpreter interpreter) {
        Frame self = (Frame) frame.pop();
        frame.push(self.getPreviousFrame());
      }
    });
  }
}
