package au.edu.anu.cs.sparkee.ui.map;

import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.UUID;

import au.edu.anu.cs.sparkee.R;

public class MapFragment extends Fragment {

    private MapViewModel mapViewModel;
    private boolean isInitView = true;
    protected MapView mMapView;
    private Location currentLocation;
    private BookmarkDatastore datastore = null;
    AlertDialog addBookmark = null;

    private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
    private MyLocationNewOverlay mLocationOverlay;
    private Context context;
    private double DEFAULT_LONG = 149.1316834;
    private double DEFAULT_LAT= -35.2534043;

    public MapView getmMapView() {
        return mMapView;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        mMapView = new MapView(context);

        IGeoPoint geoPoint =new GeoPoint(DEFAULT_LAT, DEFAULT_LONG);
        mMapView.getController().animateTo(geoPoint);
        mMapView.getController().setZoom(19.0);

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        mLocationOverlay = new MyLocationNewOverlay(mMapView);

        mapViewModel.getLocation().observe(this, new Observer<Location>() {

            @Override
            public void onChanged(@Nullable Location loc) {
                if(loc != null) {
                    currentLocation = loc;

                    if(isInitView) {
                        mMapView.setTilesScaledToDpi(true);
                        mMapView.setMultiTouchControls(true);
                        mMapView.setFlingEnabled(true);
                        mLocationOverlay.enableMyLocation();
                        mLocationOverlay.enableFollowLocation();
                        mLocationOverlay.setOptionsMenuEnabled(true);
                        mMapView.getOverlays().add(mLocationOverlay);
                        mMapView.getController().setZoom(18.0);
                        isInitView = false;

                    }
                }

            }
        });
        mapViewModel.startGPS();

        addBookmark();
        return mMapView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapViewModel.stopGPS();
    }


    public void addBookmark() {
        if (datastore == null)
            datastore = new BookmarkDatastore();
        //add all our bookmarks to the view
        mMapView.getOverlayManager().addAll(datastore.getBookmarksAsMarkers(mMapView));


        //TODO menu item to
        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Log.d("SINGLE", p.toDoubleString());
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Log.d("LONG", p.toDoubleString());
                showDialog(p);
                return true;
            }
        });
        mMapView.getOverlayManager().add(events);

    }

    private void showDialog(GeoPoint p) {
        if (addBookmark!=null)
            addBookmark.dismiss();

        //TODO prompt for user input
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = View.inflate(getContext(), R.layout.bookmark_add_dialog, null);
        builder.setView(view);
        final TextView lat = view.findViewById(R.id.bookmark_lat);
        lat.setText(p.getLatitude() + "");
        final TextView lon = view.findViewById(R.id.bookmark_lon);
        lon.setText(p.getLongitude() + "");
        final EditText title = view.findViewById(R.id.bookmark_title);
        final EditText description = view.findViewById(R.id.bookmark_description);

        view.findViewById(R.id.bookmark_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookmark.dismiss();
            }
        });

        view.findViewById(R.id.bookmark_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                boolean valid = true;
                double latD = 0;
                double lonD = 0;
                //basic validate input
                try {
                    latD = Double.parseDouble(lat.getText().toString());
                } catch (Exception ex) {
                    valid = false;
                }
                try {
                    lonD = Double.parseDouble(lon.getText().toString());
                } catch (Exception ex) {
                    valid = false;
                }

                if (!mMapView.getTileSystem().isValidLatitude(latD))
                    valid = false;
                if (!mMapView.getTileSystem().isValidLongitude(lonD))
                    valid = false;

                if (valid) {
                    Marker m = new Marker(mMapView);
                    m.setId(UUID.randomUUID().toString());
                    m.setTitle(title.getText().toString());
                    m.setSubDescription(description.getText().toString());

                    m.setPosition(new GeoPoint(latD, lonD));
                    m.setSnippet(m.getPosition().toDoubleString());
                    datastore.addBookmark(m);
                    mMapView.getOverlayManager().add(m);
                    mMapView.invalidate();
                }
                addBookmark.dismiss();
            }
        });

        addBookmark = builder.show();
    }

}