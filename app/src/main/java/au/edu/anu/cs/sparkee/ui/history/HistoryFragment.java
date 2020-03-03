package au.edu.anu.cs.sparkee.ui.history;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.model.History;
import au.edu.anu.cs.sparkee.ui.history.adapter.HistoryAdapter;
import butterknife.BindView;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;

    boolean isLoading = false;

    int numExistingItems;
    Context context;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        historyViewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);

        context = this.getActivity();
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        mRecyclerView = root.findViewById(R.id.recycle_activity);
        setUp();
        initScrollListener();

        historyViewModel.getHistory().observe(getViewLifecycleOwner(), new Observer<List<History>>() {
            @Override
            public void onChanged(@Nullable List<History> part) {
                Log.d("SIZE", "" + part.size());
                populateData(part);
            }
        });

        historyViewModel.sendRequestHistoryNumLast(numItems);
        return root;
    }
    LinearLayoutManager mLayoutManager;
    @BindView(R.id.recycle_activity)
    RecyclerView mRecyclerView;

    int lastVisibleItemPosition;
    private void initScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {

                    if (linearLayoutManager != null && lastCompletelyVisibleItemPosition != linearLayoutManager.findLastCompletelyVisibleItemPosition() &&  linearLayoutManager.findLastCompletelyVisibleItemPosition() == numExistingItems - 1) {
                        lastVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                        lastCompletelyVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    }
                }
            }
        });
    }

    int lastCompletelyVisibleItemPosition;
    int numItems;
    final int moreItems = 10;
    private void loadMore() {
        historyViewModel.sendRequestHistoryNumLast(numItems + moreItems);
        numItems += moreItems;
    }

    HistoryAdapter mHistoryAdapter;

    public void setUp() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        ArrayList<History> list = new ArrayList<>();
        mHistoryAdapter = new HistoryAdapter(list);
        numItems = 10;
    }

    public void populateData(List<History> part) {
        mHistoryAdapter.addItems(part);
        mRecyclerView.setAdapter(mHistoryAdapter);
        Log.d("POP", "POPOPO");
        if(part.size() > 0) {
            numExistingItems = part.size();
            mRecyclerView.scrollToPosition(lastVisibleItemPosition);
        }
        isLoading = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        historyViewModel.stopViewModel();
    }
}