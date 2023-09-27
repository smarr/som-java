package som.vmobjects.storagestrategies.svector;

import som.vm.Universe;
import som.vmobjects.*;

public class EmptyVectorStrategy extends VectorStorageStrategy {

  private final SObject nilObject;

  public EmptyVectorStrategy(final SObject nilObject) {
    this.nilObject = nilObject;
  }

  public void initialize(final SVector vec, final int numElements) {
    vec.storage = numElements;
  }

  @Override
  public SAbstractObject getIndexableField(final SVector vec, final int index, final SObject nilObject) {
    return nilObject;
  }

  @Override
  public SAbstractObject getFirstIndexableField(final SVector vec, final SObject nilObject) {
    return nilObject;
  }

  @Override
  public SAbstractObject getLastIndexableField(final SVector vec, final SObject nilObject) {
    return nilObject;
  }

  @Override
  public int getIndexOfElement(final SVector vec, final SAbstractObject element) {
    if (vec.getSize() > 0 && element == nilObject) {
      return vec.getFirstIndex();
    } else {
      return -1;
    }
  }

  @Override
  public boolean containsElement(final SVector vec, final SAbstractObject element) {
      return vec.getSize() > 0 && element == nilObject;
  }

  @Override
  public Object[] setIndexableFieldMaybeTransition(final SVector vec, final int index, final SAbstractObject value) {
    final int storeIndex = index + vec.getFirstIndex() - 1;

    if (vec.invalidIndex(storeIndex)) {
      return new Object[] {this, false};
    }

    if (value == nilObject) {
      return new Object[] {this, true};
    }

    if (value instanceof SInteger) {
      final long embeddedInteger = ((SInteger) value).getEmbeddedInteger();

      if (embeddedInteger != IntegerVectorStrategy.EMPTY_SLOT) {
        final IntegerVectorStrategy integerVectorStrategy = Universe.current().getIntegerVectorStrategy();
        integerVectorStrategy.initialize(vec, (int) vec.storage);
        integerVectorStrategy.setIndexableFieldNoTransition(vec, index, embeddedInteger);
        return new Object[] {integerVectorStrategy, true};
      }
    }

    if (value instanceof SDouble) {
      final double embeddedDouble = ((SDouble) value).getEmbeddedDouble();

      if (embeddedDouble != DoubleVectorStrategy.EMPTY_SLOT) {
        final DoubleVectorStrategy doubleVectorStrategy = Universe.current().getDoubleVectorStrategy();
        doubleVectorStrategy.initialize(vec, (int) vec.storage);
        doubleVectorStrategy.setIndexableFieldNoTransition(vec, index, embeddedDouble);
        return new Object[] {doubleVectorStrategy, true};
      }
    }

    final AbstractObjectVectorStrategy abstractObjectVectorStrategy = Universe.current().getAbstractObjectVectorStrategy();
    abstractObjectVectorStrategy.initialize(vec, (int) vec.storage);
    abstractObjectVectorStrategy.setIndexableFieldNoTransition(vec, index, value);
    return new Object[] {abstractObjectVectorStrategy, true};
  }

  @Override
  public VectorStorageStrategy setLastIndexableFieldMaybeTransition(final SVector vec, final SAbstractObject value) {
    if (value == nilObject) {
      vec.setLast(vec.getLastIndex() + 1);
      return this;
    }

    if (value instanceof SInteger) {
      final long embeddedInteger = ((SInteger) value).getEmbeddedInteger();

      if (embeddedInteger != IntegerVectorStrategy.EMPTY_SLOT) {
        final IntegerVectorStrategy integerVectorStrategy = Universe.current().getIntegerVectorStrategy();
        integerVectorStrategy.initialize(vec, (int) vec.storage);
        integerVectorStrategy.setLastIndexableFieldNoTransition(vec, embeddedInteger);
        return integerVectorStrategy;
      }
    }

    if (value instanceof SDouble) {
      final double embeddedDouble = ((SDouble) value).getEmbeddedDouble();

      if (embeddedDouble != DoubleVectorStrategy.EMPTY_SLOT) {
        final DoubleVectorStrategy doubleVectorStrategy = Universe.current().getDoubleVectorStrategy();
        doubleVectorStrategy.initialize(vec, (int) vec.storage);
        doubleVectorStrategy.setLastIndexableFieldNoTransition(vec, embeddedDouble);
        return doubleVectorStrategy;
      }
    }

    final AbstractObjectVectorStrategy abstractObjectVectorStrategy = Universe.current().getAbstractObjectVectorStrategy();
    abstractObjectVectorStrategy.initialize(vec, (int) vec.storage);
    abstractObjectVectorStrategy.setLastIndexableFieldNoTransition(vec, value);
    return abstractObjectVectorStrategy;
  }

  @Override
  public int getCapacity(final SVector vec) {
    return (int) vec.storage;
  }

  @Override
  public boolean removeElement(final SVector vec, final SAbstractObject element) {
    if (element == nilObject) {
      return false;
    }

    vec.setFirst(1);
    vec.setLast(vec.getSize());

    return true;
  }

  @Override
  public SAbstractObject removeFirstElement(final SVector vec, final SObject nilObject) {
    if (vec.isEmpty()) {
      return null;
    } else {
      vec.setFirst(vec.getFirstIndex() + 1);
      return nilObject;
    }
  }

  @Override
  public SAbstractObject removeLastElement(final SVector vec, final  SObject nilObject) {
    if (vec.getSize() > 0) {
      vec.setLast(vec.getLastIndex() - 1);
      return nilObject;
    } else {
      return null;
    }
  }

  @Override
  public SArray asArray(final SVector vec, final SObject nilObject) {
    final SArray arr = new SArray(vec.getSize());
    for (int i = 0; i < vec.getSize(); i++) {
        arr.setIndexableField(i, nilObject);
    }
    return arr;
  }

  @Override
  public SArray asRawArray(final SVector vec) {
    final SArray arr = new SArray((int) vec.storage);
    for (int i = 0; i < (int) vec.storage; i++) {
      arr.setIndexableField(i, nilObject);
    }
    return arr;
  }

}
