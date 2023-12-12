interface IByteBuffer {
    adjustPadding(predictionLength: number, beforeWriteIndex: number): void
    compatibleRead(beforeReadIndex: number, length: number): boolean
    setWriteOffset(writeOffset: number): void
    getWriteOffset(): number
    setReadOffset(readOffset: number): void
    getReadOffset(): number
    getCapacity(): number
    ensureCapacity(minCapacity: number): void
    isReadable(): boolean
    writeBytes(byteArray: ArrayBuffer): void
    toBytes(): ArrayBuffer
    writeBoolean(value: boolean): void
    readBoolean(): boolean
    writeByte(value: number): void
    readByte(): number
    writeShort(value: number): void
    readShort(): number
    writeRawInt(value: number): void
    readRawInt(): number
    writeInt(value: number): void
    writeIntCount(value: number): number
    readInt(): number
    writeLong(value: number): void
    readLong(): number
    writeFloat(value: number): void
    readFloat(): number
    writeDouble(value: number): void
    readDouble(): number
    writeString(value: string): void
    readString(): string
    writePacket(packet: any, protocolId: number): void
    readPacket(protocolId: number): any
    writeBooleanArray(array: Array<boolean> | null): void
    readBooleanArray(): boolean[]
    writeByteArray(array: Array<number> | null): void
    readByteArray(): number[]
    writeShortArray(array: Array<number> | null): void
    readShortArray(): number[]
    writeIntArray(array: Array<number> | null): void
    readIntArray(): number[]
    writeLongArray(array: Array<number> | null): void
    readLongArray(): number[]
    writeFloatArray(array: Array<number> | null): void
    readFloatArray(): number[]
    writeDoubleArray(array: Array<number> | null): void
    readDoubleArray(): number[]
    writeStringArray(array: Array<string> | null): void
    readStringArray(): string[]
    writePacketArray(array: Array<any> | null, protocolId: number): void
    readPacketArray(protocolId: number): any
    // ---------------------------------------------list-------------------------------------------
    writeBooleanList(list: Array<boolean> | null): void
    readBooleanList(): boolean[]
    writeByteList(list: Array<number> | null): void
    readByteList(): number[]
    writeShortList(list: Array<number> | null): void
    readShortList(): number[]
    writeIntList(list: Array<number> | null): void
    readIntList(): number[]
    writeLongList(list: Array<number> | null): void
    readLongList(): number[]
    writeFloatList(list: Array<number> | null): void
    readFloatList(): number[]
    writeDoubleList(list: Array<number> | null): void
    readDoubleList(): number[]
    writeStringList(list: Array<string> | null): void
    readStringList(): string[]
    writePacketList(list: Array<any> | null, protocolId: number): void
    readPacketList(protocolId: number): any[]
    // ---------------------------------------------set-------------------------------------------
    writeBooleanSet(set: Set<boolean> | null): void
    readBooleanSet(): Set<boolean>
    writeByteSet(set: Set<number> | null): void
    readByteSet(): Set<number>
    writeShortSet(set: Set<number> | null): void
    readShortSet(): Set<number>
    writeIntSet(set: Set<number> | null): void
    readIntSet(): Set<number>
    writeLongSet(set: Set<number> | null): void
    readLongSet(): Set<number>
    writeFloatSet(set: Set<number> | null): void
    readFloatSet(): Set<number>
    writeDoubleSet(set: Set<number> | null): void
    readDoubleSet(): Set<number>
    writeStringSet(set: Set<string> | null): void
    readStringSet(): Set<string>
    writePacketSet(set: Set<any> | null, protocolId: number): void
    readPacketSet(protocolId: number): Set<any>
    // ---------------------------------------------map-------------------------------------------
    writeIntIntMap(map: Map<number, number> | null): void
    readIntIntMap(): Map<number, number>
    writeIntLongMap(map: Map<number, number> | null): void
    readIntLongMap(): Map<number, number>
    writeIntStringMap(map: Map<number, string> | null): void
    readIntStringMap(): Map<number, string>
    writeIntPacketMap(map: Map<number, any> | null, protocolId: number): void
    readIntPacketMap(protocolId: number): Map<number, any>
    writeLongIntMap(map: Map<number, number> | null): void
    readLongIntMap(): Map<number, number>
    writeLongLongMap(map: Map<number, number> | null): void
    readLongLongMap(): Map<number, number>
    writeLongStringMap(map: Map<number, string> | null): void
    readLongStringMap(): Map<number, string>
    writeLongPacketMap(map: Map<number, any> | null, protocolId: number): any
    readLongPacketMap(protocolId: number): Map<number, any>
    writeStringIntMap(map: Map<string, number> | null): void
    readStringIntMap(): Map<string, number>
    writeStringLongMap(map: Map<string, number> | null): void
    readStringLongMap(): Map<string, number>
    writeStringStringMap(map: Map<string, string> | null): void
    readStringStringMap(): Map<string, string>
    writeStringPacketMap(map: Map<string, any> | null, protocolId: number): void
    readStringPacketMap(protocolId: number): Map<string, any>
}

export default IByteBuffer;