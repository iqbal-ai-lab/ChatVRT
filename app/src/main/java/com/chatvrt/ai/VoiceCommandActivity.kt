package com.chatvrt.ai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.chatvrt.ai.util.CommandExecutor

class VoiceCommandActivity : AppCompatActivity() {
  private var speech: SpeechRecognizer? = null
  private lateinit var wave: ProgressBar
  private lateinit var tvHeard: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_voice_command)
    wave = findViewById(R.id.wave2)
    tvHeard = findViewById(R.id.tvHeard)

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
      startRecognizer()
    } else finish()
  }

  private fun startRecognizer() {
    speech = SpeechRecognizer.createSpeechRecognizer(this)
    val intent = RecognizerIntent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }
    speech?.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(p0: Bundle?) {}
      override fun onBeginningOfSpeech() {}
      override fun onRmsChanged(rms: Float) { wave.progress = ((rms + 2) * 10).toInt().coerceIn(0, 100) }
      override fun onBufferReceived(p0: ByteArray?) {}
      override fun onEndOfSpeech() {}
      override fun onError(e: Int) { finish() }
      override fun onResults(res: Bundle) { handle((res.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?: arrayListOf(""))[0]) }
      override fun onPartialResults(res: Bundle) { val t = (res.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?: arrayListOf(""))[0]; tvHeard.text = t }
      override fun onEvent(p0: Int, p1: Bundle?) {}
    })
    speech?.startListening(intent)
  }

  private fun handle(text: String) {
    tvHeard.text = text
    CommandExecutor(this).execute(text)
    finish()
  }

  override fun onDestroy() { super.onDestroy(); speech?.destroy() }
}