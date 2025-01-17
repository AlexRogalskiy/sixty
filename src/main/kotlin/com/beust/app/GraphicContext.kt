package com.beust.app

import com.beust.sixty.IKeyProvider
import com.beust.sixty.IMemory
import com.beust.sixty.h
import com.beust.sixty.log
import com.beust.swt.*
import org.eclipse.jface.dialogs.MessageDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.*
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import java.awt.Color

class GraphicContext {
    private lateinit var computer: Apple2Computer

    val hiResWindow: HiResWindow
    val textWindow: TextWindow
    private val display: Display = Display()
    private val shell: Shell

    fun reset(c: Apple2Computer) {
        clear()
        computer = c
    }

    fun clear() {
        hiResWindow.clear()
        textWindow.clear()
    }

    enum class Controller {
        MAX_LEFT, MAX_RIGHT, MAX_UP, MAX_DOWN;
    }

    private val keyProvider = object: IKeyProvider {
        override fun keyPressed(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
            with(memory) {
                forceInternalRomValue(0xc000, value.or(0x80))
                forceInternalRomValue(0xc010, 0x80)
            }
        }

        override fun keyReleased(memory: IMemory, value: Int, shift: Boolean, control: Boolean) {
            log("Key released")
            with(memory) {
                forceInternalRomValue(0xc064, 0x80)
                forceInternalRomValue(0xc065, 0x80)
            }
        }

        override fun onController(memory: IMemory, c: Controller) {
            log("onController: $c")
            with(memory) {
                when (c) {
                    Controller.MAX_LEFT -> forceInternalRomValue(0xc064, 0x0)
                    Controller.MAX_RIGHT -> forceInternalRomValue(0xc064, 0x7f)
                    Controller.MAX_UP -> forceInternalRomValue(0xc065, 0x00)
                    Controller.MAX_DOWN -> forceInternalRomValue(0xc065, 0x7f)
                }
            }
        }
    }

    private fun maybeResize(control: Control) {
        if (control == hiResWindow) {
            control.display.asyncExec {
                val fullHeight = ACTUAL_HEIGHT
                val shortHeight = ACTUAL_MIXED_HEIGHT
                val b = control.bounds
                val newHeight = if (UiState.mainScreenMixed.value) shortHeight else fullHeight
                control.setBounds(b.x, b.y, b.width, newHeight)
                control.parent.layout()
            }
        }
    }

    private fun show(control: Control) {
        if (! control.isDisposed) with(control) {
            display.asyncExec {
                maybeResize(control)
                control.moveAbove(null)
            }
        }
    }

    fun run() {
        shell.open()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        hiResWindow.stop()
//        Fonts.disposeAll()
//        display.dispose()
    }

    companion object {
        lateinit var textFont: Font
        lateinit var textFontSmaller: Font
    }

    init {
        shell = Shell(display).apply {
            layout = GridLayout(3, false).apply {
                marginWidth = 0
                marginHeight = 0
            }
        }

        UiState.error.addAfterListener { _, new ->
            MessageDialog.openError(shell, "Error", new)
        }
        UiState.speedMegahertz.addAfterListener { _, new ->
            val diskName = UiState.diskStates[0].file.value?.name ?: ""
            shell.text = String.format("$diskName %2.2f Mhz", new)
        }
        val isFontLoaded = shell.display.loadFont("fonts/PrintChar21.ttf")
        textFont = if (isFontLoaded) {
            Font(shell.display, "Print Char 21", 12, SWT.NORMAL)
        } else {
            Fonts.font(shell, "Arial", 9, SWT.BOLD)
        }
        textFontSmaller = if (isFontLoaded) {
            Font(shell.display, "Print Char 21", 8, SWT.NORMAL)
        } else {
            Fonts.font(shell, "Arial", 8, SWT.BOLD)
        }

        //
        // Contains the text/graphic windows and below it, the drive1/swap/drive2 buttons
        //
        val leftContainerColumns = 11
        Composite(shell, SWT.NONE).apply {
            layout = GridLayout(leftContainerColumns, false)
            layoutData = GridData().apply {
                verticalAlignment = SWT.BEGINNING
            }

            //
            // Contains the text/graphic windows
            // Span over the three columns
            //
            Composite(this, SWT.NONE).apply {
                layoutData = GridData().apply {
                    horizontalSpan = leftContainerColumns
                }
                display.addFilter(SWT.KeyDown) { e ->
                    keyProvider.keyReleased(computer.memory, e.keyCode)
                }
                display.addFilter(SWT.KeyDown) { e ->
//                    println("Key code: " + e.keyCode.h() + " character: " + e.character)
                    val index = when {
                        e.stateMask.and(SWT.SHIFT) != 0 -> 1
//                        e.stateMask.and(SWT.CONTROL) != 0 -> 2
//                        e.stateMask.and(SWT.ALT) != 0 -> 3
                        else -> 0
                    }

                    // http://www.lazilong.com/apple_II/bbros/ascii.jpg
                    if (e.keyCode != 0xd) {
                        val key = when(e.keyCode) {
                            SWT.ARROW_LEFT -> 0x88
                            SWT.ARROW_RIGHT -> 0x95
                            SWT.ARROW_UP -> 0x8b
                            SWT.ARROW_DOWN -> 0x8a
                            SWT.KEYPAD_4 -> {
                                keyProvider.onController(computer.memory, Controller.MAX_LEFT)
                                e.keyCode
                            }
                            SWT.KEYPAD_6 -> {
                                keyProvider.onController(computer.memory, Controller.MAX_RIGHT)
                                e.keyCode
                            }
                            SWT.KEYPAD_8 -> {
                                keyProvider.onController(computer.memory, Controller.MAX_UP)
                                e.keyCode
                            }
                            SWT.KEYPAD_2 -> {
                                keyProvider.onController(computer.memory, Controller.MAX_DOWN)
                                e.keyCode
                            }
                            in 0x61..0x7a -> e.keyCode - 0x20
                            else -> {
                                val l = KEY_MAP[e.keyCode]
                                if (l != null) {
                                    l[index].toInt()
                                } else {
                                    e.keyCode
                                }
                            }
                        }
                        keyProvider.keyPressed(computer.memory, key)
                    }
                }
                display.addFilter(SWT.Traverse) { e ->
                    val ignore = e.keyCode == 0xd || e.keyCode == 0x20
                    if (ignore && e.widget is Shell) {
                        keyProvider.keyPressed(computer.memory, e.keyCode)
                        e.doit = false
                    }
                }


                //
                // Text screens
                //
                textWindow = TextWindow(this, 0x400).apply {
                    bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT + 10)
                }
                //                    .apply {
                //                pack()
                //            }

                //
                // Graphic screens
                //
                hiResWindow = HiResWindow(this).apply {
                    bounds = Rectangle(0, 0, ACTUAL_WIDTH, ACTUAL_HEIGHT + 10)
                }
            }

            //
            // Drive 1 / swap / Drive2 buttons
            //
            val width = 250
            val height = 150
            fun driveButton(parent: Composite, drive: Int) = Composite(parent, SWT.NONE).apply {
                layout = GridLayout(1, true).apply {
                    marginWidth = 0
                }
                layoutData = GridData().apply {
                    horizontalSpan = 5
                    horizontalAlignment = SWT.FILL
                    grabExcessHorizontalSpace = true
                }
                diskDescription(this, drive).apply {
                    layoutData = GridData(SWT.FILL, SWT.FILL, true, false)
                }
//                Button(this, SWT.WRAP).apply {
//                    val ins = this::class.java.classLoader.getResource("disk-04.png")!!.openStream()
//                    val imageData = ImageData(ins)
//                    image = Image(display, imageData)
//                    layoutData = GridData().apply {
//                        widthHint = width
//                        heightHint = height
//                    }
//                    addPaintListener { e ->
//                        with(e.gc) {
//                            if (UiState.diskStates[drive].motor.value) {
//                                background = red(display)
//                                foreground = red(display)
//                            } else {
//                                background = black(display)
//                                foreground = black(display)
//                            }
//                            fillOval(42, 102, 13, 13)
//                        }
//                    }
//                    fileDialog(shell, this, UiState.diskStates[drive].file)
//                    obs.addListener { _, _ -> redraw() }
//                }
            }

            driveButton(this, 0)
//            button(this, "Swap").apply {
////                layoutData = GridData().apply {
////                    horizontalSpan = 6
////                    horizontalAlignment = SWT.FILL
////                    grabExcessHorizontalSpace = true
////                }
//                addListener(SWT.Selection) { e ->
//                    val d1 = UiState.diskStates[0]
//                    UiState.diskStates[0] = UiState.diskStates[1]
//                    UiState.diskStates[1] = d1
//                }
//            }
            driveButton(this, 1)
        }

        //
        // Middle panel, where the buttons live
        //
        val buttonContainer = Composite(shell, SWT.BORDER)
        val rebootButton = button(buttonContainer, "Reboot").apply {
            addListener(SWT.Selection) { e ->
                computer.reboot()
            }
        }

        buttonContainer.apply {
            layoutData = GridData(SWT.FILL, SWT.FILL, false, true)
            layout = GridLayout(1, true)
            listOf(rebootButton).forEach {
                it.layoutData = GridData().apply {
                    widthHint = 50
                    heightHint = 50
                }
            }
        }

        //
        // Right panel
        //
//
//    createScrollableByteBuffer(shell, parentHeight).apply {
////        layoutData = GridData(GridData.FILL_BOTH, GridData.FILL_BOTH, true, true)
//    }
        val folder = TabFolder(shell, SWT.NONE).apply {
            layoutData = GridData(GridData.FILL, GridData.FILL, true, true).apply {
                grabExcessVerticalSpace = true
//            heightHint = parentHeight
            }
        }

        val diskWindow = DiskWindow(folder)
        TabItem(folder, SWT.NONE).apply {
            text = "DISK"
            control = diskWindow
        }
        TabItem(folder, SWT.NONE).apply {
            text = "DEBUGGER"
            control = DebuggerWindow(folder, { -> computer })
        }

        folder.setSelection(0)


//    folder.setSize(500, 900)

//    mainWindow.pack()
//    folder.pack()
        shell.pack()
        shell.setSize(shell.bounds.width + 300, MAIN_WINDOW_HEIGHT)
//        rebootButton.addListener(SWT.Selection) { e ->
//            computer.reboot()
//        }

        UiState.mainScreenHires.addListener { _, new ->
            if (new) show(hiResWindow)
        }
        UiState.mainScreenPage2.addAfterListener { _, _ ->
            hiResWindow.page = if (computer.memory.page2) 1 else 0
            if (! computer.memory.store80On) {
                if (UiState.mainScreenText.value) {
                    show(textWindow)
                } else {
                    show(hiResWindow)
                }
            }
        }
        UiState.mainScreenText.addAfterListener { _, new ->
            if (new) show(textWindow)
            else show(hiResWindow)
        }
        UiState.mainScreenMixed.addAfterListener { _, new ->
            maybeResize(hiResWindow)
        }
    }

    /**
     * Disk name, the type of the disk, and the current track.
     */
    fun diskDescription(parent: Composite, drive: Int): Composite {
        val columns = 8
        val fontSize = 12
        val bg = lightGrey(display)
        var currentDisk: Button? = null
        lateinit var currentDiskType: Label
        lateinit var currentTrack: Label
        lateinit var currentSector: Label
        lateinit var light: Composite

        fun nameAndType(drive: Int): Pair<String, String> {
            val obs = UiState.diskStates[drive].file
            val dn = obs.value?.name
            val result = if (dn != null) {
                val index = dn.lastIndexOf(".")
                if (index != -1) {
                    Pair(dn.substring(0, index), dn.substring(index + 1).toUpperCase())
                } else {
                    Pair(dn, "?")
                }
            } else {
                Pair("", "")
            }
            return result
        }

        val result = Composite(parent, SWT.BORDER).apply {
            background = bg
            layout = GridLayout(columns, false)
            layoutData = GridData(SWT.BEGINNING, SWT.TOGGLE, true, false)

            val (diskName, diskType) = nameAndType(drive)

            currentDisk = button(this, diskName).apply {
                background = bg
                font = Fonts.font(shell, "Roboto", fontSize, SWT.BOLD)
                layoutData = GridData(SWT.FILL, SWT.CENTER, true, true).apply {
                    verticalSpan = 2
                    grabExcessHorizontalSpace = true
                    horizontalSpan = columns
                }
                fileDialog(shell, this, UiState.diskStates[drive].file)
            }

            fun c1(c: Control) = with(c) {
                background = bg
                font = Fonts.font(shell, "Helvetica", fontSize - 4)
                layoutData = GridData().apply {
                    horizontalAlignment = SWT.RIGHT
                    grabExcessHorizontalSpace = true
                }
            }
            fun c2(c: Control) = with(c) {
                background = bg
                font = Fonts.font(shell, "Verdana", fontSize - 2, SWT.BOLD)
                layoutData = GridData().apply {
                    horizontalAlignment = SWT.FILL
                    grabExcessHorizontalSpace = true
                }
            }
            c1(label(this, "Type:"))
            currentDiskType = label(this, diskType)
            c2(currentDiskType!!)

            c1(label(this, "Track:"))
            currentTrack = label(this, "0")
            c2(currentTrack)

            c1(label(this, "Sector:"))
            currentSector = label(this, "0")
            c2(currentSector)

            val size = 32
            light = Composite(this, SWT.NONE).apply {
                background = bg
                addPaintListener { e -> with(e.gc) {
                    val color = when (UiState.diskStates[drive].motor.value) {
                        DiskController.MotorState.ON -> red(display)
                        DiskController.MotorState.SPINNING_DOWN -> yellow(display)
                        else -> bg
                    }
                    foreground = color
                    background = color
                    fillOval(size / 4, size / 4, size / 2, size / 2)
                } }
                layoutData = GridData().apply {
                    widthHint = size
                    heightHint = size
                }
            }
        }

        //
        // Add listeners
        //
        UiState.diskStates[drive].file.addAfterListener { _, _ ->
            val (diskName, type) = nameAndType(drive)
            currentDisk?.let {
                it.text = diskName
                it.requestLayout()
            }
            currentDiskType.text = type
            currentDiskType.requestLayout()
        }
        UiState.diskStates[drive].currentPhase.addAfterListener { _, new ->
            currentTrack.text = new.toString()
        }
        UiState.diskStates[drive].currentSector.addAfterListener { _, new ->
            currentSector.text = new?.sector.toString() ?: ""
        }
        UiState.diskStates[drive].motor.addAfterListener { _, _ ->
            light.redraw()
        }

        return result
    }
}