package au.edu.anu.cs.sparkee.ui.summary;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;

public class SummaryFragment extends Fragment {

    private SummaryViewModel summaryViewModel;

    String http_host_port;
    String amqp_host;
    int amqp_port;

    Context ctx;
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

        final TextView txt_http_host_port = root.findViewById(R.id.txt_http_host_port);
        final TextView txt_amqp_host_port = root.findViewById(R.id.txt_amqp_host_port);
        final Button btn_save = root.findViewById(R.id.btn_save);

        ctx  = this.getActivity();
        SharedPreferences sharedPref = getContext().getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        String device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");
        textView.setText(device_uuid);


        http_host_port = sharedPref.getString(Constants.HTTP_IP_PORT_IDENTIFIER, Constants.HTTP_IP_PORT);
        amqp_host = sharedPref.getString(Constants.RABBIT_HOST_IDENTIFIER, Constants.RABBIT_HOST);
        amqp_port = sharedPref.getInt(Constants.RABBIT_PORT_IDENTIFIER, Constants.RABBIT_PORT);

        txt_http_host_port.setText(http_host_port);
        txt_amqp_host_port.setText(amqp_host + ":" + amqp_port);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_http = (String) txt_http_host_port.getText().toString();
                String txt_amqp = (String) txt_amqp_host_port.getText().toString();
                Log.d("DD", txt_http + "   " + txt_amqp);
                if(txt_http.contains(":") && txt_amqp.contains(":")) {
                    try {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(Constants.HTTP_IP_PORT_IDENTIFIER, txt_http);
                        StringTokenizer st = new StringTokenizer(txt_amqp, ":");
                        String rabbit_host = st.nextToken();
                        int rabbit_port = Integer.parseInt(st.nextToken());
                        editor.putString(Constants.RABBIT_HOST_IDENTIFIER, rabbit_host);
                        editor.putInt(Constants.RABBIT_PORT_IDENTIFIER, rabbit_port);
                        editor.commit();
                        Toast.makeText(ctx, "Restart app to use new configuration", Toast.LENGTH_LONG).show();
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ctx, "Please follow format [IP:PORT]", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
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