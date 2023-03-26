package info.skyblond.gulikit.generator

import info.skyblond.gulikit.apg.APGFile
import info.skyblond.gulikit.apg.ControlWord
import info.skyblond.gulikit.apg.DPad
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

/**
 * Generate a draw seq for splatoon 3.
 *
 * Limited by the APG file size, this must be split into 2 parts.
 * */
object SplatoonCanvas {
    private val image = ImageIO.read(javaClass.getResource("/test.png"))
    private val path = File("G:\\")

    private const val CANVAS_WIDTH = 320
    private const val CANVAS_HEIGHT = 120
    private const val VALUE_BLACK = 0xFF000000.toInt()
    private const val VALUE_WHITE = 0xFFFFFFFF.toInt()

    // this is limited by 65535 words per file
    private const val LINE_PER_FILE = 16

    // each word repeat 6 times (aka 60ms)
    // although the polling rate should be 125HZ (8ms)
    // but using 10ms interval the game just ignore the input
    // 60ms is the minimal acceptable interval
    private const val WORD_DURATION = 6

    init {
        require(image.type == BufferedImage.TYPE_BYTE_BINARY) { "Picture is not binary bitmap (BYTE_BINARY)" }
        require(image.width == CANVAS_WIDTH) { "Picture must be 320 px wide" }
        require(image.height == CANVAS_HEIGHT) { "Picture must be 120 px high" }
    }

    private fun APGFile.writeAPG(duration: Int = WORD_DURATION, block: ControlWord.() -> Unit) {
        repeat(duration) {
            this.setControlWord { block.invoke(this@setControlWord) }
        }
    }

    private fun APGFile.drawLine(y: Int, toRight: Boolean) {
        val range =
            if (toRight) 0 until CANVAS_WIDTH
            else CANVAS_WIDTH - 1 downTo 0
        for (x in range) {
            // print the pixel
            when (val pixel = image.getRGB(x, y)) {
                VALUE_BLACK -> {
                    print("*")
                    this.writeAPG {
                        dPad = DPad.NOT_PRESSED
                        buttonA = true
                        buttonB = false
                    }
                }

                VALUE_WHITE -> {
                    print(".")
                    this.writeAPG {
                        dPad = DPad.NOT_PRESSED
                        buttonA = false
                        buttonB = true
                    }

                }

                else -> error("Unknown pixel value: $pixel")
            }

            this.writeAPG {
                buttonA = false
                buttonB = false
                dPad = if ((!toRight && x == 0) || (toRight && x == CANVAS_WIDTH - 1)) {
                    DPad.DOWN
                } else {
                    if (toRight) DPad.RIGHT else DPad.LEFT
                }
            }
        }
        println()
    }

    private fun generatePart(offsetY: Int, file: File) {
        val apg = APGFile()
        // start drawing
        for (y in offsetY until (offsetY + LINE_PER_FILE).coerceAtMost(CANVAS_HEIGHT) step 2) {
            apg.drawLine(y, true)
            apg.drawLine(y + 1, false)
        }
        apg.finish()
        file.delete()
        apg writeTo file
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val scanner = Scanner(System.`in`)
        for (y in 0 until CANVAS_HEIGHT step LINE_PER_FILE) {
            print("Generate line $y to ${y + LINE_PER_FILE}? (Enter to confirm)")
            scanner.nextLine()
            generatePart(y, File(path, "Auto.apg"))
            println("Generated file: Auto.apg")
        }
        println("All files are generated. Exit...")
    }
}
