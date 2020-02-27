package au.edu.anu.cs.sparkee.ui.participation;

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
import au.edu.anu.cs.sparkee.model.Participation;
import au.edu.anu.cs.sparkee.ui.participation.adapter.ParticipationAdapter;
import butterknife.BindView;

public class ParticipationFragment extends Fragment {

    private ParticipationViewModel participationViewModel;

    boolean isLoading = false;

    int numExistingItems;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        participationViewModel = ViewModelProviders.of(this).get(ParticipationViewModel.class);

        View root = inflater.inflate(R.layout.fragment_participation, container, false);
        mRecyclerView = root.findViewById(R.id.recycle_activity);
        setUp();
        initScrollListener();

        participationViewModel.getParticipation().observe(getViewLifecycleOwner(), new Observer<List<Participation>>() {
            @Override
            public void onChanged(@Nullable List<Participation> part) {
                populateData(part);
            }
        });

        participationViewModel.sendRequestParticipationsNumLast(numItems);

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

                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == numExistingItems - 1) {
                        lastVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

    int numItems;
    final int moreItems = 10;
    private void loadMore() {
        participationViewModel.sendRequestParticipationsNumLast(numItems + moreItems);
        numItems += moreItems;
    }

    ParticipationAdapter mParticipationAdapter;

    public void setUp() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        ArrayList<Participation> list = new ArrayList<>();
        mParticipationAdapter = new ParticipationAdapter(list);
        numItems = 10;
    }

    public void populateData(List<Participation> part) {
        if(part != null) {
            mParticipationAdapter.addItems(part);
            mRecyclerView.setAdapter(mParticipationAdapter);
            numExistingItems = part.size();
            mRecyclerView.scrollToPosition(lastVisibleItemPosition);
        }
        isLoading = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        participationViewModel.stopViewModel();
    }
}