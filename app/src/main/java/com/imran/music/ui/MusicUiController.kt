package com.imran.music.ui
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.imran.music.MainActivity
import com.imran.music.data.MusicRepository
import com.imran.music.databinding.ActivityMainBinding
import com.imran.music.player.PlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MusicUiController(private val activity:MainActivity,private val binding:ActivityMainBinding){
 private val repository=MusicRepository(activity)
 private val playback=PlaybackController(activity)
 private val adapter=SongAdapter{playback.play(it)}
 fun bind(){
  binding.songList.layoutManager=LinearLayoutManager(activity)
  binding.songList.adapter=adapter
  binding.shuffleButton.setOnClickListener{playback.setShuffle(true)}
  binding.emptyText.text="Scanning your offline music…"
  activity.lifecycleScope.launch{
   val songs=withContext(Dispatchers.IO){repository.scan()}
   adapter.submit(songs)
   binding.emptyText.text=if(songs.isEmpty()) "No music found on this device" else songs.size.toString()+" songs • Tap a song to play"
  }
 }
}