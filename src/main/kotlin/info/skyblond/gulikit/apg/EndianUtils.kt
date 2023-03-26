package info.skyblond.gulikit.apg

fun ByteArray.readUShortLE(offset: Int): UShort {
    var result: UShort = 0u
    for (i in 0 until UShort.SIZE_BYTES) {
        val t = this[offset + i].toInt() and 0x000000FF
        result = result or (t shl i * 8).toUShort()
    }
    return result
}

fun ByteArray.writeUShortLE(value: UShort, offset: Int) {
    var t = value.toInt()
    for (i in 0 until UShort.SIZE_BYTES) {
        this[offset + i] = (t and 0x000000FF).toByte()
        t = t shr 8
    }
}
