package som.vmobjects.storagestrategies.sarray;

import som.vmobjects.*;

import java.util.Arrays;

/**
 * Object SArray Strategy
 *
 * Stores an SAbstractObject[]
 */
public class AbstractObjectArrayStrategy extends ArrayStorageStrategy {
  private final SObject nilObject;

  public AbstractObjectArrayStrategy(SObject nilObject) {
    this.nilObject = nilObject;
  }
  public void initialize(SArray arr, int numElements) {
    initializeAll(arr, nilObject, numElements);
  }

  public void initialize(SArray arr, long[] elements) {
    SAbstractObject[] storage = new SAbstractObject[elements.length];

    for (int i = 0; i < elements.length; i++) {
      storage[i] = elements[i] == IntegerArrayStrategy.EMPTY_SLOT ? nilObject : SInteger.getInteger(elements[i]);
    }

    arr.storage = storage;
  }

  public void initialize(SArray arr, double[] elements) {
    SAbstractObject[] storage = new SAbstractObject[elements.length];

    for (int i = 0; i < elements.length; i++) {
      storage[i] = elements[i] == DoubleArrayStrategy.EMPTY_SLOT ? nilObject : new SDouble(elements[i]);
    }

    arr.storage = storage;
  }

  public void initializeAll(SArray arr, SAbstractObject value, int numElements) {
    SAbstractObject[] storage = new SAbstractObject[numElements];
    Arrays.fill(storage, value);
    arr.storage = storage;
  }

  @Override
  public int getNumberOfIndexableFields(SArray arr) {
    return ((SAbstractObject[]) arr.storage).length;
  }

  @Override
  public SAbstractObject getIndexableField(SArray arr, int index) {
    return ((SAbstractObject[]) arr.storage)[index];
  }

  @Override
  public ArrayStorageStrategy setIndexableFieldMaybeTransition(SArray arr, int index, SAbstractObject value) {
    ((SAbstractObject[]) arr.storage)[index] = value;
    return this;
  }

  public void setIndexableFieldNoTransition(SArray arr, int index, SAbstractObject value) {
    ((SAbstractObject[]) arr.storage)[index] = value;
  }

}
