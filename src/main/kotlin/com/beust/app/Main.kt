package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.FileWatcher
import com.beust.sixty.PulseManager
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val RUN = true
var DEBUG = false
val BREAKPOINT: Int? = null // 0x5a0//0xc6a6//0x5f8 // 0x5f8 // 0x5a0 //0x5f8 // 0x59e // null//0x556//0xc2de // 0xc2db

fun disk(s: String) = File("disks/$s")

val DISKS = listOf(
        disk("Apple DOS 3.3.dsk"), // 0  6
        disk("Apple DOS 3.3.woz"), // 1
        File("src/test/resources/audit.dsk"), // 2
        disk("Blade_of_Blackpoole_A.dsk"), // 3
        disk("Sherwood_Forest.dsk") ,       // 4
        File("d:/pd/Apple disks/Ultima I - The Beginning.woz"), // 5
        disk("Rescue Raiders.dsk"), // 6
        disk("Ultima4.dsk") , // 7
        disk("Force 7.woz"), // 8
        disk("Masquerade-1.dsk"), // 9
        disk("Bouncing Kamungas - Disk 1, Side A.woz"), // 10
        disk("King's Quest - A.woz"), // 11
        disk("Karateka.dsk") // 12
        // 10
)

val DISK = DISKS[6]

fun main() {
    val pulseManager = PulseManager()
    val fw = FileWatcher()
    var c = Apple2Computer()
    val gc = GraphicContext({ -> c }) { ->
        c.memory
    }
    val memoryListener = Apple2MemoryListener({ -> c.memory }, gc.textWindow, gc.hiResWindow)

    c.memory.listeners.add(memoryListener)

    c.memoryListeners.forEach { c.memory.listeners.add(it) }

    if (RUN) {
        val command = object: Runnable {
            override fun run() {
                var stop = false
                while (! stop)
                {
                    val status = pulseManager.run(c)
                    if (status == Computer.RunStatus.STOP) {
                        stop = true
                    } else if (status == Computer.RunStatus.REBOOT) {
                        gc.clear()
                        c = Apple2Computer()
                        with(c) {
                            memory.listeners.add(memoryListener)
                            c.memoryListeners.forEach { c.memory.listeners.add(it) }
                        }
                    } // else STOP
                }
            }
        }
        val tp = Executors.newScheduledThreadPool(1)
        tp.scheduleWithFixedDelay(command, 0, 100, TimeUnit.MILLISECONDS)

    }
    gc.run()
    fw.stop = true
    pulseManager.stop()
}

