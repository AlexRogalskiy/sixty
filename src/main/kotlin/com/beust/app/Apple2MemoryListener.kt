package com.beust.app

import com.beust.app.app.TextPanel
import com.beust.sixty.Apple2Memory
import com.beust.sixty.IMemory
import com.beust.sixty.MemoryListener
import com.beust.swt.HiResWindow

class Apple2MemoryListener(private val memory: Apple2Memory,
        private val textPanel: TextPanel, private val hiresScreen: HiResWindow):
        MemoryListener() {
    override fun isInRange(address: Int): Boolean {
        return address in 0x400..0x800 || address in 0x2000..0x6000 || address == 0xc054 || address == 0xc055
    }

    override fun onWrite(location: Int, value: Int) {
        when (location) {
            in 0x400..0x7ff -> {
                textPanel.drawMemoryLocation(location, value)
            }
            in 0x2000..0x4000 -> {
                if (memory.hires) {
                    hiresScreen.drawMemoryLocation(memory, location, 0)
                }
            }
            in 0x4000..0x6000 -> {
                if (memory.hires) {
                    hiresScreen.drawMemoryLocation(memory, location, 1)
                }
            }
            0xc054  -> {
                hiresScreen.page = 0
            }
            0xc055 -> {
                hiresScreen.page = 1
            }
        }
    }
}
