package com.imran.music;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.imran.music.databinding.ActivityMainBinding;
import com.imran.music.ui.MusicUiController;

public class MainActivity extends ComponentActivity {
 private static final int AUDIO_PERMISSION=100;
 @Override protected void onCreate(Bundle savedInstanceState){
  super.onCreate(savedInstanceState);
  ActivityMainBinding binding=ActivityMainBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());
  new MusicUiController(this,binding).bind();
  String permission=android.os.Build.VERSION.SDK_INT>=33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
  if(ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED)
   ActivityCompat.requestPermissions(this,new String[]{permission},AUDIO_PERMISSION);
 }
}