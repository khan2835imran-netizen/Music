package com.imran.music.ui

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.imran.music.MainActivity
import com.imran.music.data.LibraryStore
import com.imran.music.data.MusicRepository
import com.imran.music.databinding.ActivityMainBinding
import com.imran.music.model.Song
import com.imran.music.player.PlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicUiController(private val activity:MainActivity,private val binding:ActivityMainBinding){
 private val repository=MusicRepository(activity)
 private val playback=PlaybackController(activity)
 private val store=LibraryStore(activity)
 private val adapter=SongAdapter{ song -> playback.play(song); activity.startActivity(android.content.Intent(activity, com.imran.music.player.NowPlayingActivity::class.java)) }
 private var songs=listOf<Song>()
 private var showingFavorites=false

 fun bind(){
  binding.songList.layoutManager=LinearLayoutManager(activity)
  binding.songList.adapter=adapter
  binding.emptyText.text="Scanning your offline music…"
  binding.shuffleButton.setOnClickListener{if(songs.isNotEmpty())playback.playQueue(songs);playback.toggleShuffle()}
  binding.repeatButton.setOnClickListener{playback.cycleRepeat()}
  binding.favoritesButton.setOnClickListener{showingFavorites=!showingFavorites;render(songs)}
  binding.search.addTextChangedListener(object:TextWatcher{
   override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
   override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){render(filter(s?.toString().orEmpty()))}
   override fun afterTextChanged(s:Editable?){}
  })
  activity.lifecycleScope.launch{
   songs=withContext(Dispatchers.IO){repository.scan()}
   render(songs)
  }
 }
 private fun filter(q:String)=songs.filter{it.title.contains(q,true)||it.artist.contains(q,true)||it.album.contains(q,true)}
 private fun render(list:List<Song>){
  val shown=if(showingFavorites)list.filter{store.isFavorite(it.id)} else list
  adapter.submit(shown)
  binding.emptyText.text=when{
   shown.isNotEmpty()->""
   showingFavorites->"No favorite songs yet"
   else->"No music found on this device"
  }
 }
}