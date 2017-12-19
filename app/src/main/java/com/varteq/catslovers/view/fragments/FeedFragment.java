package com.varteq.catslovers.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.FeedPost;
import com.varteq.catslovers.view.adapters.FeedAdapter;
import com.varteq.catslovers.view.presenter.FeedPresenter;

import java.util.ArrayList;
import java.util.List;


public class FeedFragment extends Fragment {

    RecyclerView feedRecyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    List<FeedPost> feedList;
    FeedPresenter presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new FeedPresenter(this);
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedList = new ArrayList<>();

        feedRecyclerView = view.findViewById(R.id.feedRecyclerView);

        adapter = new FeedAdapter(feedList, getContext());
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        feedRecyclerView.setAdapter(adapter);
        feedRecyclerView.setLayoutManager(layoutManager);

        /*// video
        feedList.add(new FeedPost(String.valueOf(5), new Date(), MediaViewerActivity.MEDIA_TYPE_VIDEO,
                Uri.parse("https://lh3.googleusercontent.com/9aCU-PzSu7ZPN-ihwxBuuYHMl-BX09fWvZEcXNBo1h52rM--gB--88EgnoFQRI0cyX4=h355"),
                Uri.parse("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4"),
                Uri.parse("https://images-na.ssl-images-amazon.com/images/S/sgp-catalog-images/region_US/paramount-35065-Full-Image_GalleryBackground-en-US-1483994508505._RI_SX940_.jpg"),
                "Greg Winters", "Look at our newest family additions!!!\nMissy, Chloe and Coco <3<3<3", 158

        ));
        // photo
        feedList.add(new FeedPost(String.valueOf(5), new Date(), MediaViewerActivity.MEDIA_TYPE_PHOTO,
                Uri.parse("http://www.cat-n-canary.com/wp-content/uploads/2016/11/cat15.jpeg"),
                Uri.parse("http://www.cat-n-canary.com/wp-content/uploads/2016/11/cat15.jpeg"),
                Uri.parse("https://images-na.ssl-images-amazon.com/images/S/sgp-catalog-images/region_US/paramount-35065-Full-Image_GalleryBackground-en-US-1483994508505._RI_SX940_.jpg"),
                "Greg Winters", "Look at our newest family additions!!!\nMissy, Chloe and Coco <3<3<3", 158

        ));

        // no media
        feedList.add(new FeedPost(String.valueOf(5), new Date(), MediaViewerActivity.MEDIA_TYPE_NO_MEDIA,
                Uri.parse("https://tinyclipart.com/resource/man/man-3.jpg"),
                "Brian Adams", "Has anyone tried the new anti flee powders from XXX pharmacy??\nOscar is due for his treatment...", 50

        ));
        adapter.notifyDataSetChanged();*/
        presenter.loadFeeds();
    }

    public void feedsLoaded(List<FeedPost> feeds) {
        feedList.clear();
        feedList.addAll(feeds);
        adapter.notifyDataSetChanged();
    }
}
