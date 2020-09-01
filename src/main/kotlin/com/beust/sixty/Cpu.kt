//@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.beust.sixty

import com.beust.app.StackPointer

/**
 * Specs used:
 * https://en.wikipedia.org/wiki/MOS_Technology_6502
 * http://www.6502.org/tutorials/6502opcodes.html
 */
interface ICpu {
    fun nextInstruction(computer: Computer, noThrows: Boolean = false): Instruction?
    fun clone(): Cpu
}

interface Instruction {
    /**
     * Opcode of this instruction
     */
    val opCode: Int

    /**
     * Number of bytes occupied by this op (1, 2, or 3).
     */
    val size: Int

    /**
     * This should be a property and not a constant since the value of the timing can change for certain ops when
     * a page boundary is crossed.
     */
    val timing: Int

    fun run()

    /**
     * @return 1 if a page bounday was crossed, 0 otherwise
     */
    fun pageCrossed(old: Int, new: Int): Int {
        return if (old.and(0x80).xor(new.and(0x80)) != 0) 1 else 0
    }
}

data class Cpu(var A: Int = 0, var X: Int = 0, var Y: Int = 0, var PC: Int = 0xffff,
        val memory: Memory, val P: StatusFlags = StatusFlags()) : ICpu
{
    val SP: StackPointer
    init {
        SP = StackPointer(memory)
    }
    override fun clone() = Cpu(A, X, Y, PC, memory.clone(), P)
    override fun nextInstruction(computer: Computer, noThrows: Boolean): InstructionBase? {
        val op = computer.memory[PC] and 0xff
        val result = when(op) {
            0x00 -> Brk(computer)
//            0x01 -> OraIndirectX(computer)
            ORA_ZP -> OraZp(computer)
//            0x06 -> AslZp(computer)
            0x08 -> Php(computer)
            ORA_IMM -> OraImm(computer)
            0x0a -> Asl(computer)
//            0x0d -> OraAbsolute(computer)
//            0x0e -> AslAbsolute(computer)
            0x10 -> Bpl(computer)
//            0x11 -> OraIndirectY(computer)
//            0x15 -> OraZpX(computer)
//            0x1e -> AslAbsoluteX(computer)
//            0x16 -> AslZpX(computer)
            0x18 -> Clc(computer)
//            0x19 -> OraAbsoluteY(computer)
//            0x1d -> OraAbsoluteX(computer)
            0x20 -> Jsr(computer)
//            0x21 -> AndIndirectX(computer)
//            0x24 -> BitZp(computer)
            AND_ZP -> AndZp(computer)
            ROL_ZP -> RolZp(computer)
            0x28 -> Plp(computer)
            AND_IMM -> And(computer)
//            0x2a -> Rol(computer)
            BIT_ABS -> BitAbsolute(computer)
            AND_ABS -> AndAbsolute(computer)
//            0x2e -> RolAbsolute(computer)
            0x30 -> Bmi(computer)
//            0x31 -> AndIndirectY(computer)
//            0x3d -> AndAbsX(computer)
//            0x35 -> AndZpX(computer)
//            0x36 -> RolZpX(computer)
            0x38 -> Sec(computer)
//            0x39 -> AndAboluteY(computer)
//            0x34 -> RolAbsoluteX(computer)
            RTI -> Rti(computer)
//            0x41 -> EorIndirectX(computer)
            EOR_ZP -> EorZp(computer)
//            0x46 -> LsrZp(computer)
            0x49 -> EorImm(computer)
            LSR_A -> LsrA(computer)
            0x4c -> Jmp(computer)
            0x48 -> Pha(computer)
//            0x4d -> EorAbsolute(computer)
//            0x4e -> LsrAbsolute(computer)
            0x50 -> Bvc(computer)
            EOR_IND_Y -> EorIndirectY(computer)
//            0x55 -> EorZpX(computer)
//            0x65 -> LsrZpX(computer)
            0x58 -> Cli(computer)
//            0x59 -> EorAbsY(computer)
//            0x5d -> EorAbsX(computer)
//            0x5e -> LsrAbsoluteX(computer)
            0x60 -> Rts(computer)
//            0x61 -> AdcIndirectX(computer)
            ADC_ZP -> AdcZp(computer)
            ROR_ZP -> RorZp(computer)
            0x68 -> Pla(computer)
            0x69 -> AdcImm(computer)
            0x6c -> JmpIndirect(computer)
//            0x65 -> AdcAbsolute(computer)
//            0x66 -> RorZp(computer)
//            0x6a -> Ror(computer)
//            0x6e -> RorAbsolute(computer)
            BVS -> Bvs(computer)
//            0x71 -> AdcIndirectY(computer)
//            0x??75 -> AdcZpX(computer)
//            0x??75 -> AdcAbsoluteX(computer)
//            0x76 -> RorZpX(computer)
            0x78 -> Sei(computer)
//            0x79 -> AdcAbsoluteY(computer)
//            0x74 -> RorAbsoluteX(computer)
            STA_IND_X -> StaIndirectX(computer)
            LDA_IND_Y -> LdaIndirectY(computer)
            0x84 -> StyZp(computer)
            0x85 -> StaZp(computer)
            STX_ZP -> StxZp(computer)
            0x88 -> Dey(computer)
            0x8c -> StyAbsolute(computer)
            0x8d -> StaAbsolute(computer)
            0x8e -> StxAbsolute(computer)
            0x8a -> Txa(computer)
            0x90 -> Bcc(computer)
            0x91 -> StaIndirectY(computer)
//            0x94 -> StyZpX(computer)
            0x95 -> StaZpX(computer)
            STX_ZP_Y -> StxZpY(computer)
            0x98 -> Tya(computer)
            0x99 -> StaAbsoluteY(computer)
            0x9a -> Txs(computer)
            STA_ABS_X -> StaAbsoluteX(computer)
            0xa0 -> LdyImm(computer)
//            0xa1 -> LdaIndirectX(computer)
            0xa2 -> LdxImm(computer)
            0xa4 -> LdyZp(computer)
            0xa5 -> LdaZp(computer)
            LDX_ZP -> LdxZp(computer)
            0xa8 -> Tay(computer)
            0xa9 -> LdaImm(computer)
            0xaa -> Tax(computer)
//            0xac -> LdyAbsolute(computer)
            0xad -> LdaAbsolute(computer)
//            0xae -> LdxAbsolute(computer)
            0xb0 -> Bcs(computer)
//            0xb1 -> LdaIndirectY(computer)
            LDY_ZP_X -> LdyZpX(computer)
//            0xb5 -> LdaZpX(computer)
            LDX_ZP_Y -> LdxZpY(computer)
            LDA_ABS_Y -> LdaAbsoluteY(computer)
            CLV -> Clv(computer)
//            0xb9 -> LdaAbsoluteY(computer)
            0xba -> Tsx(computer)
//            0xbc -> LdyAbsoluteX(computer)
            LDA_ABS_X -> LdaAbsoluteX(computer)
            LDX_ABS_Y -> LdxAbsoluteY(computer)
            0xc0 -> CpyImm(computer)
//            0xc1 -> CmpIndirectX(computer)
//            0xc4 -> CpyZp(computer)
//            0xc5 -> CmpZp(computer)
//            0xc6 -> DecZp(computer)
            0xc8 -> Iny(computer)
            0xc9 -> CmpImm(computer)
            CMP_ABS -> CmpAbsolute(computer)
            0xca -> Dex(computer)
//            0xcc -> CpyAbsolute(computer)
//            0xce -> DecAbsolute(computer)
            0xd0 -> Bne(computer)
//            0xd1 -> CmpIndirectY(computer)
//            0xd5 -> CmpZpX(computer)
//            0xd6 -> DecZpX(computer)
            0xd8 -> Cld(computer)
            0xd9 -> CmpAbsoluteY(computer)
//            0xdd -> CmpAbsX(computer)
//            0xde -> DecAbsX(computer)
            CPX_IMM -> CpxImm(computer)
//            0xe1 -> SbcIndirectX(computer)
//            0xe4 -> CpxZp(computer)
            0xe5 -> SbcZp(computer)
            0xe6 -> IncZp(computer)
            0xe8 -> Inx(computer)
            SBC_IMM -> SbcImmediate(computer)
            0xea -> Nop(computer)
//            0xec -> CpxAbsolute(computer)
//            0xed -> SbcAbsolute(computer)
//            0xee -> IncAbsolute(computer)
            0xf0 -> Beq(computer)
//            0xf1 -> SbcIndirectY(computer)
//            0xf5 -> SbcZpX(computer)
//            0xf7 -> IncZpX(computer)
            0xf8 -> Sed(computer)
//            0xf9 -> SbcAbsoluteY(computer)
//            0xfd -> SbcAbsoluteX(computer)
//            0xfe -> IncAbsoluteX(computer)
            else -> {
                if (noThrows) {
                    Unknown(computer, op)
                } else {
                    null
                }
            }
        }

        return result
    }

    override fun toString(): String {
        return "A=${A.h()} X=${X.h()} Y=${Y.h()} S=${SP.S.h()} P=${P.toByte().h()} PC=\$${PC.h()} P=${P} SP=$SP"
    }

    companion object {
        // NMI vector
//        const val NMI_VECTOR_L = 0xfffa
//        const val NMI_VECTOR_H = 0xfffb
//
//        // Reset vector
//        const val RST_VECTOR_L = 0xfffc
//        const val RST_VECTOR_H = 0xfffd

        // IRQ vector
        const val IRQ_VECTOR_L = 0xfffe
        const val IRQ_VECTOR_H = 0xffff
    }
}

abstract class InstructionBase(val computer: Computer): Instruction {
    val cpu by lazy { computer.cpu }
    val memory by lazy { computer.memory }
    val pc by lazy { cpu.PC}
    val operand by lazy { memory[pc + 1] }
    val word by lazy { memory[pc + 2].shl(8).or(memory[pc + 1]) }

    protected fun indirectX(address: Int): Int = memory[address + cpu.X]
    protected fun indirectY(address: Int): Int = memory[address] + cpu.Y
}

/** 0x00, BRK */
class Brk(c: Computer): InstructionBase(c) {
    private fun handleInterrupt(brk: Boolean, vectorHigh: Int, vectorLow: Int) {
        cpu.SP.pushWord(cpu.PC + 1)
        cpu.SP.pushByte(cpu.P.toByte())
        cpu.P.I = true
        cpu.PC = memory[vectorHigh].shl(8).or(memory[vectorLow])
    }

    override val opCode = 0
    override val size = 1
    override val timing = 7
    override fun run() {
        handleInterrupt(true, Cpu.IRQ_VECTOR_H, Cpu.IRQ_VECTOR_L)
        cpu.P.B = true
        cpu.P.I = false
        cpu.P.reserved = true
    }
    override fun toString(): String = "BRK"
}

/** 0x5, ORA #$12 */
class OraZp(c: Computer): InstructionBase(c) {
    override val opCode = ORA_ZP
    override val size = 2
    override val timing = 3
    override fun run() {
        val result = cpu.A.or(operand)
        cpu.P.setNZFlags(result)
        cpu.A = result
    }
    override fun toString(): String = "ORA $${operand.h()}"
}

/** 0x8, PHP */
class Php(c: Computer): StackInstruction(c, PHP, "PHP") {
    override val timing = 3
    override fun run() {
        cpu.P.B = true
        cpu.P.reserved = true
        cpu.SP.pushByte(cpu.P.toByte())
    }
}

/** 0x98, ORA #$12 */
class OraImm(c: Computer): InstructionBase(c) {
    override val opCode = ORA_IMM
    override val size = 2
    override val timing = 2
    override fun run() {
        val result = cpu.A.or(operand)
        cpu.P.setNZFlags(result)
        cpu.A = result
    }
    override fun toString(): String = "ORA #$${operand.h()}"
}

/** 0x0a, ASL */
class Asl(c: Computer): InstructionBase(c) {
    override val opCode = 0xa
    override val size = 1
    override val timing = 2
    override fun run() {
        cpu.P.C = if (cpu.A.and(0x80) != 0) true else false
        val newValue = cpu.A.shl(1).and(0xff)
        cpu.P.setNZFlags(newValue)
        cpu.A = newValue
    }
    override fun toString(): String = "ASL"
}

/** 0x10, BPL */
class Bpl(computer: Computer): BranchBase(computer, 0x10, "BPL", { !computer.cpu.P.N })

abstract class FlagInstruction(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun toString(): String = name
}

/** 0x18, CLC */
class Clc(c: Computer): FlagInstruction(c, 0x18, "CLC") {
    override fun run() { cpu.P.C = false }
}

abstract class StackInstruction(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 1
    override fun toString(): String = name
}

/** 0x20, JSR $1234 */
class Jsr(c: Computer): InstructionBase(c) {
    override val opCode = 0x20
    override val size = 3
    override val timing = 6
    override fun run() {
        cpu.SP.pushWord(pc + size - 1)
        cpu.PC = word
    }
    override fun toString(): String = "JSR $${word.hh()}"
}

/** 0x25, AND $34 */
class AndZp(c: Computer): InstructionBase(c) {
    override val opCode = AND_ZP
    override val size = 2
    override val timing = 3
    override fun run() {
        cpu.A = cpu.A.and(memory[operand])
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "AND $${operand.h()}"
}

/** 0x26, ROL $12 */
class RolZp(c: Computer): InstructionBase(c) {
    override val opCode = ROL_ZP
    override val size = 2
    override val timing = 5
    override fun run() {
        val bit7 = if (cpu.A.and(1.shl(7)) != 0) 1 else 0
        val result = cpu.A.shl(1).or(cpu.P.C.int())
        cpu.P.setNZFlags(result)
        cpu.P.C = bit7.toBoolean()
        cpu.A = result
    }
    override fun toString(): String = "ROL $${operand.h()}"
}

/** 0x28, PLP */
class Plp(c: Computer): StackInstruction(c, PLP, "PLP") {
    override val timing = 4
    override fun run() {
        cpu.P.fromByte(cpu.SP.popByte())
    }
}

/** 0x29, AND #$34 */
class And(c: Computer): InstructionBase(c) {
    override val opCode = AND_IMM
    override val size = 2
    override val timing = 2
    override fun run() {
        cpu.A = cpu.A.and(operand)
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "AND #$${operand.h()}"
}

/** 0x2c, BIT $1234 */
class BitAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = BIT_ABS
    override val size = 3
    override val timing = 4
    override fun run() {
        val value = cpu.A.and(memory[word])
        cpu.P.setNZFlags(value)
        cpu.P.V = if (value.and(1.shl(6)) != 0) true else false
    }
    override fun toString(): String = "BIT $${word.hh()}"
}

/** 0x2d, AND $1234 */
class AndAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = AND_ABS
    override val size = 3
    override val timing = 4
    override fun run() {
        cpu.A = cpu.A.and(memory[word])
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "AND $${word.hh()}"
}

/** 0x30, BMI */
class Bmi(computer: Computer): BranchBase(computer, 0x30, "BMI", { computer.cpu.P.N })

abstract class CmpImmBase(c: Computer, private val name: String, private val immediate: String = "#")
    : InstructionBase(c)
{
    override val size = 2
    override val timing = 2

    abstract val register: Int
    abstract val value: Int
    abstract val argName: String

    override fun run() {
        val tmp: Int = (register - value) and 0xff
        cpu.P.C = register >= value
        cpu.P.Z = tmp == 0
        cpu.P.N = (tmp and 0x80) != 0
    }

    override fun toString(): String = "$name ${immediate}$$argName"
}

/** 0x38, SEC */
class Sec(c: Computer): FlagInstruction(c, 0x38, "SEC") {
    override fun run() { cpu.P.C = true }
}

/** 0x40, RTI */
class Rti(c: Computer): InstructionBase(c) {
    override val opCode = RTI
    override val size = 1
    override val timing = 6
    override fun run() {
        cpu.P.fromByte(cpu.SP.popByte())
        cpu.PC = cpu.SP.popWord()
    }
    override fun toString(): String = "RTI"
}

/** 0x45, EOR $12 */
class EorZp(c: Computer): InstructionBase(c) {
    override val opCode = EOR_ZP
    override val size = 2
    override val timing = 3
    override fun run() {
        cpu.A = cpu.A.xor(memory[operand])
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "EOR $${operand.h()}"
}

/** 0x49, EOR #$12 */
class EorImm(c: Computer): InstructionBase(c) {
    override val opCode = 0x49
    override val size = 2
    override val timing = 2
    override fun run() {
        val result = cpu.A.xor(operand)
        cpu.P.setNZFlags(result)
        cpu.A = result
    }
    override fun toString(): String = "EOR #$${operand.h()}"
}

/** 0x4a, LSR */
class LsrA(c: Computer): InstructionBase(c) {
    override val opCode = LSR_A
    override val size = 1
    override val timing = 2
    override fun run() {
        cpu.P.C = cpu.A.and(1.shl(7)).shr(7).toBoolean()
        val result = cpu.A.shr(1)
        cpu.P.setNZFlags(result)
        cpu.A = result
    }
    override fun toString(): String = "LSR"
}

/** 0x48, PHA */
class Pha(c: Computer): StackInstruction(c, 0x48, "PHA") {
    override val timing = 3
    override fun run() {
        cpu.SP.pushByte(cpu.A.toByte())
    }
}

/** 0x50, BVC */
class Bvc(computer: Computer): BranchBase(computer, 0x50, "BVC", { !computer.cpu.P.V })


/** 0x51, EOR ($12),Y */
class EorIndirectY(c: Computer): InstructionBase(c) {
    override val opCode = EOR_IND_Y
    override val size = 2
    override var timing = 5  // variable timing
    override fun run() {
        val targetAddress = indirectY(operand)
        val new = cpu.A.xor(memory[targetAddress])
        cpu.P.setNZFlags(new)
        timing += pageCrossed(cpu.PC, targetAddress)
        cpu.A = new
    }
    override fun toString(): String = "EOR ($${operand.h()}), Y"
}

/** 0x58, CLI */
class Cli(c: Computer): FlagInstruction(c, 0x58, "CLI") {
    override fun run() { cpu.P.I = false }
}

/** 0x4c, JMP $1234 */
class Jmp(c: Computer): InstructionBase(c) {
    override val opCode = 0x4c
    override val size = 3
    override val timing = 3
    override fun run() {
        cpu.PC = word
    }

    override fun toString(): String = "JMP $${word.hh()}"
}

/** 0x60, RTS */
class Rts(c: Computer): InstructionBase(c) {
    override val opCode = 0x60
    override val size = 1
    override val timing = 6
    override fun run() {
        computer.cpu.PC = cpu.SP.popWord() + 1
    }
    override fun toString(): String = "RTS"
}

/** 0x65, ADC $12 */
class AdcZp(c: Computer): AddBase(c) {
    override val opCode = ADC_ZP
    override val size = 2
    override val timing = 3
    override fun run() { cpu.A = adc(cpu.A, memory[operand]) }
    override fun toString(): String = "ADC ${operand.h()}"
}

/** 0x66, ROR $12 */
class RorZp(c: Computer): InstructionBase(c) {
    override val opCode = ROR_ZP
    override val size = 2
    override val timing = 5
    override fun run() {
        val bit0 = cpu.A.and(0x1)
        val result = cpu.A.shr(1).or(cpu.P.C.int().shl(7))
        cpu.P.setNZFlags(result)
        cpu.P.C = bit0.toBoolean()
        cpu.A = result
    }
    override fun toString(): String = "ROR $${operand.h()}"
}


/** 0x68, PLA */
class Pla(c: Computer): StackInstruction(c, 0x68, "PLA") {
    override val timing = 4
    override fun run() {
        cpu.A = cpu.SP.popByte().toInt().and(0xff)
        cpu.P.setNZFlags(cpu.A)
    }
}

abstract class AddBase(c: Computer): InstructionBase(c) {
    fun adc(value: Int, operand: Int): Int {
        var result: Int = operand + value + cpu.P.C.int()
        val carry6: Int = operand.and(0x7f) + value.and(0x7f) + cpu.P.C.int()
        cpu.P.C = result.and(0x100) == 1
        cpu.P.V = cpu.P.C.xor((carry6.and(0x80) != 0))
        result = result and 0xff
        cpu.P.setNZFlags(result)
        return result
    }
}

/** 0x69, ADC #$12 */
class AdcImm(c: Computer): AddBase(c) {
    override val opCode = 0x69
    override val size = 2
    override val timing = 2
    override fun run() { cpu.A = adc(cpu.A, operand) }
    override fun toString(): String = "ADC #${operand.h()}"
}

/** 0x6c, JMP ($0036) */
class JmpIndirect(c: Computer): InstructionBase(c) {
    override val opCode = 0x6c
    override val size = 3
    override val timing = 5
    override fun run() { cpu.PC = memory.wordAt(word) }
    override fun toString(): String = "JMP ($${word.hh()})"
}

/** 0x70, BVS */
class Bvs(computer: Computer): BranchBase(computer, BVS, "BVS", { computer.cpu.P.V })

/** 0x78, SEI */
class Sei(c: Computer): FlagInstruction(c, 0x78, "SEI") {
    override fun run() { cpu.P.I = true }
}

/** 0x81, STA($82,X) */
class StaIndirectX(c: Computer): InstructionBase(c) {
    override val opCode = STA_IND_X
    override val size = 2
    override val timing = 6
    override fun run() {
        val targetAddress = indirectX(operand)
        memory[targetAddress] = cpu.A
    }
    override fun toString(): String = "STA ($${operand.h()},X)"
}

/** 0xb1, LDA $1234 */
class LdaIndirectY(c: Computer): InstructionBase(c) {
    override val opCode = LDA_IND_Y
    override val size = 2
    override var timing = 5  // variable timing
    override fun run() {
        val targetAddress = indirectY(operand)
        val new = memory[targetAddress]
        cpu.P.setNZFlags(new)
        timing += pageCrossed(cpu.PC, targetAddress)
        cpu.A = new
    }
    override fun toString(): String = "LDA $${word.hh()}"
}

/** 0x84, STY $10 */
class StyZp(c: Computer): ZpBase(c, 0x84, "STY") {
    override fun run() { memory[operand] = cpu.Y }
}

/** 0x85, STA ($10) */
class StaZp(c: Computer): InstructionBase(c) {
    override val opCode = 0x85
    override val size = 2
    override val timing = 3
    override fun run() { memory[operand] = cpu.A }
    override fun toString(): String = "STA $" + memory[cpu.PC + 1].h()
}

/** 0x86, STX $10 */
class StxZp(c: Computer): ZpBase(c, 0x86, "STX") {
    override fun run() { memory[operand] = cpu.X }
}

/** 0x88, INX */
class Dey(c: Computer): RegisterInstruction(c, 0x88, "DEY") {
    override fun run() {
        cpu.Y = (--cpu.Y).and(0xff)
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0x8c, STY ($1234) */
class StyAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8c
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.Y }
    override fun toString(): String = "STY $${word.hh()}"
}

/** 0x8d, STA ($1234) */
class StaAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8d
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.A }
    override fun toString(): String = "STA $${word.hh()}"
}

/** 0x8e, STX ($1234) */
class StxAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0x8e
    override val size = 3
    override val timing = 4
    override fun run() { memory[word] = cpu.X }
    override fun toString(): String = "STX $${word.hh()}"
}

/** 0x8a, TXA */
class Txa(c: Computer): RegisterInstruction(c, 0x8a, "TXA") {
    override fun run() {
        cpu.A = cpu.X
        cpu.P.setNZFlags(cpu.A)
    }
}

open class BranchBase(c: Computer, override val opCode: Int, val name: String, val condition: () -> Boolean)
    : InstructionBase(c)
{
    override val size = 2
    /** TODO(Varied timing if the branch is taken/not taken and if it crosses a page) */
    override var timing = 2
    override fun run() {
        if (condition()) {
            val old = cpu.PC
            cpu.PC += operand.toByte() + size
            timing++
            timing += pageCrossed(old, cpu.PC)
        }  // needs to be signed here
    }
    override fun toString(): String
            = "$name $${(cpu.PC + size + operand.toByte()).h()}"
}

/** 0x90, BCC */
class Bcc(computer: Computer): BranchBase(computer, 0x90, "BCC", { !computer.cpu.P.C })

/** 0x91, STA ($12),Y */
class StaIndirectY(c: Computer): InstructionBase(c) {
    override val opCode = 0x91
    override val size = 2
    override val timing = 6
    override fun run() {
        val targetAddress = indirectY(operand)
        memory[targetAddress] = cpu.A
    }
    override fun toString(): String = "STA ($${operand.toByte().h()}),Y"
}

/** 0x95, STA $12,X */
class StaZpX(c: Computer): InstructionBase(c) {
    override val opCode = 0x95
    override val size = 2
    override val timing = 4
    override fun run() {
        memory[operand + cpu.X] = cpu.A
    }
    override fun toString(): String = "STA $${operand.h()},X"
}

/** 0x96, STX $12,Y */
class StxZpY(c: Computer): InstructionBase(c) {
    override val opCode = STX_ZP_Y
    override val size = 2
    override val timing = 4
    override fun run() {
        memory[operand + cpu.Y] = cpu.X
    }
    override fun toString(): String = "STX $${operand.h()},Y"
}

/** 0x98, TYA */
class Tya(c: Computer): RegisterInstruction(c, 0x98, "TYA") {
    override fun run() {
        cpu.A = cpu.Y
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0x99, STA $1234,Y */
class StaAbsoluteY(c: Computer): InstructionBase(c) {
    override val opCode = STA_ABS_Y
    override val size = 3
    override val timing = 5
    override fun run() {
        memory[operand + cpu.Y] = cpu.A
    }
    override fun toString(): String = "STA $${operand.h()},X"
}

/** 0xc0, CPY #$12 */
class CpyImm(c: Computer): CmpImmBase(c, "CPY") {
    override val opCode = 0xc0
    override val value = operand
    override val register get() = computer.cpu.Y
    override val argName = operand.h()
}

/** 0xc9, CMP $#12 */
class CmpImm(c: Computer): CmpImmBase(c, "CMP") {
    override val opCode = 0xc9
    override val value = operand
    override val register get() = computer.cpu.A
    override val argName = operand.h()
}

/** 0xcd, CMP $1234 */
class CmpAbsolute(c: Computer): CmpImmBase(c, "CMP") {
    override val opCode = CMP_ABS
    override val size = 3
    override val timing = 4
    override val register get() = cpu.A
    override val value get() = memory[word]
    override val argName = "$${word.h()}"
}

/** 0x9a, TXS */
class Txs(c: Computer): StackInstruction(c, 0x9a, "TXS") {
    override val timing = 2
    override fun run() { cpu.SP.S = cpu.X }
}

abstract class ZpBase(c: Computer, override val opCode: Int, private val name: String,
        private val suffix: String = "") : InstructionBase(c) {
    override val size = 2
    override val timing = 3
    override fun toString(): String = "$name $" + operand.h() + suffix
}

/** 0xa4, LDY $10 */
class LdyZp(c: Computer): ZpBase(c, 0xa5, "LDY") {
    override fun run() {
        cpu.Y = memory[operand]
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xa5, LDA $10 */
class LdaZp(c: Computer): ZpBase(c, 0xa5, "LDA") {
    override fun run() {
        cpu.A = memory[operand]
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xa6, LDX $10 */
class LdxZp(c: Computer): ZpBase(c, 0xa6, "LDX") {
    override fun run() {
        cpu.X = memory[operand]
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xa8, TAY */
class Tay(c: Computer): RegisterInstruction(c, 0xa8, "TAY") {
    override fun run() {
        cpu.Y = cpu.A
        cpu.P.setNZFlags(cpu.Y)
    }
}

abstract class LdImmBase(c: Computer, override val opCode: Int, val name: String): InstructionBase(c) {
    override val size = 2
    override val timing = 2
    override fun toString(): String = "$name #$" + operand.h()
}

/** 0x9d, STA $1234,X */
class StaAbsoluteX(c: Computer): InstructionBase(c) {
    override val opCode = STA_ABS_X
    override val size = 3
    override val timing = 5
    override fun run() { memory[word + cpu.X] = cpu.A }
    override fun toString(): String = "STA $${word.hh()},X"
}

/** 0xa0, LDY #$10 */
class LdyImm(c: Computer): LdImmBase(c, 0xa0, "LDY") {
    override fun run() {
        cpu.Y = operand
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xa2, LDX #$10 */
class LdxImm(c: Computer): LdImmBase(c, 0xa2, "LDX") {
    override fun run() {
        cpu.X = operand
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xa9, LDA #$10 */
class LdaImm(c: Computer): LdImmBase(c, 0xa9, "LDA") {
    override fun run() {
        cpu.A = operand
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xaa, TAX */
class Tax(c: Computer): RegisterInstruction(c, 0xaa, "TAX") {
    override fun run() {
        cpu.X = cpu.A
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xad, LDA $1234 */
class LdaAbsolute(c: Computer): InstructionBase(c) {
    override val opCode = 0xad
    override val size = 3
    override val timing = 4
    override fun run() {
        cpu.A = memory[word]
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "LDA $${word.hh()}"
}

/** 0xb0, BCS */
class Bcs(computer: Computer): BranchBase(computer, 0xb0, "BCS", { computer.cpu.P.C })

/** 0xb4, LDY $12,YX*/
class LdyZpX(c: Computer): ZpBase(c, LDY_ZP_X, "LDY", ",X") {
    override fun run() {
        cpu.Y = memory[operand + cpu.X]
        cpu.P.setNZFlags(cpu.Y)
    }
}

/** 0xb6, LDA $12,Y */
class LdxZpY(c: Computer): ZpBase(c, LDX_ZP_Y, "LDX", ",Y") {
    override fun run() {
        cpu.X = memory[operand + cpu.Y]
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xb9, LDA $12,Y */
class LdaAbsoluteY(c: Computer): ZpBase(c, LDA_ABS_Y, "LDA", ",Y") {
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.A = memory[word + cpu.Y]
        timing += pageCrossed(word, word + cpu.Y)
        cpu.P.setNZFlags(cpu.A)
    }
}

/** 0xb8, CLV */
class Clv(c: Computer): FlagInstruction(c, CLV, "CLV") {
    override fun run() { cpu.P.V = false }
}

/** 0xba, TSX */
class Tsx(c: Computer): StackInstruction(c, 0xba, "TSX") {
    override val timing = 2
    override fun run() {
        cpu.X = cpu.SP.S
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xbd, LDA $1234,X */
class LdaAbsoluteX(c: Computer): InstructionBase(c) {
    override val opCode = LDA_ABS_X
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.A = memory[word + cpu.X]
        timing += pageCrossed(word, word + cpu.X)
        cpu.P.setNZFlags(cpu.A)
    }
    override fun toString(): String = "LDA $${word.hh()},X"
}

/** 0xbe, LDX $1234,Y */
class LdxAbsoluteY(c: Computer): InstructionBase(c) {
    override val opCode = LDX_ABS_Y
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        cpu.X = memory[word + cpu.Y]
        timing += pageCrossed(word, word + cpu.Y)
        cpu.P.setNZFlags(cpu.X)
    }
    override fun toString(): String = "LDX $${word.hh()},Y"
}

abstract class IncBase(c: Computer, override val opCode: Int): InstructionBase(c) {
    protected fun calculate(oldValue: Int): Int {
        val result = (oldValue + 1).and(0xff)
        cpu.P.setNZFlags(result)
        return result
    }
}

/** 0xc8, INY */
class Iny(c: Computer): RegisterInstruction(c, 0xc8, "INY") {
    override fun run() {
        cpu.Y = (cpu.Y + 1).and(0xff)
        cpu.P.setNZFlags(cpu.Y)
    }
}

abstract class RegisterInstruction(c: Computer, override val opCode: Int, val name: String) : InstructionBase(c) {
    override val size = 1
    override val timing = 2
    override fun toString(): String = name
}

/** 0xca, DEX */
class Dex(c: Computer): RegisterInstruction(c, 0xca, "DEX") {
    override fun run() {
        cpu.X = (--cpu.X).and(0xff)
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xd0, BNE */
class Bne(computer: Computer): BranchBase(computer, 0xd0, "BNE", { !computer.cpu.P.Z })

/** 0xd8, CLD */
class Cld(c: Computer): FlagInstruction(c, 0xd8, "CLD") {
    override fun run() { cpu.P.D = false }
}

/** 0xd9, CMP $1234,Y */
class CmpAbsoluteY(c: Computer): InstructionBase(c) {
    override val opCode = CMP_ABS_Y
    override val size = 3
    override var timing = 4 // variable
    override fun run() {
        val value: Int = memory[word + cpu.Y].and(0xff)
        cpu.P.C = cpu.A >= value
        cpu.P.Z = value == 0
        cpu.P.N = (value and 0x80) != 0
        timing += pageCrossed(cpu.PC, word + cpu.Y)
    }
    override fun toString(): String = "CMP $${word.hh()},Y"
}


/** 0xe0, CPX #$12 */
class CpxImm(c: Computer): CmpImmBase(c, "CPX") {
    override val opCode = 0xe0
    override val value = operand
    override val register get() = computer.cpu.X
    override val argName = "${operand.h()}"
}

/** 0xe5, SBC $10 */
class SbcZp(c: Computer): AddBase(c) {
    override val opCode = 0xe5
    override val size = 2
    override val timing = 3
    override fun run() {
        if (cpu.P.D) {
            TODO("Decimal mode not implemented")
        } else {
            // Call ADC with the one complement of the operand
            cpu.A = adc(cpu.A, memory[operand].inv())
        }
    }
    override fun toString(): String = "SBC $${operand.h()}"
}

/** 0xe6, INC $10 */
class IncZp(c: Computer): IncBase(c, 0xe6) {
    override val size = 2
    override val timing = 4
    override fun run() {
        memory[operand] = calculate(memory[operand])
    }
    override fun toString(): String = "INC $${operand.h()}"
}

/** 0xe8, INX */
class Inx(c: Computer): RegisterInstruction(c, 0xe8, "INX") {
    override fun run() {
        cpu.X = (cpu.X + 1).and(0xff)
        cpu.P.setNZFlags(cpu.X)
    }
}

/** 0xe9, SBC #$10 */
class SbcImmediate(c: Computer): AddBase(c) {
    override val opCode = SBC_IMM
    override val size = 2
    override val timing = 2
    override fun run() {
        if (cpu.P.D) {
            TODO("Decimal mode not implemented")
        } else {
            // Call ADC with the one complement of the operand
            cpu.A = adc(cpu.A, operand.inv())
        }
    }
    override fun toString(): String = "SBC #$${operand.h()}"
}

/** 0xea, NOP */
class Nop(c: Computer): InstructionBase(c) {
    override val opCode = 0xea
    override val size = 1
    override val timing = 2
    override fun run() { }
    override fun toString(): String = "NOP"
}

/** 0xf0, BEQ */
class Beq(computer: Computer): BranchBase(computer, 0xf0, "BEQ", { computer.cpu.P.Z })

/** 0xf8, SED */
class Sed(c: Computer): FlagInstruction(c, 0xf8, "SED") {
    override fun run() { cpu.P.D = true }
}

/** Unknown */
class Unknown(c: Computer, override val opCode: Int): InstructionBase(c) {
    override val size = 1
    override val timing = 1
    override fun run() { }
    override fun toString(): String = "???"
}

fun main() {
    val memory = Memory(0xa9, 0x23)
    val computer = Computer(memory = memory)
    computer.cpu.nextInstruction(computer)!!.run()
//    println(computer.cpu)
}
