// from protobuf
import Long from './long.js';

/**
 * Constructs new long bits.
 * @classdesc Helper class for working with the low and high bits of a 64 bit value.
 * @memberof util
 * @constructor
 * @param {number} lo Low 32 bits, unsigned
 * @param {number} hi High 32 bits, unsigned
 */
function LongBits(lo, hi) {
    // note that the casts below are theoretically unnecessary as of today, but older statically
    // generated converter code might still call the ctor with signed 32bits. kept for compat.

    /**
     * Low bits.
     * @type {number}
     */
    this.lo = lo >>> 0;

    /**
     * High bits.
     * @type {number}
     */
    this.hi = hi >>> 0;
}

/**
 * Zig-zag encodes this long bits.
 * @returns {util.LongBits} `this`
 */
LongBits.prototype.zzEncode = function zzEncode() {
    const mask = this.hi >> 31;
    this.hi = ((this.hi << 1 | this.lo >>> 31) ^ mask) >>> 0;
    this.lo = (this.lo << 1 ^ mask) >>> 0;
    return this;
};

/**
 * Zig-zag decodes this long bits.
 * @returns {util.LongBits} `this`
 */
LongBits.prototype.zzDecode = function zzDecode() {
    const mask = -(this.lo & 1);
    this.lo = ((this.lo >>> 1 | this.hi << 31) ^ mask) >>> 0;
    this.hi = (this.hi >>> 1 ^ mask) >>> 0;
    return this;
};

/**
 * Converts this long bits to a long.
 * @param {boolean} [unsigned=false] Whether unsigned or not
 * @returns {Long} Long
 */
LongBits.prototype.toLong = function toLong(unsigned) {
    return new Long(this.lo | 0, this.hi | 0, Boolean(unsigned));
};

/**
 * Zero bits.
 * @memberof util.LongBits
 * @type {util.LongBits}
 */
const zero = LongBits.zero = new LongBits(0, 0);

function from(value) {
    if (typeof value === 'number') {
        return fromNumber(value);
    }
    if (typeof value === 'string' || value instanceof String) {
        value = Long.fromString(value);
    }
    return value.low || value.high ? new LongBits(value.low >>> 0, value.high >>> 0) : zero;
}


/**
 * Constructs new long bits from the specified number.
 * @param {number} value Value
 * @returns {util.LongBits} Instance
 */
function fromNumber(value) {
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
    return new LongBits(lo, hi);
}

function writeVarint64(byteBuffer, value) {
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

function readLongVarint(buffer) {
    // tends to deopt with local vars for octet etc.
    const bits = new LongBits(0, 0);
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


export function writeInt64(byteBuffer, value) {
    const bits = from(value).zzEncode();
    writeVarint64(byteBuffer, bits);
}

export function readInt64(buffer) {
    return readLongVarint(buffer).zzDecode().toLong(false);
}
