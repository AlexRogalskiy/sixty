package com.beust.sixty.op

import com.beust.sixty.*

abstract class RorBase(c: Computer, override val opCode: Int, override val size: Int, override val timing: Int)
    : InstructionBase(c)
{
    abstract var value: Int
    abstract val name: String

    override fun run() {
        val bit0 = value.and(0x1)
        val result = value.shr(1).or(cpu.P.C.int().shl(7))
        cpu.P.setNZFlags(result)
        cpu.P.C = bit0.toBoolean()
        value = result
    }
    override fun toString(): String = "ROR${name}"
}

/** 0x6a, ROR */
class Ror(c: Computer): RorBase(c, ROR, 1, 2) {
    override var value by ValRegisterA()
    override val name = ""
}

/** 0x66, ROR $12 */
class RorZp(c: Computer): RorBase(c, ROR_ZP, 2, 5) {
    override var value by ValZp()
    override val name = nameZp()
}

/** 0x76, ROR $12,X */
class RorZpX(c: Computer): RorBase(c, ROR_ZP_X, 2, 6) {
    override var value by ValZpX()
    override val name = nameZpX()
}

/** 0x6e, ROR $1234 */
class RorAbsolute(c: Computer): RorBase(c, ROR_ABS, 3, 6) {
    override var value by ValAbsolute()
    override val name = nameAbs()
}

/** 0x7e, ROR $1234,X */
class RorAbsoluteX(c: Computer): RorBase(c, ROR_ABS_X, 3, 7) {
    override var value by ValAbsoluteX()
    override val name = nameAbsX()
}

