package com.imran.music.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.imran.music.databinding.ActivityNowPlayingBinding

class NowPlayingActivity : AppCompatActivity() {
 private lateinit var binding: ActivityNowPlayingBinding
 private var controller: MediaController? = null
 private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityNowPlayingBinding.inflate(layoutInflater)
  setContentView(binding.root)

  val token = SessionToken(this, android.content.ComponentName(this, MusicPlaybackService::class.java))
  controllerFuture = MediaController.Builder(this, token).buildAsync()
  controllerFuture?.addListener({
   try {
    controller = controllerFuture?.get()
    runOnUiThread { bindControls() }
   } catch (_: Exception) {
   }
  }, { command -> command.run() })
 }

 private fun bindControls() {
  val c = controller ?: return
  binding.playPause.setOnClickListener { if (c.isPlaying) c.pause() else c.play() }
  binding.next.setOnClickListener { c.seekToNext() }
  binding.previous.setOnClickListener { c.seekToPrevious() }
  binding.shuffle.setOnClickListener { c.shuffleModeEnabled = !c.shuffleModeEnabled }
  binding.repeat.setOnClickListener { c.repeatMode = (c.repeatMode + 1) % 3 }
  binding.trackTitle.text = c.mediaMetadata.title ?: "Nothing playing"
  binding.trackArtist.text = c.mediaMetadata.artist ?: "Choose a song"
  c.addListener(object : androidx.media3.common.Player.Listener {
   override fun onMediaMetadataChanged(m: androidx.media3.common.MediaMetadata) {
    binding.trackTitle.text = m.title ?: "Unknown"
    binding.trackArtist.text = m.artist ?: "Unknown artist"
   }
   override fun onIsPlayingChanged(isPlaying: Boolean) {
    binding.playPause.text = if (isPlaying) "Pause" else "Play"
   }
  })
 }

 override fun onDestroy() {
  controller?.release()
  controller = null
  controllerFuture?.let { MediaController.releaseFuture(it) }
  controllerFuture = null
  super.onDestroy()
 }
}