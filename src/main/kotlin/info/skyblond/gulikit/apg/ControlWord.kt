package info.skyblond.gulikit.apg

import java.io.InputStream
import java.io.OutputStream

class ControlWord {
    companion object {
        /**
         * Size in byte of each control word
         * */
        private const val SIZE_BYTES = 16

        private const val LEFT_JOYSTICK_HORIZONTAL_OFFSET = 0
        private const val LEFT_JOYSTICK_VERTICAL_OFFSET = 2
        private const val RIGHT_JOYSTICK_HORIZONTAL_OFFSET = 4
        private const val RIGHT_JOYSTICK_VERTICAL_OFFSET = 6

        private const val ZL_LINEAR_OFFSET = 9
        private const val ZR_LINEAR_OFFSET = 11

        private const val BUTTON_P1_OFFSET = 12
        private const val BUTTON_P1_MASK_L = 0x10
        private const val BUTTON_P1_MASK_R = 0x20
        private const val BUTTON_P1_MASK_ZL = 0x40
        private const val BUTTON_P1_MASK_ZR = 0x80
        private const val BUTTON_P1_MASK_A = 0x01
        private const val BUTTON_P1_MASK_B = 0x02
        private const val BUTTON_P1_MASK_Y = 0x04
        private const val BUTTON_P1_MASK_X = 0x08

        private const val BUTTON_P2_OFFSET = 13
        private const val BUTTON_P2_MASK_HOME = 0x10
        private const val BUTTON_P2_MASK_SCREENSHOT = 0x20
        private const val BUTTON_P2_MASK_LEFT_JOYSTICK_PRESS = 0x01
        private const val BUTTON_P2_MASK_RIGHT_JOYSTICK_PRESS = 0x02
        private const val BUTTON_P2_MASK_MINUS = 0x04
        private const val BUTTON_P2_MASK_PLUS = 0x08

        private const val D_PAD_OFFSET = 14
        private const val D_PAD_MASK = 0x0F

        private const val END_OFFSET = 15
        private const val END_BYTE: Byte = 0x02
        private const val PADDING_BYTE: Byte = 0xFF.toByte()
    }

    private val buffer = ByteArray(SIZE_BYTES)

    private fun mapJoystickRead(offset: Int): Int =
        // Raw range: 0000~0FFF, toInt: 0~4095, mapped: -2048~2047
        buffer.readUShortLE(offset).toInt() - 2048

    private fun mapJoystickWrite(value: Int, offset: Int): Unit =
        // input range: -2048~2047, mapped: 0~4095, toUShort: 0000~0FFF
        buffer.writeUShortLE((value.also { require(it in -2048..2047) } + 2048).toUShort(), offset)

    private fun mapTriggerLinearRead(offset: Int): Int =
        // raw: -128 to 127, result: 0 to 255
        buffer[offset].toInt() and 0x000000FF

    private fun mapTriggerLinearWrite(value: Int, offset: Int) {
        require(value in 0..255)
        buffer[offset] = value.toByte()
    }

    private fun mapButtonRead(offset: Int, mask: Int): Boolean =
        (buffer[offset].toInt() and mask) != 0

    private fun mapButtonWrite(offset: Int, mask: Int, value: Boolean) {
        val v =
            if (value) buffer[offset].toInt() or mask
            else buffer[offset].toInt() and mask.inv()
        buffer[offset] = v.toByte()
    }


    /**
     * Left joystick horizontal movement.
     *
     * Range: -2048(left) to 2047(right). 0 means center.
     * */
    var leftJoystickHorizontal: Int
        get() = mapJoystickRead(LEFT_JOYSTICK_HORIZONTAL_OFFSET)
        set(value) = mapJoystickWrite(value, LEFT_JOYSTICK_HORIZONTAL_OFFSET)

    /**
     * Left joystick vertical movement.
     *
     * Range: -2048(down) to 2047(up). 0 means center.
     * */
    var leftJoystickVertical: Int
        get() = mapJoystickRead(LEFT_JOYSTICK_VERTICAL_OFFSET)
        set(value) = mapJoystickWrite(value, LEFT_JOYSTICK_VERTICAL_OFFSET)

    /**
     * Right joystick horizontal movement.
     *
     * Range: -2048(left) to 2047(right). 0 means center.
     * */
    var rightJoystickHorizontal: Int
        get() = mapJoystickRead(RIGHT_JOYSTICK_HORIZONTAL_OFFSET)
        set(value) = mapJoystickWrite(value, RIGHT_JOYSTICK_HORIZONTAL_OFFSET)

    /**
     * Right joystick vertical movement.
     *
     * Range: -2048(down) to 2047(up). 0 means center.
     * */
    var rightJoystickVertical: Int
        get() = mapJoystickRead(RIGHT_JOYSTICK_VERTICAL_OFFSET)
        set(value) = mapJoystickWrite(value, RIGHT_JOYSTICK_VERTICAL_OFFSET)

    /**
     * ZL is linear.
     * Range: 0(release) to 255(full).
     * Note: You have to set [shoulderZL] to make platform like switch to register the "click".
     * */
    var zlLinear: Int
        get() = mapTriggerLinearRead(ZL_LINEAR_OFFSET)
        set(value) = mapJoystickWrite(value, ZL_LINEAR_OFFSET)

    /**
     * ZR is linear.
     * Range: 0(release) to 255(full).
     * Note: You have to set [shoulderZR] to make platform like switch to register the "click".
     * */
    var zrLinear: Int
        get() = mapTriggerLinearRead(ZR_LINEAR_OFFSET)
        set(value) = mapJoystickWrite(value, ZR_LINEAR_OFFSET)


    /**
     * Set shoulder button L triggered.
     * */
    var shoulderL: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_L)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_L, value)

    /**
     * Set shoulder button R triggered.
     * */
    var shoulderR: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_R)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_R, value)

    /**
     * Set shoulder button ZL triggered.
     * */
    var shoulderZL: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_ZL)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_ZL, value)

    /**
     * Set shoulder button R triggered.
     * */
    var shoulderZR: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_ZR)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_ZR, value)

    /**
     * Set button A triggered.
     * This is nintendo arrangement. Button A: right button.
     * */
    var buttonA: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_A)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_A, value)

    /**
     * Set button B triggered.
     * This is nintendo arrangement. Button B: bottom button.
     * */
    var buttonB: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_B)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_B, value)

    /**
     * Set button Y triggered.
     * This is nintendo arrangement. Button Y: left button.
     * */
    var buttonY: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_Y)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_Y, value)

    /**
     * Set button X triggered.
     * This is nintendo arrangement. Button X: top button.
     * */
    var buttonX: Boolean
        get() = mapButtonRead(BUTTON_P1_OFFSET, BUTTON_P1_MASK_X)
        set(value) = mapButtonWrite(BUTTON_P1_OFFSET, BUTTON_P1_MASK_X, value)

    /**
     * Set home button pressed.
     * This is a nintendo only button
     * */
    var buttonHome: Boolean
        get() = mapButtonRead(BUTTON_P2_OFFSET, BUTTON_P2_MASK_HOME)
        set(value) = mapButtonWrite(BUTTON_P2_OFFSET, BUTTON_P2_MASK_HOME, value)

    /**
     * Set screenshot button pressed.
     * This is a nintendo only button
     * */
    var buttonScreenshot: Boolean
        get() = mapButtonRead(BUTTON_P2_OFFSET, BUTTON_P2_MASK_SCREENSHOT)
        set(value) = mapButtonWrite(BUTTON_P2_OFFSET, BUTTON_P2_MASK_SCREENSHOT, value)

    /**
     * Set if left joystick is pressed
     * */
    var buttonLeftJoystick: Boolean
        get() = mapButtonRead(BUTTON_P2_OFFSET, BUTTON_P2_MASK_LEFT_JOYSTICK_PRESS)
        set(value) = mapButtonWrite(BUTTON_P2_OFFSET, BUTTON_P2_MASK_LEFT_JOYSTICK_PRESS, value)

    /**
     * Set if left joystick is pressed
     * */
    var buttonRightJoystick: Boolean
        get() = mapButtonRead(BUTTON_P2_OFFSET, BUTTON_P2_MASK_RIGHT_JOYSTICK_PRESS)
        set(value) = mapButtonWrite(BUTTON_P2_OFFSET, BUTTON_P2_MASK_RIGHT_JOYSTICK_PRESS, value)

    /**
     * Set minus button (-) pressed.
     * This is a nintendo only button
     * */
    var buttonMinus: Boolean
        get() = mapButtonRead(BUTTON_P2_OFFSET, BUTTON_P2_MASK_MINUS)
        set(value) = mapButtonWrite(BUTTON_P2_OFFSET, BUTTON_P2_MASK_MINUS, value)

    /**
     * Set plus button (+) pressed.
     * This is a nintendo only button
     * */
    var buttonPlus: Boolean
        get() = mapButtonRead(BUTTON_P2_OFFSET, BUTTON_P2_MASK_PLUS)
        set(value) = mapButtonWrite(BUTTON_P2_OFFSET, BUTTON_P2_MASK_PLUS, value)

    var dPad: DPad
        get() = DPad.values().first { it.code == buffer[D_PAD_OFFSET].toInt() and D_PAD_MASK }
        set(value) {
            buffer[D_PAD_OFFSET] = (value.code and D_PAD_MASK).toByte()
        }

    /**
     * Set should this word be padding word.
     * Padding word: All bytes are [PADDING_BYTE].
     * Normal word: The last byte ([END_OFFSET]) is [END_BYTE].
     *
     * Note: This will wipe all current data.
     * */
    fun setPadding(isPadding: Boolean) {
        if (isPadding) {
            for (i in buffer.indices)
                buffer[i] = PADDING_BYTE
        } else {
            // reset word
            for (i in buffer.indices) {
                buffer[i] = if (i != END_OFFSET) 0 else END_BYTE
            }
            // center the joysticks
            leftJoystickHorizontal = 0
            leftJoystickVertical = 0
            rightJoystickHorizontal = 0
            rightJoystickVertical = 0
        }
    }

    fun isPadding(): Boolean = buffer.all { it == PADDING_BYTE }


    infix fun writeTo(outputStream: OutputStream) {
        outputStream.write(buffer)
    }

    infix fun readFrom(inputStream: InputStream) {
        val r = inputStream.read(buffer)
        require(r == SIZE_BYTES)
    }

    override fun toString(): String {
        return if (isPadding())
            "ControlWord(padding)"
        else
            "ControlWord(leftJoystickHorizontal=$leftJoystickHorizontal, leftJoystickVertical=$leftJoystickVertical, rightJoystickHorizontal=$rightJoystickHorizontal, rightJoystickVertical=$rightJoystickVertical, zlLinear=$zlLinear, zrLinear=$zrLinear, shoulderL=$shoulderL, shoulderR=$shoulderR, shoulderZL=$shoulderZL, shoulderZR=$shoulderZR, buttonA=$buttonA, buttonB=$buttonB, buttonY=$buttonY, buttonX=$buttonX, buttonHome=$buttonHome, buttonScreenshot=$buttonScreenshot, buttonLeftJoystick=$buttonLeftJoystick, buttonRightJoystick=$buttonRightJoystick, buttonMinus=$buttonMinus, buttonPlus=$buttonPlus, dPad=$dPad)"
    }
}
