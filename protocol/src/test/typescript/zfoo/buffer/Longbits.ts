// from https://github.com/hornta/long-ts
import {Long} from "./Long";


/**
 * Constructs new long bits.
 * @classdesc Helper class for working with the low and high bits of a 64 bit value.
 * @memberof util
 * @constructor
 * @param {number} lo Low 32 bits, unsigned
 * @param {number} hi High 32 bits, unsigned
 */
class Longbits {
  lo: number;
  hi: number;

  constructor(lo:number, hi:number) {
    this.lo = lo >>> 0;
    this.hi = hi >>> 0;
  }

  /**
   * Zig-zag encodes this long bits.
   */
  zzEncode(): Longbits {
    const mask = this.hi >> 31;
    this.hi = ((this.hi << 1 | this.lo >>> 31) ^ mask) >>> 0;
    this.lo = (this.lo << 1 ^ mask) >>> 0;
    return this;
  };

  /**
   * Zig-zag decodes this long bits.
   */
  zzDecode(): Longbits {
    const mask = -(this.lo & 1);
    this.lo = ((this.lo >>> 1 | this.hi << 31) ^ mask) >>> 0;
    this.hi = (this.hi >>> 1 ^ mask) >>> 0;
    return this;
  };


  /**
   * Converts this long bits to a long.
   */
  toLong(unsigned: boolean): Long {
    return new Long(this.lo | 0, this.hi | 0, Boolean(unsigned));
  };

  /**
   * Constructs new long bits from the specified number.
   */
  static from(value: any) {
    if (typeof value === 'number') {
      return Longbits.fromNumber(value as number);
    }
    if (typeof value === 'string' || value instanceof String) {
      value = Long.fromString(value as string);
    }
    return value.low || value.high ? new Longbits(value.low >>> 0, value.high >>> 0) : zero;
  }

  /**
   * Constructs new long bits from the specified number.
   */
  static fromNumber(value: number): Longbits {
    if (value === 0) {
      return zero;
    }
    const sign = value < 0;
    if (sign) {
      value = -value;
    }
    let lo = value >>> 0;
    let hi = (value - lo) / 4294967296 >>> 0;
    if (sign) {
      hi = ~hi >>> 0;
      lo = ~lo >>> 0;
      if (++lo > 4294967295) {
        lo = 0;
        if (++hi > 4294967295) {
          hi = 0;
        }
      }
    }
    return new Longbits(lo, hi);
  }
}



/**
 * Zero bits.
 */
const zero = new Longbits(0, 0);


function writeVarint64(byteBuffer: any, value: Longbits) {
    let count = 0;
    while (value.hi) {
        byteBuffer.writeByte(value.lo & 127 | 128);
        value.lo = (value.lo >>> 7 | value.hi << 25) >>> 0;
        value.hi >>>= 7;
        count = count + 7;
    }
    while (value.lo > 127) {
        if (count >= 56) {
            byteBuffer.writeByte(value.lo);
            return;
        }
        byteBuffer.writeByte(value.lo & 127 | 128);
        value.lo = value.lo >>> 7;
        count = count + 7;
    }
    byteBuffer.writeByte(value.lo);
}

function readLongVarint(buffer: any) {
    // tends to deopt with local vars for octet etc.
    const bits = new Longbits(0, 0);
    let i = 0;
    const len = buffer.length;
    let pos = 0;
    if (len - pos > 4) { // fast route (lo)
        for (; i < 4; ++i) {
            // 1st..4th
            bits.lo = (bits.lo | (buffer[pos] & 127) << i * 7) >>> 0;
            if (buffer[pos++] < 128) {
                return bits;
            }
        }
        // 5th
        bits.lo = (bits.lo | (buffer[pos] & 127) << 28) >>> 0;
        bits.hi = (bits.hi | (buffer[pos] & 127) >> 4) >>> 0;
        if (buffer[pos++] < 128) {
            return bits;
        }
        i = 0;
    } else {
        for (; i < 3; ++i) {
            // 1st..3th
            bits.lo = (bits.lo | (buffer[pos] & 127) << i * 7) >>> 0;
            if (buffer[pos++] < 128) {
                return bits;
            }
        }
        // 4th
        bits.lo = (bits.lo | (buffer[pos++] & 127) << i * 7) >>> 0;
        return bits;
    }

    // 6th..9th
    for (; i < 4; ++i) {
        // 最后一位直接写入
        if (pos === 8) {
            bits.hi = (bits.hi | buffer[pos] << i * 7 + 3) >>> 0;
            return bits;
        }
        bits.hi = (bits.hi | (buffer[pos] & 127) << i * 7 + 3) >>> 0;
        if (buffer[pos++] < 128) {
            return bits;
        }
    }

    return bits;
}

export function writeInt64(byteBuffer: any, value: any) {
    const bits = Longbits.from(value).zzEncode();
    writeVarint64(byteBuffer, bits);
}

export function readInt64(buffer: any): Long {
    return readLongVarint(buffer).zzDecode().toLong(false);
}
