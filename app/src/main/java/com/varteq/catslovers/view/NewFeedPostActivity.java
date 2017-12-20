package com.varteq.catslovers.view;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.presenter.CatProfilePresenter;
import com.varteq.catslovers.view.presenter.NewFeedPostPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewFeedPostActivity extends PhotoPickerActivity {

    private String TAG = CatProfileActivity.class.getSimpleName();

    @BindView(R.id.post_editText)
    EditText postEditText;
    @BindView(R.id.feedImage)
    ImageView feedImage;

    private NewFeedPostPresenter presenter;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed_post);

        ButterKnife.bind(this);

        presenter = new NewFeedPostPresenter(this);

        //set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private MenuItem addPhoto;
    private MenuItem saveFeed;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_new_feed, menu);

        addPhoto = menu.findItem(R.id.app_bar_add_photo);
        saveFeed = menu.findItem(R.id.app_bar_save);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_add_photo:
                Log.d(TAG, "app_bar_add_photo");
                pickPhotoWithPermission(getString(R.string.select_cat_photo));
                return true;
            case R.id.app_bar_save:
                Log.d(TAG, "app_bar_save");
                presenter.createFeed(this, postEditText.getText().toString(), imageUri);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void finishActivity(){
        finish();
    }


    @Override
    protected void onImageSelected(Uri uri) {
        super.onImageSelected(uri);
        if (null != uri) {
            imageUri = uri;
            feedImage.setImageURI(imageUri);
            feedImage.setVisibility(View.VISIBLE);
        }
    }

}
