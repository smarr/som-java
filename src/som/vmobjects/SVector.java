package som.vmobjects;

import som.vm.Universe;
import som.vmobjects.storagestrategies.svector.EmptyVectorStrategy;
import som.vmobjects.storagestrategies.svector.VectorStorageStrategy;


public class SVector extends SObject {

  public SVector(final long numElements, final SObject nilObject, final Universe universe) {
    super(3, nilObject);
    strategy = universe.getEmptyVectorStrategy();
    ((EmptyVectorStrategy) strategy).initialize(this, (int) numElements);
    first = last = 1;
  }

  public SAbstractObject getIndexableField(final long index, final SObject nilObject) {
    return strategy.getIndexableField(this, (int) index, nilObject);
  }

  public SAbstractObject getFirstIndexableField(final SObject nilObject) {
    return strategy.getFirstIndexableField(this, nilObject);
  }

  public SAbstractObject getLastIndexableField(final SObject nilObject) {
    return strategy.getLastIndexableField(this, nilObject);
  }

  public int getIndexOfElement(final SAbstractObject element) {
    return strategy.getIndexOfElement(this, element);
  }

  public boolean containsElement(final SAbstractObject element) {
    return strategy.containsElement(this, element);
  }

  public boolean setIndexableField(final long index, final SAbstractObject value) {
    final Object[] returnTuple = strategy.setIndexableFieldMaybeTransition(this, (int) index, value);
    strategy = (VectorStorageStrategy) returnTuple[0];
    return (boolean) returnTuple[1];
  }

  public void setLastIndexableField(final SAbstractObject value) {
    strategy = strategy.setLastIndexableFieldMaybeTransition(this, value);
  }

  public boolean removeElement(final SAbstractObject element) {
    return strategy.removeElement(this, element);
  }

  public SAbstractObject removeFirstElement(final SObject nilObject) {
    return strategy.removeFirstElement(this, nilObject);
  }

  public SAbstractObject removeLastElement(final SObject nilObject) {
    return strategy.removeLastElement(this, nilObject);
  }

  public boolean invalidIndex(final long index) {
    return first > index || index >= last;
  }

  public boolean isEmpty() {
    return first == last;
  }

  public int getSize() {
    return last - first;
  }

  public int getCapacity() {
    return strategy.getCapacity(this);
  }

  public int getFirstIndex() {
    return first;
  }

  public int getLastIndex() {
    return last;
  }

  public void setFirst(int first) {
    this.first = first;
  }

  public void setLast(int last) {
    this.last = last;
  }

  public SArray asArray(final SObject nilObject) {
    return strategy.asArray(this, nilObject);
  }

  @Override
  public String toString() {
    return "a " + getSOMClass(Universe.current()).getName().getEmbeddedString();
  }

  @Override
  public SAbstractObject getField(final long index) {
    switch ((int) index) {
      case 0:
        return SInteger.getInteger(first);
      case 1:
        return SInteger.getInteger(last);
      case 2:
        return strategy.asRawArray(this);
      default:
        return super.getField(index);
    }
  }

  private VectorStorageStrategy strategy;
  public Object storage;
  private int first, last;

}
