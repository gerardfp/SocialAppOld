package com.company.socialapp.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.company.socialapp.GlideApp;
import com.company.socialapp.MediaFiles;
import com.company.socialapp.model.Post;
import com.company.socialapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class NewPostFragment extends Fragment {
    NavController navController;

    static final int RC_IMAGE_TAKE = 8000;
    static final int RC_VIDEO_TAKE = 8001;
    static final int RC_IMAGE_PICK = 9000;
    static final int RC_VIDEO_PICK = 9001;
    static final int RC_AUDIO_PICK = 9002;

    static final int REQUEST_RECORD_AUDIO_PERMISSION = 1212;
    private boolean permissionToRecordAccepted = false;

    EditText mPostTextField;
    ImageView imagePreview;
    Button mPublishButton;
    Button mCameraImageButton;
    Button mCameraVideoButton;
    Button mMicButton;
    Button mImageButton;
    Button mVideoButton;
    Button mAudioButton;

    Uri mFileUri;

    Uri mediaUri;
    String mediaType;

    FirebaseFirestore db;
    FirebaseUser mUser;

    boolean recording = false;
    private MediaRecorder mRecorder = null;

    public NewPostFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mPostTextField = view.findViewById(R.id.postText);
        imagePreview = view.findViewById(R.id.image);
        mPublishButton = view.findViewById(R.id.publish);
        mImageButton = view.findViewById(R.id.btnImage);
        mVideoButton = view.findViewById(R.id.btnVideo);
        mAudioButton = view.findViewById(R.id.btnAudio);
        mCameraImageButton = view.findViewById(R.id.btnCameraImage);
        mCameraVideoButton = view.findViewById(R.id.btnCameraVideo);
        mMicButton = view.findViewById(R.id.btnMic);

        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });

        mCameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mCameraVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakeVideoIntent();
            }
        });

        mMicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(recording){
                    stopRecording();
                } else {
                    startRecording();
                }
                recording = !recording;
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RC_IMAGE_PICK);
            }
        });

        mVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI), RC_VIDEO_PICK);
            }
        });

        mAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI), RC_AUDIO_PICK);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IMAGE_TAKE && resultCode == RESULT_OK) {
            mediaUri = mFileUri;
            mediaType = "image";
            GlideApp.with(this).load(mediaUri).into(imagePreview);
        } else if (requestCode == RC_VIDEO_TAKE && resultCode == RESULT_OK) {
            mediaUri = mFileUri;
            mediaType = "video";
            GlideApp.with(this).load(mediaUri).into(imagePreview);
        }

        else if(data != null) {
            if (requestCode == RC_IMAGE_PICK) {
                mediaUri = data.getData();
                mediaType = "image";
                GlideApp.with(this).load(mediaUri).into(imagePreview);
            } else if (requestCode == RC_VIDEO_PICK) {
                mediaUri = data.getData();
                mediaType = "video";
                GlideApp.with(this).load(mediaUri).into(imagePreview);
            } else if (requestCode == RC_AUDIO_PICK) {
                mediaUri = data.getData();
                mediaType = "audio";
                GlideApp.with(this).load(mediaUri).into(imagePreview);
            }
        }
    }

    void submitPost(){
        final String postText = mPostTextField.getText().toString();

        if(postText.isEmpty()){
            mPostTextField.setError("Required");
            return;
        }

        mPublishButton.setEnabled(false);

        if (mediaType == null) {
            writeNewPost(postText, null);
        } else {
            uploadAndWriteNewPost(postText);
        }

    }

    private void writeNewPost(String postText, String mediaUrl) {
        Post post = new Post(mUser.getUid(), mUser.getDisplayName(), mUser.getPhotoUrl().toString(), postText, mediaUrl, mediaType);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        navController.popBackStack();
                    }
                });
    }

    private void uploadAndWriteNewPost(final String postText){
        if(mediaType != null) {
            FirebaseStorage.getInstance().getReference(mediaType + "/" + UUID.randomUUID().toString() + mediaUri.getLastPathSegment()).putFile(mediaUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    return task.getResult().getStorage().getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String downloadUri = task.getResult().toString();
                        writeNewPost(postText, downloadUri);
                    }
                }
            });
        }
    }



    private void dispatchTakePictureIntent() {

        Uri fileUri = null;
        try {
            fileUri = MediaFiles.createFile(requireContext(), MediaFiles.Type.IMAGE).uri;
        } catch (IOException ex) {
            // No se pudo crear el fichero
        }

        if (fileUri != null) {
            mFileUri = fileUri;

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, RC_IMAGE_TAKE);
        }
    }

    private void dispatchTakeVideoIntent() {

        Uri fileUri = null;
        try {
            fileUri = MediaFiles.createFile(requireContext(), MediaFiles.Type.VIDEO).uri;
        } catch (IOException ex) {
            // No se pudo crear el fichero
        }

        if (fileUri != null) {
            mFileUri = fileUri;

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, RC_VIDEO_TAKE);
        }
    }

    void startRecording(){
        MediaFiles.UriPathFile file = null;
        try {
            file = MediaFiles.createFile(requireContext(), MediaFiles.Type.AUDIO);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    void stopRecording(){
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }
}
