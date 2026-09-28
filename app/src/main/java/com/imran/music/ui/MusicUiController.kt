package com.imran.music.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.imran.music.MainActivity
import com.imran.music.R
import com.imran.music.data.LibraryStore
import com.imran.music.data.MusicRepository
import com.imran.music.databinding.ActivityMainBinding
import com.imran.music.model.Song
import com.imran.music.player.NowPlayingActivity
import com.imran.music.player.PlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicUiController(private val activity: MainActivity, private val binding: ActivityMainBinding) {
 private val repository = MusicRepository(activity)
 private val store = LibraryStore(activity)
 private val playback = PlaybackController(activity)
 private val playlistAdapter = PlaylistAdapter(store) { openPlaylist(it) }
 private val adapter = SongAdapter(store, { song ->
  playback.play(song); showMini(song)
  activity.startActivity(Intent(activity, NowPlayingActivity::class.java))
 }, { song -> addSongToPlaylist(song) })
 private var songs = listOf<Song>()
 private var mode = "library"

 fun bind() {
  binding.songList.layoutManager = LinearLayoutManager(activity)
  binding.songList.adapter = adapter
  binding.playlistList.layoutManager = LinearLayoutManager(activity)
  binding.playlistList.adapter = playlistAdapter
  binding.emptyText.text = "Scanning your offline music…"
  binding.searchButton.setOnClickListener {
   binding.search.visibility = if (binding.search.visibility == View.VISIBLE) View.GONE else View.VISIBLE
   if (binding.search.visibility == View.VISIBLE) binding.search.requestFocus()
  }
  binding.shuffleFab.setOnClickListener { if (songs.isNotEmpty()) playback.playQueue(songs.shuffled()) }
  binding.miniPlay.setOnClickListener { playback.playPause() }
  binding.miniNext.setOnClickListener { playback.next() }
  binding.miniPlayer.setOnClickListener { activity.startActivity(Intent(activity, NowPlayingActivity::class.java)) }
  binding.addPlaylist.setOnClickListener { showCreatePlaylist() }
  binding.tabLibrary.setOnClickListener { select("library") }
  binding.tabFavorites.setOnClickListener { select("favorites") }
  binding.tabAlbums.setOnClickListener { select("albums") }
  binding.tabArtists.setOnClickListener { select("artists") }
  binding.tabPlaylists.setOnClickListener { select("playlists") }
  binding.search.addTextChangedListener(object : TextWatcher {
   override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
   override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { if (mode != "playlists") render(filter(s?.toString().orEmpty())) }
   override fun afterTextChanged(s: Editable?) {}
  })
  playback.addListener(object : androidx.media3.common.Player.Listener {
   override fun onMediaMetadataChanged(m: androidx.media3.common.MediaMetadata) {
    binding.miniTitle.text = m.title ?: "Nothing playing"
    binding.miniArtist.text = m.artist ?: "Choose a song"
    m.artworkUri?.let { runCatching { binding.miniArtwork.setImageURI(Uri.parse(it.toString())) } }
   }
   override fun onIsPlayingChanged(isPlaying: Boolean) {
    binding.miniPlay.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
   }
  })
  activity.lifecycleScope.launch {
   songs = withContext(Dispatchers.IO) { repository.scan() }
   render(songs)
  }
 }

 private fun select(value: String) {
  mode = value
  val tabs = listOf(binding.tabLibrary, binding.tabPlaylists, binding.tabAlbums, binding.tabArtists, binding.tabFavorites)
  tabs.forEach { it.setTextColor(activity.getColor(R.color.text_secondary)) }
  binding.songList.visibility = if (value == "playlists") View.GONE else View.VISIBLE
  binding.playlistList.visibility = if (value == "playlists") View.VISIBLE else View.GONE
  binding.addPlaylist.visibility = if (value == "playlists") View.VISIBLE else View.GONE
  binding.emptyText.visibility = if (value == "playlists") View.GONE else View.VISIBLE
  when (value) {
   "library" -> { binding.tabLibrary.setTextColor(activity.getColor(R.color.accent)); binding.sectionTitle.text = "All Songs" }
   "favorites" -> { binding.tabFavorites.setTextColor(activity.getColor(R.color.accent)); binding.sectionTitle.text = "Favorites" }
   "albums" -> { binding.tabAlbums.setTextColor(activity.getColor(R.color.accent)); binding.sectionTitle.text = "Albums" }
   "artists" -> { binding.tabArtists.setTextColor(activity.getColor(R.color.accent)); binding.sectionTitle.text = "Artists" }
   "playlists" -> { binding.tabPlaylists.setTextColor(activity.getColor(R.color.accent)); binding.sectionTitle.text = "Your Playlists"; playlistAdapter.submit(store.playlistNames()) }
  }
  if (value != "playlists") render(filter(binding.search.text?.toString().orEmpty()))
 }

 private fun showCreatePlaylist() {
  val input = EditText(activity).apply { hint = "Playlist name"; setSingleLine(true); setPadding(48, 20, 48, 10) }
  AlertDialog.Builder(activity).setTitle("Create playlist").setView(input)
   .setNegativeButton("Cancel", null)
   .setPositiveButton("Create") { _, _ ->
    if (store.createPlaylist(input.text.toString())) {
     playlistAdapter.submit(store.playlistNames())
    }
   }.show()
 }

 private fun openPlaylist(name: String) {
  val ids = store.playlistSongIds(name)
  mode = "library"
  binding.playlistList.visibility = View.GONE
  binding.songList.visibility = View.VISIBLE
  binding.addPlaylist.visibility = View.GONE
  binding.emptyText.visibility = View.VISIBLE
  binding.sectionTitle.text = name
  adapter.submit(songs.filter { ids.contains(it.id) })
  binding.emptyText.text = if (ids.isEmpty()) "This playlist is empty\nLong-press a song to add it." else ""
 }

 private fun filter(q: String): List<Song> {
  var list = songs.filter { it.title.contains(q, true) || it.artist.contains(q, true) || it.album.contains(q, true) }
  list = when (mode) {
   "favorites" -> list.filter { store.isFavorite(it.id) }
   "albums" -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.album })
   "artists" -> list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist })
   else -> list
  }
  return list
 }

 private fun render(list: List<Song>) {
  adapter.submit(list)
  binding.emptyText.text = when {
   list.isNotEmpty() -> ""
   mode == "favorites" -> "No favorite songs yet\nTap the heart on a song to save it."
   else -> if (songs.isEmpty()) "No music found on this device" else "No matching songs"
  }
 }

 private fun showMini(song: Song) {
  binding.miniTitle.text = song.title
  binding.miniArtist.text = song.artist
  song.artwork?.let { runCatching { binding.miniArtwork.setImageURI(Uri.parse(it)) } }
 }

 fun addSongToPlaylist(song: Song) {
  val names = store.playlistNames()
  if (names.isEmpty()) { showCreatePlaylist(); return }
  AlertDialog.Builder(activity).setTitle("Add to playlist").setItems(names.toTypedArray()) { _, which ->
   store.addToPlaylist(names[which], song)
  }.show()
 }

 fun release() = playback.release()
}