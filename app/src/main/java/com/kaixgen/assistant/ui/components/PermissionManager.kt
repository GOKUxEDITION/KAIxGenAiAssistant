package com.kaixgen.assistant.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Immutable
data class KaiPermissionSnapshot(
    val hasMicrophone: Boolean,
    val hasNotifications: Boolean
) {
    val allCoreGranted: Boolean get() = hasMicrophone && hasNotifications
}

class KaiPermissionController(
    private val snapshotProvider: () -> KaiPermissionSnapshot,
    private val requestCorePermissionsAction: () -> Unit,
    private val requestMicPermissionAction: () -> Unit
) {
    val snapshot: KaiPermissionSnapshot get() = snapshotProvider()
    fun requestCorePermissions() = requestCorePermissionsAction()
    fun requestMicrophoneOnly() = requestMicPermissionAction()
}

object PermissionManager {

    fun coreRuntimePermissions(): Array<String> = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    fun checkSnapshot(context: Context): KaiPermissionSnapshot {
        val mic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return KaiPermissionSnapshot(
            hasMicrophone = mic,
            hasNotifications = notif
        )
    }
}

@Composable
fun rememberKaiPermissionController(
    onResult: (KaiPermissionSnapshot) -> Unit = {}
): KaiPermissionController {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf(PermissionManager.checkSnapshot(context)) }

    val multipleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val updated = PermissionManager.checkSnapshot(context)
        snapshot = updated
        onResult(updated)
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        val updated = PermissionManager.checkSnapshot(context)
        snapshot = updated
        onResult(updated)
    }

    return remember(snapshot) {
        KaiPermissionController(
            snapshotProvider = { snapshot },
            requestCorePermissionsAction = {
                multipleLauncher.launch(PermissionManager.coreRuntimePermissions())
            },
            requestMicPermissionAction = {
                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )
    }
}
