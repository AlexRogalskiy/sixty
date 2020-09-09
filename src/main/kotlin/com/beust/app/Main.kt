package com.beust.app

import com.beust.sixty.*
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

val DEBUG = false

fun main() {
    val choice = 2

    when(choice) {
        1 -> {
            println("Running the following 6502 program which will display HELLO")
            val c = TestComputer.createComputer()
            c.disassemble(start = 0, length = 15)
            c.run(debugAsm = false)
        }
        2 -> {
            val d = true
            val debugMem = DEBUG
            val debugAsm = DEBUG
            apple2Computer(debugMem).run(debugMem, debugAsm)//true, true)
        }
        3 -> {
            testDisk()
        }
        else -> {
            val result = functionalTestComputer(false).run()//true, true)
            with(result) {
                val sec = durationMillis / 1000
                val mhz = String.format("%.2f", cycles / sec / 1_000_000.0)
                println("Computer stopping after $cycles cycles, $sec seconds, $mhz MHz")
            }
        }
    }
}

fun testDisk() {
    val ins = Woz::class.java.classLoader.getResource("woz2/DOS 3.3 System Master.woz")!!.openStream()
    val ins2 = File("d:\\pd\\Apple DIsks\\woz2\\The Apple at Play.woz").inputStream()
    val disk = WozDisk(ins)

    fun pair() = disk.nextByte().shl(1).or(1).and(disk.nextByte()).and(0xff)

    repeat(12) {
        disk.incTrack()
    }

    repeat(13) {
        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0x96)) {
            disk.nextByte()
        }
        println("Found d5 aa 96 at position " + (disk.position / 8).hh())
        val s = disk.nextBytes(3)
        val volume = pair()
        val track = pair()
        val sector = pair()
        val checksumAddress = pair()
        if (volume.xor(track).xor(sector) != checksumAddress) {
            TODO("Checksum doesn't match")
        }
        println("Volume: $volume Track: $track Sector: $sector checksum: $checksumAddress")
        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for address")
        }

        while (disk.peekBytes(3) != listOf(0xd5, 0xaa, 0xad)) {
            disk.nextByte()
        }
        disk.nextBytes(3)

        val buffer = IntArray(342)
        var checksum = 0
        for (i in buffer.indices) {
            val b = disk.nextByte()
            if (READ_TABLE[b] == null) {
                println("INVALID NIBBLE")
            }
            checksum = checksum xor READ_TABLE[b]!!
            if (i < 86) {
                buffer[buffer.size - i - 1] = checksum
            } else {
                buffer[i - 86] = checksum
            }
        }
        checksum = checksum xor READ_TABLE[disk.nextByte()]!!
        if (checksum != 0) {
            TODO("BAD CHECKSUM")
        }

        val sectorData = IntArray(256)
        for (i in sectorData.indices) {
            val b1: Int = buffer[i]
            val lowerBits: Int = buffer.size - i % 86 - 1
            val b2: Int = buffer[lowerBits]
            val shiftPairs = i / 86 * 2
            // shift b1 up by 2 bytes (contains bits 7-2)
            // align 2 bits in b2 appropriately, mask off anything but
            // bits 0 and 1 and then REVERSE THEM...
            val reverseValues = intArrayOf(0x0, 0x2, 0x1, 0x3)
            val b = b1 shl 2 or reverseValues[b2 shr shiftPairs and 0x03]
            sectorData[i] = b
        }

        if (disk.nextBytes(3) != listOf(0xde, 0xaa, 0xeb)) {
            TODO("Didn't find closing for data")
        }
        println("  Successfully read track")
    }

    repeat(100) {
        print(disk.nextByte().h() + " ")
    }
    println("")
}
