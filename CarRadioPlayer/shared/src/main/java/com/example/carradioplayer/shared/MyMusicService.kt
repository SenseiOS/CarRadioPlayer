package com.example.carradioplayer.shared

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import java.util.*



class MyMusicService : MediaBrowserServiceCompat() {

    val TAG = "MyService"
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var session: MediaSessionCompat


    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Log.d("service", "Play")
            session.setActive(true)
            setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            mediaPlayer!!.start()}

        override fun onSkipToQueueItem(queueId: Long) {}

        override fun onSeekTo(position: Long) {}

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {}

        override fun onPause() {
            Log.d("service", "Pause")
            if( mediaPlayer!!.isPlaying() ) {
                mediaPlayer!!.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            }}

        override fun onStop() {}

        override fun onSkipToNext() {}

        override fun onSkipToPrevious() {}

        override fun onCustomAction(action: String?, extras: Bundle?) {}

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {}
    }

    private fun setMediaPlaybackState(state: Int) {
        val playbackstateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0f)
        session.setPlaybackState(playbackstateBuilder.build())
    }
    override fun onCreate() {
        super.onCreate()
        session = MediaSessionCompat(this, "MyMusicService")
        sessionToken = session.sessionToken
        session.setCallback(callback)
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        session.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build())

        session.setMetadata(MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Russian")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Radio Rock")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10000)
                .build())

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0)
        session.setMediaButtonReceiver(pendingIntent)
        config()
    }

    private fun config(){
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.isLooping = true
            mediaPlayer!!.run {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setOnPreparedListener({
                    callback.onPlay()

                })
                setOnErrorListener({ mp, what, extra ->
                    Log.i(TAG, "mp: $mp\nwhat: $what\nextra: $extra")
                    release()
                    config()
                    false
                })
                setDataSource(url)
                prepare()
            }
        } catch (e : Exception){
            e.message
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("service", "onStartCommand")
        MediaButtonReceiver.handleIntent(session, intent)
        if (session.getController().getPlaybackState().getState() === PlaybackStateCompat.STATE_PLAYING) {
            callback.onPause()

        } else {
            callback.onPlay()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
       Stop()
    }

private fun Stop()
{
    session.release()
    mediaPlayer!!.stop()
    mediaPlayer!!.release()
    mediaPlayer = null
}


  private  fun ShowLog(message: String)
    {
        Log.d(TAG,message)
    }

    companion object {
        private  val url="https://nashe1.hostingradio.ru:18000/rock-256"
        // private  val url="https://lk.castnow.ru:8100/edm-320.mp3"
    }


    override fun onGetRoot(clientPackageName: String,
                           clientUid: Int,
                           rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        result.sendResult(ArrayList())
    }
}
