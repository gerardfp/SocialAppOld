package com.company.socialapp.ui;


import com.google.firebase.firestore.Query;

public class PostsMyFragment extends PostsHomeFragment {
    Query setQuery(){
        return db.collection("posts").whereEqualTo("uid", mUser.getUid()).limit(50);
    }
}
