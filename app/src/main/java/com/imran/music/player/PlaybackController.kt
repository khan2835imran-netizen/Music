package com.imran.music.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.imran.music.model.Song

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
 fun playQueue(songs:List<Song>,start:Int=0){
  val c=future.get()
  c.setMediaItems(songs.map{MediaItem.Builder().setMediaId(it.id.toString()).setUri(it.uri)
   .setMediaMetadata(MediaMetadata.Builder().setTitle(it.title).setArtist(it.artist).setAlbumTitle(it.album).build()).build()},start,0)
  c.prepare();c.play()
 }
 fun toggleShuffle(){future.get().shuffleModeEnabled=!future.get().shuffleModeEnabled}
 fun cycleRepeat(){
  val c=future.get()
  c.repeatMode=when(c.repeatMode){Player.REPEAT_MODE_OFF->Player.REPEAT_MODE_ALL;Player.REPEAT_MODE_ALL->Player.REPEAT_MODE_ONE;else->Player.REPEAT_MODE_OFF}
 }
 fun release(){MediaController.releaseFuture(future)}
}