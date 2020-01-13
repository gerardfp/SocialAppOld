package com.company.socialapp.ui;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.company.socialapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInFragment extends Fragment {

    private NavController navController;

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN_WITH_GOOGLE = 12345;
    private FirebaseAuth mAuth;

    SignInButton signInButton;
    ProgressBar signInProgressBar;

    public SignInFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        signInButton = view.findViewById(R.id.signInButton);
        signInProgressBar = view.findViewById(R.id.signInProgressBar);

        mAuth = FirebaseAuth.getInstance();

        signInProgressBar.setVisibility(View.GONE);

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());

        firebaseAuthWithGoogle(GoogleSignIn.getLastSignedInAccount(requireContext()));

        view.findViewById(R.id.signInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if(firebaseUser != null){
            navController.navigate(R.id.postsHomeFragment);
        }
    }

    private void signIn() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN_WITH_GOOGLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_WITH_GOOGLE) {
            try {
                firebaseAuthWithGoogle(GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class));
            } catch (ApiException e) {
                Log.e("ABCD", "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if(acct == null) return;

        signInProgressBar.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);

        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.e("ABCD", "signInWithCredential:success");
                            updateUI(mAuth.getCurrentUser());
                        } else {
                            Log.e("ABCD", "signInWithCredential:failure", task.getException());
                            signInProgressBar.setVisibility(View.GONE);
                            signInButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}
