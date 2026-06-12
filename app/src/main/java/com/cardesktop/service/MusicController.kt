package com.cardesktop.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MusicController {

    private var audioManager: AudioManager? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun init(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun play() {
        audioManager?.let { am ->
            dispatchMediaKeyEvent(am, KeyEvent.KEYCODE_MEDIA_PLAY)
        }
    }

    fun pause() {
        audioManager?.let { am ->
            dispatchMediaKeyEvent(am, KeyEvent.KEYCODE_MEDIA_PAUSE)
        }
    }

    fun playPause() {
        audioManager?.let { am ->
            dispatchMediaKeyEvent(am, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        }
    }

    fun next() {
        audioManager?.let { am ->
            dispatchMediaKeyEvent(am, KeyEvent.KEYCODE_MEDIA_NEXT)
        }
    }

    fun previous() {
        audioManager?.let { am ->
            dispatchMediaKeyEvent(am, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    private fun dispatchMediaKeyEvent(audioManager: AudioManager, keyCode: Int) {
        try {
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)

            audioManager.dispatchMediaKeyEvent(downEvent)
            Thread.sleep(50)
            audioManager.dispatchMediaKeyEvent(upEvent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openMusicApp(context: Context, packageName: String? = null): Boolean {
        return try {
            val intent = if (packageName != null) {
                context.packageManager.getLaunchIntentForPackage(packageName)
            } else {
                null
            } ?: run {
                val musicApps = listOf(
                    "com.tencent.qqmusic",
                    "com.netease.cloudmusic",
                    "com.kugou.android",
                    "cn.kuwo.player"
                )
                musicApps.mapNotNull { pkg ->
                    context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }.firstOrNull()
            } ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MUSIC)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}