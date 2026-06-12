package com.cardesktop.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
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
    private var mediaSessionManager: MediaSessionManager? = null
    private var activeSessionCallback: Any? = null

    private val metadataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { updateMetadataFromIntent(it) }
        }
    }

    fun init(context: Context) {
        this.context = context.applicationContext
        
        registerReceivers()
        startMonitoringMediaSessions()
        
        tryGetInitialMetadata()
    }

    private fun registerReceivers() {
        context?.let { ctx ->
            try {
                val filter = IntentFilter().apply {
                    addAction("com.android.music.metachanged")
                    addAction("com.htc.music.metachanged")
                   .addAction("fm.last.android.metachanged")
                    addAction("com.sec.android.app.music.metachanged")
                    addAction("com.nullsoft.winamp.metachanged")
                    addAction("com.amazon.mp3.metachanged")
                    addAction("com.miui.player.metachanged")
                    addAction("com.real.IMP.metachanged")
                    actions = listOf(
                        "android.intent.action.MEDIA_BUTTON",
                        "com.android.music.playstatechanged",
                        "com.android.music.playbackcomplete",
                        "com.android.music.queuechanged",
                        "net.easyfox.musixmatch.metachanged",
                        "com.maxmpz.audioplayer.METADATA_CHANGED",
                        "com.spotify.music.metadatachanged",
                        "com.spotify.music.playbackstatechanged"
                    ).forEach { addAction(it) }
                }
                
                ContextCompat.registerReceiver(ctx, metadataReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startMonitoringMediaSessions() {
        try {
            mediaSessionManager = context?.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            
            mediaSessionManager?.getActiveSessions(null)?.forEach { session ->
                session.controller?.registerCallback(
                    object : MediaSessionManager.Callback() {},
                    android.os.Handler(android.os.Looper.getMainLooper())
                )
            }
            
            monitorActiveSessions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun monitorActiveSessions() {
        Thread {
            while (true) {
                try {
                    Thread.sleep(2000)
                    
                    val sessions = mediaSessionManager?.getActiveSessions(null)
                    if (!sessions.isNullOrEmpty()) {
                        val latestSession = sessions.firstOrNull { 
                            it.isActive && it.playbackState != null 
                        } ?: return@forEach
                        
                        val controller = latestSession.controller
                        val metadata = controller.metadata
                        
                        if (metadata != null) {
                            val title = metadata.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: ""
                            val artist = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                            val album = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM) ?: ""
                            
                            if (title.isNotEmpty()) {
                                _metadata.value = MusicMetadata(
                                    title = title,
                                    artist = artist,
                                    album = album,
                                    isPlaying = latestSession.playbackState?.state == PlaybackState.STATE_PLAYING,
                                    duration = metadata.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION),
                                    packageName = latestSession.packageName ?: ""
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
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
            val track = intent.getStringExtra("track") ?: ""
            val artist = intent.getStringExtra("artist") ?: ""
            val album = intent.getStringExtra("album") ?: ""
            val isPlaying = intent.getBooleanExtra("playing", false)
            
            if (track.isNotEmpty()) {
                _metadata.value = MusicMetadata(
                    title = track,
                    artist = artist,
                    album = album,
                    isPlaying = isPlaying,
                    packageName = intent.`package` ?: ""
                )
            } else {
                val fallbackTitle = getStringExtraSafe(intent, "title") 
                    ?: getStringExtraSafe(intent, "song") 
                    ?: ""
                
                if (fallbackTitle.isNotEmpty()) {
                    _metadata.value = MusicMetadata(
                        title = fallbackTitle,
                        artist = artist,
                        album = album,
                        isPlaying = isPlaying,
                        packageName = intent.`package` ?: ""
                    )
                }
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
            val sessions = mediaSessionManager?.getActiveSessions(null)
            if (!sessions.isNullOrEmpty()) {
                val session = sessions.find { it.isActive } ?: sessions[0]
                val controller = session.controller
                val meta = controller.metadata
                
                if (meta != null) {
                    _metadata.value = MusicMetadata(
                        title = meta.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "",
                        artist = meta.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                        album = meta.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM) ?: "",
                        isPlaying = session.playbackState?.state == PlaybackState.STATE_PLAYING,
                        duration = meta.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION),
                        packageName = session.packageName ?: ""
                    )
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stop() {
        try {
            context?.unregisterReceiver(metadataReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}