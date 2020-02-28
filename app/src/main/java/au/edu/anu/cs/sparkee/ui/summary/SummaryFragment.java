package au.edu.anu.cs.sparkee.ui.summary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Map;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;

public class SummaryFragment extends Fragment {

    private SummaryViewModel summaryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        summaryViewModel =
                ViewModelProviders.of(this).get(SummaryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.txt_uuid);
        final TextView txt_participations = root.findViewById(R.id.txt_participations);
        final TextView txt_balance = root.findViewById(R.id.txt_balance);
        final TextView txt_incentive = root.findViewById(R.id.txt_incentive);
        final TextView txt_subscription = root.findViewById(R.id.txt_subscription);

        SharedPreferences sharedPref = getContext().getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        String device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");
        textView.setText(device_uuid);

        summaryViewModel.getSummary().observe(getViewLifecycleOwner(), new Observer<Map<String,String>>() {
            @Override
            public void onChanged(@Nullable Map<String,String> map) {
                if(map != null) {
                    txt_participations.setText(map.get("participation"));
                    txt_balance.setText(map.get("balance"));
                    txt_incentive.setText(map.get("incentive"));
                    txt_subscription.setText(map.get("charged"));
                }
            }
        });
        return root;
    }


    @Override
    public void onPause() {
        super.onPause();
        summaryViewModel.stopViewModel();
    }
}