package com.nabilcodeisfun.chatmate.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.nabilcodeisfun.chatmate.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.ccp.registerCarrierNumberEditText(binding.PhoneBox);

        auth= FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null){
            Intent intent= new Intent(PhoneNumberActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        getSupportActionBar().hide();

//        binding.PhoneBox.requestFocus();
//
        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pno= binding.PhoneBox.getText().toString();

                if(pno.length()!=11){
                    binding.PhoneBox.setError("Enter last 10 digit phone");
                    return;
                }else {
                    Intent intent = new Intent(PhoneNumberActivity.this,OTPActivity.class);
                    intent.putExtra("phoneNumber", binding.ccp.getFullNumberWithPlus().replace("",""));
                    startActivity(intent);
                }

            }
        });
    }
}
