package com.nabilcodeisfun.chatmate.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.OnOtpCompletionListener;
import com.nabilcodeisfun.chatmate.Models.User;
import com.nabilcodeisfun.chatmate.databinding.ActivityOtpBinding;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    ActivityOtpBinding binding;
    String verification_id;
    String phoneNumber;
    ProgressDialog dialog;
    int userExist = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        getSupportActionBar().hide();

        phoneNumber = getIntent().getStringExtra("phoneNumber");

        initiateotp();

      otpViewClicked();
    }

    private void otpViewClicked() {
        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification_id, otp);

                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void initiateotp() {

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                        dialog.dismiss();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verify_id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verify_id, forceResendingToken);
                        dialog.dismiss();
                        verification_id = verify_id;

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                        binding.otpView.requestFocus();


                    }
                }).build();


        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            database.getReference().child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                        User user = snapshot1.getValue(User.class);

                                        if (user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                            userExist = 1;
                                        }

                                    }


                                    if (userExist == 1) {
                                        Intent intent = new Intent(OTPActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    } else {
                                        startActivity(new Intent(OTPActivity.this, SetupProfileActivity.class));
                                        finishAffinity();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {

                            Toast.makeText(getApplicationContext(), "Failed to sign in", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}
//        dialog= new ProgressDialog(this);
//        dialog.setMessage("Sending OTP...");
//        dialog.setCancelable(false);
//        dialog.show();
//
//
//        auth= FirebaseAuth.getInstance();
//
//        String phoneNumber = getIntent().getStringExtra("phoneNumber");
//
//        binding.phonelvl.setText("Verify "+phoneNumber);
//
//        PhoneAuthOptions options= PhoneAuthOptions.newBuilder(auth)
//                .setPhoneNumber(phoneNumber)
//                .setTimeout(60L, TimeUnit.SECONDS)
//                .setActivity(OTPActivity.this)
//                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                    @Override
//                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                        Toast.makeText(OTPActivity.this, "Succeed to send", Toast.LENGTH_SHORT).show();
//
//                    }
//
//                    @Override
//                    public void onVerificationFailed(@NonNull FirebaseException e) {
//                     Toast.makeText(OTPActivity.this, "Failed to send", Toast.LENGTH_SHORT).show();
//
//                        dialog.dismiss();
//                    }
//
//                    @Override
//                    public void onCodeSent(@NonNull String verify_id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                        super.onCodeSent(verify_id, forceResendingToken);
//                        dialog.dismiss();
//                        verification_id= verify_id;
//
//                        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
//                        binding.otpView.requestFocus();
//
//
//
//                    }
//                }).build();
//
//        PhoneAuthProvider.verifyPhoneNumber(options);
//
//        binding.otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
//            @Override
//            public void onOtpCompleted(String otp) {
//                PhoneAuthCredential credential= PhoneAuthProvider.getCredential(verification_id,otp);
//
//                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//
//                            Intent intent= new Intent(OTPActivity.this,SetupProfileActivity.class);
//                            startActivity(intent);
//                            finishAffinity();
//
//
//
//                        }else
//                            {
//                                Toast.makeText(OTPActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                            }
//                    }
//                });
//
//
//            }
//        });


