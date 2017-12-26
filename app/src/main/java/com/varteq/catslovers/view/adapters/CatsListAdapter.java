package com.varteq.catslovers.view.adapters;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.varteq.catslovers.R;
import com.varteq.catslovers.model.CatProfile;
import com.varteq.catslovers.utils.ResourceUtils;
import com.varteq.catslovers.utils.UiUtils;
import com.varteq.catslovers.view.fragments.CatsFragment;

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
        updateKeys();
    }

    public void onDataChanged() {
        updateKeys();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return catsList.size();
    }

    @Override
    public void onBindViewHolder(CatsListViewHolder catsListViewHolder, int i) {
        if (keysList.get(i).equals(CatsFragment.MY_CATS_KEY)) {
            catsListViewHolder.innerRecyclerView.setBackgroundColor(
                    catsListViewHolder.itemView.getContext().getResources().getColor(R.color.greyishBlueLight));
            catsListViewHolder.letterTextView.setVisibility(View.GONE);
            ((CatsListSameLetterAdapter) catsListViewHolder.innerRecyclerView.getAdapter()).setMyCats(true);
        } else {
            catsListViewHolder.innerRecyclerView.setBackgroundColor(
                    catsListViewHolder.itemView.getContext().getResources().getColor(R.color.transparent));
            catsListViewHolder.letterTextView.setVisibility(View.VISIBLE);
            ((CatsListSameLetterAdapter) catsListViewHolder.innerRecyclerView.getAdapter()).setMyCats(false);
        }

        List<CatProfile> catsSameLetterList = catsList.get(keysList.get(i));
        catsListViewHolder.personList.clear();
        catsListViewHolder.personList.addAll(catsSameLetterList);
        catsListViewHolder.innerRecyclerView.getAdapter().notifyDataSetChanged();

        catsListViewHolder.letterTextView.setText(keysList.get(i));
    }

    @Override
    public CatsListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_cats_list_main, viewGroup, false);

        return new CatsListViewHolder(itemView);
    }

    private void updateKeys() {
        keysList.clear();
        keysList.addAll(catsList.keySet());
        Collections.sort(keysList);
        if (keysList.contains(CatsFragment.MY_CATS_KEY)) {
            keysList.remove(CatsFragment.MY_CATS_KEY);
            keysList.add(0, CatsFragment.MY_CATS_KEY);
        }
    }

    public class CatsListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.start_letter_TextView)
        protected TextView letterTextView;
        @BindView(R.id.single_letter_RecyclerView)
        protected RecyclerView innerRecyclerView;
        protected List<CatProfile> personList = new ArrayList<>();

        public CatsListViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            CatsListSameLetterAdapter adapter = new CatsListSameLetterAdapter(personList, externalClickListener);
            innerRecyclerView.setAdapter(adapter);
            int colNum = UiUtils.calculateNumberOfColumns(itemView.getContext(),
                    ResourceUtils.getDimen(R.dimen.card_cat_same_letter_width) + 2 * ResourceUtils.getDimen(R.dimen.card_cat_same_letter_padding),
                    0);
            innerRecyclerView.setLayoutManager(new GridLayoutManager(itemView.getContext(), colNum));
            //catsListViewHolder.innerRecyclerView.setLayoutManager(new LinearLayoutManager(catsListViewHolder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        }
    }
}
