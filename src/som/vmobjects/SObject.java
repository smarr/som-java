package som.vmobjects;

import som.vm.Universe;


public class SObject extends SAbstractObject {

  public SObject(final SObject nilObject) {
    fields = new SAbstractObject[getDefaultNumberOfFields()];

    // Clear each and every field by putting nil into them
    for (int i = 0; i < getNumberOfFields(); i++) {
      setField(i, nilObject);
    }
  }

  public SObject(final int numberOfFields, final SObject nilObject) {
    fields = new SAbstractObject[numberOfFields];

    // Clear each and every field by putting nil into them
    for (int i = 0; i < getNumberOfFields(); i++) {
      setField(i, nilObject);
    }
  }

  public SClass getSOMClass() {
    return clazz;
  }

  public void setClass(final SClass value) {
    // Set the class of this object by writing to the field with class index
    clazz = value;
  }

  public SSymbol getFieldName(final int index) {
    // Get the name of the field with the given index
    return getSOMClass().getInstanceFieldName(index);
  }

  public int getFieldIndex(final SSymbol name) {
    // Get the index for the field with the given name
    return getSOMClass().lookupFieldIndex(name);
  }

  public int getNumberOfFields() {
    // Get the number of fields in this object
    return fields.length;
  }

  public int getDefaultNumberOfFields() {
    // Return the default number of fields in an object
    return numberOfObjectFields;
  }

  public SAbstractObject getField(final long index) {
    // Get the field with the given index
    return fields[(int) index];
  }

  public void setField(final long index, final SAbstractObject value) {
    // Set the field with the given index to the given value
    fields[(int) index] = value;
  }

  @Override
  public SClass getSOMClass(final Universe universe) {
    return clazz;
  }

  @Override
  public String toString() {
    if (clazz.getName().getEmbeddedString().equals("SObject")) {
      if (fields[1] instanceof SObject) {
        SObject somClazz = (SObject) fields[1];
        SObject nameSymbolObj = (SObject) somClazz.fields[4];
        SString nameString = (SString) nameSymbolObj.fields[0];
        return "SomSom: a " + nameString.getEmbeddedString();
      }
    }
    return "a " + getSOMClass(Universe.current()).getName().getEmbeddedString();
  }

  // Private array of fields
  private final SAbstractObject[] fields;
  private SClass                  clazz;

  // Static field indices and number of object fields
  static final int numberOfObjectFields = 0;
}
