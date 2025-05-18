package som.vmobjects.storagestrategies.svector;

import som.vmobjects.SAbstractObject;
import som.vmobjects.SArray;
import som.vmobjects.SObject;
import som.vmobjects.SVector;

public abstract class VectorStorageStrategy {

  public abstract SAbstractObject getIndexableField(SVector vec, int index, SObject nilObject);
  public abstract SAbstractObject getFirstIndexableField(SVector vec, SObject nilObject);
  public abstract SAbstractObject getLastIndexableField(SVector vec, SObject nilObject);
  public abstract int getIndexOfElement(SVector vec, SAbstractObject element);
  public abstract boolean containsElement(SVector vec, SAbstractObject element);
  public abstract Object[] setIndexableFieldMaybeTransition(SVector vec, int index, SAbstractObject value);
  public abstract VectorStorageStrategy setLastIndexableFieldMaybeTransition(SVector vec, SAbstractObject value);
  public abstract int getCapacity(SVector vec);
  public abstract boolean removeElement(SVector vec, SAbstractObject element);
  public abstract SAbstractObject removeFirstElement(SVector vec, SObject nilObject);
  public abstract SAbstractObject removeLastElement(SVector vec, SObject nilObject);
  public abstract SArray asArray(SVector vec, SObject nilObject);

  public abstract SArray asRawArray(SVector vec);
}
