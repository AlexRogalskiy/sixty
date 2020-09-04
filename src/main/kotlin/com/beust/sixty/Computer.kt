package com.beust.sixty

interface MemoryInterceptor {
    class Response(val allow: Boolean, val value: Int)

    fun onRead(location: Int): Response
    fun onWrite(location: Int, value: Int): Response
}

interface MemoryListener {
    val lastMemDebug: ArrayList<String>
    fun onRead(location: Int, value: Int)
    fun onWrite(location: Int, value: Int)
}

interface PcListener {
    fun onPcChanged(newValue: Int)
}

class Computer(val cpu: Cpu = Cpu(memory = Memory()),
        memoryListener: MemoryListener? = null,
        memoryInterceptor: MemoryInterceptor? = null,
        var pcListener: PcListener? = null
) {
    val pc get() = cpu.PC
    val memory = cpu.memory

    private var startTime: Long = 0

    init {
        memory.listener = memoryListener
        memory.interceptor = memoryInterceptor
    }

    private var stop: Boolean = false

    fun stop() {
        stop = true
    }

    fun byteWord(memory: Memory = cpu.memory, address: Int = cpu.PC + 1): Pair<Int, Int> {
        return memory[address] to memory[address].or(memory[address + 1].shl(8))
    }

    fun run() {
        startTime = System.currentTimeMillis()
        var cycles = 0
        var done = false
        var previousPc = 0
        while (! done && ! stop) {
            cycles++

            if (memory[cpu.PC] == 0x60 && cpu.SP.isEmpty()) {
                done = true
            } else {
//                if (cpu.PC == 0x2edc) {
//                    println(this)
//                    println("breakpoint: " + memory[0xe].h())
//                }
                previousPc = cpu.PC
                if (DEBUG_ASM) {
                    val inst = cpu.nextInstruction()
                    val (byte, word) = byteWord()
                    val debugString = formatPc(cpu, inst) + formatInstruction(inst, byte, word)
                    cpu.PC += inst.size
                    inst.run(this, byte, word)
                    println(debugString + " " + cpu.toString())
                } else {
                    val inst = cpu.nextInstruction()
                    val (byte, word) = byteWord()
                    cpu.PC += inst.size
                    inst.run(this, byte, word)
                }

                if (previousPc == cpu.PC) {
                    // Current functional tests highest score: 158489
                    println(this)
                    println("Forever loop after $cycles cycles")
                    println("")
                } else {
                    previousPc = cpu.PC
                }

                memory.listener?.lastMemDebug?.forEach {
                    println("  $it")
                }
                memory.listener?.lastMemDebug?.clear()
            }
            pcListener?.onPcChanged(cpu.PC)
        }
        val sec = (System.currentTimeMillis() - startTime) / 1000
        val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
        println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
    }

//    fun clone(): Computer {
//        return Computer(cpu.clone(), Memory(memory.size, *memory.content))
//    }
//
    fun disassemble(memory: Memory, a: Int, length: Int = 10, print: Boolean = true): List<String> {
        val result = arrayListOf<String>()
        var address = a
        var pc = cpu.PC
        var done = false
        var n = length
        while (! done) {
            val inst = cpu.nextInstruction(memory, address, noThrows = true)!!
            result.add(disassemble(cpu, inst, print))
            address += inst.size
            pc += inst.size
            if (--n <= 0) done = true
        }
    return result
    }

    private fun formatPc(cpu: Cpu, inst: Instruction): String {
        val pc = cpu.PC
        val bytes = StringBuffer(inst.opCode.h())
        bytes.append(if (inst.size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (inst.size == 3) (" " + memory[pc + 2].h()) else "   ")
        return String.format("%-5s: %-10s", pc.hh(), bytes.toString())
    }

    private fun formatInstruction(inst: Instruction, byte: Int, word: Int): String {
        return String.format("%-12s", inst.toString(this, byte, word))
    }

    private fun disassemble(cpu: Cpu, inst: Instruction, print: Boolean): String {
        val pc = cpu.PC
        val bytes = StringBuffer(inst.opCode.h())
        bytes.append(if (inst.size > 1) (" " + memory[pc + 1].h()) else "   ")
        bytes.append(if (inst.size == 3) (" " + memory[pc + 2].h()) else "   ")
        val cpuString = String.format("%8s ${cpu}", " ")

        val result = String.format("%-5s %-10s %-12s %s", pc.hh() + ":", bytes.toString(), inst.toString(), cpuString)

        if (print) println(result)
        return result
    }

    override fun toString(): String {
        return "{Computer cpu:$cpu}\n$memory"
    }

}