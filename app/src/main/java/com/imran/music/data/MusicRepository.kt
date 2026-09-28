package com.imran.music.data
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.imran.music.model.Song
class MusicRepository(private val context:Context){
 fun scan():List<Song>{
  val result=mutableListOf<Song>()
  val p=arrayOf(MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.DURATION)
  val selection=MediaStore.Audio.Media.IS_MUSIC+" != 0"
  context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,p,selection,null,MediaStore.Audio.Media.TITLE+" COLLATE NOCASE ASC")?.use{c->
   val id=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID); val title=c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
   val artist=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST); val album=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
   val duration=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
   while(c.moveToNext()){ val songId=c.getLong(id); result+=Song(songId,c.getString(title),c.getString(artist),c.getString(album),ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,songId).toString(),c.getLong(duration)) }
  }
  return result
 }
}