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
import com.varteq.catslovers.model.FeedPost;

public class MediaViewerActivity extends AppCompatActivity {

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
        FeedPost.FeedPostType mediaType = (FeedPost.FeedPostType) intent.getSerializableExtra("mediaType");
        Uri mediaUri = Uri.parse(intent.getStringExtra("mediaUri"));
        switch (mediaType) {
            case PICTURE:
                videoView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(mediaUri)
                        .into(imageView);
                break;
            case VIDEO:
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);

                mc = new MediaController(this);
                mc.setAnchorView(videoView);
                mc.setMediaPlayer(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoURI(mediaUri);
                videoView.requestFocus();
                videoView.start();
                break;
        }

    }


}
