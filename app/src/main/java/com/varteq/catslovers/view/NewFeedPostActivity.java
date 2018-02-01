package com.varteq.catslovers.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.qb.imagepick.ImagePickHelper;
import com.varteq.catslovers.utils.qb.imagepick.OnImagePickedListener;
import com.varteq.catslovers.view.presenter.NewFeedPostPresenter;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewFeedPostActivity extends BaseActivity implements OnImagePickedListener {

    private final int THUMBSIZE = 500;
    private final int REQUEST_CODE_ATTACHMENT = 1;
    private String TAG = CatProfileActivity.class.getSimpleName();

    @BindView(R.id.post_editText)
    EditText postEditText;
    @BindView(R.id.feedImage)
    ImageView feedImage;
    @BindView(R.id.progress_activity_new_feed)
    ProgressBar progressBar;

    private NewFeedPostPresenter presenter;
    private File mediaFile;
    private Bitmap preview;
    private FeedPost.FeedPostType mediaType = FeedPost.FeedPostType.TEXT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed_post);

        ButterKnife.bind(this);

        presenter = new NewFeedPostPresenter(this);
        //progressBar.setVisibility(View.GONE);

        //set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected View getSnackbarAnchorView() {
        return (View) postEditText.getParent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_new_feed, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_add_photo:
                Log.d(TAG, "app_bar_add_photo");
                new ImagePickHelper().pickAnImageOrVideo(this, REQUEST_CODE_ATTACHMENT);
                return true;
            case R.id.app_bar_save:
                Log.d(TAG, "app_bar_save");
                //progressBar.setVisibility(View.VISIBLE);
                showWaitDialog();
                presenter.createFeed(postEditText.getText().toString(), mediaFile, preview, mediaType);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        switch (requestCode) {
            case REQUEST_CODE_ATTACHMENT:
                if (file != null) {
                    mediaFile = file;
                    mediaType = FeedPost.FeedPostType.PICTURE;

                    preview = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getPath()),
                            THUMBSIZE, THUMBSIZE);
                    feedImage.setImageBitmap(preview);
                    feedImage.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onImagesPicked(int requestCode, List<File> file) {

    }

    @Override
    public void onVideoPicked(int requestCode, File file, Bitmap preview) {
        if (file == null) return;
        mediaType = FeedPost.FeedPostType.VIDEO;
        mediaFile = file;
        this.preview = preview;

        feedImage.setImageBitmap(preview);
        feedImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {

    }

    @Override
    public void onImagePickClosed(int requestCode) {

    }

    public void createdSuccessfully(){
        //progressBar.setVisibility(View.GONE);
        hideWaitDialog();
        finish();
    }
}
