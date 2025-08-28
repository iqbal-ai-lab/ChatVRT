package com.chatvrt.ai.hotword

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.chatvrt.ai.VoiceCommandActivity
import com.chatvrt.ai.overlay.OverlayAssistantService

class HotwordForegroundService : Service() {
  private var speech: SpeechRecognizer? = null
  private val channelId = "chatvrt_hotword"

  override fun onCreate() { super.onCreate(); startForeground(1, buildNotification()) ; startListening() }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { return START_STICKY }
  override fun onBind(p0: Intent?): IBinder? = null

  private fun buildNotification(): Notification {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      if (nm.getNotificationChannel(channelId) == null) nm.createNotificationChannel(NotificationChannel(channelId, "ChatVRT Hotword", NotificationManager.IMPORTANCE_MIN))
      return Notification.Builder(this, channelId).setContentTitle("ChatVRT aktif").setContentText("Ucapkan: 'Aktifkan AI'").setSmallIcon(android.R.drawable.ic_btn_speak_now).build()
    } else {
      return Notification()
    }
  }

  private fun startListening() {
    speech?.destroy()
    speech = SpeechRecognizer.createSpeechRecognizer(this)
    val intent = RecognizerIntent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    speech?.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(p0: android.os.Bundle?) {}
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(rms: Float) {}
      override fun onBufferReceived(p0: ByteArray?) {}
      override fun onEndOfSpeech() {}
      override fun onError(e: Int) { restart() }
      override fun onResults(res: android.os.Bundle) { checkHotword(res) ; restart() }
      override fun onPartialResults(res: android.os.Bundle) { checkHotword(res) }
      override fun onEvent(p0: Int, p1: android.os.Bundle?) {}
    })
    speech?.startListening(intent)
  }

  private fun checkHotword(bundle: android.os.Bundle) {
    val text = (bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf(""))[0].lowercase()
    if (text.contains("aktifkan ai")) {
      // show overlay and open command listener
      startService(Intent(this, OverlayAssistantService::class.java))
      val i = Intent(this, VoiceCommandActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(i)
    }
  }

  private fun restart() { speech?.destroy(); startListening() }

  override fun onDestroy() { super.onDestroy(); speech?.destroy() }
}