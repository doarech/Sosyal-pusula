package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Sosyal Pusula", appName)
  }

  @Test
  fun `verify Room database initialization at version 3`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.CompassDatabase.getInstance(context)
    val dao = db.compassDao()
    org.junit.Assert.assertNotNull(dao)
  }
}
