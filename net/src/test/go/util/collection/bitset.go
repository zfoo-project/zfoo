/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package bitset

import (
	"bytes"
	"encoding/hex"
	"errors"
	"fmt"
	"math/bits"
	"sync"

)

// BitSet bit set
type BitSet struct {
	set []byte
	mu  sync.RWMutex
}

// New creates a bit set object.
func New(init ...byte) *BitSet {
	return &BitSet{set: init}
}

// NewFromHex creates a bit set object from hex string.
func NewFromHex(s string) (*BitSet, error) {
	init, err := hex.DecodeString(s)
	if err != nil {
		return nil, err
	}
	return &BitSet{set: init}, nil
}

// Set sets the bit bool value on the specified offset,
// and returns the value of before setting.
// NOTE:
//  0 means the 1st bit, -1 means the bottom 1th bit, -2 means the bottom 2th bit and so on;
//  If offset>=len(b.set), automatically grow the bit set;
//  If the bit offset is out of the left range, returns error.
func (b *BitSet) Set(offset int, value bool) (bool, error) {
	b.mu.Lock()
	defer b.mu.Unlock()
	size := b.size()
	// 0 means the 1st bit, -1 means the bottom 1th bit,
	// -2 means the bottom 2th bit and so on.
	if offset < 0 {
		offset += size
	}
	if offset < 0 {
		return false, errors.New("the bit offset is out of the left range")
	}

	// the bit group index
	gi := offset / 8
	// the bit index of in the group
	bi := offset % 8

	// if the bit offset is out of the right range, automatically grow.
	if gi >= len(b.set) {
		newSet := make([]byte, gi+1)
		copy(newSet, b.set)
		b.set = newSet
	}

	gb := b.set[gi]
	rOff := byte(7 - bi)
	var mask byte = 1 << rOff
	oldVal := gb & mask >> rOff
	if (oldVal == 1) != value {
		if oldVal == 1 {
			b.set[gi] = gb &^ mask
		} else {
			b.set[gi] = gb | mask
		}
	}
	return oldVal == 1, nil
}

// Get gets the bit bool value on the specified offset.
// NOTE:
//  0 means the 1st bit, -1 means the bottom 1th bit, -2 means the bottom 2th bit and so on;
//  If offset>=len(b.set), returns false.
func (b *BitSet) Get(offset int) bool {
	b.mu.RLock()
	defer b.mu.RUnlock()
	size := b.size()
	// 0 means the 1st bit, -1 means the bottom 1th bit,
	// -2 means the bottom 2th bit and so on.
	if offset < 0 {
		offset += size
	}
	if offset < 0 || offset >= size {
		return false
	}
	return getBit(b.set[offset/8], byte(offset%8)) == 1
}

// Range calls f sequentially for each bit present in the bit set.
// If f returns false, range stops the iteration.
func (b *BitSet) Range(f func(offset int, truth bool) bool) {
	b.mu.RLock()
	defer b.mu.RUnlock()
	size := b.size()
	if size == 0 {
		return
	}
	for offset := 0; offset < size; offset++ {
		if !f(offset, getBit(b.set[offset/8], byte(offset%8)) == 1) {
			return
		}
	}
}

func getBit(gb, offset byte) byte {
	var rOff = 7 - offset
	var mask byte = 1 << rOff
	return gb & mask >> rOff
}

// Count counts the amount of bit set to 1 within the specified range of the bit set.
// NOTE:
//  0 means the 1st bit, -1 means the bottom 1th bit, -2 means the bottom 2th bit and so on.
func (b *BitSet) Count(start, end int) int {
	b.mu.RLock()
	defer b.mu.RUnlock()
	sgi, sbi, egi, ebi, valid := b.validRange(start, end)
	if !valid {
		return 0
	}
	var n int
	n += bits.OnesCount8(b.set[sgi] << sbi)
	for _, v := range b.set[sgi+1 : egi] {
		n += bits.OnesCount8(v)
	}
	n += bits.OnesCount8(b.set[egi] >> (7 - ebi) << (7 - ebi))
	return n
}

func (b *BitSet) validRange(start, end int) (sgi, sbi, egi, ebi uint, valid bool) {
	size := b.size()
	if start < 0 {
		start += size
	}
	if start >= size {
		return
	}
	if start < 0 {
		start = 0
	}
	if end < 0 {
		end += size
	}
	if end >= size {
		end = size - 1
	}
	if start > end {
		return
	}
	valid = true
	sgi, sbi = uint(start/8), uint(start%8)
	egi, ebi = uint(end/8), uint(end%8)
	return
}

// Not returns ^b.
func (b *BitSet) Not() *BitSet {
	b.mu.RLock()
	defer b.mu.RUnlock()
	rBitSet := &BitSet{
		set: make([]byte, len(b.set)),
	}
	for i, gb := range b.set {
		rBitSet.set[i] = ^gb
	}
	return rBitSet
}

// And returns all the "AND" bit sets.
// NOTE:
//  If the bitSets are empty, returns b.
func (b *BitSet) And(bitSets ...*BitSet) *BitSet {
	b.mu.RLock()
	defer b.mu.RUnlock()
	if len(bitSets) == 0 {
		return b
	}
	var (
		maxLen  = len(b.set)
		minLen  = maxLen
		currLen int
	)
	for _, g := range bitSets {
		g.mu.RLock()
		defer g.mu.RUnlock()

		currLen = len(g.set)
		if currLen > maxLen {
			maxLen = currLen
		} else if currLen < minLen {
			minLen = currLen
		}
	}
	rBitSet := &BitSet{
		set: make([]byte, maxLen),
	}
	for i := range rBitSet.set[:minLen] {
		rBitSet.set[i] = b.set[i]
		for _, g := range bitSets {
			rBitSet.set[i] &= g.set[i]
		}
	}
	return rBitSet
}

// Or returns all the "OR" bit sets.
// NOTE:
//  If the bitSets are empty, returns b.
func (b *BitSet) Or(bitSet ...*BitSet) *BitSet {
	return b.op("|", bitSet)
}

// Xor returns all the "XOR" bit sets.
// NOTE:
//  If the bitSets are empty, returns b.
func (b *BitSet) Xor(bitSet ...*BitSet) *BitSet {
	return b.op("^", bitSet)
}

// AndNot returns all the "&^" bit sets.
// NOTE:
//  If the bitSets are empty, returns b.
func (b *BitSet) AndNot(bitSet ...*BitSet) *BitSet {
	return b.op("&^", bitSet)
}

func (b *BitSet) op(op string, bitSets []*BitSet) *BitSet {
	if len(bitSets) == 0 {
		return b
	}
	b.mu.RLock()
	defer b.mu.RUnlock()
	var (
		maxLen, currLen = len(b.set), 0
	)
	for _, g := range bitSets {
		g.mu.RLock()
		defer g.mu.RUnlock()
		currLen = len(g.set)
		if currLen > maxLen {
			maxLen = currLen
		}
	}
	rBitSet := &BitSet{
		set: make([]byte, maxLen),
	}
	copy(rBitSet.set, b.set)
	for _, g := range bitSets {
		for i, gb := range g.set {
			switch op {
			case "|":
				rBitSet.set[i] = rBitSet.set[i] | gb
			case "^":
				rBitSet.set[i] = rBitSet.set[i] ^ gb
			case "&^":
				rBitSet.set[i] = rBitSet.set[i] &^ gb
			}
		}
	}
	return rBitSet
}

// Clear clears the bit set.
func (b *BitSet) Clear() {
	b.mu.Lock()
	for i := range b.set {
		b.set[i] = 0
	}
	b.mu.Unlock()
}

// Size returns the bits size.
func (b *BitSet) Size() int {
	b.mu.RLock()
	size := b.size()
	b.mu.RUnlock()
	return size
}

func (b *BitSet) size() int {
	size := len(b.set) * 8
	if size/8 != len(b.set) {
		panic("overflow when calculating the bit set size")
	}
	return size
}

// Bytes returns the bit set copy bytes.
func (b *BitSet) Bytes() []byte {
	b.mu.RLock()
	set := make([]byte, len(b.set))
	copy(set, b.set)
	b.mu.RUnlock()
	return set
}


// String returns the bit set by hex type.
func (b *BitSet) String() string {
	b.mu.RLock()
	defer b.mu.RUnlock()
	return hex.EncodeToString(b.set)
}

// Sub returns the bit subset within the specified range of the bit set.
// NOTE:
//  0 means the 1st bit, -1 means the bottom 1th bit, -2 means the bottom 2th bit and so on.
func (b *BitSet) Sub(start, end int) *BitSet {
	b.mu.RLock()
	defer b.mu.RUnlock()
	newBitSet := &BitSet{
		set: make([]byte, 0, len(b.set)),
	}
	sgi, sbi, egi, ebi, valid := b.validRange(start, end)
	if !valid {
		return newBitSet
	}
	pre := b.set[sgi] << sbi
	for _, v := range b.set[sgi+1 : egi] {
		newBitSet.set = append(newBitSet.set, pre|v>>(7-sbi))
		pre = v << sbi
	}
	last := b.set[egi] >> (7 - ebi) << (7 - ebi)
	newBitSet.set = append(newBitSet.set, pre|last>>(7-sbi))
	if sbi < ebi {
		newBitSet.set = append(newBitSet.set, last<<sbi)
	}
	return newBitSet
}
