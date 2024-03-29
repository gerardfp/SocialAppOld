package com.company.socialapp;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import androidx.core.content.FileProvider;

public class MediaFiles {
    public enum Type {IMAGE, VIDEO, AUDIO};

    public static class UriPathFile {
        public File file;
        public Uri uri;
        public String path;

        public UriPathFile(File file, Uri uri, String path) {
            this.file = file;
            this.uri = uri;
            this.path = path;
        }
    }

    public static UriPathFile createFile(Context context, MediaFiles.Type type) {
        UriPathFile uriPathFile = null;
        try {
            String prefix = "";
            String suffix = "";
            String directory = null;
            switch (type) {
                case IMAGE:
                    prefix = "img";
                    suffix = ".jpg";
                    directory = Environment.DIRECTORY_PICTURES; // files/pictures
                    break;
                case VIDEO:
                    prefix = "vid";
                    suffix = ".mp4";
                    directory = Environment.DIRECTORY_MOVIES; // files/movies
                    break;
                case AUDIO:
                    prefix = "aud";
                    suffix = ".3gp";
                    directory = "files/audios";
                    break;
            }
            File storageDir = context.getExternalFilesDir(directory);
            File file = File.createTempFile(prefix, suffix, storageDir);
            Uri fileUri = FileProvider.getUriForFile(context, "com.example.gerard.socialapp.fileprovider", file);

            uriPathFile = new UriPathFile(file, fileUri, file.getAbsolutePath());
        } catch (IOException e){

        }

        return uriPathFile;
    }
}
