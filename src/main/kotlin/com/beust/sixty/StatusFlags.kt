package com.beust.sixty

import java.util.*

class StatusFlags {
    private val bits = BitSet(8)

    private fun bit(n: Int) = bits.get(n)
    private fun bit(n: Int, value: Boolean) = bits.set(n, value)

    fun toByte(): Byte {
        val result = N.int().shl(7)
                .or(V.int().shl(6))
                .or(1.shl(5))
                .or(1.shl(4))
//                .or(B.int().shl(4))
                .or(D.int().shl(3))
                .or(I.int().shl(2))
                .or(Z.int().shl(1))
                .or(C.int())
        return result.toByte()
    }

    fun fromByte(byte: Byte) {
        val b = byte.toInt()
        N = b.and(0x80).shr(7).toBoolean()
        V = b.and(0x40).shr(6).toBoolean()
        D = b.and(0x8).shr(3).toBoolean()
        I = b.and(0x4).shr(2).toBoolean()
        Z = b.and(0x2).shr(1).toBoolean()
        C = b.and(1).toBoolean()
    }

    var N: Boolean // Negative
        get() = bit(7)
        set(v) = bit(7, v)

    var V: Boolean // Overflow
        get() = bit(6)
        set(v) = bit(6, v)

    var D: Boolean // Decimal
        get() = bit(3)
        set(v) = bit(3, v)

    var I: Boolean // Interrupt disable
        get() = bit(2)
        set(v) = bit(2, v)

    var Z: Boolean // Zero
        get() = bit(1)
        set(v) = bit(1, v)

    var C: Boolean // Carry
        get() = bit(0)
        set(v) = bit(0, v)

    override fun toString() = "{N=${N.int()} V=${V.int()} D=${D.int()} I=${I.int()} Z=${Z.int()} C=${C.int()}"

    fun setNZFlags(reg: Int) {
        Z = reg == 0
        N = reg.and(0x80) != 0
    }

}