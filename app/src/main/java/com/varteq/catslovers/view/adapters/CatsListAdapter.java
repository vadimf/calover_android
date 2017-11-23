package com.varteq.catslovers.view.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CatsListAdapter extends RecyclerView.Adapter<CatsListAdapter.CatsListViewHolder> {

    private CatsListSameLetterAdapter.OnCatClickListener externalClickListener;
    private HashMap<String, List<CatProfile>> catsList;
    private List<String> keysList = new ArrayList<>();

    public CatsListAdapter(HashMap<String, List<CatProfile>> catsList, CatsListSameLetterAdapter.OnCatClickListener externalClickListener) {
        this.externalClickListener = externalClickListener;
        this.catsList = catsList;
        keysList.addAll(catsList.keySet());
        Collections.sort(keysList);
    }

    @Override
    public int getItemCount() {
        return catsList.size();
    }

    @Override
    public void onBindViewHolder(CatsListViewHolder catsListViewHolder, int i) {
        List<CatProfile> catsSameLetterList = catsList.get(keysList.get(i));
        CatsListSameLetterAdapter adapter = new CatsListSameLetterAdapter(catsSameLetterList, externalClickListener);
        catsListViewHolder.innerRecyclerView.setAdapter(adapter);
        catsListViewHolder.innerRecyclerView.setLayoutManager(
                new LinearLayoutManager(catsListViewHolder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));

        catsListViewHolder.letterTextView.setText(keysList.get(i));
    }

    @Override
    public CatsListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_cats_list_main, viewGroup, false);

        return new CatsListViewHolder(itemView);
    }

    public class CatsListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.start_letter_TextView)
        protected TextView letterTextView;
        @BindView(R.id.single_letter_RecyclerView)
        protected RecyclerView innerRecyclerView;

        public CatsListViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
