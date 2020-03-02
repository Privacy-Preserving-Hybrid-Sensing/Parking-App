package au.edu.anu.cs.sparkee.helper;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;

import java.io.File;

import au.edu.anu.cs.sparkee.Constants;

public class OSMHelper {

    protected File db_file;

    public static final String TABLE="bookmarks";
    public static final String COLUMN_ID="markerid";
    public static final String COLUMN_LAT="lat";
    public static final String COLUMN_LON="lon";
    public static final String COLUMN_TITLE="title";
    public static final String COLUMN_DESC="description";
    protected SQLiteDatabase mDatabase;
    public static final String DATABASE_FILENAME = "bookmarks.mDatabase";
    private String device_uuid;

    private Fragment activity;

    public OSMHelper(Fragment activity) {
        this.activity = activity;
        Configuration.getInstance().getOsmdroidTileCache().mkdirs();
        db_file = new File(Configuration.getInstance().getOsmdroidTileCache().getAbsolutePath() + File.separator + DATABASE_FILENAME);

        try {
            mDatabase = SQLiteDatabase.openOrCreateDatabase(db_file, null);
            mDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                    COLUMN_LAT + " INTEGER , " +
                    COLUMN_LON + " INTEGER, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_ID + " TEXT, " +
                    COLUMN_DESC + " TEXT, PRIMARY KEY (" + COLUMN_ID + ") );");
        } catch (Throwable ex) {
            Log.e(IMapView.LOGTAG, "Unable to start the bookmark database. Check external storage availability.", ex);
        }
    }
}