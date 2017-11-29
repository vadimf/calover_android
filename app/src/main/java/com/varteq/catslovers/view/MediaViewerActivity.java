package com.varteq.catslovers.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.varteq.catslovers.R;

public class MediaViewerActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_NO_MEDIA = 0;
    public static final int MEDIA_TYPE_PHOTO = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    ImageButton backButton;
    FrameLayout forwardButton;
    ImageView imageView;
    VideoView videoView;
    TextView captionsTextView;

    MediaController mc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);
        backButton = findViewById(R.id.backButton);
        forwardButton = findViewById(R.id.forwardButton);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        captionsTextView = findViewById(R.id.captionsTextView);

        backButton.setOnClickListener(view -> onBackPressed());

        Intent intent = getIntent();
        int mediaType = intent.getIntExtra("mediaType", 0);
        Uri mediaUri = Uri.parse(intent.getStringExtra("mediaUri"));
        switch (mediaType) {
            case MEDIA_TYPE_PHOTO:
                videoView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(mediaUri)
                        .into(imageView);
                break;
            case MEDIA_TYPE_VIDEO:
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);

                mc = new MediaController(this);
                mc.setAnchorView(videoView);
                mc.setMediaPlayer(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoURI(mediaUri);
                videoView.requestFocus();
                videoView.start();
        }

    }


}
