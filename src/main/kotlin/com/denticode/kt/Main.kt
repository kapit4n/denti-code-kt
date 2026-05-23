package com.denticode.kt

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.denticode.kt.data.DentiDatabase
import com.denticode.kt.data.DentiRepository
import com.denticode.kt.ui.layout.AppShell

fun main(args: Array<String>) =
    application {
        val resetLocalDb =
            args.contains("--reset-local-db") ||
                args.contains("--fresh-db") ||
                System.getProperty("denti.resetLocalDb") == "true"
        DentiDatabase.connectAndMigrate(resetLocalDatabase = resetLocalDb)
        val repo = DentiRepository()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Denti-Code · Clínica (desktop)",
            state = rememberWindowState(size = DpSize(1280.dp, 800.dp)),
        ) {
            AppShell(repo)
        }
    }
