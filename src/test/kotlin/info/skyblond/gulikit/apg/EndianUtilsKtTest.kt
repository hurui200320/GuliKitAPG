package info.skyblond.gulikit.apg

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EndianUtilsKtTest {
    @Test
    fun testUShortLE() {
        val exampleBuffer = byteArrayOf(
            0x00, 0x08,
            0xF0.toByte(), 0X0F,
            0x00, // padding
            0x34, 0x12,
            0xFF.toByte(), 0x00
        )
        val testCase: List<Pair<Int, UShort>> = listOf(
            0 to 0x0800u,
            2 to 0x0FF0u,
            5 to 0x1234u,
            7 to 0x00FFu
        )
        val buffer = ByteArray(9)
        testCase.forEach {
            // test write
            buffer.writeUShortLE(it.second, it.first)
            // test read
            Assertions.assertEquals(it.second, exampleBuffer.readUShortLE(it.first))
        }
        Assertions.assertArrayEquals(exampleBuffer, buffer)
    }
}
