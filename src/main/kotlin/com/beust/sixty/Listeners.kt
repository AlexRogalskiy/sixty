package com.beust.sixty

class DebugMemoryListener(val debugMemory: Boolean = false) : MemoryListener() {
    fun logMem(i: Int, value: Int, extra: String = "") {
        lastMemDebug.add("mem[${i.hh()}] = ${(value.and(0xff)).h()} $extra")
    }

    override fun onWrite(location: Int, value: Int) {
        if (debugMemory) logMem(location, value)
    }

}