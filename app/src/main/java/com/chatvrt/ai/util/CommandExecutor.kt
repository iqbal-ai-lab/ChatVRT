package com.chatvrt.ai.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import java.util.Locale

class CommandExecutor(private val ctx: Context) {
  private val tts = TextToSpeech(ctx) { it -> if (it==TextToSpeech.SUCCESS) tts.language = Locale("id","ID") }

  fun execute(text: String) {
    val q = text.lowercase()
    when {
      q.startsWith("buka youtube") || q.startsWith("putar") -> openYouTube(extractAfter(text, listOf("buka youtube", "putar")))
      q.startsWith("buka chrome") || q.startsWith("cari") || q.startsWith("search") -> searchChrome(extractAfter(text, listOf("buka chrome", "cari", "search")))
      q.contains("hidupkan senter") || q.contains("nyalakan senter") -> toggleFlash(true)
      q.contains("matikan senter") -> toggleFlash(false)
      q.startsWith("buka ") -> openAppByName(text.removePrefix("buka ").trim())
      q.startsWith("telepon whatsapp") || q.startsWith("call whatsapp") -> callWhatsApp(extractAfter(text, listOf("telepon whatsapp","call whatsapp")))
      q.startsWith("chat whatsapp") || q.startsWith("kirim whatsapp" ) -> chatWhatsApp(extractAfter(text, listOf("chat whatsapp","kirim whatsapp")))
      else -> speak("Maaf, saya belum memahami perintah itu.")
    }
  }

  private fun extractAfter(text:String, keys: List<String>): String {
    val l = text.lowercase()
    keys.forEach { k -> if (l.startsWith(k)) return text.substring(k.length).trim() }
    return text
  }

  private fun speak(s: String){ tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "chatvrt") }

  private fun openYouTube(title: String){
    val app = "com.google.android.youtube"
    val uri = Uri.parse("vnd.youtube://results?search_query=" + Uri.encode(title))
    val web = Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(title))
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    catch (_:Exception){ ctx.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    speak("Membuka YouTube untuk $title")
  }

  private fun searchChrome(query: String){
    val url = "https://www.google.com/search?q=" + Uri.encode(query)
    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { setPackage("com.android.chrome") ; addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    try { ctx.startActivity(i) } catch (_:Exception){ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    speak("Mencari $query di Chrome")
  }

  private fun openAppByName(name: String){
    val pm = ctx.packageManager
    val apps = pm.getInstalledApplications(PackageManager.MATCH_ALL)
    val hit = apps.firstOrNull { it.loadLabel(pm).toString().equals(name, ignoreCase = true) }
    if (hit!=null){
      val la = pm.getLaunchIntentForPackage(hit.packageName)
      if (la!=null){ la.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); ctx.startActivity(la); speak("Membuka ${name}") ; return }
    }
    speak("Aplikasi ${name} tidak ditemukan")
  }

  private fun toggleFlash(on:Boolean){
    val cm = ctx.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val camId = cm.cameraIdList.firstOrNull { id -> cm.getCameraCharacteristics(id).get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)==true }
    if (camId!=null){ cm.setTorchMode(camId, on); speak(if(on)"Senter dinyalakan" else "Senter dimatikan") } else speak("Perangkat tidak mendukung senter")
  }

  private fun lookupPhoneByName(name:String): String? {
    val uri = ContactsContract.Contacts.CONTENT_URI
    val c = ctx.contentResolver.query(uri, arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER),
      "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?", arrayOf("%$name%"), null)
    c?.use {
      while (it.moveToNext()){
        val has = it.getInt(2) > 0
        if (has){
          val id = it.getString(0)
          val pCur = ctx.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
          pCur?.use { pc -> if (pc.moveToFirst()) return pc.getString(0).replace(" ", "") }
        }
      }
    }
    return null
  }

  private fun callWhatsApp(name: String){
    val phone = lookupPhoneByName(name)
    if (phone!=null){
      val uri = Uri.parse("https://wa.me/"+phone)
      val i = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      ctx.startActivity(i); speak("Membuka WhatsApp ke $name untuk panggilan atau chat")
    } else speak("Kontak $name tidak ditemukan")
  }

  private fun chatWhatsApp(nameOrMessage: String){
    // pattern: "chat whatsapp ke [nama] [pesan]"
    val parts = nameOrMessage.split(" ke ")
    val name = if (parts.size>1) parts[1] else nameOrMessage
    val phone = lookupPhoneByName(name)
    val msg = if (parts.size>1) parts[0] else ""
    if (phone!=null){
      val uri = Uri.parse("https://wa.me/"+phone+"?text="+Uri.encode(msg))
      val i = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      ctx.startActivity(i); speak("Menyiapkan chat WhatsApp ke $name")
    } else speak("Kontak $name tidak ditemukan")
  }
}