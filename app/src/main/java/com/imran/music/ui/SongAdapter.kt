package com.imran.music.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imran.music.data.LibraryStore
import com.imran.music.databinding.ItemSongBinding
import com.imran.music.model.Song

class SongAdapter(
 private val store: LibraryStore,
 private val onClick: (Song) -> Unit,
 private val onLongPress: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.Holder>() {
 private val items = mutableListOf<Song>()
 fun submit(list: List<Song>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
 override fun onCreateViewHolder(p: ViewGroup, v: Int) = Holder(ItemSongBinding.inflate(LayoutInflater.from(p.context), p, false))
 override fun onBindViewHolder(h: Holder, p: Int) = h.bind(items[p])
 override fun getItemCount() = items.size

 inner class Holder(private val b: ItemSongBinding) : RecyclerView.ViewHolder(b.root) {
  fun bind(s: Song) {
   b.songTitle.text = s.title
   b.songArtist.text = if (s.artist.isBlank()) "Unknown artist" else s.artist
   b.artwork.setImageResource(com.imran.music.R.drawable.artwork_bg)
   s.artwork?.let { runCatching { b.artwork.setImageURI(Uri.parse(it)) } }
   b.favorite.alpha = if (store.isFavorite(s.id)) 1f else 0.45f
   b.root.setOnClickListener { onClick(s) }
   b.root.setOnLongClickListener { onLongPress(s); true }
   b.favorite.setOnClickListener {
    store.toggleFavorite(s.id)
    b.favorite.alpha = if (store.isFavorite(s.id)) 1f else 0.45f
   }
  }
 }
}