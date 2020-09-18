package com.beust.app

import com.beust.sixty.Computer
import com.beust.sixty.MemoryInterceptor
import com.beust.sixty.hh

//object SoftSwitches {
//    val RANGE = 0xc000..0xc0e7
//
//    fun onRead(c: Computer, location: Int, value: Int): Int {
//        var result = 0xd
//        when(location) {
//            0xC000 -> {
//                result = value
//            } // KBD/CLR80STORE
//            0xC001 -> {} // SET80STORE
//            0xC006 -> {} // SETSLOTCXROM
//            0xc007 -> {} // SETINTCXROM
//            0xC00C -> {} // CLR80COL
//            0xC00E -> {} // CLRALTCHAR
//            0xC010 -> with(c.cpu.memory) {
//                force {
//                    this[0xc000] = this[0xc000].and(0x7f)
//                }
//            } // KBDSTRB
//            0xC015 -> {} // RDCXROM
//            0xC018 -> { result = 0x8d } // RD80STORE
//            0xC01C -> {} // RDPAGE2
//            0xc030 -> {} // SPKR
//            0xC054 -> {} // LOWSCR
//            0xC051 -> {} // TXTSET
//            0xC055 -> {} // HISCR
//            0xC056 -> {} // LORES
//            0xC058 -> {} // SETAN0
//            else -> {
//                println("Unknown soft switch: " + location.hh())
//            }
//        }
//        return result
//    }
//
//    fun onWrite(location: Int, value: Int): MemoryInterceptor.Response {
//        var allow = false
//        when(location) {
//            0xC000 -> { allow = true } // KBD/CLR80STORE
//            0xC001 -> {} // SET80STORE
//            0xC006 -> {} // SETSLOTCXROM
//            0xc007 -> {} // SETINTCXROM
//            0xC00C -> {} // CLR80COL
//            0xC00E -> {} // CLRALTCHAR
//            0xC010 -> { allow = true } // KBDSTRB
//            0xC054 -> {} // LOWSCR
//            0xC055 -> {} // HISCR
//            else -> {
//                TODO("Unknown soft switch: " + location.hh())
//            }
//        }
//        return MemoryInterceptor.Response(allow, value)
//    }
//}