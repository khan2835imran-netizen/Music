package com.imran.music.data

import android.content.Context
import com.imran.music.model.Song
import org.json.JSONArray
import org.json.JSONObject

class LibraryStore(context: Context) {
 private val prefs = context.getSharedPreferences("music_library", Context.MODE_PRIVATE)
 private val key = "favorites"

 fun favorites(): Set<Long> = prefs.getStringSet(key, emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()
 fun isFavorite(id: Long) = favorites().contains(id)

 fun toggleFavorite(id: Long) {
  val next = favorites().toMutableSet()
  if (!next.add(id)) next.remove(id)
  prefs.edit().putStringSet(key, next.map(Long::toString).toSet()).apply()
 }

 fun savePlaylist(name: String, songs: List<Song>) {
  val all = JSONObject(prefs.getString("playlists", "{}"))
  val arr = JSONArray()
  songs.forEach { arr.put(it.id) }
  all.put(name, arr)
  prefs.edit().putString("playlists", all.toString()).apply()
 }

 fun playlistNames(): List<String> {
  val all = JSONObject(prefs.getString("playlists", "{}"))
  val out = mutableListOf<String>()
  val keys = all.keys()
  while (keys.hasNext()) out += keys.next()
  return out
 }
}