package som.vmobjects.storagestrategies.svector;

import som.vm.Universe;
import som.vmobjects.*;

import java.util.Arrays;

public class IntegerVectorStrategy extends VectorStorageStrategy {

  // Magic value used to indicate an empty element
  // Vector is transitioned to an AbstractObjectStrategy if the magic value is ever inserted
  public static final long EMPTY_SLOT = Long.MIN_VALUE + 2L;

  public void initialize(final SVector vec, final int numElements) {
    initializeAll(vec, EMPTY_SLOT, numElements);
  }

  public void initializeAll(final SVector vec, final long value, final int numElements) {
    final long[] storage = new long[numElements];
    Arrays.fill(storage, value);
    vec.storage = storage;
  }

  @Override
  public SAbstractObject getIndexableField(final SVector vec, final int index, final  SObject nilObject) {
    final int storeIndex = index + vec.getFirstIndex() - 1;

    if (vec.invalidIndex(storeIndex)) {
      return null;
    }

    if (((long[]) vec.storage)[storeIndex - 1] == EMPTY_SLOT) {
      return nilObject;
    } else {
      return SInteger.getInteger(((long[]) vec.storage)[storeIndex - 1]);
    }
  }

  @Override
  public SAbstractObject getFirstIndexableField(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      return SInteger.getInteger(((long[]) vec.storage)[vec.getFirstIndex() - 1]);
    } else {
      return nilObject;
    }
  }

  @Override
  public SAbstractObject getLastIndexableField(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      return SInteger.getInteger(((long[]) vec.storage)[vec.getLastIndex() - 2]);
    } else {
      return nilObject;
    }
  }

  @Override
  public int getIndexOfElement(final SVector vec, final SAbstractObject element) {
    if (element instanceof SInteger) {
      final long embeddedInteger = ((SInteger) element).getEmbeddedInteger();

      for (int i = 0; i < ((long[]) vec.storage).length; i++) {
        if (((long[]) vec.storage)[i] == embeddedInteger) {
          return i + 2 - vec.getFirstIndex();
        }
      }
    }

    return -1;
  }

  @Override
  public boolean containsElement(final SVector vec, final SAbstractObject element) {
    if (element instanceof SInteger) {
      final long embeddedInteger = ((SInteger) element).getEmbeddedInteger();

      for (int i = vec.getFirstIndex(); i <= vec.getLastIndex() - 1; i++) {
        if (((long[]) vec.storage)[i - 1] == embeddedInteger) {
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

    if (value instanceof SInteger) {
      final long embeddedInteger = ((SInteger) value).getEmbeddedInteger();

      if (embeddedInteger != EMPTY_SLOT) {
        ((long[]) vec.storage)[storeIndex - 1] = embeddedInteger;
        return new Object[] {this, true};
      }
    }

    final AbstractObjectVectorStrategy abstractObjectVectorStrategy = Universe.current().getAbstractObjectVectorStrategy();
    abstractObjectVectorStrategy.initialize(vec, (long[]) vec.storage);
    abstractObjectVectorStrategy.setIndexableFieldNoTransition(vec, index, value);
    return new Object[] {abstractObjectVectorStrategy, true};
  }

  public void setIndexableFieldNoTransition(final SVector vec, final int index, final long value) {
    ((long[]) vec.storage)[index + vec.getFirstIndex() - 2] = value;
  }

  @Override
  public VectorStorageStrategy setLastIndexableFieldMaybeTransition(final SVector vec, final SAbstractObject value) {
    if (vec.getLastIndex() > ((long[]) vec.storage).length) {
      final long[] newStorage = new long[2 * ((long[]) vec.storage).length];
      System.arraycopy(((long[]) vec.storage), 0, newStorage, 0, ((long[]) vec.storage).length);
      vec.storage = newStorage;
    }

    if (value instanceof SInteger) {
      final long embeddedInteger = ((SInteger) value).getEmbeddedInteger();

      if (embeddedInteger != EMPTY_SLOT) {
        ((long[]) vec.storage)[vec.getLastIndex() - 1] = embeddedInteger;
        vec.setLast(vec.getLastIndex() + 1);
        return this;
      }
    }

    final AbstractObjectVectorStrategy abstractObjectVectorStrategy = Universe.current().getAbstractObjectVectorStrategy();
    abstractObjectVectorStrategy.initialize(vec, (long[]) vec.storage);
    abstractObjectVectorStrategy.setLastIndexableFieldNoTransition(vec, value);
    return abstractObjectVectorStrategy;
  }

  public void setLastIndexableFieldNoTransition(final SVector vec, final long value) {
    if (vec.getLastIndex() > ((long[]) vec.storage).length) {
      final long[] newStorage = new long[2 * ((long[]) vec.storage).length];
      System.arraycopy(((long[]) vec.storage), 0, newStorage, 0, ((long[]) vec.storage).length);
      vec.storage = newStorage;
    }

    ((long[]) vec.storage)[vec.getLastIndex() - 1] = value;
    vec.setLast(vec.getLastIndex() + 1);
  }

  @Override
  public int getCapacity(final SVector vec) {
    return ((long[]) vec.storage).length;
  }

  @Override
  public boolean removeElement(final SVector vec, final SAbstractObject element) {
    if (!(element instanceof SInteger)) {
      return false;
    }

    final long embeddedInteger = ((SInteger) element).getEmbeddedInteger();
    final long[] newStorage = new long[((long[]) vec.storage).length];
    int newLast = 1;
    boolean found = false;

    for (int i = vec.getFirstIndex(); i < vec.getLastIndex(); i++) {
      if (((long[]) vec.storage)[i] == embeddedInteger) {
        found = true;
      } else {
        newStorage[i] = ((long[]) vec.storage)[i];
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
      final long value = ((long[]) vec.storage)[vec.getFirstIndex() - 1];
      ((long[]) vec.storage)[vec.getFirstIndex() - 1] = EMPTY_SLOT;
      vec.setFirst(vec.getFirstIndex() + 1);
      return value == EMPTY_SLOT ? nilObject : SInteger.getInteger(value);
    }
  }

  @Override
  public SAbstractObject removeLastElement(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      vec.setLast(vec.getLastIndex() - 1);
      final long value = ((long[]) vec.storage)[vec.getLastIndex() - 1];
      ((long[]) vec.storage)[vec.getLastIndex() - 1] = EMPTY_SLOT;
      return value == EMPTY_SLOT ? nilObject : SInteger.getInteger(value);
    } else {
      return null;
    }
  }

  @Override
  public SArray asArray(final SVector vec, final SObject nilObject) {
    final SArray arr = new SArray(vec.getSize());
    for (int i = 0; i < vec.getSize(); i++) {
      long value = ((long[]) vec.storage)[vec.getFirstIndex() + i - 1];
      if (value == EMPTY_SLOT) {
        arr.setIndexableField(i, nilObject);
      } else {
        arr.setIndexableField(i, SInteger.getInteger(value));
      }
    }
    return arr;
  }

  @Override
  public SArray asRawArray(final SVector vec) {
    final SArray arr = new SArray(((long[]) vec.storage).length);
    for (int i = 0; i < ((long[]) vec.storage).length; i++) {
      long value = ((long[]) vec.storage)[i];
      if (value == EMPTY_SLOT) {
        arr.setIndexableField(i, Universe.current().nilObject);
      } else {
        arr.setIndexableField(i, SInteger.getInteger(value));
      }
    }
    return arr;
  }
}
