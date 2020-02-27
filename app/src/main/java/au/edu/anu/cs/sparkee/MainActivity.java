package au.edu.anu.cs.sparkee;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import au.edu.anu.cs.sparkee.helper.AMQPChannelHelper;
import au.edu.anu.cs.sparkee.helper.DataHelper;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST = 1240;
    private Context mContext = MainActivity.this;
    private GpsMyLocationProvider mGpsMyLocationProvider;
    private NavController navController;
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            int i=1;
            for (String permission : permissions) {
                Log.d("PERM " + i, permission);
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
    }


    public void launchSParkeeMessagingService() {
//        Intent dataService = new Intent(this, DataHelper.class);
//        startService(dataService);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        ButterKnife.bind(this);

        SharedPreferences sharedPref =  getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);

        String device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");

        if(device_uuid.equalsIgnoreCase("")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            String newUUID = UUID.randomUUID().toString();
            editor.putString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, newUUID);
            editor.commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] PERMISSIONS = {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET,
            };

            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST);

                String pathname = Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + SqlTileWriter.DATABASE_FILENAME;
                Log.d("Lokasi", pathname);
                File dbFile = new File(pathname);
//                if (dbFile.exists()) {
//                    Log.d("File DB", "ADA");
//                } else {
//                    Log.d("File DB", "BELUM ADA");
//                }

                showSettingsDialog();

            } else {
                setContentView(R.layout.activity_main);
                Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(myToolbar);

                getSupportActionBar().setDisplayShowHomeEnabled(true);
//                getSupportActionBar().setLogo(R.mipmap.ic_launcher);
                getSupportActionBar().setDisplayUseLogoEnabled(true);

                Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
                BottomNavigationView navView = findViewById(R.id.nav_view);

                AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.navigation_map,
                        R.id.navigation_activity,
                        R.id.navigation_profile
                ).build();


                navController = Navigation.findNavController(this, R.id.nav_host_fragment);
                NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

                NavigationUI.setupWithNavController(navView, navController);
                launchSParkeeMessagingService();
            }
        } else {
            //do here
            Log.d("SDK < 23", "NO");
        }

    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_nav_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String label = (String) navController.getCurrentDestination().getLabel();
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.configure:
                Toast.makeText(this, "Menu at " + label , Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return true;
    }
}
