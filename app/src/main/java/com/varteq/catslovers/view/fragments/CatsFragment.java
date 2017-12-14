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
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.Cat;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.TimeUtils;
import com.varteq.catslovers.view.CatProfileActivity;
import com.varteq.catslovers.view.adapters.CatsListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatsFragment extends Fragment {

    private String TAG = CatsFragment.class.getSimpleName();

    @BindView(R.id.cats_RecyclerView)
    RecyclerView catsRecyclerView;
    private HashMap<String, List<CatProfile>> catsHashMap;
    private CatsListAdapter catsListAdapter;
    private boolean listUpdated;

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
        /*catsHashMap.put("A", aList);
        catsHashMap.put("B", bList);
        catsHashMap.put("C", cList);*/

        getCats();

        catsListAdapter = new CatsListAdapter(catsHashMap, catProfile -> {
            CatProfileActivity.startInViewMode(getActivity(), catProfile);
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

    @Override
    public void onPause() {
        super.onPause();
        listUpdated = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!listUpdated)
            getCats();
    }

    public void getCats() {
        listUpdated = true;

        Call<BaseResponse<List<Cat>>> call = ServiceGenerator.getApiServiceWithToken().getCats();
        call.enqueue(new Callback<BaseResponse<List<Cat>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<Cat>>> call, Response<BaseResponse<List<Cat>>> response) {
                new BaseParser<List<Cat>>(response) {

                    @Override
                    protected void onSuccess(List<Cat> data) {
                        Log.i(TAG, String.valueOf(data.size()));
                        if (data.size() < 1) return;
                        catsHashMap.clear();
                        List<CatProfile> catProfiles = getCatProfiles(data);
                        Collections.sort(catProfiles, (catProfile, t1) -> catProfile.getPetName().toUpperCase().compareTo(t1.getPetName().toUpperCase()));

                        char letter = Character.toUpperCase(data.get(0).getName().charAt(0));
                        ArrayList<CatProfile> list = new ArrayList<>();
                        for (CatProfile cat : catProfiles) {
                            if (letter == Character.toUpperCase(cat.getPetName().charAt(0)))
                                list.add(cat);
                            else {
                                catsHashMap.put(String.valueOf(letter), list);
                                letter = Character.toUpperCase(cat.getPetName().charAt(0));
                                list = new ArrayList<>();
                                list.add(cat);
                            }

                        }
                        catsListAdapter.onDataChanged();
                    }

                    @Override
                    protected void onFail(ErrorResponse error) {
                        Log.d(TAG, error.getMessage() + error.getCode());
                    }
                };
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Cat>>> call, Throwable t) {
                Log.e(TAG, "getCats onFailure " + t.getMessage());
            }
        });
    }

    private List<CatProfile> getCatProfiles(List<Cat> data) {
        List<CatProfile> list = new ArrayList<>();
        for (Cat cat : data) {
            CatProfile catProfile = new CatProfile();
            catProfile.setId(cat.getId());
            catProfile.setPetName(cat.getName());
            catProfile.setNickname(cat.getNickname());
            catProfile.setBirthday(TimeUtils.getDateFromUTC(cat.getAge()));
            catProfile.setSex(null);
            catProfile.setWeight(cat.getWeight());
            catProfile.setCastrated(cat.getCastrated());
            catProfile.setDescription(cat.getDescription());
            catProfile.setType(cat.getType().equals("pet") ? CatProfile.Status.PET : CatProfile.Status.STRAY);
            catProfile.setFleaTreatmentDate(TimeUtils.getDateFromUTC(cat.getNextFleaTreatment()));

            List<Integer> colors = new ArrayList<>();
            for (String s : cat.getColor().split(","))
                colors.add(Integer.parseInt(s));
            catProfile.setColorsList(colors);
            list.add(catProfile);
        }
        return list;
    }
}
