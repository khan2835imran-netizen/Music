package com.imran.music.player

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.imran.music.R
import com.imran.music.databinding.ActivityNowPlayingBinding
import kotlin.math.abs
import kotlin.math.sin

class NowPlayingActivity : AppCompatActivity() {
 private lateinit var binding: ActivityNowPlayingBinding
 private var controller: MediaController? = null
 private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
 private val handler = Handler(Looper.getMainLooper())
 private var phase = 0.0
 private val progressTask = object : Runnable {
  override fun run() {
   controller?.let {
    if (it.duration > 0) {
     binding.progress.max = it.duration.toInt().coerceAtLeast(1)
     binding.progress.progress = it.currentPosition.toInt().coerceAtLeast(0)
    }
    if (it.isPlaying) animateVisualizer()
   }
   handler.postDelayed(this, 500)
  }
 }

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  binding = ActivityNowPlayingBinding.inflate(layoutInflater)
  setContentView(binding.root)
  binding.back.setOnClickListener { finish() }
  bindFeatureTabs()
  val token = SessionToken(this, android.content.ComponentName(this, MusicPlaybackService::class.java))
  controllerFuture = MediaController.Builder(this, token).buildAsync()
  controllerFuture?.addListener({
   try {
    controller = controllerFuture?.get()
    runOnUiThread { bindControls(); handler.post(progressTask) }
   } catch (_: Exception) {}
  }, { command -> command.run() })
 }

 private fun bindFeatureTabs() {
  binding.tabPlayer.setOnClickListener { showPanel("player") }
  binding.tabLyrics.setOnClickListener { showPanel("lyrics") }
  binding.tabEqualizer.setOnClickListener { showPanel("equalizer") }
  binding.tabVisualizer.setOnClickListener { showPanel("visualizer") }
 }

 private fun showPanel(panel: String) {
  binding.playerPanel.visibility = if (panel == "player") View.VISIBLE else View.GONE
  binding.lyricsPanel.visibility = if (panel == "lyrics") View.VISIBLE else View.GONE
  binding.equalizerPanel.visibility = if (panel == "equalizer") View.VISIBLE else View.GONE
  binding.visualizerPanel.visibility = if (panel == "visualizer") View.VISIBLE else View.GONE
  val tabs = listOf(binding.tabPlayer, binding.tabLyrics, binding.tabEqualizer, binding.tabVisualizer)
  tabs.forEach { it.setTextColor(getColor(R.color.text_secondary)) }
  when (panel) {
   "player" -> binding.tabPlayer.setTextColor(getColor(R.color.accent))
   "lyrics" -> binding.tabLyrics.setTextColor(getColor(R.color.accent))
   "equalizer" -> binding.tabEqualizer.setTextColor(getColor(R.color.accent))
   "visualizer" -> binding.tabVisualizer.setTextColor(getColor(R.color.accent))
  }
 }

 private fun animateVisualizer() {
  val bars = binding.visualizerBars
  for (i in 0 until bars.childCount) {
   val v = bars.getChildAt(i)
   val lp = v.layoutParams
   val h = 25 + abs(sin(phase + i * 0.72)) * 65
   lp.height = h.toInt()
   v.layoutParams = lp
  }
  phase += 0.35
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
   override fun onMediaMetadataChanged(m: androidx.media3.common.MediaMetadata) { updateMetadata(m) }
   override fun onIsPlayingChanged(isPlaying: Boolean) {
    binding.playPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
   }
   override fun onMediaItemTransition(item: androidx.media3.common.MediaItem?, reason: Int) { updateMetadata(c.mediaMetadata) }
  })
  updateMetadata(c.mediaMetadata)
 }

 private fun updateMetadata(m: androidx.media3.common.MediaMetadata) {
  binding.trackTitle.text = m.title ?: "Nothing playing"
  binding.trackArtist.text = m.artist ?: "Choose a song"
  m.artworkUri?.let { runCatching { binding.artwork.setImageURI(Uri.parse(it.toString())) } }
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