package au.edu.anu.cs.sparkee.ui.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.threeten.bp.LocalDateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;
import au.edu.anu.cs.sparkee.model.OSMBookmarkDatastore;
import au.edu.anu.cs.sparkee.model.ParkingSpot;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.ui.map.infowindow.ParkingSpotInfoWindow;
import au.edu.anu.cs.sparkee.ui.map.infowindow.ParkingZoneInfoWindow;
import au.edu.anu.cs.sparkee.ui.map.marker.ParkingSpotMarker;
import au.edu.anu.cs.sparkee.ui.map.marker.ParkingZonePolygon;

import static au.edu.anu.cs.sparkee.Constants.DEFAULT_ZOOM_PARKING_SPOT;
//import au.edu.anu.cs.sparkee.amqpReceiver.AMQPBroadcaseReceiver;

public class MapFragment extends Fragment {

    private MapViewModel mapViewModel;
    private boolean isInitView = true;
    protected MapView mMapView;
    private Location currentLocation;
    private OSMBookmarkDatastore datastore = null;
    AlertDialog addBookmark = null;
    protected ImageButton btCenterMap;

    private ItemizedOverlayWithFocus<OverlayItem> mMyLocationOverlay;
    private MyLocationNewOverlay mLocationOverlay;

    protected static final int DEFAULT_INACTIVITY_DELAY_IN_MILLISECS = 200;
    protected static final double MIN_ZOOM_LEVEL_TO_SHOW_BUBBLE = 19;

    private Context context;
    private String DEFAULT_LON = "149.120385";
    private String DEFAULT_LAT = "-35.275514";

    private long DEFAULT_DELAY_ANIMATION = 100;

//    private double DEFAULT_ZOOM_PARKING_ZONE_FAR = 18.0;
//    private double DEFAULT_ZOOM_PARKING_SPOT = 22.0;

    private String device_uuid;
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();

    private FolderOverlay folderOverlayParkingSpot;
    private FolderOverlay folderOverlayParkingZone;
    public MapView getmMapView() {
        return mMapView;
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, null);
        mMapView = v.findViewById(R.id.mapview);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);

        return v;
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        this.context = this.getActivity();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");


        double map_lon =  Double.parseDouble(sharedPref.getString(Constants.CURRENT_LOCATION_LON, DEFAULT_LON));
        double map_lat = Double.parseDouble(sharedPref.getString(Constants.CURRENT_LOCATION_LAT, DEFAULT_LAT));

        IGeoPoint geoPoint =new GeoPoint(map_lat, map_lon);
        mMapView.getController().animateTo(geoPoint);
        mMapView.getController().setZoom(Constants.DEFAULT_ZOOM_PARKING_ZONE_FAR);

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);

        mLocationOverlay = new MyLocationNewOverlay(mMapView);

        folderOverlayParkingZone = new FolderOverlay();
        folderOverlayParkingSpot = new FolderOverlay();

        mapViewModel.getHttpConnectionEstablished().observe( getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean stat) {
                initHTTP = stat;
//                evaluateInitView();

                if(stat == Boolean.FALSE)
                    Toast.makeText(context, "Connecting to server (retrying)...", Toast.LENGTH_SHORT).show();
            }
        });

        mapViewModel.getLocation().observe( getViewLifecycleOwner(), new Observer<Location>() {

            @Override
            public void onChanged(@Nullable Location loc) {
            if(loc != null) {
                currentLocation = loc;

                if(!initGPS) {
                    mLocationOverlay.enableMyLocation();
//                  mLocationOverlay.enableFollowLocation();
                    mLocationOverlay.setOptionsMenuEnabled(true);
                    mMapView.getOverlays().add(mLocationOverlay);
                }
                initGPS = true;
//                evaluateInitView();
            }
            }
        });

        // TODO:
        // 1. Get Parking Zones
//        mapViewModel.getParkingSlots().observe( getViewLifecycleOwner(), new Observer<ParkingSpot[]>() {
//
//            @Override
//            public void onChanged(@Nullable ParkingSpot[] parkingSlots) {
//                if(parkingSlots != null) {
//                    Log.d("BANYAK PARKING SLOTS", "" + parkingSlots.length);
//                    modifyParkingSlots(parkingSlots);
//                }
//            }
//        });
//
//
        mapViewModel.getParkingZones().observe( getViewLifecycleOwner(), new Observer<ParkingZone[]>() {

            @Override
            public void onChanged(@Nullable ParkingZone[] parkingZones) {
                if(parkingZones!= null ) {
                    if( folderOverlayParkingZone.getItems().size() == 0) {
                        Log.d("BANYAK PARKING ZONES", "" + parkingZones.length);
                        addParkingZones(parkingZones);
                    }
                    else {
                        Log.d("UPDATE PARKING ZONES", "" + parkingZones.length);
                        modifyParkingZones(parkingZones);
                    }
                }
            }
        });

        mapViewModel.getCreditValue().observe( getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(@Nullable Integer val) {
                TextView ic_credit = (TextView) getView().findViewById(R.id.ic_credit);
                if(initHTTP)
                    ic_credit.setText(val.toString());
                else
                    ic_credit.setText("-");
                creditValue = val;
            }
        });

        mapViewModel.startViewModel();

        btCenterMap = view.findViewById(R.id.ic_center_map);

        btCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CENTER MAP", "centerMap clicked ");
                if (currentLocation != null) {
                    GeoPoint myPosition = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    mMapView.getController().animateTo(myPosition);
                }
            }
        });

        addLocalDatastore();
        addHandleMapEvent();
        addOverlayRotation();

//        getParkingZones();

        markers = new HashMap<>();
        polygons = new HashMap<>();
    }

    private boolean initGPS;
//    private boolean initAMQP;
    private boolean initHTTP;

//    private void evaluateInitView() {
//        if(! ( initGPS || initHTTP)) { // && initAMQP
//            isInitView = false;
//            Toast.makeText(context, "Initializing ...", Toast.LENGTH_LONG).show();
//        }
//    }

    private int creditValue;
//    public void getParkingZones() {
//        mapViewModel.getParkingZones();
//    }
//    public void sendCurrentPosition() {
//        try {
//            JSONObject jo = new JSONObject();
//            jo.put("action", "participant_location");
//            jo.put("device_uuid", device_uuid);
//            jo.put("device_type", Constants.DEVICE_TYPE);
//            jo.put("msg", "Contributor Location Changed");
//            jo.put("lon", currentLocation.getLongitude());
//            jo.put("lat", currentLocation.getLatitude());
//            amqpConnectionHelper.send(jo);
//        }
//        catch (AlreadyClosedException ace) {
//            ace.printStackTrace();
//        }
//        catch (SocketException se) {
//            se.printStackTrace();
//        }
//        catch (NullPointerException npe) {
//            npe.printStackTrace();
//        }
//        catch(UnsupportedEncodingException uee) {
//            uee.printStackTrace();
//        }
//        catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        catch (JSONException je) {
//            je.printStackTrace();
//        }
//    }


    public void addOverlayRotation(){

        final DisplayMetrics dm = getActivity().getResources().getDisplayMetrics();
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mMapView);
        mRotationGestureOverlay.setEnabled(true);
        mMapView.getOverlays().add(mRotationGestureOverlay);

        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mMapView);
        mScaleBarOverlay.setScaleBarOffset(0,(int)(40 * dm.density));
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mMapView.getOverlays().add(mScaleBarOverlay);

    }

    public void addNewData(GeoPoint geoPoint) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("action", "parking_slot_registration");
            jo.put("device_uuid", device_uuid);
            jo.put("device_type", Constants.DEVICE_TYPE);
            jo.put("msg", "Parking Slot Registration");
            jo.put("lon", geoPoint.getLongitude());
            jo.put("lat", geoPoint.getLatitude());
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

    IntentFilter intentFilter;

    @Override
    public void onPause() {
        super.onPause();
        mapViewModel.stopViewModel();
    }

    private HashMap<Integer, ParkingSpotMarker> markers;
    private HashMap<Integer, Polygon> polygons;

    public ParkingSpotMarker setParkingSpotAsMarker(MapView view, ParkingSpot parkingSpot) {
        ParkingSpotMarker m = new ParkingSpotMarker(view, parkingSpot);
        try {
            m.setId("" + parkingSpot.getId() );
            m.setTitle("" + parkingSpot.getId() );
            m.setSubDescription("" + parkingSpot.getId() );

            GeoPoint geoPoint = new GeoPoint( parkingSpot.getLatitude() , parkingSpot.getLongitude());
            m.setPosition(geoPoint);


            String tmp_status = "";
            double confidence_level = parkingSpot.getConfidence_level();
            int tmp_confidence = (int) Math.round(confidence_level * 100);
            int marker_status = parkingSpot.getMarker_status();


            int participation = (parkingSpot.getParticipation_status() ? Constants.MARKER_PARKING_CATEGORY_PARTICIPATION: Constants.MARKER_PARKING_CATEGORY_DEFAULT);

            int iconId = ParkingSpotMarker.getMarkerIcon(marker_status, participation);
            Drawable d = getResources().getDrawable(iconId);
            Log.d("ICON 1", "" + iconId);

            m.setIcon( d );

            tmp_status = ParkingSpotMarker.getMarkerStatusTxt(marker_status, tmp_confidence);

            ParkingSpotInfoWindow infoWindow = new ParkingSpotInfoWindow(
                    R.layout.bubble_parking_spot_layout,
                    view,
                    geoPoint,
                    this.device_uuid,
                    tmp_status,
                    parkingSpot.getTs_update(),
                    m
            );
            m.setInfoWindow(infoWindow);
            m.setSnippet(m.getPosition().toDoubleString());

            m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    hideAllInfoWindow();
                    mMapView.getController().animateTo(marker.getPosition(), Constants.DEFAULT_ZOOM_PARKING_SPOT, (long) DEFAULT_DELAY_ANIMATION);
                    marker.showInfoWindow();
                    return true;
                }
            });
        }
        catch (final Exception e) {
            Log.w(IMapView.LOGTAG,"Error getting tile sources: ", e);
        }
//        Log.d("SET PARKING SPOT", "" + parkingSpot.getId() + " " + parkingSpot.getLatitude()+ " ---- " + parkingSpot.getLongitude());
        return m;
    }

    public void hideAllInfoWindow() {
        for (Overlay item :folderOverlayParkingZone.getItems()) {
            ParkingZonePolygon tmp_polygon = (ParkingZonePolygon) item;
            tmp_polygon.getInfoWindow().close();
        }

        for (Overlay item :folderOverlayParkingSpot.getItems()) {
            ParkingSpotMarker tmp_parking_spot = (ParkingSpotMarker) item;
            tmp_parking_spot.getInfoWindow().close();
        }
    }

    public void addHandleMapEvent() {

        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                hideAllInfoWindow();
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // TODO: Only for admin to add new ParkingSpot

                Log.d("ACTION", "LONG PRESS");
//                showDialog(p);
                return true;
            }
        });
        mMapView.getOverlayManager().add(events);

        mMapView.addMapListener(new DelayedMapListener(new MapListener() {

            @Override
            public boolean onScroll(ScrollEvent event) {
                IGeoPoint map_center = event.getSource().getMapCenter();
                SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                double lon = map_center.getLongitude();
                double lat = map_center.getLatitude();
//                Log.d("CENTER LON", "" + lon);
//                Log.d("CENTER LAT", "" + lat);
                editor.putString(Constants.CURRENT_LOCATION_LON, "" + lon);
                editor.putString(Constants.CURRENT_LOCATION_LAT, "" + lat);
                editor.commit();
                return true;
            }



            @Override
            public boolean onZoom(ZoomEvent event) {
//                double zoom_level = event.getZoomLevel();
//                SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putString(Constants.CURRENT_LOCATION_ZOOM, "" + zoom_level);
//                editor.commit();
                return true;
            }
        }, DEFAULT_INACTIVITY_DELAY_IN_MILLISECS));

        mMapView.addMapListener(new MapListener() {

            @Override
            public boolean onScroll(ScrollEvent event) {
                return true;
            }



            @Override
            public boolean onZoom(ZoomEvent event) {
//                Log.d("ZOOM TO", "" + event.getZoomLevel());
                double zoom_level = event.getZoomLevel();

                if(zoom_level > MIN_ZOOM_LEVEL_TO_SHOW_BUBBLE) {
                    if(! mMapView.getOverlays().contains(folderOverlayParkingSpot)) {
                        mMapView.getOverlays().add(folderOverlayParkingSpot);
                        mMapView.invalidate();
                    }
                }
                else {
                    if(mMapView.getOverlays().contains(folderOverlayParkingSpot)) {
                        mMapView.getOverlays().remove(folderOverlayParkingSpot);

                        Log.d("DIPANGGIL", "INII");
                        for (Overlay item:folderOverlayParkingZone.getItems()) {
                            ParkingZonePolygon polygon = (ParkingZonePolygon) item;
                            ParkingZone p = polygon.getParkingZone();

                            if(!polygon.getInfoWindow().isOpen()) {
                                polygon.getInfoWindow().open(polygon, p.getCenterGeopoint(), 0, 0);
                                Log.d("TAMPILKAN", p.getCenter_latitude() + " " + p.getCenter_longitude());
                            }
                        }
                        mMapView.invalidate();
                    }
                }

                return true;
            }
        });

    }

    private void showParkingSlots() {
        OverlayManager om = mMapView.getOverlayManager();
        Set set = markers.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            ParkingSpotMarker existing_marker = (ParkingSpotMarker) iterator.next();
            om.add(existing_marker);
        }
//        for(int i=0; i < markers.length; i++) {
//            int new_id = parkingSlots[i].getId();
//            int new_parking_status = parkingSlots[i].getParking_status();
//            ParkingSpotMarker existing_marker = markers.get(new_id);
//            if(existing_marker != null) {
//                // check is current & previous data has changed?
//                if(existing_marker.getParkingSpot().getMarker_status() != parkingSlots[i].getMarker_status()) {
//                    om.remove(existing_marker);
//                    markers.remove(new_id);
//                    ParkingSpotMarker new_marker = setParkingSpotAsMarker(mMapView, parkingSlots[i]);
//                    om.add(new_marker);
//                    markers.put(new_id, new_marker);
//                }
//            }
//            else {
//                ParkingSpotMarker new_marker = setParkingSpotAsMarker(mMapView, parkingSlots[i]);
//                om.add(new_marker);
//                markers.put(new_id, new_marker);
//            }
//        }

    }
//    public boolean isParkingSlotsVisibile() {
//        return parkingSlotsVisibile;
//    }
//
//    public void setParkingSlotsVisibile(boolean parkingSlotsVisibile) {
//        this.parkingSlotsVisibile = parkingSlotsVisibile;
//    }
//
//    public boolean isParkingZonesVisibile() {
//        return parkingZonesVisibile;
//    }
//
//    public void setParkingZonesVisibile(boolean parkingZonesVisibile) {
//        this.parkingZonesVisibile = parkingZonesVisibile;
//    }

//    boolean parkingSlotsVisibile;
//    boolean parkingZonesVisibile;


    public ParkingZonePolygon findParkingZoneByID(int id) {
        ParkingZonePolygon ret = null;

        for (Overlay item: folderOverlayParkingZone.getItems()) {
            ParkingZonePolygon tmp = (ParkingZonePolygon) item;
            if(tmp.getParkingZone().getId() == id) {
                ret = tmp;
                break;
            }
        };
        return ret;
    }

    public void modifyParkingZones(ParkingZone[] parkingZones) {

        for(int k=0; k < parkingZones.length; k++) {

            if(parkingZones[k] != null) {
                int id = parkingZones[k].getId();
                ParkingZonePolygon polygon = findParkingZoneByID(id);
                if(polygon != null) {

                    ParkingZone existing_pz = polygon.getParkingZone();
                    boolean authorized = parkingZones[k].isAuthorized();
                    LocalDateTime new_ts_update = parkingZones[k].getTs_update();
                    ParkingSpot [] parking_spots = parkingZones[k].getParking_spots();

                    existing_pz = parkingZones[k];
                    polygon.setParkingZone(existing_pz);


                    if(parking_spots != null) {
                        for(int i=0; i < parking_spots.length; i++) {
                            int new_id = parking_spots[i].getId();
                            int new_parking_status = parking_spots[i].getParking_status();
                            ParkingSpotMarker existing_marker = markers.get(new_id);

                            ParkingSpotMarker new_marker = setParkingSpotAsMarker(mMapView, parking_spots[i]);
                            folderOverlayParkingSpot.add(new_marker);
                        }



                    }
                }
            }
        }
        mMapView.getOverlays().add(folderOverlayParkingSpot);
        mMapView.invalidate();
    }

    public void addParkingZones(ParkingZone[] parkingZones) {

        if(parkingZones == null)
            return;


        for(int i=0; i < parkingZones.length; i++) {
            int geopoint_cnt = parkingZones[i].getGeoPoints().size();

            if(geopoint_cnt > 0) {

                final ParkingZonePolygon tmp_polygon = new ParkingZonePolygon(mMapView, parkingZones[i]);
                tmp_polygon.setPoints(parkingZones[i].getGeoPoints());
                tmp_polygon.setTitle(parkingZones[i].getName());

//                tmp_polygon.getOutlinePaint().setColor(Color.parseColor("#990000FF"));
                tmp_polygon.getOutlinePaint().setStrokeWidth(2);
//                tmp_polygon.getFillPaint().setColor(Color.parseColor("#330000FF"));

                ParkingZoneInfoWindow infoWindow = new ParkingZoneInfoWindow(
                        R.layout.bubble_parking_zone_layout,
                        mMapView,
                        this.device_uuid,
                        tmp_polygon
                );

                tmp_polygon.setInfoWindow(infoWindow);
                tmp_polygon.setOnClickListener(new Polygon.OnClickListener() {
                    @Override
                    public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                        hideAllInfoWindow();
                        mMapView.getController().animateTo(eventPos, DEFAULT_ZOOM_PARKING_SPOT, (long) DEFAULT_DELAY_ANIMATION);
                        polygon.setInfoWindowLocation(eventPos);
                        polygon.showInfoWindow();
                        return true;
                    }
                });


                tmp_polygon.setInfoWindowLocation(parkingZones[i].getCenterGeopoint());
                tmp_polygon.showInfoWindow();
//                tmp_polygon.getInfoWindow().open(tmp_polygon, tmp_polygon.getParkingZone().getCenterGeopoint(),0, 0);

                folderOverlayParkingZone.add(tmp_polygon);
            }
        }

        if(!mMapView.getOverlays().contains(folderOverlayParkingZone))
            mMapView.getOverlays().add(folderOverlayParkingZone);
        mMapView.invalidate();
    }

    public void addLocalDatastore() {
        if (datastore == null)
            datastore = new OSMBookmarkDatastore(this);
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

        Log.d("DialogLong", "" + p.getLongitude());
        Log.d("DialogLat", "" + p.getLatitude());
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
                lonD = Double.parseDouble(lon.getText().toString());
            } catch (Exception ex) {
                valid = false;
            }

            if (!mMapView.getTileSystem().isValidLatitude(latD))
                valid = false;
            if (!mMapView.getTileSystem().isValidLongitude(lonD))
                valid = false;

            if (valid) {
                ParkingSpot newParkingSpot = new ParkingSpot();

                ParkingSpotMarker m = new ParkingSpotMarker(mMapView, newParkingSpot);
                m.setId(UUID.randomUUID().toString());
                m.setTitle(UUID.randomUUID().toString());
                m.setSubDescription(UUID.randomUUID().toString());
                m.setIcon(getResources().getDrawable(R.drawable.unconfirmed_0));

                GeoPoint geoPoint = new GeoPoint(latD, lonD);
                m.setPosition(geoPoint);
                String tmp_status = "Unconfirmed (confidence 60%)";
                ParkingSpotInfoWindow infoWindow = new ParkingSpotInfoWindow(
                    R.layout.bubble_parking_spot_layout,
                    mMapView,
                    geoPoint,
                    device_uuid,
                    tmp_status,
                    LocalDateTime.now(),
                    m
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