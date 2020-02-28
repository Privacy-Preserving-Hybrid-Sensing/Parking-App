package au.edu.anu.cs.sparkee;

public class Constants {

//    public static final String BASE_URL = "http://192.168.0.218:8000";
//    public static final String BASE_URL = "http://10.234.0.5:8000";
    public static final String BASE_URL = "http://ec2-3-133-91-181.us-east-2.compute.amazonaws.com:8000";

    public static final String URL_API_ZONES_ALL = "/api/zones/all";
    public static final String URL_API_ZONES_ID = "/api/zones/%d";
    public static final String URL_API_ZONES_SPOTS_ALL = "/api/zones/%d/spots/all";
    public static final String URL_API_ZONES_SUBSCRIBE = "/api/zones/%d/subscribe";
    public static final String URL_API_PROFILE_SUMMARY = "/api/profile/summary";
    public static final String URL_API_PARTICIPATE = "/api/participate/%d/%d/%s";
    public static final String URL_API_PROFILE_PARTICIPATION_LATEST = "/api/profile/participations/latest";
    public static final String URL_API_PROFILE_PARTICIPATION_NUM_LAST = "/api/profile/participations/%d";


    public static final String RABBIT_HOST = "ec2-3-133-91-181.us-east-2.compute.amazonaws.com";
    public static final String RABBIT_USER = "mobile";
    public static final String RABBIT_PASS = "ieGh4thi";
    public static final String RABBIT_EXCHANGE_OUTGOING_NAME = "amq.topic";
    public static final String RABBIT_EXCHANGE_INCOMING_NAME = "amq.topic";
    public static final String RABBIT_EXCHANGE_PARKING_SLOTS_TOPIC = "parking_slot.zone.*";


    public static final double DEFAULT_ZOOM_PARKING_ZONE_FAR = 18.0;
    public static final double DEFAULT_ZOOM_PARKING_ZONE_MED = 20.0;
    public static final double DEFAULT_ZOOM_PARKING_ZONE_NEAR = 21.0;

    public static final double DEFAULT_ZOOM_PARKING_SPOT = 22.0;

    public static final String DEFAULT_PARTICIPANT_TO_SERVER_ROUTING_KEY = "PARTICIPANT_TO_SERVER";
    public static final String BROADCAST_AMQP_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.AMQP";
    public static final String BROADCAST_DATA_HELPER_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.HTTP.response";
    public static final String BROADCAST_HTTP_BODY_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.HTTP.body";
    public static final String BROADCAST_DATA_STATUS_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.HTTP.status";
    public static final String BROADCAST_STATUS_OK = "au.edu.anu.cs.sparkee.broadcast.HTTP.status.OK";
    public static final String BROADCAST_STATUS_ERR = "au.edu.anu.cs.sparkee.broadcast.HTTP.status.ERR";

    public static final String CURRENT_LOCATION_LON = "au.edu.anu.cs.sparkee.current.location.lon";
    public static final String CURRENT_LOCATION_LAT = "au.edu.anu.cs.sparkee.current.location.lat";
    public static final String CURRENT_LOCATION_ZOOM = "au.edu.anu.cs.sparkee.current.location.zoom";

    public static final String SHARED_PREFERENCE_FILE_SPARKEE = "au.edu.anu.cs.sparkee.shared_preference";
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID = "au.edu.anu.cs.sparkee.shared_preference.host.uuid";

    public static final int MARKER_PARTICIPATION_UNAVAILABLE_RECEIVED = -101;
    public static final int MARKER_PARTICIPATION_AVAILABLE_RECEIVED = 101;


    public static final int MARKER_PARKING_CATEGORY_DEFAULT = 1000;
    public static final int MARKER_PARKING_CATEGORY_PARTICIPATION = 1001;


    public static final int MARKER_PARKING_UNCONFIRMED_DEFAULT = 0;
    public static final int MARKER_PARKING_AVAILABLE_CONFIDENT_1_DEFAULT = 1;
    public static final int MARKER_PARKING_AVAILABLE_CONFIDENT_2_DEFAULT = 2;
    public static final int MARKER_PARKING_AVAILABLE_CONFIDENT_3_DEFAULT = 3;

    public static final int MARKER_PARKING_UNAVAILABLE_CONFIDENT_1_DEFAULT = -1;
    public static final int MARKER_PARKING_UNAVAILABLE_CONFIDENT_2_DEFAULT = -2;
    public static final int MARKER_PARKING_UNAVAILABLE_CONFIDENT_3_DEFAULT = -3;

}