package com.varteq.catslovers.view.fragments;

import android.content.Intent;
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
import com.varteq.catslovers.view.CatProfileActivity;
import com.varteq.catslovers.view.adapters.CatsListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatsFragment extends Fragment {

    @BindView(R.id.cats_RecyclerView)
    RecyclerView catsRecyclerView;
    private HashMap<String, List<CatProfile>> catsHashMap;
    private CatsListAdapter catsListAdapter;

    final private int SEEKBAR_STEPS_COUNT = 7;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cats, container, false);
        ButterKnife.bind(this, view);

        ArrayList<CatProfile> aList = new ArrayList<>();
        aList.add(new CatProfile(1, "A Cat"));
        aList.add(new CatProfile(2, "AB Cat"));

        ArrayList<CatProfile> bList = new ArrayList<>();
        bList.add(new CatProfile(3, "B Cat"));
        bList.add(new CatProfile(4, "BA Cat"));
        bList.add(new CatProfile(5, "BB Cat"));
        bList.add(new CatProfile(6, "BC Cat"));
        bList.add(new CatProfile(7, "BD Cat"));
        bList.add(new CatProfile(8, "BE Cat"));
        bList.add(new CatProfile(9, "BF Cat"));
        bList.add(new CatProfile(10, "BG Cat"));
        bList.add(new CatProfile(11, "BH Cat"));

        ArrayList<CatProfile> cList = new ArrayList<>();
        cList.add(new CatProfile(12, "C Cat"));
        cList.add(new CatProfile(13, "CA Cat"));
        cList.add(new CatProfile(14, "CB Cat"));
        cList.add(new CatProfile(15, "CC Cat"));
        cList.add(new CatProfile(16, "CD Cat"));
        cList.add(new CatProfile(17, "CE Cat"));
        cList.add(new CatProfile(18, "CF Cat"));
        cList.add(new CatProfile(19, "CG Cat"));
        cList.add(new CatProfile(20, "CH Cat"));

        catsHashMap = new HashMap<>();
        catsHashMap.put("A", aList);
        catsHashMap.put("B", bList);
        catsHashMap.put("C", cList);

        catsListAdapter = new CatsListAdapter(catsHashMap, catProfile -> {
            Intent intent = new Intent(getActivity(), CatProfileActivity.class);
            intent.putExtra(CatProfileActivity.IS_EDIT_MODE_KEY, false);
            intent.putExtra(CatProfileActivity.CAT_KEY, catProfile);
            startActivity(intent);
        });
        catsRecyclerView.setAdapter(catsListAdapter);
        catsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        seekBar.setMax(SEEKBAR_STEPS_COUNT - 1);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
