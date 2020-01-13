package com.company.socialapp.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.company.socialapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterFragment extends Fragment implements View.OnClickListener {

    private NavController navController;

    private FirebaseAuth mAuth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button verifyEmailButton;
    private Button createAccountButton;

    public RegisterFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        verifyEmailButton = view.findViewById(R.id.verifyEmailButton);
        createAccountButton = view.findViewById(R.id.createAccountButton);

        verifyEmailButton.setOnClickListener(this);
        createAccountButton.setOnClickListener(this);

        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if(firebaseUser != null){
            navController.navigate(R.id.postsHomeFragment);
        }
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUI(mAuth.getCurrentUser());
                        } else {
                            Log.w("ABCD", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            updateUI(mAuth.getCurrentUser());
                        } else {
                            Log.w("ABCD", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    private void sendEmailVerification() {
       verifyEmailButton.setEnabled(false);

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        verifyEmailButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(requireActivity(), "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("ABCD", "sendEmailVerification", task.getException());
                            Toast.makeText(requireActivity(), "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.createAccountButton:
                createAccount(emailEditText.getText().toString(), passwordEditText.getText().toString());
                break;
            case R.id.verifyEmailButton:
                sendEmailVerification();
                break;
        }
    }
}
