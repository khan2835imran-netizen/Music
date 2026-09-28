package com.imran.music.ui
import androidx.lifecycle.lifecycleScope
import com.imran.music.MainActivity
import com.imran.music.data.MusicRepository
import com.imran.music.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MusicUiController(private val activity:MainActivity,private val binding:ActivityMainBinding){
 private val repository=MusicRepository(activity)
 fun bind(){
  binding.emptyText.text="Scanning your offline music…"
  activity.lifecycleScope.launch{
   val songs=withContext(Dispatchers.IO){repository.scan()}
   binding.emptyText.text=if(songs.isEmpty()) "No music found on this device" else songs.size.toString()+" songs found • Offline library ready"
  }
 }
}