package au.edu.anu.cs.sparkee.ui.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.rabbitmq.client.AlreadyClosedException;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.threeten.bp.LocalDateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;
import au.edu.anu.cs.sparkee.model.ParkingSlot;
//import au.edu.anu.cs.sparkee.receiver.AMQPBroadcaseReceiver;

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
    private double DEFAULT_LONG = 149.120385;
    private double DEFAULT_LAT= -35.275514;
    private String device_uuid;
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();

    public MapView getmMapView() {
        return mMapView;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        mMapView = new MapView(context);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);

        SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");

        IGeoPoint geoPoint =new GeoPoint(DEFAULT_LAT, DEFAULT_LONG);
        mMapView.getController().animateTo(geoPoint);
        mMapView.getController().setZoom(19.0);

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        mLocationOverlay = new MyLocationNewOverlay(mMapView);

        mapViewModel.getLocation().observe( getViewLifecycleOwner(), new Observer<Location>() {

            @Override
            public void onChanged(@Nullable Location loc) {
                if(loc != null) {
                    currentLocation = loc;

                    if(isInitView) {
                        mLocationOverlay.enableMyLocation();
                        mLocationOverlay.enableFollowLocation();
                        mLocationOverlay.setOptionsMenuEnabled(true);
                        mMapView.getOverlays().add(mLocationOverlay);
                        mMapView.getController().setZoom(18.0);
                        isInitView = false;

                    }

                    sendCurrentPosition();
                }

            }
        });

        mapViewModel.getParkingSlots().observe( getViewLifecycleOwner(), new Observer<ParkingSlot[]>() {

            @Override
            public void onChanged(@Nullable ParkingSlot[] parkingSlots) {
                if(parkingSlots != null) {
                    Log.d("BANYAK PARKING SLOTS", "" +parkingSlots.length);
                    addParkingSlots(parkingSlots);
                }
                else {
                    addParkingSlots(new ParkingSlot[0]);
                }
            }
        });

        mapViewModel.startViewModel();

        addBookmark();

        return mMapView;
    }

    public void sendCurrentPosition() {
        try {
            JSONObject jo = new JSONObject();
            jo.put("action", "participant_location");
            jo.put("device_uuid", device_uuid);
            jo.put("device_type", Constants.DEVICE_TYPE);
            jo.put("msg", "Contributor Location Changed");
            jo.put("long", currentLocation.getLongitude());
            jo.put("latt", currentLocation.getLatitude());
            amqpConnectionHelper.send(jo);
        }
        catch (AlreadyClosedException ace) {
            ace.printStackTrace();
        }
        catch (SocketException se) {
            se.printStackTrace();
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (JSONException je) {
            je.printStackTrace();
        }
    }

    public void addNewData(GeoPoint geoPoint) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("action", "parking_slot_registration");
            jo.put("device_uuid", device_uuid);
            jo.put("device_type", Constants.DEVICE_TYPE);
            jo.put("msg", "Parking Slot Registration");
            jo.put("long", geoPoint.getLongitude());
            jo.put("latt", geoPoint.getLatitude());
            amqpConnectionHelper.send(jo);
        }
        catch (AlreadyClosedException ace) {
            ace.printStackTrace();
        }
        catch (SocketException se) {
            se.printStackTrace();
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        catch(UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (JSONException je) {
            je.printStackTrace();
        }

    }

//    AMQPBroadcaseReceiver receiver;
    IntentFilter intentFilter;

    @Override
    public void onPause() {
        super.onPause();
        mapViewModel.stopViewModel();

    }

    private List<Marker> markers;


    public List<Marker> setParkingSlotAsMarkers(MapView view, ParkingSlot [] parkingSlots) {
        List<Marker> markers = new ArrayList<>();
        try {

            for(int i=0; i < parkingSlots.length; i++) {

                Marker m = new Marker(view);
                m.setId("" + parkingSlots[i].getId() );
                m.setTitle("" + parkingSlots[i].getId() );
                m.setSubDescription("" + parkingSlots[i].getId() );

                GeoPoint geoPoint = new GeoPoint( parkingSlots[i].getLatitude() ,parkingSlots[i].getLongitude());
                m.setPosition(geoPoint);


                int id_status = parkingSlots[i].getStatus();
                String tmp_status = "";
                switch(id_status) {
                    case -3:
                        m.setIcon( getResources().getDrawable(R.drawable.confirmed_unavailable));
                        tmp_status = "Unavailable";
                        break;
                    case -2:
                        m.setIcon( getResources().getDrawable(R.drawable.unconfirmed_minus_2));
                        tmp_status = "Unconfirmed";
                        break;
                    case -1:
                        m.setIcon( getResources().getDrawable(R.drawable.unconfirmed_minus_1));
                        tmp_status = "Unconfirmed";
                        break;
                    case 0:
                        m.setIcon( getResources().getDrawable(R.drawable.unconfirmed_0));
                        tmp_status = "Unconfirmed";
                        break;
                    case 1:
                        m.setIcon( getResources().getDrawable(R.drawable.unconfirmed_plus_1));
                        tmp_status = "Unconfirmed";
                        break;
                    case 2:
                        m.setIcon( getResources().getDrawable(R.drawable.unconfirmed_plus_2));
                        tmp_status = "Unconfirmed";
                        break;
                    case 3:
                        m.setIcon( getResources().getDrawable(R.drawable.confirmed_available));
                        tmp_status = "Available";
                        break;
                }


                InfoWindow infoWindow = new CustomInfoWindow(
                        R.layout.bubble_layout,
                        view,
                        geoPoint,
                        this.device_uuid,
                        tmp_status,
                        parkingSlots[i].getTs_update()
                );
                m.setInfoWindow(infoWindow);


                m.setSnippet(m.getPosition().toDoubleString());
                markers.add(m);
            }

        } catch (final Exception e) {
            Log.w(IMapView.LOGTAG,"Error getting tile sources: ", e);
        }
        return markers;
    }

    public void addParkingSlots(ParkingSlot [] parkingSlots) {

        //add all our bookmarks to the view
        markers = setParkingSlotAsMarkers(mMapView, parkingSlots);
        mMapView.getOverlayManager().addAll(markers);


        //TODO menu item to
        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Log.d("SINGLE", p.toDoubleString());
                Iterator i = markers.iterator();
                while(i.hasNext()) {
                    Marker m = (Marker) i.next();
                    m.getInfoWindow().close();
                }
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

    public void addBookmark() {
        if (datastore == null)
            datastore = new BookmarkDatastore(this);

        //add all our bookmarks to the view
//        markers = datastore.getBookmarksAsMarkers(mMapView);
//        mMapView.getOverlayManager().addAll(markers);
//
//
//        //TODO menu item to
//        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
//            @Override
//            public boolean singleTapConfirmedHelper(GeoPoint p) {
//                Log.d("SINGLE", p.toDoubleString());
//                Iterator i = markers.iterator();
//                while(i.hasNext()) {
//                    Marker m = (Marker) i.next();
//                    m.getInfoWindow().close();
//                }
//                return false;
//            }
//
//            @Override
//            public boolean longPressHelper(GeoPoint p) {
//                Log.d("LONG", p.toDoubleString());
//                showDialog(p);
//                return true;
//            }
//        });
//        mMapView.getOverlayManager().add(events);

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
//        final EditText title = view.findViewById(R.id.bookmark_title);
//        final EditText description = view.findViewById(R.id.bookmark_description);

        Log.d("DialogLong", "" + p.getLongitude());
        Log.d("DialogLatt", "" + p.getLatitude());
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
                    m.setTitle(UUID.randomUUID().toString());
                    m.setSubDescription(UUID.randomUUID().toString());
                    m.setIcon(getResources().getDrawable(R.drawable.unconfirmed_0));

                    GeoPoint geoPoint = new GeoPoint(latD, lonD);
                    m.setPosition(geoPoint);
                    String tmp_status = "Unconfirmed (confidence 60%)";
                    InfoWindow infoWindow = new CustomInfoWindow(
                            R.layout.bubble_layout,
                            mMapView,
                            geoPoint,
                            device_uuid,
                            tmp_status,
                            LocalDateTime.now()
                    );

                    m.setInfoWindow(infoWindow);

                    m.setSnippet(m.getPosition().toDoubleString());
                    datastore.addBookmark(m);
                    mMapView.getOverlayManager().add(m);
                    mMapView.invalidate();
                    addNewData(geoPoint);
                }
                addBookmark.dismiss();

            }
        });

        addBookmark = builder.show();
    }

}