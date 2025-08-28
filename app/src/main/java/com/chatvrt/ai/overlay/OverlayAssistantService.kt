package com.chatvrt.ai.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ProgressBar
import android.app.Service
import com.chatvrt.ai.R

class OverlayAssistantService: Service() {
  private var wm: WindowManager? = null
  private var view: android.view.View? = null
  private val handler = Handler()
  private val runnable = object: Runnable {
    override fun run() {
      val pb = view?.findViewById<ProgressBar>(R.id.wave)
      pb?.progress = ((System.currentTimeMillis()/50)%100).toInt()
      handler.postDelayed(this, 50)
    }
  }

  override fun onBind(intent: android.content.Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val lp = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
      PixelFormat.TRANSLUCENT
    )
    lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    lp.y = 120
    view = LayoutInflater.from(this).inflate(R.layout.overlay_assistant, null)
    wm?.addView(view, lp)
    handler.post(runnable)
  }

  override fun onDestroy() { super.onDestroy(); handler.removeCallbacks(runnable); if (view!=null) wm?.removeView(view) }
}