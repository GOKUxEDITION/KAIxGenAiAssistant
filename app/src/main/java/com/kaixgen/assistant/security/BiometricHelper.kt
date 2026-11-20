package com.kaixgen.assistant.security

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.kaixgen.assistant.domain.model.SecurityMode

/**
 * Hardware & enrollment readiness state for system-level biometric authentication
 * (Fingerprint, Face, or Iris sensors managed by Android BiometricManager).
 */
enum class BiometricAvailability(val userMessage: String) {
    AVAILABLE("Fingerprint or facial recognition is ready."),
    NONE_ENROLLED("No fingerprint or face enrolled in Android Settings yet."),
    NO_HARDWARE("This device does not have biometric hardware."),
    HARDWARE_UNAVAILABLE("Biometric sensor is temporarily unavailable."),
    SECURITY_UPDATE_REQUIRED("A system security update is required to use biometrics."),
    UNSUPPORTED("Biometric authentication is not supported on this Android version.")
}

/**
 * Type of credential that succeeded during a BiometricPrompt challenge.
 */
enum class BiometricAuthType(val displayLabel: String) {
    BIOMETRIC("Fingerprint / Face"),
    DEVICE_CREDENTIAL("Device Screen Lock"),
    UNKNOWN("System Biometric")
}

/**
 * Sealed result hierarchy emitted by [BiometricHelper] during authentication.
 */
sealed interface BiometricAuthResult {
    data class Success(val authType: BiometricAuthType) : BiometricAuthResult
    data object UseBackupPinRequested : BiometricAuthResult
    data object Cancelled : BiometricAuthResult
    data object FailedAttempt : BiometricAuthResult
    data class Lockout(val isPermanent: Boolean, val message: String) : BiometricAuthResult
    data class NotAvailable(val availability: BiometricAvailability, val message: String) : BiometricAuthResult
    data class Error(val errorCode: Int, val message: String) : BiometricAuthResult
}

/**
 * Configuration options for presenting a system biometric prompt.
 */
data class BiometricPromptConfig(
    val title: String = "Unlock KAI Voice Lock",
    val subtitle: String = "Use your fingerprint or face as a fallback to voice unlock",
    val description: String? = "On-device verification handled securely by Android.",
    val negativeButtonText: String = "Use Backup PIN",
    val requireStrongBiometric: Boolean = false,
    val allowDeviceCredentialFallback: Boolean = false,
    val confirmationRequired: Boolean = false
)

/**
 * Handle allowing callers (e.g., Compose DisposableEffect) to cancel an active BiometricPrompt.
 */
fun interface BiometricCancellationHandle {
    fun cancel()
}

/**
 * System-level Fingerprint & Facial Recognition authentication helper for KAIxGen Assistant.
 *
 * Security & Compliance alignment (PRD 06 §2, §3.1, §3.3):
 * - Uses AndroidX [BiometricPrompt] backed by hardware TEE/StrongBox without storing any raw biometric data.
 * - Adapts allowed authenticators cleanly across API 24–36 (avoiding illegal API 28–29 combinations).
 * - Supports Class 3 (`BIOMETRIC_STRONG`) enforcement in [SecurityMode.STRICT] and Class 2/3
 *   (`BIOMETRIC_STRONG or BIOMETRIC_WEAK` including facial recognition) in Balanced/Convenience modes.
 * - Supports Device Credential fallback (`DEVICE_CREDENTIAL`) for Backup PIN recovery flows.
 */
class BiometricHelper(
    private val appContext: Context
) {
    private val biometricManager: BiometricManager by lazy {
        BiometricManager.from(appContext)
    }

    /**
     * Checks whether system biometric authentication is available for the requested policy.
     */
    fun checkAvailability(
        requireStrongBiometric: Boolean = false,
        allowDeviceCredentialFallback: Boolean = false
    ): BiometricAvailability {
        val authenticators = resolveAllowedAuthenticators(
            sdkInt = Build.VERSION.SDK_INT,
            requireStrongBiometric = requireStrongBiometric,
            allowDeviceCredentialFallback = allowDeviceCredentialFallback
        )
        return mapCanAuthenticateResult(biometricManager.canAuthenticate(authenticators))
    }

    /**
     * Returns true if fingerprint or face recognition can be triggered right now.
     */
    fun canAuthenticate(
        requireStrongBiometric: Boolean = false,
        allowDeviceCredentialFallback: Boolean = false
    ): Boolean {
        return checkAvailability(
            requireStrongBiometric = requireStrongBiometric,
            allowDeviceCredentialFallback = allowDeviceCredentialFallback
        ) == BiometricAvailability.AVAILABLE
    }

    /**
     * Builds a [BiometricPromptConfig] tuned for the user's current Voice Lock [SecurityMode].
     * - STRICT: Enforces Class 3 Strong biometrics + explicit confirmation.
     * - BALANCED / CONVENIENCE: Allows fingerprint or Class 2/3 face recognition without extra tap.
     */
    fun configForSecurityMode(mode: SecurityMode): BiometricPromptConfig {
        val isStrict = mode == SecurityMode.STRICT
        return BiometricPromptConfig(
            title = "Unlock KAI Voice Lock",
            subtitle = if (isStrict) {
                "Strict Mode: Verify with strong fingerprint or 3D face"
            } else {
                "Verify with fingerprint or face recognition"
            },
            description = "On-device verification — biometric data never leaves your phone.",
            negativeButtonText = "Use Backup PIN",
            requireStrongBiometric = isStrict,
            allowDeviceCredentialFallback = false,
            confirmationRequired = isStrict
        )
    }

    /**
     * Launches the Android system biometric dialog (fingerprint / face) attached to [activity].
     *
     * @return a [BiometricCancellationHandle] that callers can invoke on lifecycle disposal,
     *         or `null` if biometrics were unavailable and [onResult] was already called with [BiometricAuthResult.NotAvailable].
     */
    fun authenticate(
        activity: FragmentActivity,
        config: BiometricPromptConfig = BiometricPromptConfig(),
        onResult: (BiometricAuthResult) -> Unit
    ): BiometricCancellationHandle? {
        val availability = checkAvailability(
            requireStrongBiometric = config.requireStrongBiometric,
            allowDeviceCredentialFallback = config.allowDeviceCredentialFallback
        )
        if (availability != BiometricAvailability.AVAILABLE) {
            onResult(BiometricAuthResult.NotAvailable(availability, availability.userMessage))
            return null
        }

        val authenticators = resolveAllowedAuthenticators(
            sdkInt = Build.VERSION.SDK_INT,
            requireStrongBiometric = config.requireStrongBiometric,
            allowDeviceCredentialFallback = config.allowDeviceCredentialFallback
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(config.confirmationRequired)

        if (!config.description.isNullOrBlank()) {
            promptInfoBuilder.setDescription(config.description)
        }

        // AndroidX BiometricPrompt forbids setting negativeButtonText when DEVICE_CREDENTIAL is enabled.
        val includesDeviceCredential = (authenticators and Authenticators.DEVICE_CREDENTIAL) != 0
        if (!includesDeviceCredential) {
            promptInfoBuilder.setNegativeButtonText(config.negativeButtonText)
        }

        val promptInfo = promptInfoBuilder.build()
        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(
                        BiometricAuthResult.Success(
                            authType = mapAuthenticationType(result.authenticationType)
                        )
                    )
                }

                override fun onAuthenticationFailed() {
                    onResult(BiometricAuthResult.FailedAttempt)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(mapErrorCodeToResult(errorCode, errString.toString()))
                }
            }
        )

        prompt.authenticate(promptInfo)
        return BiometricCancellationHandle { prompt.cancelAuthentication() }
    }

    /**
     * Convenience overload for Jetpack Compose screens that hold a [Context].
     * Unwraps [ContextWrapper] chains safely to locate the hosting [FragmentActivity].
     */
    fun authenticateFromContext(
        context: Context,
        config: BiometricPromptConfig = BiometricPromptConfig(),
        onResult: (BiometricAuthResult) -> Unit
    ): BiometricCancellationHandle? {
        val fragmentActivity = context.findFragmentActivity()
        if (fragmentActivity == null) {
            onResult(
                BiometricAuthResult.Error(
                    errorCode = BiometricPrompt.ERROR_HW_UNAVAILABLE,
                    message = "Biometric prompt requires an active screen context. Please use Backup PIN."
                )
            )
            return null
        }
        return authenticate(fragmentActivity, config, onResult)
    }

    /**
     * Creates a Play-compliant system Settings intent to guide users to enroll a fingerprint or face
     * when [checkAvailability] returns [BiometricAvailability.NONE_ENROLLED].
     */
    fun createEnrollmentIntent(requireStrongBiometric: Boolean = false): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                val authenticators = if (requireStrongBiometric) {
                    Authenticators.BIOMETRIC_STRONG
                } else {
                    Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
                }
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, authenticators)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }

    companion object {
        /**
         * Resolves the legally supported [BiometricManager.Authenticators] bitmask for a given Android API level.
         *
         * Key Android compatibility rules:
         * - API 30+: `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` and `BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL`
         *   are both fully supported.
         * - API 28–29: Combining `BIOMETRIC_STRONG` with `DEVICE_CREDENTIAL` throws `IllegalArgumentException`;
         *   use `BIOMETRIC_WEAK | DEVICE_CREDENTIAL` instead when device credential fallback is requested.
         * - API 24–27: Uses `BIOMETRIC_WEAK` (or `BIOMETRIC_WEAK | DEVICE_CREDENTIAL`) for broad device support.
         */
        internal fun resolveAllowedAuthenticators(
            sdkInt: Int,
            requireStrongBiometric: Boolean,
            allowDeviceCredentialFallback: Boolean
        ): Int {
            return when {
                allowDeviceCredentialFallback -> {
                    if (sdkInt >= Build.VERSION_CODES.R) {
                        if (requireStrongBiometric) {
                            Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL
                        } else {
                            Authenticators.BIOMETRIC_STRONG or
                                Authenticators.BIOMETRIC_WEAK or
                                Authenticators.DEVICE_CREDENTIAL
                        }
                    } else {
                        Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL
                    }
                }
                requireStrongBiometric && sdkInt >= Build.VERSION_CODES.R -> {
                    Authenticators.BIOMETRIC_STRONG
                }
                else -> {
                    Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
                }
            }
        }

        internal fun mapCanAuthenticateResult(statusCode: Int): BiometricAvailability {
            return when (statusCode) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
                else -> BiometricAvailability.UNSUPPORTED
            }
        }

        internal fun mapAuthenticationType(authenticationType: Int): BiometricAuthType {
            return when (authenticationType) {
                BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC -> BiometricAuthType.BIOMETRIC
                BiometricPrompt.AUTHENTICATION_RESULT_TYPE_DEVICE_CREDENTIAL -> BiometricAuthType.DEVICE_CREDENTIAL
                else -> BiometricAuthType.UNKNOWN
            }
        }

        internal fun mapErrorCodeToResult(errorCode: Int, rawMessage: String): BiometricAuthResult {
            val cleanMessage = rawMessage.ifBlank { "Biometric verification did not complete." }
            return when (errorCode) {
                BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricAuthResult.UseBackupPinRequested
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_CANCELED -> BiometricAuthResult.Cancelled
                BiometricPrompt.ERROR_LOCKOUT -> BiometricAuthResult.Lockout(
                    isPermanent = false,
                    message = "Too many biometric attempts. Use your Backup PIN or wait 30 seconds."
                )
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricAuthResult.Lockout(
                    isPermanent = true,
                    message = "Biometrics temporarily locked by Android. Unlock with your Backup PIN."
                )
                BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricAuthResult.NotAvailable(
                    availability = BiometricAvailability.NONE_ENROLLED,
                    message = BiometricAvailability.NONE_ENROLLED.userMessage
                )
                BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometricAuthResult.NotAvailable(
                    availability = BiometricAvailability.NO_HARDWARE,
                    message = BiometricAvailability.NO_HARDWARE.userMessage
                )
                BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometricAuthResult.NotAvailable(
                    availability = BiometricAvailability.HARDWARE_UNAVAILABLE,
                    message = BiometricAvailability.HARDWARE_UNAVAILABLE.userMessage
                )
                else -> BiometricAuthResult.Error(errorCode = errorCode, message = cleanMessage)
            }
        }
    }
}

/**
 * Safely unwraps [ContextWrapper] chains (e.g., Compose LocalContext) to obtain the host [FragmentActivity].
 */
tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
