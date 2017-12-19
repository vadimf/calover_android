package com.varteq.catslovers.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import com.varteq.catslovers.R;
import com.varteq.catslovers.view.presenter.NewFeedPostPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewFeedPostActivity extends AppCompatActivity {

    @BindView(R.id.post_editText)
    EditText postEditText;
    @BindView(R.id.create_post_button)
    Button createPostButton;
    private NewFeedPostPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_feed_post);

        ButterKnife.bind(this);

        presenter = new NewFeedPostPresenter(this);
    }

    @OnClick(R.id.create_post_button)
    void createPost() {
        presenter.createFeed(postEditText.getText().toString());
    }
}
