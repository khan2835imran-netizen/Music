package com.imran.music.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.imran.music.model.Song
import com.google.common.util.concurrent.ListenableFuture

class PlaybackController(context:Context){
 private val future:ListenableFuture<MediaController> = MediaController.Builder(
  context, SessionToken(context,ComponentName(context,MusicPlaybackService::class.java))
 ).buildAsync()
 fun play(song:Song){
  val c=future.get()
  val item=MediaItem.Builder().setMediaId(song.id.toString()).setUri(song.uri)
   .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).setAlbumTitle(song.album).build()).build()
  c.setMediaItem(item);c.prepare();c.play()
 }
 fun setShuffle(enabled:Boolean){future.get().shuffleModeEnabled=enabled}
 fun setRepeat(mode:Int){future.get().repeatMode=mode}
 fun release(){MediaController.releaseFuture(future)}
}