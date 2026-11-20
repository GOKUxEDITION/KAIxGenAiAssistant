package com.kaixgen.assistant.security

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricHelperTest {

    @Test
    fun `resolveAllowedAuthenticators enforces BIOMETRIC_STRONG on API 30+ in Strict mode`() {
        val flags = BiometricHelper.resolveAllowedAuthenticators(
            sdkInt = Build.VERSION_CODES.R,
            requireStrongBiometric = true,
            allowDeviceCredentialFallback = false
        )
        assertEquals(Authenticators.BIOMETRIC_STRONG, flags)
    }

    @Test
    fun `resolveAllowedAuthenticators allows fingerprint and face in Balanced mode`() {
        val flags = BiometricHelper.resolveAllowedAuthenticators(
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
            requireStrongBiometric = false,
            allowDeviceCredentialFallback = false
        )
        assertEquals(
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK,
            flags
        )
    }

    @Test
    fun `resolveAllowedAuthenticators avoids illegal STRONG plus DEVICE_CREDENTIAL combo on API 29`() {
        val flags = BiometricHelper.resolveAllowedAuthenticators(
            sdkInt = Build.VERSION_CODES.Q,
            requireStrongBiometric = true,
            allowDeviceCredentialFallback = true
        )
        assertEquals(
            Authenticators.BIOMETRIC_WEAK or Authenticators.DEVICE_CREDENTIAL,
            flags
        )
        assertFalse(
            flags == (Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL)
        )
    }

    @Test
    fun `resolveAllowedAuthenticators permits STRONG plus DEVICE_CREDENTIAL on API 30+`() {
        val flags = BiometricHelper.resolveAllowedAuthenticators(
            sdkInt = Build.VERSION_CODES.R,
            requireStrongBiometric = true,
            allowDeviceCredentialFallback = true
        )
        assertEquals(
            Authenticators.BIOMETRIC_STRONG or Authenticators.DEVICE_CREDENTIAL,
            flags
        )
    }

    @Test
    fun `mapCanAuthenticateResult maps all BiometricManager codes accurately`() {
        assertEquals(
            BiometricAvailability.AVAILABLE,
            BiometricHelper.mapCanAuthenticateResult(BiometricManager.BIOMETRIC_SUCCESS)
        )
        assertEquals(
            BiometricAvailability.NONE_ENROLLED,
            BiometricHelper.mapCanAuthenticateResult(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        )
        assertEquals(
            BiometricAvailability.NO_HARDWARE,
            BiometricHelper.mapCanAuthenticateResult(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)
        )
        assertEquals(
            BiometricAvailability.HARDWARE_UNAVAILABLE,
            BiometricHelper.mapCanAuthenticateResult(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
        )
        assertEquals(
            BiometricAvailability.SECURITY_UPDATE_REQUIRED,
            BiometricHelper.mapCanAuthenticateResult(BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED)
        )
    }

    @Test
    fun `mapErrorCodeToResult routes negative button to Backup PIN and lockouts cleanly`() {
        assertEquals(
            BiometricAuthResult.UseBackupPinRequested,
            BiometricHelper.mapErrorCodeToResult(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "Use Backup PIN")
        )
        assertEquals(
            BiometricAuthResult.Cancelled,
            BiometricHelper.mapErrorCodeToResult(BiometricPrompt.ERROR_USER_CANCELED, "Cancelled")
        )

        val tempLockout = BiometricHelper.mapErrorCodeToResult(BiometricPrompt.ERROR_LOCKOUT, "Locked")
        assertTrue(tempLockout is BiometricAuthResult.Lockout)
        assertFalse((tempLockout as BiometricAuthResult.Lockout).isPermanent)

        val permLockout = BiometricHelper.mapErrorCodeToResult(BiometricPrompt.ERROR_LOCKOUT_PERMANENT, "Locked")
        assertTrue(permLockout is BiometricAuthResult.Lockout)
        assertTrue((permLockout as BiometricAuthResult.Lockout).isPermanent)
    }
}
