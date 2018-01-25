package com.varteq.catslovers.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.CatProfileActivity;
import com.varteq.catslovers.view.adapters.CatsListAdapter;
import com.varteq.catslovers.view.presenter.CatsPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CatsFragment extends Fragment {

    public final static int CATS_SECTION_PRIVATE = 0;
    public final static int CATS_SECTION_PUBLIC = 1;
    public final static int CATS_SECTION_FRIENDS = 2;
    public final static int CATS_SECTION_EXPLORE = 3;

    public static final String MY_CATS_KEY = "my_cats";
    private String TAG = CatsFragment.class.getSimpleName();

    @BindView(R.id.cats_RecyclerView)
    RecyclerView catsRecyclerView;
    @BindView(R.id.button_slider_public)
    Button publicSliderButton;
    @BindView(R.id.button_slider_private)
    Button privateSliderButton;
    @BindView(R.id.button_slider_friends)
    Button friendsSliderButton;
    @BindView(R.id.button_slider_explore)
    Button exploreSliderButton;
    private HashMap<String, List<CatProfile>> catsHashMap;
    private CatsListAdapter catsListAdapter;
    private List<CatProfile> myCatsList = new ArrayList<>();
    private boolean listUpdated;
    private CatsPresenter presenter;

    final private int SEEKBAR_STEPS_COUNT = 3;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cats, container, false);
        ButterKnife.bind(this, view);

        catsHashMap = new HashMap<>();

        presenter.getCats(CATS_SECTION_PRIVATE);
        listUpdated = true;

        catsListAdapter = new CatsListAdapter(catsHashMap, this::onCatClicked);
        catsRecyclerView.setAdapter(catsListAdapter);
        catsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void onCatClicked(CatProfile catProfile) {
        CatProfileActivity.startInViewMode(getActivity(), catProfile);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        seekBar.setMax(SEEKBAR_STEPS_COUNT);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 0:
                        presenter.getCats(CATS_SECTION_PRIVATE);
                        break;
                    case 1:
                        presenter.getCats(CATS_SECTION_PUBLIC);
                        break;
                    case 2:
                        presenter.getCats(CATS_SECTION_FRIENDS);
                        break;
                    case 3:
                        presenter.getCats(CATS_SECTION_EXPLORE);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new CatsPresenter(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        listUpdated = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!listUpdated) {
            presenter.getCats(seekBar.getProgress());
            listUpdated = true;
        }
    }


    @OnClick(R.id.button_slider_public)
    void publicSliderButtonClicked(View view) {
        clearSliderButtonSelection();
        selectButton((Button) view);
        presenter.getCats(CATS_SECTION_PUBLIC);
    }

    @OnClick(R.id.button_slider_private)
    void privateSliderButtonClicked(View view) {
        clearSliderButtonSelection();
        selectButton((Button) view);
        presenter.getCats(CATS_SECTION_PRIVATE);
    }

    @OnClick(R.id.button_slider_friends)
    void friendsSliderButtonClicked(View view) {
        clearSliderButtonSelection();
        selectButton((Button) view);
        presenter.getCats(CATS_SECTION_FRIENDS);
    }

    @OnClick(R.id.button_slider_explore)
    void exploreSliderButtonClicked(View view) {
        clearSliderButtonSelection();
        selectButton((Button) view);
        presenter.getCats(CATS_SECTION_EXPLORE);
    }

    private void selectButton(Button button) {
        button.setTextColor(getResources().getColor(R.color.white));
        button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    private void clearSliderButtonSelection() {
        publicSliderButton.setBackgroundColor(getResources().getColor(R.color.white));
        publicSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        privateSliderButton.setBackgroundColor(getResources().getColor(R.color.white));
        privateSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        friendsSliderButton.setBackgroundColor(getResources().getColor(R.color.white));
        friendsSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        exploreSliderButton.setBackgroundColor(getResources().getColor(R.color.white));
        exploreSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    public void catsLoaded(List<CatProfile> catProfiles, int catsSection) {

        listUpdated = true;
        catsHashMap.clear();
        myCatsList.clear();

        char letter = Character.toUpperCase(catProfiles.get(0).getPetName().charAt(0));
        ArrayList<CatProfile> list = new ArrayList<>();

        Integer userId = Integer.parseInt(Profile.getUserId(getContext()));
        switch (catsSection) {
            case CatsFragment.CATS_SECTION_PRIVATE:
                for (CatProfile cat : catProfiles) {
                    if (cat.getType() != null && cat.getUserRole() != null &&
                            cat.getType().equals(CatProfile.Status.PET) && cat.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                        if (letter == Character.toUpperCase(cat.getPetName().charAt(0)))
                            list.add(cat);
                        else {
                            if (!list.isEmpty())
                                catsHashMap.put(String.valueOf(letter), list);
                            letter = Character.toUpperCase(cat.getPetName().charAt(0));
                            list = new ArrayList<>();
                            list.add(cat);
                        }
                    }
                }
                break;
            case CatsFragment.CATS_SECTION_PUBLIC:
                for (CatProfile cat : catProfiles) {
                    if (cat.getUserId() != null
                            && cat.getUserId().equals(userId)) {
                        if (letter == Character.toUpperCase(cat.getPetName().charAt(0)))
                            list.add(cat);
                        else {
                            if (!list.isEmpty())
                                catsHashMap.put(String.valueOf(letter), list);
                            letter = Character.toUpperCase(cat.getPetName().charAt(0));
                            list = new ArrayList<>();
                            list.add(cat);
                        }
                    }
                }
                break;
            case CatsFragment.CATS_SECTION_FRIENDS:
                for (CatProfile cat : catProfiles) {
                    if ((cat.getUserId() == null || (cat.getUserId() != null && !cat.getUserId().equals(userId)))
                            && cat.getFeedStationStatus() != null
                            && cat.getFeedStationStatus().equals("joined")
                            ) {
                        if (letter == Character.toUpperCase(cat.getPetName().charAt(0)))
                            list.add(cat);
                        else {
                            if (!list.isEmpty())
                                catsHashMap.put(String.valueOf(letter), list);
                            letter = Character.toUpperCase(cat.getPetName().charAt(0));
                            list = new ArrayList<>();
                            list.add(cat);
                        }
                    }
                }
                break;
            case CatsFragment.CATS_SECTION_EXPLORE:
                for (CatProfile cat : catProfiles) {
                    if (letter == Character.toUpperCase(cat.getPetName().charAt(0)))
                        list.add(cat);
                    else {
                        if (!list.isEmpty())
                            catsHashMap.put(String.valueOf(letter), list);
                        letter = Character.toUpperCase(cat.getPetName().charAt(0));
                        list = new ArrayList<>();
                        list.add(cat);
                    }
                }
                break;
        }
        if (!list.isEmpty())
            catsHashMap.put(String.valueOf(letter), list);

        if (myCatsList.size() > 0)
            catsHashMap.put(MY_CATS_KEY, myCatsList);
        catsListAdapter.onDataChanged();
    }
}
