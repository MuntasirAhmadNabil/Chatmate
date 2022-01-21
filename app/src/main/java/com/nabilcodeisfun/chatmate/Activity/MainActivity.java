package com.nabilcodeisfun.chatmate.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nabilcodeisfun.chatmate.R;
import com.nabilcodeisfun.chatmate.Models.User;
import com.nabilcodeisfun.chatmate.Adapters.UsersAdapter;
import com.nabilcodeisfun.chatmate.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User>users;
    ArrayList<String>contactList;
    UsersAdapter usersAdapter;
    FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.status:
                        Toast.makeText(MainActivity.this, "Status coming soon", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.calls:
                        Toast.makeText(MainActivity.this, "Calls coming soon", Toast.LENGTH_SHORT).show();
                        return true;

                }
                return true;
            }
        });

        contactList= new ArrayList<>();


        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading chat...");
        dialog.setCancelable(false);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {


                /* background change*/
                String backgroundImage= mFirebaseRemoteConfig.getString("backgroundImage");


                /*Toolbar color*/
                String toolbarColor= mFirebaseRemoteConfig.getString("toolbarColor");
                String toolbarImage= mFirebaseRemoteConfig.getString("toolbarImage");
                boolean isToolbarImageEnabled= mFirebaseRemoteConfig.getBoolean("toolbarImageEnabled");

                if(isToolbarImageEnabled){
                    Glide.with(MainActivity.this)
                            .load(backgroundImage)
                            .into(binding.backgroundImage);


                    Glide.with(MainActivity.this)
                            .load(toolbarImage)
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    getSupportActionBar().setBackgroundDrawable(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                }else {
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));
                }



            }
        });

        database= FirebaseDatabase.getInstance();
        auth= FirebaseAuth.getInstance();
        users= new ArrayList<>();

        usersAdapter= new UsersAdapter(this,users);
        binding.recyclerView.setAdapter(usersAdapter);


        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token",token);


                        database.getReference().child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);

                    }
                });

        checkPermission();


    }

    void checkChangeOnDB(){
        dialog.show();
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    User user= snapshot1.getValue(User.class);



                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid())){

                        if(isObjectPresentInArrayList(contactList,user.getPhoneNumber())!=-1)
                        {
                            users.add(user);
                        }

                    }

                }

                usersAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        usersAdapter= new UsersAdapter(this,users);
//        binding.recyclerView.setAdapter(usersAdapter);
    }

    int isObjectPresentInArrayList(ArrayList<String> arr, String number){

        int index= arr.indexOf(number);
        return index;
    }



    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_CONTACTS},100);
        }
        else{
            getContactList();
        }
    }

    private void getContactList() {


        ContentResolver contentResolver= getContentResolver();
        Uri uri= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor= contentResolver.query(uri,null,null,null,ContactsContract.CommonDataKinds.Phone.NUMBER+" ASC");

      if(cursor.getCount()>0){
          while (cursor.moveToNext()){
              String contactNumber= cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


              String number= contactNumber.replaceAll("[^0-9\\+]", "");
              Log.e("Got contact from phone","+88"+number);

              if(number.length()==11){
                  contactList.add("+88"+number);
              }else{
                  contactList.add(number);
              }

          }
      }

      checkChangeOnDB();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //check Condition

        if(requestCode==100 && grantResults.length>0 && grantResults[0]
        == PackageManager.PERMISSION_GRANTED){

            //when permission is granted then call method

            getContactList();


        }else {

            //When permission is denied then display toast
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();

            //call check permission method
            checkPermission();
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                Toast.makeText(this, "Search Updating soon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.setting:
                Toast.makeText(this, "Setting Updating soon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.invite:
                Toast.makeText(this, "Invite Updating soon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.group:
                Toast.makeText(this, "Group Updating soon", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.signout:
                auth.signOut();
                startActivity(new Intent(MainActivity.this,PhoneNumberActivity.class));
                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);

    }


}
