package com.varteq.catslovers.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.view.CatProfileActivity;
import com.varteq.catslovers.view.adapters.CatsListAdapter;
import com.varteq.catslovers.view.presenter.CatsPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatsFragment extends Fragment {

    public static final String MY_CATS_KEY = "my_cats";
    private String TAG = CatsFragment.class.getSimpleName();

    @BindView(R.id.cats_RecyclerView)
    RecyclerView catsRecyclerView;
    private HashMap<String, List<CatProfile>> catsHashMap;
    private CatsListAdapter catsListAdapter;
    private List<CatProfile> myCatsList = new ArrayList<>();
    private boolean listUpdated;
    private CatsPresenter presenter;

    final private int SEEKBAR_STEPS_COUNT = 1;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cats, container, false);
        ButterKnife.bind(this, view);

        catsHashMap = new HashMap<>();

        presenter.getCats(seekBar.getProgress() > 0);
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
                if (i == 0)
                    presenter.getCats(false);
                else
                    presenter.getCats(true);
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
            presenter.getCats(seekBar.getProgress() > 0);
            listUpdated = true;
        }
    }

    public void catsLoaded(List<CatProfile> catProfiles) {

        listUpdated = true;
        catsHashMap.clear();
        myCatsList.clear();

        char letter = Character.toUpperCase(catProfiles.get(0).getPetName().charAt(0));
        ArrayList<CatProfile> list = new ArrayList<>();
        for (CatProfile cat : catProfiles) {
            if (cat.getType() != null && cat.getUserRole() != null &&
                    cat.getType().equals(CatProfile.Status.PET) && cat.getUserRole().equals(Feedstation.UserRole.ADMIN)) {
                myCatsList.add(cat);
            } else if (letter == Character.toUpperCase(cat.getPetName().charAt(0)))
                list.add(cat);
            else {
                if (!list.isEmpty())
                    catsHashMap.put(String.valueOf(letter), list);
                letter = Character.toUpperCase(cat.getPetName().charAt(0));
                list = new ArrayList<>();
                list.add(cat);
            }
        }
        if (!list.isEmpty())
            catsHashMap.put(String.valueOf(letter), list);

        if (myCatsList.size() > 0)
            catsHashMap.put(MY_CATS_KEY, myCatsList);
        catsListAdapter.onDataChanged();
    }
}
