package com.imran.music.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.imran.music.R
import com.imran.music.databinding.ActivityNowPlayingBinding

class NowPlayingActivity : AppCompatActivity() {
 private lateinit var binding: ActivityNowPlayingBinding
 private var controller: MediaController? = null
 private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
 private val handler = Handler(Looper.getMainLooper())
 private val progressTask = object : Runnable {
  override fun run() {
   controller?.let {
    if (it.duration > 0) {
     binding.progress.max = it.duration.toInt().coerceAtLeast(1)
     binding.progress.progress = it.currentPosition.toInt().coerceAtLeast(0)
    }
   }
   handler.postDelayed(this, 500)
  }
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityNowPlayingBinding.inflate(layoutInflater)
  setContentView(binding.root)
  binding.back.setOnClickListener { finish() }

  val token = SessionToken(this, android.content.ComponentName(this, MusicPlaybackService::class.java))
  controllerFuture = MediaController.Builder(this, token).buildAsync()
  controllerFuture?.addListener({
   try {
    controller = controllerFuture?.get()
    runOnUiThread { bindControls(); handler.post(progressTask) }
   } catch (_: Exception) {}
  }, { command -> command.run() })
 }

 private fun bindControls() {
  val c = controller ?: return
  binding.playPause.setOnClickListener { if (c.isPlaying) c.pause() else c.play() }
  binding.next.setOnClickListener { c.seekToNext() }
  binding.previous.setOnClickListener { c.seekToPrevious() }
  binding.shuffle.setOnClickListener { c.shuffleModeEnabled = !c.shuffleModeEnabled }
  binding.repeat.setOnClickListener { c.repeatMode = (c.repeatMode + 1) % 3 }
  binding.menu.setOnClickListener { c.repeatMode = (c.repeatMode + 1) % 3 }
  binding.progress.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
   override fun onProgressChanged(s: android.widget.SeekBar?, p: Int, fromUser: Boolean) { if (fromUser) c.seekTo(p.toLong()) }
   override fun onStartTrackingTouch(s: android.widget.SeekBar?) {}
   override fun onStopTrackingTouch(s: android.widget.SeekBar?) {}
  })
  c.addListener(object : androidx.media3.common.Player.Listener {
   override fun onMediaMetadataChanged(m: androidx.media3.common.MediaMetadata) {
    binding.trackTitle.text = m.title ?: "Nothing playing"
    binding.trackArtist.text = m.artist ?: "Choose a song"
    m.artworkUri?.let { runCatching { binding.artwork.setImageURI(Uri.parse(it.toString())) } }
   }
   override fun onIsPlayingChanged(isPlaying: Boolean) {
    binding.playPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
   }
   override fun onMediaItemTransition(item: androidx.media3.common.MediaItem?, reason: Int) {
    c.mediaMetadata.let {
     binding.trackTitle.text = it.title ?: "Nothing playing"
     binding.trackArtist.text = it.artist ?: "Choose a song"
     it.artworkUri?.let { uri -> runCatching { binding.artwork.setImageURI(Uri.parse(uri.toString())) } }
    }
   }
  })
  binding.trackTitle.text = c.mediaMetadata.title ?: "Nothing playing"
  binding.trackArtist.text = c.mediaMetadata.artist ?: "Choose a song"
  c.mediaMetadata.artworkUri?.let { runCatching { binding.artwork.setImageURI(Uri.parse(it.toString())) } }
 }

 override fun onDestroy() {
  handler.removeCallbacks(progressTask)
  controller?.release()
  controller = null
  controllerFuture?.let { MediaController.releaseFuture(it) }
  controllerFuture = null
  super.onDestroy()
 }
}