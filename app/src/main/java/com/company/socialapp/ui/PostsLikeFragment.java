package com.company.socialapp.ui;


import com.google.firebase.firestore.Query;

public class PostsLikeFragment extends PostsHomeFragment {
    Query setQuery(){
        return db.collection("posts").whereEqualTo("likes." + mUser.getUid(), true).limit(50);
    }
}
