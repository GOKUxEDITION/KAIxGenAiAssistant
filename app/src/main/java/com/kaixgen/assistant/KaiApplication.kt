package com.kaixgen.assistant

import android.app.Application
import com.kaixgen.assistant.di.KaiAppContainer
import com.kaixgen.assistant.domain.ai.GeminiHybridAssistant
import com.kaixgen.assistant.domain.audio.AcousticDspEngine
import com.kaixgen.assistant.domain.hardware.FindPhoneAlarmEngine
import com.kaixgen.assistant.domain.routine.DynamicRoutineEngine
import com.kaixgen.assistant.security.IntruderSelfieCapture

class KaiApplication : Application() {

    lateinit var appContainer: KaiAppContainer
        private set

    lateinit var acousticDspEngine: AcousticDspEngine
        private set

    lateinit var findPhoneAlarmEngine: FindPhoneAlarmEngine
        private set

    lateinit var geminiHybridAssistant: GeminiHybridAssistant
        private set

    lateinit var dynamicRoutineEngine: DynamicRoutineEngine
        private set

    lateinit var intruderSelfieCapture: IntruderSelfieCapture
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = KaiAppContainer(applicationContext)
        acousticDspEngine = AcousticDspEngine()
        findPhoneAlarmEngine = FindPhoneAlarmEngine(applicationContext)
        geminiHybridAssistant = GeminiHybridAssistant(
            apiKeyProvider = { BuildConfig.GEMINI_API_KEY },
            modelName = BuildConfig.GEMINI_MODEL
        )
        dynamicRoutineEngine = DynamicRoutineEngine(
            actionExecutor = appContainer.actionExecutor,
            parser = appContainer.parser,
            ttsManager = appContainer.ttsManager
        )
        intruderSelfieCapture = IntruderSelfieCapture(applicationContext)
    }

    override fun onTerminate() {
        findPhoneAlarmEngine.stopAlarm()
        appContainer.release()
        super.onTerminate()
    }
}
