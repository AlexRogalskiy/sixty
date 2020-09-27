package com.beust.swt

import com.beust.app.app.ITextScreen
import com.beust.sixty.h
import org.eclipse.jface.resource.FontDescriptor
import org.eclipse.jface.resource.JFaceResources
import org.eclipse.jface.resource.LocalResourceManager
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label

class MainWindow(parent: Composite): Composite(parent, SWT.NONE), ITextScreen {
    private val textFont: Font = font(shell, "Arial", 10, SWT.BOLD)
    private val labels = arrayListOf<Label>()

    init {
        layout = GridLayout(40, true).apply {
            horizontalSpacing = 0
            verticalSpacing = 2
        }
        background = display.getSystemColor(SWT.COLOR_BLACK)
        repeat(ITextScreen.WIDTH) {
            repeat(ITextScreen.HEIGHT) {
                labels.add(Label(this, SWT.NONE).apply {
                    font = textFont
                    background = display.getSystemColor(SWT.COLOR_BLACK)
                    foreground = display.getSystemColor(SWT.COLOR_GREEN)
                    text = "@"
                })
            }
        }

        pack()
    }

    /**
     * Reference:  https://en.wikipedia.org/wiki/Apple_II_character_set
     */
    override fun drawCharacter(x: Int, y: Int, value: Int) {
        display.asyncExec {
            if (! shell.isDisposed) {
                labels[y * ITextScreen.WIDTH + x].let { label ->
                    if (!label.isDisposed) {
                        when(value) {
                            in 0x01..0x3f -> { // inverse
                                label.background = green(display)
                                label.foreground = black(display)
                            }
//                            in 0x40..0x7f -> { // flashing
//
//                            }
                            else -> {
                                label.background = black(display)
                                label.foreground = green(display)
                            }
                        }

                        val c = when(value) {
                            in 0x01..0x1a -> {
                                value + 0x40
                            }
                            else -> value.and(0x7f)
                        }
                        if (value != 0xff && value != 0xa0) {
                            println("Drawing ${value.h()} ${c.h()} " + c.toChar().toString())
                        }
                        label.text = c.toChar().toString()
                    }
                }
            }
        }
    }
}