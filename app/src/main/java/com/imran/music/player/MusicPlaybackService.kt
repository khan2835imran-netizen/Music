package com.imran.music.player
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
class MusicPlaybackService:MediaSessionService(){
 private lateinit var player:ExoPlayer
 private var session:MediaSession?=null
 override fun onCreate(){
  super.onCreate()
  player=ExoPlayer.Builder(this).build().apply{repeatMode=Player.REPEAT_MODE_OFF;setHandleAudioBecomingNoisy(true)}
  session=MediaSession.Builder(this,player).build()
 }
 override fun onGetSession(controllerInfo:MediaSession.ControllerInfo):MediaSession?=session
 override fun onDestroy(){session?.release();player.release();super.onDestroy()}
}