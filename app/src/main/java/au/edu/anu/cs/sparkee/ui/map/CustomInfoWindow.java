package au.edu.anu.cs.sparkee.ui.map;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import au.edu.anu.cs.sparkee.R;

public class CustomInfoWindow extends InfoWindow {
    public CustomInfoWindow(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
    }

    public void onClose() {
    }

    public void onOpen(Object arg0) {
        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.bubble_layout);
        Button btnAvailable = (Button) mView.findViewById(R.id.bubble_available);
        Button btnUnavailable = (Button) mView.findViewById(R.id.bubble_unavailable);
        TextView txtTitle = (TextView) mView.findViewById(R.id.bubble_title);
        TextView txtDescription = (TextView) mView.findViewById(R.id.bubble_description);
        TextView txtSubdescription = (TextView) mView.findViewById(R.id.bubble_subdescription);

        txtTitle.setText("Title of my marker");
        txtDescription.setText("Click here to view details!");
        txtSubdescription.setText("You can also edit the subdescription");
        layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Override Marker's onClick behaviour here
                close();
            }
        });

        btnAvailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Available");
                close();
            }
        });

        btnUnavailable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d("Btn", "Unavailable");
                close();
            }
        });
    }

    public void close() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            onClose();
        }
    }

}