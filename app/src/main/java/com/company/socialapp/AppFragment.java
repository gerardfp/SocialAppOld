package com.company.socialapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public abstract class AppFragment extends Fragment {
    public AppViewModel appViewModel;
    public NavController navController;
    public FirebaseFirestore db;
    public FirebaseUser user;
    public FirebaseStorage storage;

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appViewModel = ViewModelProviders.of(requireActivity()).get(AppViewModel.class);
        navController = Navigation.findNavController(view);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        Log.e("NBS", " ");
        for(int is : navController.saveState().getIntArray("android-support-nav:controller:backStackIds")){
            Log.e("NBS", "> " + view.getContext().getResources().getResourceEntryName(is));
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navController.popBackStack();
                    }
                });
    }
}
