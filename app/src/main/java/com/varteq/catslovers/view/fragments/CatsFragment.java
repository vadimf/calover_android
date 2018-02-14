package com.varteq.catslovers.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.view.BottomNavigationViewHelper;
import com.varteq.catslovers.view.CatProfileActivity;
import com.varteq.catslovers.view.adapters.CatsListAdapter;
import com.varteq.catslovers.view.presenter.CatsPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatsFragment extends Fragment {

    public enum Selection {
        PRIVATE,
        PUBLIC,
        FRIENDS,
        EXPLORE
    }

    private String TAG = CatsFragment.class.getSimpleName();

    @BindView(R.id.cats_RecyclerView)
    RecyclerView catsRecyclerView;
    @BindView(R.id.progress_layout)
    View progressLayout;
    @BindView(R.id.cats_navigation)
    BottomNavigationView navigationView;

    private HashMap<String, List<CatProfile>> catsHashMap;
    private CatsListAdapter catsListAdapter;
    private boolean listUpdated;
    private CatsPresenter presenter;
    private Selection selectedCatsSection = Selection.PRIVATE;
    private int navigationSelectedItemId;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cats, container, false);
        ButterKnife.bind(this, view);

        catsHashMap = new HashMap<>();

        getCats(selectedCatsSection);
        listUpdated = true;

        catsListAdapter = new CatsListAdapter(catsHashMap, this::onCatClicked);
        catsRecyclerView.setAdapter(catsListAdapter);
        catsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        progressLayout.setOnTouchListener((view1, motionEvent) -> true);

        BottomNavigationViewHelper.disableShiftMode(navigationView);
        initNavigationView();

        return view;
    }

    private void initNavigationView() {

        View menuView = navigationView.getChildAt(0);
        if (menuView != null) {
            ViewGroup.LayoutParams params = menuView.getLayoutParams();
            if (params != null && params instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) params).gravity = Gravity.BOTTOM;
                menuView.setLayoutParams(params);
            }
        }

        navigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() != navigationSelectedItemId) {
                switch (item.getItemId()) {
                    case R.id.action_private:
                        onTabChanged(Selection.PRIVATE);
                        break;
                    case R.id.action_public:
                        onTabChanged(Selection.PUBLIC);
                        break;
                    case R.id.action_friends:
                        onTabChanged(Selection.FRIENDS);
                        break;
                    case R.id.action_explore:
                        onTabChanged(Selection.EXPLORE);
                        break;
                }
            }
            navigationSelectedItemId = item.getItemId();
            return true;
        });
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
            getCats(getSelectedCatsSection());
            listUpdated = true;
        }
    }

    void onTabChanged(Selection selection) {
        selectedCatsSection = selection;
        getCats(selectedCatsSection);
    }

    private void getCats(Selection section) {
        presenter.getCats(section);
    }

    private Selection getSelectedCatsSection() {

        return selectedCatsSection != null ? selectedCatsSection : Selection.PRIVATE;
    }

    public void catsLoaded(List<CatProfile> catProfiles, Selection catsSection) {
        stopRefreshing();
        listUpdated = true;
        catsHashMap.clear();
        if (!catProfiles.isEmpty()) {
            char letter = Character.toUpperCase(catProfiles.get(0).getPetName().charAt(0));
            ArrayList<CatProfile> list = new ArrayList<>();

            switch (catsSection) {
                case PRIVATE:
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
                case PUBLIC:
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
                case FRIENDS:
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
                case EXPLORE:
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
        navigationView.getMenu().setGroupEnabled(R.id.menu_group_cat, false);
        progressLayout.setVisibility(View.VISIBLE);
    }

    public void stopRefreshing() {
        progressLayout.setVisibility(View.GONE);
        navigationView.getMenu().setGroupEnabled(R.id.menu_group_cat, true);
    }
}
