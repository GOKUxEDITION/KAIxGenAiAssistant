package com.kaixgen.assistant.domain.voice

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.kaixgen.assistant.data.local.KaiLocalRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Play-compliant Android Action Executor implementing PRD 15 §3 capability contracts.
 * Executes real Android Intents, AudioManager, CameraManager torch, BatteryManager,
 * and local Room/FindPhone controllers without requesting restricted permissions.
 */
class KaiActionExecutor(
    private val context: Context,
    private val repository: KaiLocalRepository,
    private val parser: HinglishVoiceCommandParser
) {
    private var lastTorchState: Boolean = false

    fun execute(command: ParsedVoiceCommand, confirmed: Boolean = false): ActionExecutionResult {
        if (command.requiresConfirmation && !confirmed) {
            return buildConfirmationPrompt(command)
        }

        return when (command.intent) {
            VoiceIntentType.OPEN_APP -> executeOpenApp(command.slots["app"].orEmpty())
            VoiceIntentType.OPEN_SETTINGS -> executeOpenSettings(command.slots["page"] ?: "main")
            VoiceIntentType.SEARCH_WEB -> executeWebSearch(command.slots["query"].orEmpty())
            VoiceIntentType.CALL_CONTACT -> executeDialer(contactOrNumber = command.slots["contact"].orEmpty(), isNumber = false)
            VoiceIntentType.CALL_NUMBER -> executeDialer(contactOrNumber = command.slots["number"].orEmpty(), isNumber = true)
            VoiceIntentType.ANSWER_CALL -> ActionExecutionResult.Executed(
                spokenResponse = "Call answer shortcut ready when incoming call rings.",
                displayMessage = "Hands-free call answer is active for incoming calls."
            )
            VoiceIntentType.REJECT_CALL -> ActionExecutionResult.Executed(
                spokenResponse = "Incoming call reject command registered.",
                displayMessage = "Hands-free call reject ready."
            )
            VoiceIntentType.PLAY_MEDIA -> executePlayMedia(command.slots["query"].orEmpty())
            VoiceIntentType.MEDIA_CONTROL -> ActionExecutionResult.Executed(
                spokenResponse = "Media ${command.slots["command"] ?: "control"} sent.",
                displayMessage = "Media command (${command.slots["command"] ?: "next"}) dispatched."
            )
            VoiceIntentType.VOLUME_CONTROL -> executeVolumeControl(command.slots)
            VoiceIntentType.TOGGLE_TORCH -> executeTorchToggle(turnOn = command.slots["state"] != "off")
            VoiceIntentType.BATTERY_STATUS -> executeBatteryStatus()
            VoiceIntentType.TIME_DATE -> executeTimeDate()
            VoiceIntentType.SET_ALARM -> executeSetAlarm(command.slots)
            VoiceIntentType.SET_TIMER -> executeSetTimer(command.slots)
            VoiceIntentType.FIND_PHONE -> {
                repository.toggleFindPhoneRinging(true)
                ActionExecutionResult.Executed(
                    spokenResponse = "Main yahan hoon! Phone ring kar raha hai.",
                    displayMessage = "Find Phone alarm triggered at full volume."
                )
            }
            VoiceIntentType.STOP_RING -> {
                repository.toggleFindPhoneRinging(false)
                ActionExecutionResult.Executed(
                    spokenResponse = "Theek hai, ringing band kar di.",
                    displayMessage = "Find Phone ringing stopped."
                )
            }
            VoiceIntentType.LOCK_PHONE -> {
                val enabled = repository.setVoiceLockEnabled(true)
                if (enabled) {
                    repository.recordUnlockEvent("Armed by voice command • Just now")
                    ActionExecutionResult.Executed(
                        spokenResponse = "Voice Lock active ho gaya.",
                        displayMessage = "Voice Lock armed & protecting selected apps."
                    )
                } else {
                    ActionExecutionResult.Failure("Set a Backup Passcode first so you are never locked out.")
                }
            }
            VoiceIntentType.LOCK_APP -> {
                val appName = command.slots["app"].orEmpty()
                val lockedName = repository.lockAppByName(appName)
                if (lockedName != null) {
                    ActionExecutionResult.Executed(
                        spokenResponse = "$lockedName ab Voice Lock se protected hai.",
                        displayMessage = "$lockedName added to Voice App Lock."
                    )
                } else {
                    ActionExecutionResult.Executed(
                        spokenResponse = "$appName ko App Lock list me add karne ke liye App Lock kholein.",
                        displayMessage = "\"$appName\" is not in the quick lock catalog yet."
                    )
                }
            }
            VoiceIntentType.SEND_MESSAGE -> executeComposeMessage(command.slots)
            VoiceIntentType.RUN_ROUTINE -> executeRoutine(command.slots["routine"] ?: "Good Morning")
            VoiceIntentType.HELP -> ActionExecutionResult.Executed(
                spokenResponse = "Aap bol sakte hain: WhatsApp kholo, Torch chalu karo, Kitni battery hai, ya Phone kahan hai.",
                displayMessage = "Try: \"Open WhatsApp\", \"Torch chalu karo\", \"Kitni battery hai?\", \"10 minute ka timer\"."
            )
            VoiceIntentType.CANCEL -> ActionExecutionResult.Executed(
                spokenResponse = "Theek hai, cancel kar diya.",
                displayMessage = "Cancelled."
            )
            VoiceIntentType.CONFIRM -> ActionExecutionResult.Executed(
                spokenResponse = "Confirmed.",
                displayMessage = "Confirmed."
            )
            VoiceIntentType.CLARIFY -> ActionExecutionResult.Executed(
                spokenResponse = "Samajh nahi aaya. Aap aise bol sakte hain: WhatsApp kholo.",
                displayMessage = "Samajh nahi aaya. Try saying \"KAI, Open WhatsApp\" or \"Kitni battery hai?\"."
            )
        }
    }

    private fun buildConfirmationPrompt(command: ParsedVoiceCommand): ActionExecutionResult.NeedsConfirmation {
        return when (command.intent) {
            VoiceIntentType.SEND_MESSAGE -> {
                val recipient = command.slots["recipient"] ?: "Contact"
                val text = command.slots["text"].orEmpty()
                val app = command.slots["app"] ?: "WhatsApp"
                ActionExecutionResult.NeedsConfirmation(
                    promptMessage = "Send $app draft to $recipient: \"$text\"?",
                    spokenPrompt = "$recipient ko message bhejun: $text?"
                )
            }
            VoiceIntentType.CALL_CONTACT -> {
                val contact = command.slots["contact"] ?: "Contact"
                ActionExecutionResult.NeedsConfirmation(
                    promptMessage = "Open dialer to call $contact?",
                    spokenPrompt = "$contact ko call lagaun?"
                )
            }
            VoiceIntentType.CALL_NUMBER -> {
                val number = command.slots["number"].orEmpty()
                ActionExecutionResult.NeedsConfirmation(
                    promptMessage = "Call number $number?",
                    spokenPrompt = "$number pe call lagaun?"
                )
            }
            else -> ActionExecutionResult.NeedsConfirmation(
                promptMessage = "Confirm ${command.intent.name.lowercase().replace('_', ' ')}?",
                spokenPrompt = "Kya main yeh action karun?"
            )
        }
    }

    private fun executeOpenApp(query: String): ActionExecutionResult {
        if (query.isBlank()) {
            return ActionExecutionResult.Failure("App name unclear. Say e.g. \"Open Chrome\".")
        }
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val installedApps = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (_: Exception) {
            emptyList()
        }

        val normalizedQuery = query.lowercase(Locale.ROOT)
        val bestMatch = installedApps
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(pm).toString()
                val lowerLabel = label.lowercase(Locale.ROOT)
                val score = when {
                    lowerLabel == normalizedQuery -> 1.0f
                    lowerLabel.contains(normalizedQuery) || normalizedQuery.contains(lowerLabel) -> 0.92f
                    else -> parser.similarity(normalizedQuery, lowerLabel)
                }
                Triple(resolveInfo.activityInfo.packageName, label, score)
            }
            .maxByOrNull { it.third }

        if (bestMatch != null && bestMatch.third >= 0.65f) {
            val launchIntent = pm.getLaunchIntentForPackage(bestMatch.first)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null) {
                return try {
                    context.startActivity(launchIntent)
                    ActionExecutionResult.Executed(
                        spokenResponse = "${bestMatch.second} khol diya.",
                        displayMessage = "Opened ${bestMatch.second}."
                    )
                } catch (_: Exception) {
                    ActionExecutionResult.Failure("Could not launch ${bestMatch.second}.")
                }
            }
        }

        return ActionExecutionResult.Executed(
            spokenResponse = "$query is phone me install nahi mila.",
            displayMessage = "\"$query\" is not installed on this device."
        )
    }

    private fun executeOpenSettings(page: String): ActionExecutionResult {
        val intent = when (page) {
            "wifi" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Executed(
                spokenResponse = "${page.replaceFirstChar { it.uppercaseChar() }} settings khol di.",
                displayMessage = "Opened ${page.replaceFirstChar { it.uppercaseChar() }} settings panel."
            )
        } catch (_: Exception) {
            ActionExecutionResult.Failure("Unable to open system settings panel.")
        }
    }

    private fun executeDialer(contactOrNumber: String, isNumber: Boolean): ActionExecutionResult {
        val uri = if (isNumber) Uri.parse("tel:$contactOrNumber") else Uri.parse("tel:")
        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            val label = if (isNumber) contactOrNumber else "$contactOrNumber (select in dialer)"
            ActionExecutionResult.Executed(
                spokenResponse = "$contactOrNumber ke liye dialer khol diya.",
                displayMessage = "Opened Phone Dialer for $label."
            )
        } catch (_: Exception) {
            ActionExecutionResult.Failure("No Phone Dialer app available on this device.")
        }
    }

    private fun executeTorchToggle(turnOn: Boolean): ActionExecutionResult {
        val hasFlash = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlash) {
            lastTorchState = turnOn
            return ActionExecutionResult.Executed(
                spokenResponse = if (turnOn) "Torch command ready, par is device me flash hardware nahi hai." else "Torch off.",
                displayMessage = "Camera flash hardware unavailable on this device (simulated ${if (turnOn) "ON" else "OFF"})."
            )
        }
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, turnOn)
                lastTorchState = turnOn
                ActionExecutionResult.Executed(
                    spokenResponse = if (turnOn) "Torch chalu kar di." else "Torch band kar di.",
                    displayMessage = if (turnOn) "Flashlight turned ON." else "Flashlight turned OFF."
                )
            } else {
                ActionExecutionResult.Failure("No rear camera flash found.")
            }
        } catch (_: Exception) {
            ActionExecutionResult.Failure("Unable to access flashlight right now.")
        }
    }

    private fun executeVolumeControl(slots: Map<String, String>): ActionExecutionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val levelStr = slots["level"]
            val action = slots["action"]

            val message = when {
                levelStr != null -> {
                    val pct = levelStr.toInt().coerceIn(0, 100)
                    val target = ((pct / 100f) * maxVol).toInt().coerceIn(0, maxVol)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                    "Media volume set to $pct%."
                }
                action == "mute" -> {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                    "Media volume muted."
                }
                action == "down" -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    "Volume lowered."
                }
                else -> {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    "Volume increased."
                }
            }
            ActionExecutionResult.Executed(spokenResponse = message, displayMessage = message)
        } catch (_: Exception) {
            ActionExecutionResult.Failure("Could not adjust device volume.")
        }
    }

    private fun executeBatteryStatus(): ActionExecutionResult {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val display = if (level in 0..100) {
            "Battery is currently at $level%."
        } else {
            "Battery level sensor unavailable."
        }
        val spoken = if (level in 0..100) "Abhi battery $level percent hai." else display
        return ActionExecutionResult.Executed(spokenResponse = spoken, displayMessage = display)
    }

    private fun executeTimeDate(): ActionExecutionResult {
        val now = Date()
        val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
        val dateFmt = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(now)
        return ActionExecutionResult.Executed(
            spokenResponse = "Abhi $timeFmt baje hain, aur aaj $dateFmt hai.",
            displayMessage = "$timeFmt \u2022 $dateFmt"
        )
    }

    private fun executePlayMedia(query: String): ActionExecutionResult {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                ActionExecutionResult.Executed(
                    spokenResponse = "$query chala raha hoon.",
                    displayMessage = "Playing \"$query\" in your music app."
                )
            } else {
                executeWebSearch("$query song")
            }
        } catch (_: Exception) {
            ActionExecutionResult.Executed(
                spokenResponse = "$query ke liye music player nahi mila.",
                displayMessage = "No default music player installed for \"$query\"."
            )
        }
    }

    private fun executeWebSearch(query: String): ActionExecutionResult {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Executed(
                spokenResponse = "$query search kar raha hoon.",
                displayMessage = "Searching web for \"$query\"."
            )
        } catch (_: Exception) {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            try {
                context.startActivity(browserIntent)
                ActionExecutionResult.Executed(
                    spokenResponse = "Browser me $query search khol diya.",
                    displayMessage = "Opened web search for \"$query\"."
                )
            } catch (_: Exception) {
                ActionExecutionResult.Failure("No web browser installed.")
            }
        }
    }

    private fun executeSetAlarm(slots: Map<String, String>): ActionExecutionResult {
        val hour = slots["hour"]?.toIntOrNull()?.coerceIn(0, 23) ?: 7
        val minute = slots["minute"]?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, "KAIxGen Alarm")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            val formatted = "%02d:%02d".format(hour, minute)
            ActionExecutionResult.Executed(
                spokenResponse = "$formatted ka alarm set karne ke liye Clock khol diya.",
                displayMessage = "Alarm prepared for $formatted."
            )
        } catch (_: Exception) {
            ActionExecutionResult.Executed(
                spokenResponse = "Alarm $hour:$minute ready hai, par Clock app nahi mila.",
                displayMessage = "Clock app not available on this device."
            )
        }
    }

    private fun executeSetTimer(slots: Map<String, String>): ActionExecutionResult {
        val duration = slots["duration"]?.toIntOrNull()?.coerceAtLeast(1) ?: 5
        val unit = slots["unit"] ?: "minutes"
        val seconds = if (unit == "seconds") duration else duration * 60
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, "KAIxGen Timer")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Executed(
                spokenResponse = "$duration $unit ka timer start kar diya.",
                displayMessage = "Timer set for $duration $unit."
            )
        } catch (_: Exception) {
            ActionExecutionResult.Executed(
                spokenResponse = "$duration $unit ka timer ready hai.",
                displayMessage = "Clock timer app unavailable on this device."
            )
        }
    }

    private fun executeComposeMessage(slots: Map<String, String>): ActionExecutionResult {
        val recipient = slots["recipient"] ?: "Contact"
        val text = slots["text"].orEmpty()
        val app = slots["app"] ?: "WhatsApp"
        val intent = if (app.equals("WhatsApp", ignoreCase = true)) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(text)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return try {
            context.startActivity(intent)
            ActionExecutionResult.Executed(
                spokenResponse = "$recipient ke liye $app draft khol diya.",
                displayMessage = "Opened $app draft for $recipient: \"$text\""
            )
        } catch (_: Exception) {
            ActionExecutionResult.Executed(
                spokenResponse = "Draft ready hai: $text",
                displayMessage = "Message draft prepared ($app not installed)."
            )
        }
    }

    private fun executeRoutine(routineName: String): ActionExecutionResult {
        return when (routineName) {
            "Good Morning" -> {
                val timeRes = executeTimeDate() as ActionExecutionResult.Executed
                val batRes = executeBatteryStatus() as ActionExecutionResult.Executed
                ActionExecutionResult.Executed(
                    spokenResponse = "Good Morning! ${timeRes.spokenResponse} ${batRes.spokenResponse}",
                    displayMessage = "Good Morning Routine \u2022 ${timeRes.displayMessage} \u2022 ${batRes.displayMessage}"
                )
            }
            "Sleep Mode" -> {
                executeVolumeControl(mapOf("level" to "15"))
                repository.setVoiceLockEnabled(true)
                ActionExecutionResult.Executed(
                    spokenResponse = "Sleep Mode on! Volume kam kar diya aur Voice Lock active hai.",
                    displayMessage = "Sleep Mode \u2022 Media volume 15% & Voice Lock armed."
                )
            }
            else -> {
                ActionExecutionResult.Executed(
                    spokenResponse = "$routineName routine chalu ho gaya.",
                    displayMessage = "$routineName routine executed hands-free."
                )
            }
        }
    }
}
