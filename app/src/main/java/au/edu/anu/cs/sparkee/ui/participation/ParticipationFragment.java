package au.edu.anu.cs.sparkee.ui.participation;

import android.os.Bundle;
import android.provider.Telephony;
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

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;

import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.model.ActivityModel;
import au.edu.anu.cs.sparkee.model.Participation;
import au.edu.anu.cs.sparkee.ui.participation.adapter.ParticipationAdapter;
import butterknife.BindView;

public class ParticipationFragment extends Fragment {

    private ParticipationViewModel participationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        participationViewModel =
                ViewModelProviders.of(this).get(ParticipationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_activity, container, false);
        mRecyclerView = root.findViewById(R.id.recycle_activity);
        participationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }
        });

        setUp();
        populateData();

        return root;
    }
    LinearLayoutManager mLayoutManager;
    @BindView(R.id.recycle_activity)
    RecyclerView mRecyclerView;

    ParticipationAdapter mParticipationAdapter;

    public void setUp() {
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
        ArrayList<Participation> list = new ArrayList<>();
        mParticipationAdapter = new ParticipationAdapter(list);
    }

    public void populateData() {
        ArrayList<Participation> mActivities = new ArrayList<Participation>();

//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));
//        mActivities.add(new ActivityModel(
//                "https://androidwave.com/wp-content/uploads/2020/01/androidwave-logo.png",
//                "123",
//                "Computer Science",
//                "+2",
//                LocalDateTime.parse("2020-01-01T02:02:02")
//        ));

        mParticipationAdapter.addItems(mActivities);
        mRecyclerView.setAdapter(mParticipationAdapter);

    }
}