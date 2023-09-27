package som.vmobjects.storagestrategies.sarray;

import som.vm.Universe;
import som.vmobjects.*;

/**
 * Empty SArray Strategy
 *
 * Stores the array length as an int
 */
public class EmptyArrayStrategy extends ArrayStorageStrategy {

  private final SObject nilObject;

  public EmptyArrayStrategy(SObject nilObject) {
    this.nilObject = nilObject;
  }

  public void initialize(SArray arr, int numElements) {
    arr.storage = numElements;
  }

  @Override
  public int getNumberOfIndexableFields(SArray arr) {
    return (int) arr.storage;
  }

  @Override
  public SAbstractObject getIndexableField(SArray arr, int index) {
    return nilObject;
  }

  @Override
  public ArrayStorageStrategy setIndexableFieldMaybeTransition(SArray arr, int index, SAbstractObject value) {
    if (value == nilObject) {
        return this;
    }

    if (value instanceof SInteger) {
      final long embeddedInteger = ((SInteger) value).getEmbeddedInteger();

      if (embeddedInteger != IntegerArrayStrategy.EMPTY_SLOT) {
        final IntegerArrayStrategy sIntegerArrayStrategy = Universe.current().getIntegerArrayStrategy();
        sIntegerArrayStrategy.initialize(arr, (int) arr.storage);
        sIntegerArrayStrategy.setIndexableFieldNoTransition(arr, index, embeddedInteger);
        return sIntegerArrayStrategy;
      }
    } else if (value instanceof SDouble) {
      final double embeddedDouble = ((SDouble) value).getEmbeddedDouble();

      if (embeddedDouble != DoubleArrayStrategy.EMPTY_SLOT) {
        final DoubleArrayStrategy sDoubleArrayStrategy = Universe.current().getDoubleArrayStrategy();
        sDoubleArrayStrategy.initialize(arr, (int) arr.storage);
        sDoubleArrayStrategy.setIndexableFieldNoTransition(arr, index, embeddedDouble);
        return sDoubleArrayStrategy;
      }
    }

    final AbstractObjectArrayStrategy abstractObjectArrayStrategy = Universe.current().getAbstractObjectArrayStrategy();
    abstractObjectArrayStrategy.initialize(arr, (int) arr.storage);
    abstractObjectArrayStrategy.setIndexableFieldNoTransition(arr, index, value);
    return abstractObjectArrayStrategy;
  }

}
