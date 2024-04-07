@file:JvmName("Lwjgl3Launcher")

package com.fatih.kingsofpigs.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.fatih.kingsofpigs.KingOfPigs
import java.io.PrintStream

/** Launches the desktop (LWJGL3) application. */
fun main() {
    Lwjgl3Application(KingOfPigs(), Lwjgl3ApplicationConfiguration().apply {
        setTitle("KingOfPigs")
        setWindowedMode(1080, 720)
        useVsync(true)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    })
}
