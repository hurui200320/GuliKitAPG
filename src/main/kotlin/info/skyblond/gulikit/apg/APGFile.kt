package info.skyblond.gulikit.apg

import info.skyblond.gulikit.generator.SplatoonCanvas
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class APGFile : Iterable<ControlWord> {
    companion object {
        const val MAX_CONTROL_WORD = 65536
    }

    private val words = Array(MAX_CONTROL_WORD) { ControlWord() }

    var pointer = 0
        private set

    val size: Int = words.size

    fun setControlWord(block: ControlWord.() -> Unit) {
        val w = words[pointer++]
        w.setPadding(false)
        block.invoke(w)
    }

    fun finish() {
        // set the reset words to padding
        for (i in pointer until words.size)
            words[i].setPadding(true)
    }

    operator fun get(index: Int) = words[index]

    infix fun readFrom(file: File) = file.inputStream().use { readFrom(it) }

    infix fun readFrom(inputStream: InputStream) {
        for (i in words.indices)
            words[i].readFrom(inputStream)
    }

    infix fun writeTo(file: File) = file.outputStream().use { writeTo(it) }

    infix fun writeTo(outputStream: OutputStream) {
        for (i in words.indices)
            words[i].writeTo(outputStream)
    }

    override fun iterator(): Iterator<ControlWord> = words.iterator()
}
