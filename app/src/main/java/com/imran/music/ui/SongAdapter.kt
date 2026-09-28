package com.imran.music.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.imran.music.databinding.ItemSongBinding
import com.imran.music.model.Song

class SongAdapter(private val onClick:(Song)->Unit):RecyclerView.Adapter<SongAdapter.Holder>(){
 private val items=mutableListOf<Song>()
 fun submit(list:List<Song>){items.clear();items.addAll(list);notifyDataSetChanged()}
 override fun onCreateViewHolder(p:ViewGroup,v:Int)=Holder(ItemSongBinding.inflate(LayoutInflater.from(p.context),p,false))
 override fun onBindViewHolder(h:Holder,p:Int)=h.bind(items[p])
 override fun getItemCount()=items.size
 inner class Holder(private val b:ItemSongBinding):RecyclerView.ViewHolder(b.root){
  fun bind(s:Song){b.songTitle.text=s.title;b.songArtist.text=if(s.artist.isBlank())"Unknown artist" else s.artist;b.root.setOnClickListener{onClick(s)}}
 }
}