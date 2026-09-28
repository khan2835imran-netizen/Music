package com.imran.music.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.imran.music.model.Song

class PlaybackController(context: Context) {
 private val mainHandler = Handler(Looper.getMainLooper())
 private val future: ListenableFuture<MediaController> = MediaController.Builder(
  context, SessionToken(context, android.content.ComponentName(context, MusicPlaybackService::class.java))
 ).buildAsync()

 fun play(song: Song) {
  val item = mediaItem(song)
  runOnController { it.setMediaItem(item); it.prepare(); it.play() }
 }

 fun playQueue(songs: List<Song>, start: Int = 0) {
  val items = songs.map(::mediaItem)
  runOnController { it.setMediaItems(items, start, 0); it.prepare(); it.play() }
 }

 fun playPause() = runOnController { if (it.isPlaying) it.pause() else it.play() }
 fun next() = runOnController { it.seekToNext() }
 fun previous() = runOnController { it.seekToPrevious() }
 fun toggleShuffle() = runOnController { it.shuffleModeEnabled = !it.shuffleModeEnabled }

 fun cycleRepeat() = runOnController {
  it.repeatMode = when (it.repeatMode) {
   Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
   Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
   else -> Player.REPEAT_MODE_OFF
  }
 }

 fun addListener(listener: Player.Listener) = runOnController { it.addListener(listener) }

 private fun mediaItem(s: Song) = MediaItem.Builder()
  .setMediaId(s.id.toString()).setUri(s.uri)
  .setMediaMetadata(MediaMetadata.Builder().setTitle(s.title).setArtist(s.artist).setAlbumTitle(s.album).build())
  .build()

 private fun runOnController(action: (MediaController) -> Unit) {
  if (future.isDone) {
   try { mainHandler.post { action(future.get()) } } catch (_: Exception) {}
  } else {
   future.addListener({
    try { mainHandler.post { action(future.get()) } } catch (_: Exception) {}
   }, { command -> command.run() })
  }
 }

 fun release() = MediaController.releaseFuture(future)
}