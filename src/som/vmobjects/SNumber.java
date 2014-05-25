package som.vmobjects;

import java.math.BigInteger;

import som.vm.Universe;


public abstract class SNumber extends SAbstractObject {
  public abstract SString primAsString(final Universe universe);
  public abstract SNumber primSqrt(final Universe universe);

  public abstract SNumber primAdd(final SNumber right, final Universe universe);
  public abstract SNumber primSubtract(final SNumber right, final Universe universe);
  public abstract SNumber primMultiply(final SNumber right, final Universe universe);
  public abstract SNumber primDoubleDivide(final SNumber right, final Universe universe);
  public abstract SNumber primIntegerDivide(final SNumber right, final Universe universe);
  public abstract SNumber primModulo(final SNumber right, final Universe universe);
  public abstract SNumber primBitAnd(final SNumber right, final Universe universe);
  public abstract SNumber primBitXor(final SNumber right, final Universe universe);
  public abstract SNumber primLeftShift(final SNumber right, final Universe universe);

  public abstract SObject primEqual(final SAbstractObject right, final Universe universe);
  public abstract SObject primLessThan(final SNumber right, final Universe universe);


  protected final SNumber intOrBigInt(double value, Universe universe) {
    if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
      return universe.newBigInteger(new BigInteger(Double.toString(Math.rint(value))));
    } else {
      return universe.newInteger((long) Math.rint(value));
    }
  }

  protected final SObject asSBoolean(boolean result, final Universe universe) {
    if (result) {
      return universe.trueObject;
    } else {
      return universe.falseObject;
    }
  }

}
