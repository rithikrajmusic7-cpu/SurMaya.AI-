package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ApiCredentialManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SurMaya AI", appName)
  }

  @Test
  fun `verify ApiCredentialManager secure key persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = ApiCredentialManager.getInstance(context)

    // Initially keys should be empty
    manager.geminiApiKey = ""
    manager.sunoApiKey = ""
    manager.musicGenApiKey = ""
    manager.apiEndpoint = ""

    assertEquals("", manager.geminiApiKey)
    assertEquals("", manager.sunoApiKey)
    assertEquals("", manager.musicGenApiKey)
    assertEquals("", manager.apiEndpoint)

    // Save credentials
    manager.geminiApiKey = "test_gemini_key_123"
    manager.sunoApiKey = "test_suno_key_456"
    manager.musicGenApiKey = "test_musicgen_key_789"
    manager.apiEndpoint = "https://custom.api.endpoint/v1"

    // Retrieve and verify
    assertEquals("test_gemini_key_123", manager.geminiApiKey)
    assertEquals("test_suno_key_456", manager.sunoApiKey)
    assertEquals("test_musicgen_key_789", manager.musicGenApiKey)
    assertEquals("https://custom.api.endpoint/v1", manager.apiEndpoint)

    // Clear credentials
    manager.geminiApiKey = ""
    manager.sunoApiKey = ""
    manager.musicGenApiKey = ""
    manager.apiEndpoint = ""

    assertEquals("", manager.geminiApiKey)
    assertEquals("", manager.sunoApiKey)
    assertEquals("", manager.musicGenApiKey)
    assertEquals("", manager.apiEndpoint)
  }

  @Test
  fun `verify Developer mode state via SharedPreferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("surmaya_dev_prefs", Context.MODE_PRIVATE)

    // Initially disabled
    prefs.edit().putBoolean("dev_mode_enabled", false).apply()
    var isEnabled = prefs.getBoolean("dev_mode_enabled", false)
    assertEquals(false, isEnabled)

    // Enable dev mode
    prefs.edit()
      .putBoolean("dev_mode_enabled", true)
      .putLong("dev_mode_activation_time", System.currentTimeMillis())
      .apply()

    isEnabled = prefs.getBoolean("dev_mode_enabled", false)
    assertEquals(true, isEnabled)
  }
}
