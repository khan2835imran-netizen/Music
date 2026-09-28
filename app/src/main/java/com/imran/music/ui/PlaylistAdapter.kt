package com.imran.music.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imran.music.data.LibraryStore
import com.imran.music.databinding.ItemPlaylistBinding

class PlaylistAdapter(
 private val store: LibraryStore,
 private val onClick: (String) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.Holder>() {
 private var items = emptyList<String>()
 fun submit(names: List<String>) { items = names; notifyDataSetChanged() }
 override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
  Holder(ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false))
 override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(items[position])
 override fun getItemCount() = items.size
 inner class Holder(private val b: ItemPlaylistBinding) : RecyclerView.ViewHolder(b.root) {
  fun bind(name: String) {
   b.name.text = name
   b.count.text = store.playlistSongIds(name).size.toString() + " songs"
   b.root.setOnClickListener { onClick(name) }
   b.delete.setOnClickListener { store.deletePlaylist(name); submit(store.playlistNames()) }
  }
 }
}