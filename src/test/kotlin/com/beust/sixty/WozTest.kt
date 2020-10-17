package com.beust.sixty

import com.beust.app.*
import org.assertj.core.api.Assertions.assertThat
import org.testng.annotations.Test

@Test
class WozTest {
    private fun diskStream(name: String)
            = Woz::class.java.classLoader.getResource(name)!!.openStream()

    fun bits() {
        val ins = diskStream("woz2/DOS 3.3 System Master.woz")!!
        val bytes: ByteArray = ins.readAllBytes()
        val slice = bytes.slice(0x600 until bytes.size)
        val bitStream = BitBitStream(slice)
        val bitStream2 = BitStream2(slice.toByteArray())
        repeat(slice.size) {
            val b = bitStream.nextBit()
            val d = bitStream2.nextBit()
            assertThat(b).isEqualTo(d)
        }
    }

    private fun createHeadlessApple2Computer(disk: IDisk) = Apple2Computer().apply {
        diskController.loadDisk(disk)
        val runner = Runner()
        repeat(100) {
            runner.runSlice(this)
        }
        ""
    }

    fun multipleDisks() {
        createHeadlessApple2Computer(IDisk.create(DISKS[0])!!)
    }

    @Test(enabled = false)
    fun bytes() {
        val diskName = "woz2/DOS 3.3 System Master.woz"
        val ins = Woz::class.java.classLoader.getResource(diskName)!!.openStream()
        val disk = WozDisk("DOS 3.3.woz", ins)

        val ins2 = Woz::class.java.classLoader.getResource(diskName)!!.openStream()
        val bytes: ByteArray = ins2.readAllBytes()
        val size = bytes.size - 0x600
        repeat(35) { track ->
            val bitStream = disk.bitStream
            repeat(size) {
                var latch = 0
                val byte = disk.nextByte()
                if (latch and 0x80 != 0) latch = 0
                while (latch and 0x80 == 0) {
                    val bit = bitStream.nextBit()
                    latch = latch.shl(1).or(bit)
                }
                val byte2 = latch
                assertThat(byte)
                        .withFailMessage("Failure at track $track")
                        .isEqualTo(byte2)
                println("Matched " + byte.h() + " with " + latch.h())
            }
            disk.incPhase()
            disk.incPhase()
        }
    }
}

class BitStream2(val bytes: ByteArray, override val sizeInBits: Int = bytes.size * 8): IBitStream {
    private val bits = arrayListOf<Int>()
    private var position = 0
    private var saved = -1

    override fun save() { saved = position }
    override fun restore() { position = saved }

    init {
        bytes.forEach { b ->
            var i = 7
            repeat(8) {
                bits.add(b.bit(i--))
            }
        }
    }

    override fun nextBit(): Int {
        val result = bits[position]
        position = (position + 1) % bytes.size
        return result
    }

}