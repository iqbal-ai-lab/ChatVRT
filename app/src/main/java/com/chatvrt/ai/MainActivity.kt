package com.chatvrt.ai

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chatvrt.ai.databinding.ActivityMainBinding
import com.chatvrt.ai.hotword.HotwordForegroundService

class MainActivity : AppCompatActivity() {
  private lateinit var b: ActivityMainBinding

  private val reqPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
    startHotword()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    b = ActivityMainBinding.inflate(layoutInflater)
    setContentView(b.root)

    b.btnPlus.setOnClickListener { /* no-op */ }

    ensureOverlayPermission()

    reqPermissions.launch(arrayOf(
      Manifest.permission.RECORD_AUDIO,
      Manifest.permission.CAMERA,
      Manifest.permission.POST_NOTIFICATIONS
    ))
  }

  private fun ensureOverlayPermission() {
    if (!Settings.canDrawOverlays(this)) {
      val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
      startActivity(intent)
    }
  }

  private fun startHotword() {
    val intent = Intent(this, HotwordForegroundService::class.java)
    if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
  }
}