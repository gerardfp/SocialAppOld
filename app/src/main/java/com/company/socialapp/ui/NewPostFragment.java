package com.company.socialapp.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.company.socialapp.AppFragment;
import com.company.socialapp.GlideApp;
import com.company.socialapp.MediaFiles;
import com.company.socialapp.R;
import com.company.socialapp.model.Post;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import static android.app.Activity.RESULT_OK;

public class NewPostFragment extends AppFragment {
    private static final int RC_IMAGE_TAKE = 8000;
    private static final int RC_VIDEO_TAKE = 8001;
    private static final int RC_IMAGE_PICK = 9000;
    private static final int RC_VIDEO_PICK = 9001;
    private static final int RC_AUDIO_PICK = 9002;

    private static final int RC_PERMISSIONS = 1212;
    private boolean permissionsAcepted = false;

    private EditText contentEditText;
    private ImageView previewImageView;
    private Button publishButton;
    private Button cameraImageButton;
    private Button cameraVideoButton;
    private Button recordAudioButton;
    private Button galleryImageButton;
    private Button galleryVideoButton;
    private Button galleryAudioButton;

    private Uri fileUri;
    private Uri mediaUri;
    private String mediaType;

    private boolean recording = false;
    private MediaRecorder mRecorder = null;

    public NewPostFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contentEditText = view.findViewById(R.id.contentEditText);
        previewImageView = view.findViewById(R.id.previewImageView);
        publishButton = view.findViewById(R.id.publishButton);
        galleryImageButton = view.findViewById(R.id.galleryImageButton);
        galleryVideoButton = view.findViewById(R.id.galleryVideoButton);
        galleryAudioButton = view.findViewById(R.id.galleryAudioButton);
        cameraImageButton = view.findViewById(R.id.cameraImageButton);
        cameraVideoButton = view.findViewById(R.id.cameraVideoButton);
        recordAudioButton = view.findViewById(R.id.recordAudioButton);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });

        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) takePicture();
            }
        });

        cameraVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) takeVideo();
            }
        });

        recordAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(recording){
                    stopRecording();
                } else {
                    if(checkPermissions()) startRecording();
                }
                recording = !recording;
            }
        });

        galleryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RC_IMAGE_PICK);
            }
        });

        galleryVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI), RC_VIDEO_PICK);
            }
        });

        galleryAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI), RC_AUDIO_PICK);
            }
        });
    }

    boolean checkPermissions(){
        if(!permissionsAcepted){
            requestPermissions();
        }
        return permissionsAcepted;
    }

    private void requestPermissions(){
        if(ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSIONS);
        } else {
            permissionsAcepted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS) {
            permissionsAcepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;

        if (requestCode == RC_IMAGE_TAKE) {
            mediaUri = fileUri;
            mediaType = "image";
            GlideApp.with(this).load(mediaUri).into(previewImageView);
        } else if (requestCode == RC_VIDEO_TAKE) {
            mediaUri = fileUri;
            mediaType = "video";
            GlideApp.with(this).load(mediaUri).into(previewImageView);
        } else if (requestCode == RC_IMAGE_PICK) {
            mediaUri = data.getData();
            mediaType = "image";
            GlideApp.with(this).load(mediaUri).into(previewImageView);
        } else if (requestCode == RC_VIDEO_PICK) {
            mediaUri = data.getData();
            mediaType = "video";
            GlideApp.with(this).load(mediaUri).into(previewImageView);
        } else if (requestCode == RC_AUDIO_PICK) {
            mediaUri = data.getData();
            mediaType = "audio";
            GlideApp.with(this).load(mediaUri).into(previewImageView);
        }
    }

    private void submitPost(){
        final String postText = contentEditText.getText().toString();

        if(postText.isEmpty()){
            contentEditText.setError("Required");
            return;
        }

        publishButton.setEnabled(false);

        if (mediaType == null) {
            writeNewPost(postText, null);
        } else {
            uploadAndWriteNewPost(postText);
        }

    }

    private void writeNewPost(String postText, String mediaUrl) {
        db.collection("posts")
                .add(new Post(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), postText, mediaUrl, mediaType))
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        navController.popBackStack();
                    }
                });
    }

    private void uploadAndWriteNewPost(final String postText){
        if(mediaType != null) {
            storage.getReference(mediaType + "/" + UUID.randomUUID())
                    .putFile(mediaUri)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            return task.getResult().getStorage().getDownloadUrl();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            writeNewPost(postText, uri.toString());
                        }
                    });
        }
    }



    private void takePicture() {
        Uri fileUri = MediaFiles.createFile(requireContext(), MediaFiles.Type.IMAGE).uri;

        if (fileUri != null) {
            this.fileUri = fileUri;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, RC_IMAGE_TAKE);
        }
    }

    private void takeVideo() {
        Uri fileUri =  MediaFiles.createFile(requireContext(), MediaFiles.Type.VIDEO).uri;

        if (fileUri != null) {
            this.fileUri = fileUri;

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, RC_VIDEO_TAKE);
        }
    }

    private void startRecording(){
        MediaFiles.UriPathFile file = MediaFiles.createFile(requireContext(), MediaFiles.Type.AUDIO);

        if(file != null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(file.path);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                mRecorder.prepare();
            } catch (IOException e) {

            }

            mediaType = "audio";
            mediaUri = file.uri;
            mRecorder.start();
        }
    }

    private void stopRecording(){
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
