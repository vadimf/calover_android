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
    @BindView(R.id.progress_layout)
    View progressLayout;
    private HashMap<String, List<CatProfile>> catsHashMap;
    private CatsListAdapter catsListAdapter;
    private boolean listUpdated;
    private CatsPresenter presenter;
    private int selectedCatsSection;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cats, container, false);
        ButterKnife.bind(this, view);

        catsHashMap = new HashMap<>();

        getCats(CATS_SECTION_PRIVATE);
        listUpdated = true;

        catsListAdapter = new CatsListAdapter(catsHashMap, this::onCatClicked);
        catsRecyclerView.setAdapter(catsListAdapter);
        catsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progressLayout.setOnTouchListener((view1, motionEvent) -> true);

        return view;
    }

    private void onCatClicked(CatProfile catProfile) {
        CatProfileActivity.startInViewMode(getActivity(), catProfile);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
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
            getCats(selectedCatsSection);
            listUpdated = true;
        }
    }


    @OnClick(R.id.button_slider_public)
    void publicSliderButtonClicked() {
        selectedCatsSection = CATS_SECTION_PUBLIC;
        getCats(selectedCatsSection);
    }

    @OnClick(R.id.button_slider_private)
    void privateSliderButtonClicked() {
        selectedCatsSection = CATS_SECTION_PRIVATE;
        getCats(selectedCatsSection);
    }

    @OnClick(R.id.button_slider_friends)
    void friendsSliderButtonClicked() {
        selectedCatsSection = CATS_SECTION_FRIENDS;
        getCats(selectedCatsSection);
    }

    @OnClick(R.id.button_slider_explore)
    void exploreSliderButtonClicked() {
        selectedCatsSection = CATS_SECTION_EXPLORE;
        getCats(selectedCatsSection);
    }

    private void getCats(int section) {
        clearSliderButtonSelection();
        switch (section) {
            case CatsFragment.CATS_SECTION_PRIVATE:
                selectButton(privateSliderButton);
                break;
            case CatsFragment.CATS_SECTION_PUBLIC:
                selectButton(publicSliderButton);
                break;
            case CatsFragment.CATS_SECTION_FRIENDS:
                selectButton(friendsSliderButton);
                break;
            case CatsFragment.CATS_SECTION_EXPLORE:
                selectButton(exploreSliderButton);
                break;
        }
        presenter.getCats(section);
    }

    private void selectButton(Button button) {
        button.setTextColor(getResources().getColor(R.color.white));
        int backgroundResource;
        switch (button.getId()) {
            case R.id.button_slider_private:
                backgroundResource = R.drawable.shape_border_primary_color_button_left_selected;
                break;
            case R.id.button_slider_explore:
                backgroundResource = R.drawable.shape_border_primary_color_button_right_selected;
                break;
            default:
                backgroundResource = R.drawable.shape_border_primary_color_button_selected;
                break;
        }
        button.setBackground(getResources().getDrawable(backgroundResource));
    }

    private void clearSliderButtonSelection() {
        privateSliderButton.setBackground(getResources().getDrawable(R.drawable.shape_border_primary_color_button_left_unselected));
        privateSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        publicSliderButton.setBackground(getResources().getDrawable(R.drawable.shape_border_primary_color_button_unselected));
        publicSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        friendsSliderButton.setBackground(getResources().getDrawable(R.drawable.shape_border_primary_color_button_unselected));
        friendsSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        exploreSliderButton.setBackground(getResources().getDrawable(R.drawable.shape_border_primary_color_button_right_unselected));
        exploreSliderButton.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    public void catsLoaded(List<CatProfile> catProfiles, int catsSection) {
        stopRefreshing();
        listUpdated = true;
        catsHashMap.clear();
        if (!catProfiles.isEmpty()) {
            char letter = Character.toUpperCase(catProfiles.get(0).getPetName().charAt(0));
            ArrayList<CatProfile> list = new ArrayList<>();

            switch (catsSection) {
                case CatsFragment.CATS_SECTION_PRIVATE:
                    for (CatProfile cat : catProfiles) {
                        if (isCatInPrivateSection(cat)) {
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
                        if (isCatInPublicSection(cat)) {
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
                        if (isCatInFriendSection(cat)) {
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
        }
        catsListAdapter.onDataChanged();
    }

    private boolean isCatInPrivateSection(CatProfile cat) {
        return cat.getType() != null && cat.getUserRole() != null &&
                cat.getType().equals(CatProfile.Status.PET) && cat.getUserRole().equals(Feedstation.UserRole.ADMIN);
    }

    private boolean isCatInPublicSection(CatProfile cat) {
        if (isCatInPrivateSection(cat))
            return false;
        Integer userId = Integer.parseInt(Profile.getUserId(getContext()));
        return cat.getUserId() != null && cat.getUserId().equals(userId)
                && cat.getFeedStationStatus() != null && cat.getFeedStationStatus().equals("joined");
    }

    private boolean isCatInFriendSection(CatProfile cat) {
        Integer userId = Integer.parseInt(Profile.getUserId(getContext()));
        return (cat.getUserId() == null || (cat.getUserId() != null && !cat.getUserId().equals(userId)))
                && cat.getFeedStationStatus() != null
                && cat.getFeedStationStatus().equals("joined");

    }

    public void startRefreshing() {
        progressLayout.setVisibility(View.VISIBLE);
    }

    public void stopRefreshing() {
        progressLayout.setVisibility(View.GONE);
    }
}
