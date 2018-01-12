package com.varteq.catslovers.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.utils.PostMediaDownloader;

import java.io.File;

public class MediaViewerActivity extends BaseActivity {

    private static String MEDIA_KEY = "media";

    ImageView imageView;
    VideoView videoView;
    TextView captionsTextView;
    ProgressBar progressBar;

    MediaController mc;
    private PostMediaDownloader downloadTask;

    public static void startActivity(Context context, FeedPost feed) {
        Intent intent = new Intent(context, MediaViewerActivity.class);
        intent.putExtra(MEDIA_KEY, feed);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        captionsTextView = findViewById(R.id.captionsTextView);
        progressBar = findViewById(R.id.progress_activity_media);


        Intent intent = getIntent();
        FeedPost media = (FeedPost) intent.getSerializableExtra(MEDIA_KEY);
        if (imageView.getVisibility() == View.VISIBLE && imageView.getDrawable() != null)
            return;

        progressBar.setVisibility(View.VISIBLE);
        downloadTask = new PostMediaDownloader(media, new PostMediaDownloader.OnMediaLoaded() {
            @Override
            public void onImageLoaded(Bitmap bitmap) {
                progressBar.setVisibility(View.GONE);
                videoView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onVideoLoaded(File file) {
                progressBar.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);

                mc = new MediaController(MediaViewerActivity.this);
                mc.setAnchorView(videoView);
                mc.setMediaPlayer(videoView);
                videoView.setMediaController(mc);
                videoView.setVideoPath(file.getPath());
                videoView.requestFocus();
                videoView.start();
            }
        });
        /*switch (media.getType()) {
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
        }*/
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (downloadTask != null)
            downloadTask.cancelLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadTask != null)
            downloadTask.cancelLoading();
    }
}
