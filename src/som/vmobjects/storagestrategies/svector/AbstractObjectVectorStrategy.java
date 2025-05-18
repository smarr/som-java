package som.vmobjects.storagestrategies.svector;

import som.vmobjects.*;

import java.util.Arrays;

public class AbstractObjectVectorStrategy extends VectorStorageStrategy {

  private final SObject nilObject;

  public AbstractObjectVectorStrategy(final SObject nilObject) {
    this.nilObject = nilObject;
  }

  public void initialize(final SVector vec, final int numElements) {
    initializeAll(vec, nilObject, numElements);
  }

  public void initialize(final SVector vec, final long[] elements) {
    final SAbstractObject[] storage = new SAbstractObject[elements.length];

    for (int i = 0; i < elements.length; i++) {
      storage[i] = elements[i] == IntegerVectorStrategy.EMPTY_SLOT ? nilObject : SInteger.getInteger(elements[i]);
    }

    vec.storage = storage;
  }

  public void initialize(final SVector vec, final double[] elements) {
    final SAbstractObject[] storage = new SAbstractObject[elements.length];

    for (int i = 0; i < elements.length; i++) {
      storage[i] = elements[i] == DoubleVectorStrategy.EMPTY_SLOT ? nilObject : new SDouble(elements[i]);
    }

    vec.storage = storage;
  }

  public void initializeAll(final SVector vec, final SAbstractObject value, final int numElements) {
    final SAbstractObject[] storage = new SAbstractObject[numElements];
    Arrays.fill(storage, value);
    vec.storage = storage;
  }

  @Override
  public SAbstractObject getIndexableField(final SVector vec, final int index, final SObject nilObject) {
    final int storeIndex = index + vec.getFirstIndex() - 1;

    if (vec.invalidIndex(storeIndex)) {
      return null;
    }

    if (((SAbstractObject[]) vec.storage)[storeIndex - 1] == null) {
      return nilObject;
    } else {
      return ((SAbstractObject[]) vec.storage)[storeIndex - 1];
    }
  }

  @Override
  public SAbstractObject getFirstIndexableField(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      return ((SAbstractObject[]) vec.storage)[vec.getFirstIndex() - 1];
    } else {
      return nilObject;
    }
  }

  @Override
  public SAbstractObject getLastIndexableField(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      return ((SAbstractObject[]) vec.storage)[vec.getLastIndex() - 2];
    } else {
      return nilObject;
    }
  }

  @Override
  public int getIndexOfElement(final SVector vec, final SAbstractObject element) {
    for (int i = 0; i < ((SAbstractObject[]) vec.storage).length; i++) {
      if (((SAbstractObject[]) vec.storage)[i] == element) {
        return i + 2 - vec.getFirstIndex();
      }
    }

    return -1;
  }

  @Override
  public boolean containsElement(final SVector vec, final SAbstractObject element) {
    if (element instanceof SString) {
      final String elementString = ((SString) element).getEmbeddedString();
      for (int i = vec.getFirstIndex(); i <= vec.getLastIndex() - 1; i++) {
        if (((SAbstractObject[]) vec.storage)[i - 1] instanceof SString
                && elementString.equals(((SString) ((SAbstractObject[]) vec.storage)[i - 1]).getEmbeddedString())) {
          return true;
        }
      }
    } else {
      for (int i = vec.getFirstIndex(); i <= vec.getLastIndex() - 1; i++) {
        if (((SAbstractObject[]) vec.storage)[i - 1] == element) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public Object[] setIndexableFieldMaybeTransition(final SVector vec, final int index, final SAbstractObject value) {
    final int storeIndex = index + vec.getFirstIndex() - 1;

    if (vec.invalidIndex(storeIndex)) {
      return new Object[] {this, false};
    }

    ((SAbstractObject[]) vec.storage)[storeIndex - 1] = value;

    return new Object[] {this, true};
  }

  public void setIndexableFieldNoTransition(final SVector vec, final int index, final SAbstractObject value) {
    ((SAbstractObject[]) vec.storage)[index + vec.getFirstIndex() - 2] = value;
  }

  @Override
  public VectorStorageStrategy setLastIndexableFieldMaybeTransition(final SVector vec, final SAbstractObject value) {
    if (vec.getLastIndex() > ((SAbstractObject[]) vec.storage).length) {
      final SAbstractObject[] newStorage = new SAbstractObject[2 * ((SAbstractObject[]) vec.storage).length];
      System.arraycopy(((SAbstractObject[]) vec.storage), 0, newStorage, 0, ((SAbstractObject[]) vec.storage).length);
      vec.storage = newStorage;
    }

    ((SAbstractObject[]) vec.storage)[vec.getLastIndex() - 1] = value;
    vec.setLast(vec.getLastIndex() + 1);

    return this;
  }

    public int getCapacity(final SVector vec) {
    return ((SAbstractObject[]) vec.storage).length;
  }

  public void setLastIndexableFieldNoTransition(final SVector vec, final SAbstractObject value) {
    if (vec.getLastIndex() > ((SAbstractObject[]) vec.storage).length) {
      final SAbstractObject[] newStorage = new SAbstractObject[2 * ((SAbstractObject[]) vec.storage).length];
      System.arraycopy(((SAbstractObject[]) vec.storage), 0, newStorage, 0, ((SAbstractObject[]) vec.storage).length);
      vec.storage = newStorage;
    }

    ((SAbstractObject[]) vec.storage)[vec.getLastIndex() - 1] = value;
    vec.setLast(vec.getLastIndex() + 1);
  }

  @Override
  public boolean removeElement(final SVector vec, final SAbstractObject element) {
    final SAbstractObject[] newStorage = new SAbstractObject[((SAbstractObject[]) vec.storage).length];
    int newLast = 1;
    boolean found = false;

    for (int i = vec.getFirstIndex(); i < vec.getLastIndex(); i++) {
      if (((SAbstractObject[]) vec.storage)[i] == element) {
        found = true;
      } else {
        newStorage[i] = ((SAbstractObject[]) vec.storage)[i];
        newLast++;
      }
    }

    vec.storage = newStorage;
    vec.setLast(newLast);
    vec.setFirst(1);

    return found;
  }

  @Override
  public SAbstractObject removeFirstElement(final SVector vec, final SObject nilObject) {
    if (vec.isEmpty()) {
      return null;
    } else {
      final SAbstractObject value = ((SAbstractObject[]) vec.storage)[vec.getFirstIndex() - 1];
      ((SAbstractObject[]) vec.storage)[vec.getFirstIndex() - 1] = nilObject;
      vec.setFirst(vec.getFirstIndex() + 1);
      return value == null ? nilObject : value;
    }
  }

  @Override
  public SAbstractObject removeLastElement(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      vec.setLast(vec.getLastIndex() - 1);
      final SAbstractObject value = ((SAbstractObject[]) vec.storage)[vec.getLastIndex() - 1];
      ((SAbstractObject[]) vec.storage)[vec.getLastIndex() - 1] = nilObject;
      return value == null ? nilObject : value;
    } else {
      return null;
    }
  }

  @Override
  public SArray asArray(final SVector vec, final SObject nilObject) {
    final SArray arr = new SArray(vec.getSize());
    for (int i = 0; i < vec.getSize(); i++) {
      SAbstractObject value = ((SAbstractObject[]) vec.storage)[vec.getFirstIndex() + i - 1];
      if (value == null) {
        arr.setIndexableField(i, nilObject);
      } else {
        arr.setIndexableField(i, value);
      }
    }
    return arr;
  }

  @Override
  public SArray asRawArray(final SVector vec) {
    final SArray arr = new SArray(((SAbstractObject[]) vec.storage).length);
    for (int i = 0; i < ((SAbstractObject[]) vec.storage).length; i++) {
      arr.setIndexableField(i, ((SAbstractObject[]) vec.storage)[i]);
    }
    return arr;
  }

}
