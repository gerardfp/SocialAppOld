package com.company.socialapp.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.company.socialapp.AppFragment;
import com.company.socialapp.GlideApp;
import com.company.socialapp.R;
import com.company.socialapp.model.Post;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


public class PostsHomeFragment extends AppFragment {

    public PostsHomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.newPostFragment);
            }
        });

        RecyclerView postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(setQuery(), Post.class)
                .setLifecycleOwner(this)
                .build();

        postsRecyclerView.setAdapter(new PostsAdapter(options));
    }

    class PostsAdapter extends FirestoreRecyclerAdapter<Post, PostsAdapter.PostsViewHolder>{
        PostsAdapter(@NonNull FirestoreRecyclerOptions<Post> options) { super(options); }

        @NonNull
        @Override
        public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PostsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_post, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull final Post post) {
            final String postKey = getSnapshots().getSnapshot(position).getId();

            holder.authorTextView.setText(post.author);
            GlideApp.with(PostsHomeFragment.this).load(post.authorPhotoUrl).circleCrop().into(holder.photoImageView);

            if (post.likes.containsKey(user.getUid())) {
                holder.likeImageView.setImageResource(R.drawable.like_on);
                holder.numLikesTextView.setTextColor(getResources().getColor(R.color.red, requireActivity().getTheme()));
            } else {
                holder.likeImageView.setImageResource(R.drawable.like_off);
                holder.numLikesTextView.setTextColor(getResources().getColor(R.color.grey, requireActivity().getTheme()));
            }

            holder.contentTextView.setText(post.content);

            if (post.mediaUrl != null) {
                holder.imageImageView.setVisibility(View.VISIBLE);
                if ("audio".equals(post.mediaType)) {
                    GlideApp.with(requireView()).load(R.drawable.audio).centerCrop().into(holder.imageImageView);
                } else {
                    GlideApp.with(requireView()).load(post.mediaUrl).centerCrop().into(holder.imageImageView);
                }
                holder.imageImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        appViewModel.selectedPost.setValue(post);
                        navController.navigate(R.id.mediaFragment);
                    }
                });
            } else {
                holder.imageImageView.setVisibility(View.GONE);
            }

            holder.numLikesTextView.setText(String.valueOf(post.likes.size()));

            holder.likeLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (post.likes.containsKey(user.getUid())) {
                        db.collection("posts").document(postKey).update("likes." + user.getUid(), FieldValue.delete());
                    } else {
                        db.collection("posts").document(postKey).update("likes." + user.getUid(), true);
                    }
                }
            });
        }

        class PostsViewHolder extends RecyclerView.ViewHolder{
            ImageView photoImageView, imageImageView, likeImageView;
            TextView authorTextView, contentTextView, numLikesTextView;
            LinearLayout likeLinearLayout;

            PostsViewHolder(@NonNull View itemView) {
                super(itemView);

                photoImageView = itemView.findViewById(R.id.photoImageView);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                contentTextView = itemView.findViewById(R.id.contentTextView);
                imageImageView = itemView.findViewById(R.id.imageImageView);
                likeImageView = itemView.findViewById(R.id.likeImageView);
                numLikesTextView = itemView.findViewById(R.id.numLikesTextView);
                likeLinearLayout = itemView.findViewById(R.id.likeLinearLayout);
            }
        }
    }

    Query setQuery(){
        return db.collection("posts").limit(50);
    }
}
