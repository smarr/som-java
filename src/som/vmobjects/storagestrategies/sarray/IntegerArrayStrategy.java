package som.vmobjects.storagestrategies.sarray;

import som.vm.Universe;
import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SInteger;

import java.util.Arrays;

/**
 * Integer SArray Strategy
 *
 * Stores a long[]
 */
public class IntegerArrayStrategy extends ArrayStorageStrategy {

  // Magic value used to indicate an empty element
  // Array is transitioned to an AbstractObjectStrategy if the magic value is ever inserted
  public static final long EMPTY_SLOT = Long.MIN_VALUE + 2L;

  public void initialize(SArray arr, int numElements) {
    initializeAll(arr, EMPTY_SLOT, numElements);
  }

  public void initializeAll(SArray arr, long value, int numElements) {
    long[] storage = new long[numElements];
    Arrays.fill(storage, value);
    arr.storage = storage;
  }

  @Override
  public int getNumberOfIndexableFields(SArray arr) {
    return ((long[]) arr.storage).length;
  }

  @Override
  public SAbstractObject getIndexableField(SArray arr, int index) {
    return SInteger.getInteger(((long[]) arr.storage)[index]);
  }

  @Override
  public ArrayStorageStrategy setIndexableFieldMaybeTransition(SArray arr, int index, SAbstractObject value) {
    if (value instanceof SInteger) {
      final long embeddedInteger = ((SInteger) value).getEmbeddedInteger();

      if (embeddedInteger != EMPTY_SLOT) {
        ((long[]) arr.storage)[index] = embeddedInteger;
        return this;
      }
    }

    final AbstractObjectArrayStrategy abstractObjectArrayStrategy = Universe.current().getAbstractObjectArrayStrategy();
    abstractObjectArrayStrategy.initialize(arr, (long[]) arr.storage);
    abstractObjectArrayStrategy.setIndexableFieldNoTransition(arr, index, value);
    return abstractObjectArrayStrategy;
  }

  public void setIndexableFieldNoTransition(SArray arr, int index, long value) {
    ((long[]) arr.storage)[index] = value;
  }

}
