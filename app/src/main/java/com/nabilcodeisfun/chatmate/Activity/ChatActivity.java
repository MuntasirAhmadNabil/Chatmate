package com.nabilcodeisfun.chatmate.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nabilcodeisfun.chatmate.Adapters.MessagesAdapter;
import com.nabilcodeisfun.chatmate.Models.Message;
import com.nabilcodeisfun.chatmate.Models.User;
import com.nabilcodeisfun.chatmate.R;
import com.nabilcodeisfun.chatmate.databinding.ActivityChatBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message>messages;

    ActionBar actionBar;

    String senderRoom, receiverRoom;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        messages= new ArrayList<>();


        String name= getIntent().getStringExtra("name");
        String token= getIntent().getStringExtra("token");


        String receiveruid= getIntent().getStringExtra("uid");
        String senderuid= FirebaseAuth.getInstance().getUid();

        senderRoom= senderuid + receiveruid;
        receiverRoom= receiveruid + senderuid;

        database= FirebaseDatabase.getInstance();

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();

                        for (DataSnapshot snapshot1: snapshot.getChildren()){
                            Message message= snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            messages.add(message);
                        }

                        adapter.notifyDataSetChanged();

                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        adapter= new MessagesAdapter(this,messages,senderRoom,receiverRoom);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerview.setAdapter(adapter);

        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageTxt= binding.messagebox.getText().toString();

            //    if (messageTxt.matches(".*\\w.*")){
                if (messageTxt.trim().length() > 0){
                    Date date= new Date();
                    Message message= new Message(messageTxt,senderuid,date.getTime());
                    binding.messagebox.setText("");

                    String randomKey= database.getReference().push().getKey();

                    HashMap<String,Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg",message.getMessage());
                    lastMsgObj.put("lastMsgTime",date.getTime());

                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);




                    assert randomKey != null;
                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    database.getReference().child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    User user = snapshot.getValue(User.class);
                                                    String myName= user.getName();
                                                    Log.e("My name", myName);
                                                    sendNotification(myName ,message.getMessage(),token);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });





                                }
                            });

                            HashMap<String,Object> lastMsgObj = new HashMap<>();
                            lastMsgObj.put("lastMsg",message.getMessage());
                            lastMsgObj.put("lastMsgTime",date.getTime());

                            database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                            database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);



                        }
                    });
                } else {
                    return;
                }
                }


        });

        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
   //     Objects.requireNonNull(getSupportActionBar()).setTitle(Html.fromHtml("<font color='#000000'>"+name+"</font>"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void sendNotification(String name, String message, String token){
        try {
        RequestQueue queue= Volley.newRequestQueue(this);
        String url= "https://fcm.googleapis.com/fcm/send";

        JSONObject data= new JSONObject();

        data.put("title",name);
        data.put("body",message);

            JSONObject notificationData= new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);

            JsonObjectRequest request= new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                //    Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_LONG).show();

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_LONG).show();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> map= new HashMap<>();
                    String key= "key=AAAAl6fiy5s:APA91bGPsBEYnLGndKqmyWwpKOWwWG4uBLpRpBEBvLycTp3udjMzg4Lqht-m174GpZDp0Df_v5TbCUp4ElPPp3U5OhzLAZ58-64_0fXwczsYljikQAZDAph7niu5QDoTD4yCoQ-OUDlb";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");
                    return map;
                }
            }
                    ;

            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}