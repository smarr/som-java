package som.vmobjects.storagestrategies.svector;

import som.vm.Universe;
import som.vmobjects.*;

import java.util.Arrays;

public class DoubleVectorStrategy extends VectorStorageStrategy {

  // Magic value used to indicate an empty element
  // Vector is transitioned to an AbstractObjectStrategy if the magic value is ever inserted
  public static final double EMPTY_SLOT = Double.MIN_VALUE + 2L;

  public void initialize(final SVector vec, final int numElements) {
    initializeAll(vec, EMPTY_SLOT, numElements);
  }

  public void initializeAll(final SVector vec, final double value, final int numElements) {
    final double[] storage = new double[numElements];
    Arrays.fill(storage, value);
    vec.storage = storage;
  }

  @Override
  public SAbstractObject getIndexableField(final SVector vec, final int index, final SObject nilObject) {
    final int storeIndex = index + vec.getFirstIndex() - 1;

    if (vec.invalidIndex(storeIndex)) {
      return null;
    }

    if (((double[]) vec.storage)[storeIndex - 1] == EMPTY_SLOT) {
      return nilObject;
    } else {
      return new SDouble(((double[]) vec.storage)[storeIndex - 1]);
    }
  }

  @Override
  public SAbstractObject getFirstIndexableField(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      return new SDouble(((double[]) vec.storage)[vec.getFirstIndex() - 1]);
    } else {
      return nilObject;
    }
  }

  @Override
  public SAbstractObject getLastIndexableField(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      return new SDouble(((double[]) vec.storage)[vec.getLastIndex() - 2]);
    } else {
      return nilObject;
    }
  }

  @Override
  public int getIndexOfElement(final SVector vec, final SAbstractObject element) {
    if (element instanceof SDouble) {
      final double embeddedDouble = ((SDouble) element).getEmbeddedDouble();

      for (int i = 0; i < ((double[]) vec.storage).length; i++) {
        if (((double[]) vec.storage)[i] == embeddedDouble) {
          return i + 2 - vec.getFirstIndex();
        }
      }
    }

    return -1;
  }

  @Override
  public boolean containsElement(final SVector vec, final SAbstractObject element) {
    if (element instanceof SDouble) {
      final double embeddedDouble = (((SDouble) element).getEmbeddedDouble());

      for (int i = vec.getFirstIndex(); i <+ vec.getLastIndex() - 1; i++) {
        if (((double[]) vec.storage)[i - 1] == embeddedDouble) {
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

    if (value instanceof SDouble) {
      final double embeddedDouble = ((SDouble) value).getEmbeddedDouble();

      if (embeddedDouble != EMPTY_SLOT) {
        ((double[]) vec.storage)[storeIndex - 1] = embeddedDouble;
        return new Object[] {this, true};
      }
    }

    final AbstractObjectVectorStrategy abstractObjectVectorStrategy = Universe.current().getAbstractObjectVectorStrategy();
    abstractObjectVectorStrategy.initialize(vec, (double[]) vec.storage);
    abstractObjectVectorStrategy.setIndexableFieldNoTransition(vec, index, value);
    return new Object[] {abstractObjectVectorStrategy, true};
  }

  public void setIndexableFieldNoTransition(final SVector vec, final int index, final double value) {
    ((double[]) vec.storage)[index + vec.getFirstIndex() - 2] = value;
  }

  @Override
  public VectorStorageStrategy setLastIndexableFieldMaybeTransition(final SVector vec, final SAbstractObject value) {
    if (vec.getLastIndex() > ((double[]) vec.storage).length) {
      final double[] newStorage = new double[2 * ((double[]) vec.storage).length];
      System.arraycopy(((double[]) vec.storage), 0, newStorage,0, ((double[]) vec.storage).length);
      vec.storage = newStorage;
    }

    if (value instanceof SDouble) {
      final double embeddedDouble = ((SDouble) value).getEmbeddedDouble();

      if (embeddedDouble != EMPTY_SLOT) {
        ((double[]) vec.storage)[vec.getLastIndex() - 1] = embeddedDouble;
        vec.setLast(vec.getLastIndex() + 1);
        return this;
      }
    }

    final AbstractObjectVectorStrategy abstractObjectVectorStrategy = Universe.current().getAbstractObjectVectorStrategy();
    abstractObjectVectorStrategy.initialize(vec, (double[]) vec.storage);
    abstractObjectVectorStrategy.setLastIndexableFieldNoTransition(vec, value);
    return abstractObjectVectorStrategy;
  }

  public void setLastIndexableFieldNoTransition(final SVector vec, final double value) {
    if (vec.getLastIndex() > ((double[]) vec.storage).length) {
      final double[] newStorage = new double[2 * ((double[]) vec.storage).length];
      System.arraycopy(((double[]) vec.storage), 0, newStorage, 0, ((double[]) vec.storage).length);
      vec.storage = newStorage;
    }

    ((double[]) vec.storage)[vec.getLastIndex() - 1] = value;
    vec.setLast(vec.getLastIndex() + 1);
  }

  @Override
  public int getCapacity(final SVector vec) {
    return ((double[]) vec.storage).length;
  }

  @Override
  public boolean removeElement(final SVector vec, final SAbstractObject element) {
    if (!(element instanceof SDouble)) {
      return false;
    }

    final double embeddedDouble = ((SDouble) element).getEmbeddedDouble();
    final double[] newStorage = new double[((double[]) vec.storage).length];
    int newLast = 1;
    boolean found = false;

    for (int i = vec.getFirstIndex(); i < vec.getLastIndex(); i++) {
      if (((double[]) vec.storage)[i] == embeddedDouble) {
        found = true;
      } else {
        newStorage[i] = ((double[]) vec.storage)[i];
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
      final double value = ((double[]) vec.storage)[vec.getFirstIndex() - 1];
      ((double[]) vec.storage)[vec.getFirstIndex() - 1] = EMPTY_SLOT;
      vec.setFirst(vec.getFirstIndex() + 1);
      return value == EMPTY_SLOT ? nilObject : new SDouble(value);
    }
  }

  @Override
  public SAbstractObject removeLastElement(final SVector vec, final SObject nilObject) {
    if (vec.getSize() > 0) {
      vec.setLast(vec.getLastIndex() - 1);
      final double value = ((double[]) vec.storage)[vec.getLastIndex() - 1];
      ((double[]) vec.storage)[vec.getLastIndex() - 1] = EMPTY_SLOT;
      return value == EMPTY_SLOT ? nilObject : new SDouble(value);
    } else {
      return null;
    }
  }

  @Override
  public SArray asArray(final SVector vec, final SObject nilObject) {
    final SArray arr = new SArray(vec.getSize());
    for (int i = 0; i < vec.getSize(); i++) {
      double value = ((double[]) vec.storage)[vec.getFirstIndex() + 1 - 1];
      if (value == EMPTY_SLOT) {
        arr.setIndexableField(i, nilObject);
      } else {
        arr.setIndexableField(i, new SDouble(value));
      }
    }
    return arr;
  }

  @Override
  public SArray asRawArray(final SVector vec) {
    final SArray arr = new SArray(((double[]) vec.storage).length);
    for (int i = 0; i < ((double[]) vec.storage).length; i++) {
      double value = ((double[]) vec.storage)[i];
      if (value == EMPTY_SLOT) {
        arr.setIndexableField(i, Universe.current().nilObject);
      } else {
        arr.setIndexableField(i, new SDouble(value));
      }
    }
    return arr;
  }
}
