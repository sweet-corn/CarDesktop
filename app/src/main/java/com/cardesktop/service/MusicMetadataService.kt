package com.cardesktop.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MusicMetadata(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val isPlaying: Boolean = false,
    val duration: Long = 0,
    val currentPosition: Long = 0,
    val packageName: String = ""
)

object MusicMetadataService {

    private val _metadata = MutableStateFlow(MusicMetadata())
    val metadata: StateFlow<MusicMetadata> = _metadata.asStateFlow()

    private var context: Context? = null

    private val metadataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateMetadataFromIntent(it) }
        }
    }

    fun init(context: Context) {
        this.context = context.applicationContext
        
        registerReceivers()
        
        tryGetInitialMetadata()
    }

    private fun registerReceivers() {
        context?.let { ctx ->
            try {
                val filter = IntentFilter().apply {
                    addAction("com.android.music.metachanged")
                    addAction("com.htc.music.metachanged")
                    addAction("fm.last.android.metachanged")
                    addAction("com.sec.android.app.music.metachanged")
                    addAction("com.nullsoft.winamp.metachanged")
                    addAction("com.amazon.mp3.metachanged")
                    addAction("com.miui.player.metachanged")
                    addAction("com.real.IMP.metachanged")
                    addAction("android.intent.action.MEDIA_BUTTON")
                    addAction("com.android.music.playstatechanged")
                    addAction("com.android.music.playbackcomplete")
                    addAction("com.android.music.queuechanged")
                    addAction("net.easyfox.musixmatch.metachanged")
                    addAction("com.maxmpz.audioplayer.METADATA_CHANGED")
                    addAction("com.spotify.music.metadatachanged")
                    addAction("com.spotify.music.playbackstatechanged")
                }
                
                ContextCompat.registerReceiver(ctx, metadataReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun tryGetInitialMetadata() {
        try {
            context?.contentResolver?.query(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                    android.provider.MediaStore.Audio.Media.TITLE,
                    android.provider.MediaStore.Audio.Media.ARTIST,
                    android.provider.MediaStore.Audio.Media.ALBUM
                ),
                null, null,
                "${android.provider.MediaStore.Audio.Media.DATE_ADDED} DESC LIMIT 1"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val title = cursor.getString(0) ?: ""
                    val artist = cursor.getString(1) ?: ""
                    
                    if (title.isNotEmpty()) {
                        _metadata.value = _metadata.value.copy(title = title, artist = artist)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateMetadataFromIntent(intent: Intent) {
        try {
            var track = intent.getStringExtra("track") ?: ""
            var artist = intent.getStringExtra("artist") ?: ""
            var album = intent.getStringExtra("album") ?: ""
            
            if (track.isEmpty()) {
                track = getStringExtraSafe(intent, "title") 
                    ?: getStringExtraSafe(intent, "song") 
                    ?: ""
            }
            
            val isPlaying = when (intent.action) {
                "com.android.music.playstatechanged" -> {
                    intent.getBooleanExtra("playing", false)
                }
                "com.android.music.metachanged",
                "com.htc.music.metachanged",
                "fm.last.android.metachanged",
                "com.sec.android.app.music.metachanged" -> true
                else -> _metadata.value.isPlaying
            }
            
            if (track.isNotEmpty()) {
                _metadata.value = MusicMetadata(
                    title = track,
                    artist = artist,
                    album = album,
                    isPlaying = isPlaying,
                    packageName = intent.`package` ?: ""
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getStringExtraSafe(intent: Intent, key: String): String? {
        return try {
            intent.getStringExtra(key)
        } catch (e: Exception) {
            null
        }
    }

    fun refreshMetadata(): Boolean {
        return try {
            tryGetInitialMetadata()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun updatePlaybackState(isPlaying: Boolean, title: String = "") {
        _metadata.value = _metadata.value.copy(
            isPlaying = isPlaying,
            title = if (title.isNotEmpty()) title else _metadata.value.title
        )
    }

    fun stop() {
        try {
            context?.unregisterReceiver(metadataReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}