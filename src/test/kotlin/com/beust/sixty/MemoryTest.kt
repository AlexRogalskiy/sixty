package com.beust.sixty

import com.beust.app.DEBUG
import org.assertj.core.api.Assertions.assertThat
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

@Test
class MemoryTest {


    fun languageCard() {
        runTest("auxmem.bin")
    }

    @Test(enabled = true)
    fun bankMemory() {
        runTest("bank-memory.bin")
    }

    private fun runTest(fileName: String) {
        val start = 0x6000
        val memory = Memory().apply {
            loadResource("Apple2e.rom", 0xc000)
            val ins2 = File("asm/$fileName").inputStream()
            load(ins2, start)
            listeners.add(DebugMemoryListener())
        }
        var success = true
        var message: String? = null

        DEBUG = true
        val c = Computer(cpu = Cpu(memory = memory)).apply {
            cpu.PC = start
            pcListener = object: PcListener {
                override fun onPcChanged(c: Computer) {
                    if (memory[c.cpu.PC] == 0) {
                        success = false
                        val word = word(address = 0x3d + c.cpu.Y)
                        val expected = memory[word]
                        message = "Failed at test #" + (memory[0x3d] + 1) + " at comparison #" + c.cpu.Y +
                            " got " + c.cpu.A
                            ", PC=" + c.cpu.PC.hh()
                        stop()
                    }
                }
            }
        }
        c.run()

        assertThat(success)
                .withFailMessage(message ?: "")
                .isTrue()
        println(memory[0x3d].toString() + " memory tests passed")
//        assertThat(c.cpu.P.C)
//                .withFailMessage("Failed after " + memory[0x3d] + " tests")
//                .isFalse()
    }
}
