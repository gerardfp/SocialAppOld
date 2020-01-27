package com.company.socialapp;

import android.app.Application;

import com.company.socialapp.model.Post;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class AppViewModel extends AndroidViewModel {
    public MutableLiveData<Post> selectedPost = new MutableLiveData<>();

    public AppViewModel(@NonNull Application application) {
        super(application);
    }
}
