package au.edu.anu.cs.sparkee;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST = 112;
    private Context mContext = MainActivity.this;
    private GpsMyLocationProvider mGpsMyLocationProvider;

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(mContext, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if(mGpsMyLocationProvider != null) {
//            mGpsMyLocationProvider.stopLocationProvider();
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST);
                Log.d("HAS AKSES", "YA");
            } else {
                Log.d("HAS AKSES", "NO");
            }
        } else {
            //do here
            Log.d("SDK < 23", "NO");
        }

        String pathname = Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME;
        Log.d("Lokasi", pathname);
        File dbFile = new File(pathname);
        if (dbFile.exists()) {
            Log.d("File DB", "ADA");
        } else {
            Log.d("File DB", "BELUM ADA");
        }

//        mGpsMyLocationProvider = new GpsMyLocationProvider(getApplicationContext());
//
//        mGpsMyLocationProvider.startLocationProvider(new IMyLocationConsumer() {
//            @Override
//            public void onLocationChanged(Location location, IMyLocationProvider source) {
//                Log.d("Long", "" + location.getLongitude());
//                Log.d("Lat", "" + location.getLatitude());
//                Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(location.getLongitude()) + "\nLatitude:" + Double.toString(location.getLatitude()), Toast.LENGTH_LONG).show();
//            }
//        });

        setContentView(R.layout.activity_main);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_activity, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


//        Toast.makeText(getApplicationContext(),"my location "+ provider.getLastKnownLocation(),Toast.LENGTH_LONG).show();
    }

}
