package som.vmobjects;

import java.math.BigDecimal;
import java.math.BigInteger;

import som.vm.Universe;


public abstract class SNumber extends SAbstractObject {
  public abstract SString primAsString(Universe universe);

  public abstract SNumber primAsDouble(Universe universe);

  public abstract SNumber primSqrt(Universe universe);

  public abstract SNumber primAdd(SNumber right, Universe universe);

  public abstract SNumber primSubtract(SNumber right, Universe universe);

  public abstract SNumber primMultiply(SNumber right, Universe universe);

  public abstract SNumber primDoubleDivide(SNumber right, Universe universe);

  public abstract SNumber primIntegerDivide(SNumber right, Universe universe);

  public abstract SNumber primModulo(SNumber right, Universe universe);

  public abstract SNumber primBitAnd(SNumber right, Universe universe);

  public abstract SNumber primBitXor(SNumber right, Universe universe);

  public abstract SNumber primLeftShift(SNumber right, Universe universe);

  public abstract SNumber primShiftRight(SInteger right, Universe universe);

  public abstract SObject primEqual(SAbstractObject right, Universe universe);

  public abstract SObject primLessThan(SNumber right, Universe universe);

  protected final SNumber intOrBigInt(final double value, final Universe universe) {
    if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
      BigDecimal bigDecimal = new BigDecimal(value);
      return universe.newBigInteger(bigDecimal.toBigInteger());
    } else {
      return universe.newInteger((long) Math.rint(value));
    }
  }

  protected static SNumber fitIntoInteger(final double value, final Universe universe) {
    if (value >= Long.MAX_VALUE || value < Long.MIN_VALUE) {
      return universe.newBigInteger(new BigDecimal(value).toBigInteger());
    }
    return universe.newInteger((long) value);
  }

  public static SNumber asSNumber(final BigInteger result, final Universe universe) {
    if (result.bitLength() >= Long.SIZE) {
      return universe.newBigInteger(result);
    } else {
      return universe.newInteger(result.longValue());
    }
  }

  protected static BigInteger floorMod(final BigInteger left, final BigInteger right) {
    BigInteger result = left.remainder(right);
    if (result.signum() != 0 && result.signum() != right.signum()) {
      result = result.add(right);
    }
    return result;
  }

  protected final SObject asSBoolean(final boolean result, final Universe universe) {
    if (result) {
      return universe.trueObject;
    } else {
      return universe.falseObject;
    }
  }

}
