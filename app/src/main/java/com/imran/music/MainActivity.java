package com.imran.music;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.imran.music.databinding.ActivityMainBinding;
import com.imran.music.ui.MusicUiController;

public class MainActivity extends ComponentActivity {
 private static final int AUDIO_PERMISSION=100;
 private MusicUiController controller;
 @Override protected void onCreate(Bundle savedInstanceState){
  super.onCreate(savedInstanceState);
  ActivityMainBinding binding=ActivityMainBinding.inflate(getLayoutInflater());
  setContentView(binding.getRoot());
  controller=new MusicUiController(this,binding);
  controller.bind();
  requestAudioPermissionIfNeeded();
 }
 private String audioPermission(){ return Build.VERSION.SDK_INT>=33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE; }
 private void requestAudioPermissionIfNeeded(){
  String permission=audioPermission();
  if(ContextCompat.checkSelfPermission(this,permission)!=PackageManager.PERMISSION_GRANTED)
   ActivityCompat.requestPermissions(this,new String[]{permission},AUDIO_PERMISSION);
 }
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults);
  if(requestCode==AUDIO_PERMISSION && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && controller!=null) controller.refreshLibrary();
 }
 @Override protected void onDestroy(){ if(controller!=null) controller.release(); super.onDestroy(); }
}