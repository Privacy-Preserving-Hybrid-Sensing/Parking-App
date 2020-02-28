package au.edu.anu.cs.sparkee.ui.map;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.threeten.bp.LocalDateTime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.R;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;
import au.edu.anu.cs.sparkee.model.ParkingSpot;
import au.edu.anu.cs.sparkee.model.ParkingZone;
import au.edu.anu.cs.sparkee.model.Participation;
import au.edu.anu.cs.sparkee.ui.map.overlay.HashFolderOverlay;
import au.edu.anu.cs.sparkee.ui.map.overlay.infowindow.ParkingSpotInfoWindow;
import au.edu.anu.cs.sparkee.ui.map.overlay.infowindow.ParkingZoneInfoWindow;
import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingSpotMarker;
import au.edu.anu.cs.sparkee.ui.map.overlay.marker.ParkingZonePolygon;

import static au.edu.anu.cs.sparkee.Constants.DEFAULT_ZOOM_PARKING_ZONE_NEAR;

public class MapFragment extends Fragment {

    private MapViewModel mapViewModel;
    private boolean isInitView = true;
    protected MapView mMapView;
    private Location currentLocation;

    protected ImageButton btCenterMap;

    private MyLocationNewOverlay mLocationOverlay;

    protected static final int DEFAULT_INACTIVITY_DELAY_IN_MILLISECS = 200;
    protected static final double MIN_ZOOM_LEVEL_TO_SHOW_BUBBLE = 19.9;

    private Context context;
    private String DEFAULT_LON = "149.120385";
    private String DEFAULT_LAT = "-35.275514";

    private long DEFAULT_DELAY_ANIMATION = 200;

    private String device_uuid;
    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();

    private HashFolderOverlay folderOverlayParkingSpot;
    private HashFolderOverlay folderOverlayParkingZone;

    private boolean initGPS;
    private boolean initHTTP;

    private SharedPreferences sharedPref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, null);
        context = this.getActivity();
        sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
        device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");

        mMapView = v.findViewById(R.id.mapview);
        mMapView.setTilesScaledToDpi(true);
        mMapView.setMultiTouchControls(true);
        mMapView.setFlingEnabled(true);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        double map_lon =  Double.parseDouble(sharedPref.getString(Constants.CURRENT_LOCATION_LON, DEFAULT_LON));
        double map_lat = Double.parseDouble(sharedPref.getString(Constants.CURRENT_LOCATION_LAT, DEFAULT_LAT));

        IGeoPoint geoPoint =new GeoPoint(map_lat, map_lon);
        mMapView.getController().animateTo(geoPoint);
        mMapView.getController().setZoom(Constants.DEFAULT_ZOOM_PARKING_ZONE_FAR);

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        mapViewModel.startViewModel();

        mLocationOverlay = new MyLocationNewOverlay(mMapView);

        folderOverlayParkingZone = new HashFolderOverlay();
        folderOverlayParkingSpot = new HashFolderOverlay();


        mapViewModel.getServerConnectionEstablished().observe( getViewLifecycleOwner(), new Observer<Pair<Boolean, String>>() {
            @Override
            public void onChanged(@Nullable Pair<Boolean, String> stat) {
                initHTTP = stat.first;
                if(stat.first == Boolean.FALSE && ! stat.second.equalsIgnoreCase(""))
                    Toast.makeText(context, stat.second, Toast.LENGTH_SHORT).show();
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
            }
            }
        });

        mapViewModel.getParkingZones().observe( getViewLifecycleOwner(), new Observer<  HashMap<Integer, ParkingZone> >() {

            @Override
            public void onChanged(@Nullable HashMap<Integer, ParkingZone> parkingZones) {
                Log.d("PZ", "" + parkingZones.size());
                drawParkingZones(parkingZones);
            }
        });

        mapViewModel.getParkingSpots().observe( getViewLifecycleOwner(), new Observer<  HashMap<Integer, ParkingSpot> >() {
            @Override
            public void onChanged(@Nullable HashMap<Integer, ParkingSpot> parkingSpots) {
                Log.d("PS", "" + parkingSpots.size());
                drawParkingSpots(parkingSpots);
            }
        });

        mapViewModel.getChangedParkingSpot().observe( getViewLifecycleOwner(), new Observer< ParkingSpot>() {
            @Override
            public void onChanged(@Nullable ParkingSpot parkingSpot) {
                Log.d("CHANGED PS", "" + parkingSpot.getId());
                redrawParkingSpot(parkingSpot);
            }
        });

        mapViewModel.getChangedParkingZone().observe( getViewLifecycleOwner(), new Observer< ParkingZone>() {
            @Override
            public void onChanged(@Nullable ParkingZone parkingZone) {
                Log.d("CHANGED PZ", "" + parkingZone.getId());
                redrawParkingZone(parkingZone);
            }
        });

        mapViewModel.getCreditBalance().observe( getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(@Nullable Integer val) {
                TextView ic_credit = (TextView) getView().findViewById(R.id.ic_credit);
//                if(initHTTP)
                    ic_credit.setText(val.toString());
//                else
//                    ic_credit.setText("-");

            }
        });

        mapViewModel.getParticipations().observe( getViewLifecycleOwner(), new Observer< HashMap<Integer, Participation> >() {

            @Override
            public void onChanged(@Nullable HashMap<Integer, Participation> val) {
                Iterator<Map.Entry<Integer,Participation>> itr_participation = val.entrySet().iterator();

                while(itr_participation.hasNext()) {
                    Map.Entry<Integer,Participation> psm = itr_participation.next();
                    Participation part = psm.getValue();
                    int spot_id = part.getSpot_id();
                    LocalDateTime spot_update = part.getTs_update();

                    ParkingSpotMarker existingMarker = (ParkingSpotMarker) folderOverlayParkingSpot.get(spot_id);
                    if(existingMarker != null && existingMarker.getParkingSpot().getTs_update().isBefore(spot_update)) {
                        // TODO: change icon
                        Log.d("ICON", "" + spot_id);
                        int marker_status = existingMarker.getParkingSpot().getParking_status();

                        int participation = Constants.MARKER_PARKING_CATEGORY_PARTICIPATION;

                        int iconId = ParkingSpotMarker.getMarkerIcon(marker_status, participation);
                        Drawable d = getResources().getDrawable(iconId);

                        existingMarker.setIcon( d );
                    }
                }
            }
        });



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

        addHandleMapEvent();
        addOverlayRotation();

        mapViewModel.sendRequestProfileSummary();
        mapViewModel.sendRequestZonesAll();

    }

    private void redrawParkingSpot(ParkingSpot freshParkingSpot) {
        if(freshParkingSpot == null )
            return;

        if(folderOverlayParkingSpot == null)
            return;

        ParkingSpotMarker existingMarker = (ParkingSpotMarker) folderOverlayParkingSpot.get(freshParkingSpot.getId());
        if(existingMarker == null)
            return;

        int participation = Constants.MARKER_PARKING_CATEGORY_DEFAULT;
        int iconId = ParkingSpotMarker.getMarkerIcon(freshParkingSpot.getParking_status(), participation);
        Log.d("FRESH STATUS", "" + freshParkingSpot.getParking_status());
        Drawable d = getResources().getDrawable(iconId);

        existingMarker.setIcon( d );
        existingMarker.getParkingSpot().setTs_update(freshParkingSpot.getTs_update());
        existingMarker.getParkingSpot().setConfidence_level(freshParkingSpot.getConfidence_level());
        existingMarker.getParkingSpot().setParking_status(freshParkingSpot.getParking_status());
        existingMarker.getParkingSpot().setVote_available(freshParkingSpot.getVote_available());
        existingMarker.getParkingSpot().setVote_unavailable(freshParkingSpot.getVote_unavailable());
        mMapView.invalidate();

    }

    private void redrawParkingZone(ParkingZone freshParkingZone) {
        if(freshParkingZone == null )
            return;

        if(folderOverlayParkingSpot == null)
            return;

        ParkingZonePolygon existingPolygon = (ParkingZonePolygon) folderOverlayParkingZone.get(freshParkingZone.getId());
        if(existingPolygon == null)
            return;

        Log.d("FRESH AVAILABLE", "" + freshParkingZone.getSpot_available());
        int available = freshParkingZone.getSpot_available();
        int unavailable = freshParkingZone.getSpot_unavailable();
        int total = freshParkingZone.getSpot_total();
        int undefined = freshParkingZone.getSpot_undefined();
        LocalDateTime ts = freshParkingZone.getTs_update();
        existingPolygon.getParkingZone().setSpot_available(available);
        existingPolygon.getParkingZone().setSpot_unavailable(unavailable);
        existingPolygon.getParkingZone().setSpot_total(total);
        existingPolygon.getParkingZone().setSpot_undefined(undefined);
        existingPolygon.getParkingZone().setTs_update(ts);
        ((ParkingZoneInfoWindow) existingPolygon.getInfoWindow()).getParkingZone().setSpot_available(available);
        ((ParkingZoneInfoWindow) existingPolygon.getInfoWindow()).getParkingZone().setSpot_unavailable(unavailable);
        ((ParkingZoneInfoWindow) existingPolygon.getInfoWindow()).getParkingZone().setSpot_total(total);
        ((ParkingZoneInfoWindow) existingPolygon.getInfoWindow()).getParkingZone().setSpot_undefined(undefined);
        ((ParkingZoneInfoWindow) existingPolygon.getInfoWindow()).getParkingZone().setTs_update(ts);
        ((ParkingZoneInfoWindow) existingPolygon.getInfoWindow()).updateInfoWindow();

        Log.d("DEB", "" + available + " " + unavailable + " " + total + " " + undefined + " " + ts.toString());
        mMapView.invalidate();

    }


    private void drawParkingSpots(HashMap<Integer, ParkingSpot> parkingSpots) {
        if(parkingSpots == null)
            return;

        // ADD NEW PARKING SPOTS
        Iterator itr_add = parkingSpots.entrySet().iterator();
        while(itr_add.hasNext()) {
            Map.Entry entry = (Map.Entry) itr_add.next();
            ParkingSpot freshParkingSpot = (ParkingSpot) entry.getValue();
            ParkingSpotMarker existingMarker = (ParkingSpotMarker) folderOverlayParkingSpot.get(freshParkingSpot.getId());

            if( existingMarker == null ) {

                ParkingSpotMarker m = new ParkingSpotMarker(mMapView, freshParkingSpot);

                m.setId("" + freshParkingSpot.getId() );
                m.setTitle("" + freshParkingSpot.getId() );
                m.setSubDescription("" + freshParkingSpot.getId() );
                GeoPoint geoPoint = freshParkingSpot.getGeoPoint();
                m.setPosition( geoPoint );

                String tmp_status = "";
                double confidence_level = freshParkingSpot.getConfidence_level();
                int tmp_confidence = (int) Math.round(confidence_level * 100);
                int marker_status = freshParkingSpot.getParking_status();

                int participation = Constants.MARKER_PARKING_CATEGORY_DEFAULT;
                int iconId = ParkingSpotMarker.getMarkerIcon(marker_status, participation);
                Drawable d = getResources().getDrawable(iconId);

                m.setIcon( d );

                tmp_status = ParkingSpotMarker.getMarkerStatusTxt(marker_status, tmp_confidence);

                ParkingSpotInfoWindow infoWindow = new ParkingSpotInfoWindow(
                        R.layout.bubble_parking_spot_layout,
                        mMapView,
                        geoPoint,
                        device_uuid,
                        tmp_status,
                        freshParkingSpot.getTs_update(),
                        m,
                        mapViewModel
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

                folderOverlayParkingSpot.add(freshParkingSpot.getId(), m);
            }
        }

        if(!mMapView.getOverlays().contains(folderOverlayParkingSpot))
            mMapView.getOverlays().add(folderOverlayParkingSpot);
        mMapView.invalidate();
    }


    private void drawParkingZones(HashMap<Integer, ParkingZone> parkingZones) {
        if(parkingZones == null)
            return;

        // ADD NEW POLYGON
        Iterator itr = parkingZones.entrySet().iterator();
        while(itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            final ParkingZone parkingZone = (ParkingZone) entry.getValue();
            int geopoint_cnt = parkingZone.getGeoPoints().size();

            ParkingZonePolygon tmp_polygon = (ParkingZonePolygon) folderOverlayParkingZone.get(parkingZone.getId());
            if(tmp_polygon == null && geopoint_cnt > 0) {

                tmp_polygon = new ParkingZonePolygon(mMapView, parkingZone);
                tmp_polygon.setPoints(parkingZone.getGeoPoints());
                tmp_polygon.setTitle(parkingZone.getName());
                tmp_polygon.getOutlinePaint().setStrokeWidth(2);

                int polygon_color = ParkingZonePolygon.getPolygonColor(parkingZone.getSpot_total(), parkingZone.getSpot_available());
                int colorColor = ContextCompat.getColor(getContext(), polygon_color);
                tmp_polygon.getFillPaint().setColor( colorColor );

                ParkingZoneInfoWindow infoWindow = new ParkingZoneInfoWindow(
                        R.layout.bubble_parking_zone_layout,
                        mMapView,
                        this.device_uuid,
                        tmp_polygon,
                        mapViewModel
                );

                tmp_polygon.setInfoWindow(infoWindow);
                tmp_polygon.setOnClickListener(new Polygon.OnClickListener() {
                    @Override
                    public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                        Log.d("ZON DETIL", "" + parkingZone.getId());

                        if(polygon.isInfoWindowOpen()) {
                            hideAllInfoWindow();
                        }
                        else {
                            hideAllInfoWindow();
                            mMapView.getController().animateTo(eventPos, DEFAULT_ZOOM_PARKING_ZONE_NEAR, (long) DEFAULT_DELAY_ANIMATION);
                            polygon.setInfoWindowLocation(eventPos);
                            polygon.showInfoWindow();
                            mapViewModel.sendRequestZone(parkingZone.getId());
                        }

                        mapViewModel.sendRequestZoneSpotsAll(parkingZone.getId());
                        String subscription_token = parkingZone.getSubscription_token();
                        mapViewModel.subscribeAsyncChannel(subscription_token);

                        return true;
                    }
                });


                tmp_polygon.setInfoWindowLocation(parkingZone.getCenterGeopoint());
                tmp_polygon.showInfoWindow();

                folderOverlayParkingZone.add(parkingZone.getId(), tmp_polygon);
            }
            else {
                int polygon_color = ParkingZonePolygon.getPolygonColor(parkingZone.getSpot_total(), parkingZone.getSpot_available());
                int colorColor = ContextCompat.getColor(getContext(), polygon_color);
                tmp_polygon.getFillPaint().setColor( colorColor );
                ParkingZoneInfoWindow pziw = (ParkingZoneInfoWindow) tmp_polygon.getInfoWindow();
                pziw.setParkingZone(parkingZone);
                tmp_polygon.showInfoWindow();
//                ParkingZonePolygon po = (ParkingZonePolygon) folderOverlayParkingZone.get(parkingZone.getId());
//                po = tmp_polygon;
            }

        }

        if(!mMapView.getOverlays().contains(folderOverlayParkingZone))
            mMapView.getOverlays().add(folderOverlayParkingZone);
        mMapView.invalidate();

    }

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

    @Override
    public void onPause() {
        super.onPause();
        mapViewModel.stopViewModel();
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
                SharedPreferences.Editor editor = sharedPref.edit();
                double lon = map_center.getLongitude();
                double lat = map_center.getLatitude();
                editor.putString(Constants.CURRENT_LOCATION_LON, "" + lon);
                editor.putString(Constants.CURRENT_LOCATION_LAT, "" + lat);
                editor.commit();
                return true;
            }



            @Override
            public boolean onZoom(ZoomEvent event) {
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
}