package som.primitives;

import som.interpreter.Interpreter;
import som.vm.Universe;
import som.vmobjects.SFrame;
import som.vmobjects.SPrimitive;

public class FramePrimitives extends Primitives {

  public FramePrimitives(final Universe universe) {
    super(universe);
  }

  public void installPrimitives() {
    installInstancePrimitive(new SPrimitive("method", universe) {

      @Override
      public void invoke(SFrame frame, Interpreter interpreter) {
        SFrame self = (SFrame) frame.pop();
        frame.push(self.getMethod());
      }
    });

    installInstancePrimitive(new SPrimitive("previousFrame", universe) {

      @Override
      public void invoke(SFrame frame, Interpreter interpreter) {
        SFrame self = (SFrame) frame.pop();
        frame.push(self.getPreviousFrame());
      }
    });
  }
}
