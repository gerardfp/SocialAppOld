package com.company.socialapp.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.company.socialapp.AppFragment;
import com.company.socialapp.GlideApp;
import com.company.socialapp.R;
import com.company.socialapp.model.Post;


public class MediaFragment extends AppFragment {

    ImageView mImageView;
    VideoView mVideoView;

    public MediaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = view.findViewById(R.id.imageView);
        mVideoView = view.findViewById(R.id.videoView);

        appViewModel.selectedPost.observe(getViewLifecycleOwner(), new Observer<Post>() {
            @Override
            public void onChanged(Post post) {
                if ("video".equals(post.mediaType) || "audio".equals(post.mediaType)) {
                    MediaController mc = new MediaController(requireContext());
                    mc.setAnchorView(mVideoView);
                    mVideoView.setMediaController(mc);
                    mVideoView.setVideoPath(post.mediaUrl);
                    mVideoView.start();
                } else if ("image".equals(post.mediaType)) {
                    GlideApp.with(requireView()).load(post.mediaUrl).into(mImageView);
                }
            }
        });
    }
}
