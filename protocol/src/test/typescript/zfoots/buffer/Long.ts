const INT_CACHE: Record<number, Long> = {};
const UINT_CACHE: Record<number, Long> = {};

type WasmExports = {
  mul: (low: number, high: number, mLow: number, mHigh: number) => number;
  div_s: (low: number, high: number, dLow: number, dHigh: number) => number;
  div_u: (low: number, high: number, dLow: number, dHigh: number) => number;
  rem_s: (low: number, high: number, dLow: number, dHigh: number) => number;
  rem_u: (low: number, high: number, dLow: number, dHigh: number) => number;
  get_high: () => number;
};

let wasm: WasmExports;

export class Long {
  /**
   * Signed zero.
   */
  static readonly ZERO = Long.fromInt(0);

  /**
   * Unsigned zero.
   */
  static readonly UZERO = Long.fromInt(0, true);

  /**
   * Signed one.
   */
  static readonly ONE = Long.fromInt(1);

  /**
   * Unsigned one.
   */
  static readonly UONE = Long.fromInt(1, true);

  /**
   * Signed negative one.
   */
  static readonly NEG_ONE = Long.fromInt(-1);

  /**
   * Maximum signed value.
   */
  static readonly MAX_VALUE = Long.fromBits(
    0xffffffff | 0,
    0x7fffffff | 0,
    false
  );

  /**
   * Maximum unsigned value.
   */
  static readonly MAX_UNSIGNED_VALUE = Long.fromBits(
    0xffffffff | 0,
    0xffffffff | 0,
    true
  );

  /**
   * Minimum signed value.
   */
  static readonly MIN_VALUE = Long.fromBits(0, 0x80000000 | 0, false);

  /**
   * The low 32 bits as a signed value.
   */
  low: number;

  /**
   * The high 32 bits as a signed value.
   */
  high: number;

  /**
   * Whether unsigned or not.
   */
  unsigned: boolean;

  /**
   * Constructs a 64 bit two's-complement integer, given its low and high 32 bit values as signed integers. See the from* functions below for more convenient ways of constructing Longs.
   */
  constructor(low: number, high: number, unsigned = false) {
    this.low = low | 0;
    this.high = high | 0;
    this.unsigned = unsigned;
  }

  /**
   * Converts the Long to a 32 bit integer, assuming it is a 32 bit integer.
   */
  toInt() {
    return this.unsigned ? this.low >>> 0 : this.low;
  }

  /**
   * Converts the Long to a the nearest floating-point representation of this value (double, 53 bit mantissa).
   */
  toNumber() {
    if (this.unsigned) {
      return (this.high >>> 0) * TWO_PWR_32_DBL + (this.low >>> 0);
    }
    return this.high * TWO_PWR_32_DBL + (this.low >>> 0);
  }

  /**
   * Converts the Long to a string written in the specified radix.
   */
  toString(radix = 10): string {
    if (radix < 2 || 36 < radix) {
      throw RangeError("radix");
    }
    if (this.isZero()) {
      return "0";
    }
    if (this.isNegative()) {
      // Unsigned Longs are never negative
      if (this.equals(Long.MIN_VALUE)) {
        // We need to change the Long value before it can be negated, so we remove
        // the bottom-most digit in this base and then recurse to do the rest.
        const radixLong = Long.fromNumber(radix);
        const div = this.divide(radixLong);
        const rem1 = div.multiply(radixLong).subtract(this);
        return div.toString(radix) + rem1.toInt().toString(radix);
      } else {
        return "-" + this.negate().toString(radix);
      }
    }

    // Do several (6) digits each time through the loop, so as to
    // minimize the calls to the very expensive emulated div.
    const radixToPower = Long.fromNumber(Math.pow(radix, 6), this.unsigned);
    let result = "";
    let rem = this as Long;
    while (true) {
      const remDiv = rem.divide(radixToPower);
      const intval = rem.subtract(remDiv.multiply(radixToPower)).toInt() >>> 0;
      let digits = intval.toString(radix);
      rem = remDiv;
      if (rem.isZero()) {
        return digits + result;
      } else {
        while (digits.length < 6) {
          digits = "0" + digits;
        }
        result = "" + digits + result;
      }
    }
  }

  /**
   * Gets the high 32 bits as a signed integer.
   */
  getHighBits() {
    return this.high;
  }

  /**
   * Gets the high 32 bits as an unsigned integer.
   */
  getHighBitsUnsigned() {
    return this.high >>> 0;
  }

  /**
   * Gets the low 32 bits as a signed integer.
   */
  getLowBits() {
    return this.low;
  }

  /**
   * Gets the low 32 bits as an unsigned integer.
   */
  getLowBitsUnsigned() {
    return this.low >>> 0;
  }

  /**
   * Gets the number of bits needed to represent the absolute value of this Long.
   */
  getNumBitsAbs(): number {
    if (this.isNegative()) {
      // Unsigned Longs are never negative
      return this.equals(Long.MIN_VALUE) ? 64 : this.negate().getNumBitsAbs();
    }
    const val = this.high != 0 ? this.high : this.low;
    let bit;
    for (bit = 31; bit > 0; bit--) {
      if ((val & (1 << bit)) != 0) {
        break;
      }
    }
    return this.high != 0 ? bit + 33 : bit + 1;
  }

  /**
   * Tests if this Long's value equals zero.
   */
  isZero() {
    return this.high === 0 && this.low === 0;
  }

  /**
   * Tests if this Long's value is negative.
   */
  isNegative() {
    return !this.unsigned && this.high < 0;
  }

  /**
   * Tests if this Long's value is positive.
   */
  isPositive() {
    return this.unsigned || this.high >= 0;
  }

  /**
   * Tests if this Long's value is odd.
   */
  isOdd() {
    return (this.low & 1) === 1;
  }

  /**
   * Tests if this Long's value is even.
   */
  isEven() {
    return (this.low & 1) === 0;
  }

  /**
   * Tests if this Long's value equals the specified's.
   */
  equals(other: Long | number | string) {
    if (!(other instanceof Long)) {
      other = Long.fromValue(other);
    }
    if (
      this.unsigned !== other.unsigned &&
      this.high >>> 31 === 1 &&
      other.high >>> 31 === 1
    ) {
      return false;
    }
    return this.high === other.high && this.low === other.low;
  }

  /**
   * Tests if this Long's value differs from the specified's.
   */
  notEquals(other: Long | number | string) {
    return !this.equals(other);
  }

  /**
   * Tests if this Long's value is less than the specified's.
   */
  lessThan(other: Long | number | string) {
    return this.compare(other) < 0;
  }

  /**
   * Tests if this Long's value is less than or equal the specified's.
   */
  lessThanOrEqual(other: Long | number | string) {
    return this.compare(other) <= 0;
  }

  /**
   * Tests if this Long's value is greater than the specified's.
   */
  greaterThan(other: Long | number | string) {
    return this.compare(other) > 0;
  }

  /**
   * Tests if this Long's value is greater than or equal the specified's.
   */
  greaterThanOrEqual(other: Long | number | string) {
    return this.compare(other) >= 0;
  }

  /**
   * Compares this Long's value with the specified's.
   */
  compare(other: Long | number | string) {
    if (!(other instanceof Long)) {
      other = Long.fromValue(other);
    }
    if (this.equals(other)) {
      return 0;
    }
    const thisNeg = this.isNegative();
    const otherNeg = other.isNegative();
    if (thisNeg && !otherNeg) {
      return -1;
    }
    if (!thisNeg && otherNeg) {
      return 1;
    }
    // At this point the sign bits are the same
    if (!this.unsigned) {
      return this.subtract(other).isNegative() ? -1 : 1;
    }
    // Both are positive if at least one is unsigned
    return other.high >>> 0 > this.high >>> 0 ||
    (other.high === this.high && other.low >>> 0 > this.low >>> 0)
      ? -1
      : 1;
  }

  /**
   * Negates this Long's value.
   */
  negate() {
    if (!this.unsigned && this.equals(Long.MIN_VALUE)) {
      return Long.MIN_VALUE;
    }
    return this.not().add(Long.ONE);
  }

  /**
   * Returns the sum of this and the specified Long.
   */
  add(addend: Long | number | string) {
    if (!(addend instanceof Long)) {
      addend = Long.fromValue(addend);
    }

    // Divide each number into 4 chunks of 16 bits, and then sum the chunks.

    const a48 = this.high >>> 16;
    const a32 = this.high & 0xffff;
    const a16 = this.low >>> 16;
    const a00 = this.low & 0xffff;

    const b48 = addend.high >>> 16;
    const b32 = addend.high & 0xffff;
    const b16 = addend.low >>> 16;
    const b00 = addend.low & 0xffff;

    let c48 = 0;
    let c32 = 0;
    let c16 = 0;
    let c00 = 0;
    c00 += a00 + b00;
    c16 += c00 >>> 16;
    c00 &= 0xffff;
    c16 += a16 + b16;
    c32 += c16 >>> 16;
    c16 &= 0xffff;
    c32 += a32 + b32;
    c48 += c32 >>> 16;
    c32 &= 0xffff;
    c48 += a48 + b48;
    c48 &= 0xffff;
    return Long.fromBits((c16 << 16) | c00, (c48 << 16) | c32, this.unsigned);
  }

  /**
   * Returns the difference of this and the specified Long.
   */
  subtract(subtrahend: Long | number | string) {
    if (!(subtrahend instanceof Long)) {
      subtrahend = Long.fromValue(subtrahend);
    }
    return this.add(subtrahend.negate());
  }

  /**
   * Returns the product of this and the specified Long.
   */
  multiply(multiplier: Long | number | string): Long {
    if (this.isZero()) {
      return Long.ZERO;
    }
    if (!(multiplier instanceof Long)) {
      multiplier = Long.fromValue(multiplier);
    }

    if (wasm) {
      const low = wasm.mul(
        this.low,
        this.high,
        multiplier.low,
        multiplier.high
      );
      return Long.fromBits(low, wasm.get_high(), this.unsigned);
    }

    if (multiplier.isZero()) {
      return Long.ZERO;
    }
    if (this.equals(Long.MIN_VALUE)) {
      return multiplier.isOdd() ? Long.MIN_VALUE : Long.ZERO;
    }
    if (multiplier.equals(Long.MIN_VALUE)) {
      return this.isOdd() ? Long.MIN_VALUE : Long.ZERO;
    }

    if (this.isNegative()) {
      if (multiplier.isNegative()) {
        return this.negate().multiply(multiplier.negate());
      } else {
        return this.negate().multiply(multiplier).negate();
      }
    } else if (multiplier.isNegative()) {
      return this.multiply(multiplier.negate()).negate();
    }

    // If both longs are small, use float multiplication
    if (this.lessThan(TWO_PWR_24) && multiplier.lessThan(TWO_PWR_24)) {
      return Long.fromNumber(
        this.toNumber() * multiplier.toNumber(),
        this.unsigned
      );
    }

    // Divide each long into 4 chunks of 16 bits, and then add up 4x4 products.
    // We can skip products that would overflow.

    const a48 = this.high >>> 16;
    const a32 = this.high & 0xffff;
    const a16 = this.low >>> 16;
    const a00 = this.low & 0xffff;

    const b48 = multiplier.high >>> 16;
    const b32 = multiplier.high & 0xffff;
    const b16 = multiplier.low >>> 16;
    const b00 = multiplier.low & 0xffff;

    let c48 = 0;
    let c32 = 0;
    let c16 = 0;
    let c00 = 0;
    c00 += a00 * b00;
    c16 += c00 >>> 16;
    c00 &= 0xffff;
    c16 += a16 * b00;
    c32 += c16 >>> 16;
    c16 &= 0xffff;
    c16 += a00 * b16;
    c32 += c16 >>> 16;
    c16 &= 0xffff;
    c32 += a32 * b00;
    c48 += c32 >>> 16;
    c32 &= 0xffff;
    c32 += a16 * b16;
    c48 += c32 >>> 16;
    c32 &= 0xffff;
    c32 += a00 * b32;
    c48 += c32 >>> 16;
    c32 &= 0xffff;
    c48 += a48 * b00 + a32 * b16 + a16 * b32 + a00 * b48;
    c48 &= 0xffff;
    return Long.fromBits((c16 << 16) | c00, (c48 << 16) | c32, this.unsigned);
  }

  /**
   * Returns this Long divided by the specified. The result is signed if this Long is signed or unsigned if this Long is unsigned.
   */
  divide(divisor: Long | number | string): Long {
    if (!(divisor instanceof Long)) {
      divisor = Long.fromValue(divisor);
    }
    if (divisor.isZero()) {
      throw Error("division by zero");
    }

    if (wasm) {
      // guard against signed division overflow: the largest
      // negative number / -1 would be 1 larger than the largest
      // positive number, due to two's complement.
      if (
        !this.unsigned &&
        this.high === -0x80000000 &&
        divisor.low === -1 &&
        divisor.high === -1
      ) {
        // be consistent with non-wasm code path
        return this;
      }
      const low = (this.unsigned ? wasm.div_u : wasm.div_s)(
        this.low,
        this.high,
        divisor.low,
        divisor.high
      );
      return Long.fromBits(low, wasm.get_high(), this.unsigned);
    }

    if (this.isZero()) {
      return this.unsigned ? Long.UZERO : Long.ZERO;
    }
    let approx;
    let rem;
    let res;
    if (!this.unsigned) {
      // This section is only relevant for signed longs and is derived from the
      // closure library as a whole.
      if (this.equals(Long.MIN_VALUE)) {
        if (divisor.equals(Long.ONE) || divisor.equals(Long.NEG_ONE)) {
          return Long.MIN_VALUE;
        }
        // recall that -MIN_VALUE == MIN_VALUE
        else if (divisor.equals(Long.MIN_VALUE)) {
          return Long.ONE;
        } else {
          // At this point, we have |other| >= 2, so |this/other| < |MIN_VALUE|.
          const halfThis = this.shiftRight(1);
          approx = halfThis.divide(divisor).shiftLeft(1);
          if (approx.equals(Long.ZERO)) {
            return divisor.isNegative() ? Long.ONE : Long.NEG_ONE;
          } else {
            rem = this.subtract(divisor.multiply(approx));
            res = approx.add(rem.divide(divisor));
            return res;
          }
        }
      } else if (divisor.equals(Long.MIN_VALUE)) {
        return this.unsigned ? Long.UZERO : Long.ZERO;
      }
      if (this.isNegative()) {
        if (divisor.isNegative()) {
          return this.negate().divide(divisor.negate());
        }
        return this.negate().divide(divisor).negate();
      } else if (divisor.isNegative()) {
        return this.divide(divisor.negate()).negate();
      }
      res = Long.ZERO;
    } else {
      // The algorithm below has not been made for unsigned longs. It's therefore
      // required to take special care of the MSB prior to running it.
      if (!divisor.unsigned) {
        divisor = divisor.toUnsigned();
      }
      if (divisor.greaterThan(this)) {
        return Long.UZERO;
      }
      if (divisor.greaterThan(this.shiftRightUnsigned(1))) {
        // 15 >>> 1 = 7 ; with divisor = 8 ; true
        return Long.UONE;
      }
      res = Long.UZERO;
    }

    // Repeat the following until the remainder is less than other:  find a
    // floating-point that approximates remainder / other *from below*, add this
    // into the result, and subtract it from the remainder.  It is critical that
    // the approximate value is less than or equal to the real value so that the
    // remainder never becomes negative.
    rem = this;
    while (rem.greaterThanOrEqual(divisor)) {
      // Approximate the result of division. This may be a little greater or
      // smaller than the actual value.
      approx = Math.max(1, Math.floor(rem.toNumber() / divisor.toNumber()));

      // We will tweak the approximate result by changing it in the 48-th digit or
      // the smallest non-fractional digit, whichever is larger.
      const log2 = Math.ceil(Math.log(approx) / Math.LN2);
      const delta = log2 <= 48 ? 1 : Math.pow(2, log2 - 48);
      // Decrease the approximation until it is smaller than the remainder.  Note
      // that if it is too large, the product overflows and is negative.
      let approxRes = Long.fromNumber(approx);
      let approxRem = approxRes.multiply(divisor);
      while (approxRem.isNegative() || approxRem.greaterThan(rem)) {
        approx -= delta;
        approxRes = Long.fromNumber(approx, this.unsigned);
        approxRem = approxRes.multiply(divisor);
      }

      // We know the answer can't be zero... and actually, zero would cause
      // infinite recursion since we would make no progress.
      if (approxRes.isZero()) {
        approxRes = Long.ONE;
      }

      res = res.add(approxRes);
      rem = rem.subtract(approxRem);
    }
    return res;
  }

  /**
   * Returns this Long modulo the specified.
   */
  modulo(divisor: Long | number | string) {
    if (!(divisor instanceof Long)) {
      divisor = Long.fromValue(divisor);
    }

    if (wasm) {
      const low = (this.unsigned ? wasm.rem_u : wasm.rem_s)(
        this.low,
        this.high,
        divisor.low,
        divisor.high
      );
      return Long.fromBits(low, wasm.get_high(), this.unsigned);
    }

    return this.subtract(this.divide(divisor).multiply(divisor));
  }

  /**
   * Returns the bitwise NOT of this Long.
   */
  not() {
    return Long.fromBits(~this.low, ~this.high, this.unsigned);
  }

  /**
   * Returns the bitwise AND of this Long and the specified.
   */
  and(other: Long | number | string) {
    if (!(other instanceof Long)) {
      other = Long.fromValue(other);
    }
    return Long.fromBits(
      this.low & other.low,
      this.high & other.high,
      this.unsigned
    );
  }

  /**
   * Returns the bitwise OR of this Long and the specified.
   */
  or(other: Long | number | string) {
    if (!(other instanceof Long)) {
      other = Long.fromValue(other);
    }
    return Long.fromBits(
      this.low | other.low,
      this.high | other.high,
      this.unsigned
    );
  }

  /**
   * Returns the bitwise XOR of this Long and the given one.
   */
  xor(other: Long | number | string) {
    if (!(other instanceof Long)) {
      other = Long.fromValue(other);
    }
    return Long.fromBits(
      this.low ^ other.low,
      this.high ^ other.high,
      this.unsigned
    );
  }

  /**
   * Returns this Long with bits shifted to the left by the given amount.
   */
  shiftLeft(numBits: number | Long) {
    if (numBits instanceof Long) {
      numBits = numBits.toInt();
    }
    if ((numBits &= 63) === 0) {
      return this;
    } else if (numBits < 32) {
      const lowNum: number = this.low << numBits;
      return Long.fromBits(lowNum, (this.high << numBits) | (this.low >>> (32 - numBits)), this.unsigned);
    } else {
      return Long.fromBits(0, this.low << (numBits - 32), this.unsigned);
    }
  }

  /**
   * Returns this Long with bits arithmetically shifted to the right by the given amount.
   */
  shiftRight(numBits: number | Long) {
    if (numBits instanceof Long) {
      numBits = numBits.toInt();
    }
    if ((numBits &= 63) === 0) {
      return this;
    } else if (numBits < 32) {
      return Long.fromBits(
        (this.low >>> numBits) | (this.high << (32 - numBits)),
        this.high >> numBits,
        this.unsigned
      );
    } else {
      return Long.fromBits(
        this.high >> (numBits - 32),
        this.high >= 0 ? 0 : -1,
        this.unsigned
      );
    }
  }

  /**
   * Returns this Long with bits logically shifted to the right by the given amount.
   */
  shiftRightUnsigned(numBits: number | Long) {
    if (numBits instanceof Long) {
      numBits = numBits.toInt();
    }
    if ((numBits &= 63) === 0) {
      return this;
    }
    if (numBits < 32) {
      return Long.fromBits(
        (this.low >>> numBits) | (this.high << (32 - numBits)),
        this.high >>> numBits,
        this.unsigned
      );
    }
    if (numBits === 32) {
      return Long.fromBits(this.high, 0, this.unsigned);
    }
    return Long.fromBits(this.high >>> (numBits - 32), 0, this.unsigned);
  }

  /**
   * Returns this Long with bits rotated to the left by the given amount.
   */
  rotateLeft(numBits: number | Long) {
    let b;
    if (numBits instanceof Long) {
      numBits = numBits.toInt();
    }
    if ((numBits &= 63) === 0) {
      return this;
    }
    if (numBits === 32) {
      return Long.fromBits(this.high, this.low, this.unsigned);
    }
    if (numBits < 32) {
      b = 32 - numBits;
      return Long.fromBits(
        (this.low << numBits) | (this.high >>> b),
        (this.high << numBits) | (this.low >>> b),
        this.unsigned
      );
    }
    numBits -= 32;
    b = 32 - numBits;
    return Long.fromBits(
      (this.high << numBits) | (this.low >>> b),
      (this.low << numBits) | (this.high >>> b),
      this.unsigned
    );
  }

  /**
   * Returns this Long with bits rotated to the right by the given amount.
   */
  rotateRight(numBits: number | Long) {
    let b;
    if (numBits instanceof Long) {
      numBits = numBits.toInt();
    }
    if ((numBits &= 63) === 0) {
      return this;
    }
    if (numBits === 32) {
      return Long.fromBits(this.high, this.low, this.unsigned);
    }
    if (numBits < 32) {
      b = 32 - numBits;
      return Long.fromBits(
        (this.high << b) | (this.low >>> numBits),
        (this.low << b) | (this.high >>> numBits),
        this.unsigned
      );
    }
    numBits -= 32;
    b = 32 - numBits;
    return Long.fromBits(
      (this.low << b) | (this.high >>> numBits),
      (this.high << b) | (this.low >>> numBits),
      this.unsigned
    );
  }

  /**
   * Converts this Long to signed.
   */
  toSigned() {
    if (!this.unsigned) {
      return this;
    }
    return Long.fromBits(this.low, this.high, false);
  }

  /**
   * Converts this Long to unsigned.
   */
  toUnsigned() {
    if (this.unsigned) {
      return this;
    }
    return Long.fromBits(this.low, this.high, true);
  }

  /**
   * Converts this Long to its byte representation.
   */
  toBytes(le?: boolean) {
    return le ? this.toBytesLE() : this.toBytesBE();
  }

  /**
   * Converts this Long to its little endian byte representation.
   */
  toBytesLE() {
    const hi = this.high;
    const lo = this.low;
    return [
      lo & 0xff,
      (lo >>> 8) & 0xff,
      (lo >>> 16) & 0xff,
      lo >>> 24,
      hi & 0xff,
      (hi >>> 8) & 0xff,
      (hi >>> 16) & 0xff,
      hi >>> 24,
    ];
  }

  /**
   * Converts this Long to its big endian byte representation.
   */
  toBytesBE() {
    const hi = this.high;
    const lo = this.low;
    return [
      hi >>> 24,
      (hi >>> 16) & 0xff,
      (hi >>> 8) & 0xff,
      hi & 0xff,
      lo >>> 24,
      (lo >>> 16) & 0xff,
      (lo >>> 8) & 0xff,
      lo & 0xff,
    ];
  }

  /**
   * Creates a Long from its byte representation.
   */
  static fromBytes(bytes: number[], unsigned?: boolean, le?: boolean) {
    return le
      ? Long.fromBytesLE(bytes, unsigned)
      : Long.fromBytesBE(bytes, unsigned);
  }

  /**
   * Creates a Long from its little endian byte representation.
   */
  static fromBytesLE(bytes: number[], unsigned?: boolean) {
    return new Long(
      bytes[0] | (bytes[1] << 8) | (bytes[2] << 16) | (bytes[3] << 24),
      bytes[4] | (bytes[5] << 8) | (bytes[6] << 16) | (bytes[7] << 24),
      unsigned
    );
  }

  /**
   * Creates a Long from its big endian byte representation.
   */
  static fromBytesBE(bytes: number[], unsigned?: boolean) {
    return new Long(
      (bytes[4] << 24) | (bytes[5] << 16) | (bytes[6] << 8) | bytes[7],
      (bytes[0] << 24) | (bytes[1] << 16) | (bytes[2] << 8) | bytes[3],
      unsigned
    );
  }

  /**
   * Returns a Long representing the given 32 bit integer value.
   */
  static fromInt(value: number, unsigned?: boolean) {
    let cache;
    if (unsigned) {
      value >>>= 0;
      if ((cache = 0 <= value && value < 256)) {
        const cachedObj = UINT_CACHE[value];
        if (cachedObj) {
          return cachedObj;
        }
      }
      const obj = Long.fromBits(value, (value | 0) < 0 ? -1 : 0, true);
      if (cache) {
        UINT_CACHE[value] = obj;
      }
      return obj;
    } else {
      value |= 0;
      if ((cache = -128 <= value && value < 128)) {
        const cachedObj = INT_CACHE[value];
        if (cachedObj) {
          return cachedObj;
        }
      }
      const obj = Long.fromBits(value, value < 0 ? -1 : 0, false);
      if (cache) {
        INT_CACHE[value] = obj;
      }
      return obj;
    }
  }

  /**
   * Returns a Long representing the given value, provided that it is a finite number. Otherwise, zero is returned.
   */
  static fromNumber(value: number, unsigned?: boolean): Long {
    if (isNaN(value)) {
      return unsigned ? Long.UZERO : Long.ZERO;
    }
    if (unsigned) {
      if (value < 0) {
        return Long.UZERO;
      }
      if (value >= TWO_PWR_64_DBL) {
        return Long.MAX_UNSIGNED_VALUE;
      }
    } else {
      if (value <= -TWO_PWR_63_DBL) {
        return Long.MIN_VALUE;
      }
      if (value + 1 >= TWO_PWR_63_DBL) {
        return Long.MAX_VALUE;
      }
    }
    if (value < 0) {
      return Long.fromNumber(-value, unsigned).negate();
    }
    return Long.fromBits(
      value % TWO_PWR_32_DBL | 0,
      (value / TWO_PWR_32_DBL) | 0,
      unsigned
    );
  }

  /**
   * Returns a Long representing the 64 bit integer that comes by concatenating the given low and high bits. Each is assumed to use 32 bits.
   */
  static fromBits(lowBits: number, highBits: number, unsigned?: boolean) {
    return new Long(lowBits, highBits, unsigned);
  }

  /**
   * Returns a Long representation of the given string, written using the specified radix.
   */
  static fromString(
    str: string,
    unsigned?: boolean | number,
    radix?: number
  ): Long {
    if (str.length === 0) {
      throw Error("empty string");
    }
    if (
      str === "NaN" ||
      str === "Infinity" ||
      str === "+Infinity" ||
      str === "-Infinity"
    ) {
      return Long.ZERO;
    }
    if (typeof unsigned === "number") {
      // For goog.math.long compatibility
      (radix = unsigned), (unsigned = false);
    } else {
      unsigned = !!unsigned;
    }
    radix = radix || 10;
    if (radix < 2 || 36 < radix) {
      throw RangeError("radix");
    }

    let p;
    if ((p = str.indexOf("-")) > 0) {
      throw Error("interior hyphen");
    } else if (p === 0) {
      return Long.fromString(str.substring(1), unsigned, radix).negate();
    }

    // Do several (8) digits each time through the loop, so as to
    // minimize the calls to the very expensive emulated div.
    const radixToPower = Long.fromNumber(Math.pow(radix, 8));

    let result = Long.ZERO;
    for (let i = 0; i < str.length; i += 8) {
      const size = Math.min(8, str.length - i);
      const value = parseInt(str.substring(i, i + size), radix);
      if (size < 8) {
        const power = Long.fromNumber(Math.pow(radix, size));
        result = result.multiply(power).add(Long.fromNumber(value));
      } else {
        result = result.multiply(radixToPower);
        result = result.add(Long.fromNumber(value));
      }
    }
    result.unsigned = unsigned;
    return result;
  }

  /**
   * Converts the specified value to a Long using the appropriate from* function for its type.
   */
  static fromValue(
    val:
      | Long
      | number
      | string
      | { low: number; high: number; unsigned: boolean },
    unsigned?: boolean
  ) {
    if (typeof val === "number") {
      return Long.fromNumber(val, unsigned);
    }
    if (typeof val === "string") {
      return Long.fromString(val, unsigned);
    }
    return Long.fromBits(
      val.low,
      val.high,
      typeof unsigned === "boolean" ? unsigned : val.unsigned
    );
  }
}

const TWO_PWR_16_DBL = 1 << 16;
const TWO_PWR_24_DBL = 1 << 24;
const TWO_PWR_32_DBL = TWO_PWR_16_DBL * TWO_PWR_16_DBL;
const TWO_PWR_64_DBL = TWO_PWR_32_DBL * TWO_PWR_32_DBL;
const TWO_PWR_63_DBL = TWO_PWR_64_DBL / 2;
const TWO_PWR_24 = Long.fromInt(TWO_PWR_24_DBL);
