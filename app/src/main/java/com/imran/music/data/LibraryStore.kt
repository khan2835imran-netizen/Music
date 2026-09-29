package com.imran.music.data

import android.content.Context
import com.imran.music.model.Song
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class LibraryStore(context: Context) {
 private val prefs = context.getSharedPreferences("music_library", Context.MODE_PRIVATE)
 private val favoriteKey = "favorites"
 private val playlistKey = "playlists"

 fun favorites(): Set<Long> {
  return try {
   prefs.getStringSet(favoriteKey, emptySet())
    ?.mapNotNull { it.toLongOrNull() }
    ?.toSet()
    ?: emptySet()
  } catch (_: ClassCastException) {
   prefs.edit().remove(favoriteKey).apply()
   emptySet()
  }
 }

 fun isFavorite(id: Long) = favorites().contains(id)

 fun toggleFavorite(id: Long) {
  val next = favorites().toMutableSet()
  if (!next.add(id)) next.remove(id)
  prefs.edit().putStringSet(favoriteKey, next.map(Long::toString).toSet()).apply()
 }

 private fun playlistsJson(): JSONObject {
  return try {
   JSONObject(prefs.getString(playlistKey, "{}") ?: "{}")
  } catch (_: JSONException) {
   prefs.edit().remove(playlistKey).apply()
   JSONObject()
  } catch (_: ClassCastException) {
   prefs.edit().remove(playlistKey).apply()
   JSONObject()
  }
 }

 fun savePlaylist(name: String, songs: List<Song>) {
  val all = playlistsJson()
  val arr = JSONArray()
  songs.distinctBy { it.id }.forEach { arr.put(it.id) }
  all.put(name.trim(), arr)
  prefs.edit().putString(playlistKey, all.toString()).apply()
 }

 fun createPlaylist(name: String): Boolean {
  val n = name.trim()
  if (n.isBlank() || playlistNames().contains(n)) return false
  savePlaylist(n, emptyList())
  return true
 }

 fun playlistNames(): List<String> {
  val all = playlistsJson()
  val out = mutableListOf<String>()
  val keys = all.keys()
  while (keys.hasNext()) out += keys.next()
  return out.sortedWith(String.CASE_INSENSITIVE_ORDER)
 }

 fun playlistSongIds(name: String): Set<Long> {
  val arr = playlistsJson().optJSONArray(name) ?: return emptySet()
  return buildSet { for (i in 0 until arr.length()) add(arr.optLong(i)) }
 }

 fun addToPlaylist(name: String, song: Song) {
  val ids = playlistSongIds(name).toMutableSet()
  ids.add(song.id)
  savePlaylist(name, ids.map { song.copy(id = it) })
 }

 fun deletePlaylist(name: String) {
  val all = playlistsJson()
  all.remove(name)
  prefs.edit().putString(playlistKey, all.toString()).apply()
 }
}