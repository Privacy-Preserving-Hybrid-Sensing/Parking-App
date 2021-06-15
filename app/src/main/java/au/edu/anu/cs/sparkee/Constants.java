package au.edu.anu.cs.sparkee;

public class Constants {

    public static final int HISTORY_TYPE_EMPTY = 2000;
    public static final int HISTORY_TYPE_SUBSCRIPTION = 2001;
    public static final int HISTORY_TYPE_PARTICIPATION = 2002;

    public static final String HTTP_IP_PORT = "103.219.249.10:48000";
    public static final String HTTP_IP_PORT_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.HTTP.IP_PORT";

    public static final String URL_API_ZONES_ALL = "/api/zones/all";
    public static final String URL_API_ZONES_ID = "/api/zones/%d";
    public static final String URL_API_ZONES_SPOTS_ALL = "/api/zones/%d/spots/all";
    public static final String URL_API_ZONES_SUBSCRIBE = "/api/zones/%d/subscribe";
    public static final String URL_API_PROFILE_SUMMARY = "/api/profile/summary";
    public static final String URL_API_PARTICIPATE = "/api/participate/%d/%d/%s";
    public static final String URL_API_PROFILE_HISTORY_NUM_LAST = "/api/profile/history/%d";

    // ZK URLS
    public static final String URL_API_ZK_GET_CRYPTO_INFO = "/api/zk/cryptoinfo";
    public static final String URL_API_ZK_REGISTER = "/api/zk/register";
    public static final String URL_API_ZK_CLAIM_VERIFY_CREDENTIAL = "/api/zk/claim-verify-credential";
    public static final String URL_API_ZK_CLAIM_VERIFY_Q = "/api/zk/claim-verify-q";
    public static final String URL_API_ZK_CLAIM_REWARD = "/api/zk/claim-reward";

    public static final String URL_API_ZK_DATA_SUBMISSION_ELIGIBILITY_RESULT = "/api/zk/submission-accepted-init-reward";

    // ZK MESSAGES
    public static final String ZK_MESSAGE_WAIT_BEFORE_SENDING_ANOTHER_PARTICIPATION_DATA =
            "Please wait before sending another submission.";
    public static final String ZK_MESSAGE_ELIGIBLE_TO_CLAIM_REWARD = "You are eligible to claim reward, claiming is in progres...";
    public static final String ZK_MESSAGE_NOT_ELIGIBLE_TO_CLAIM_REWARD = "You are not eligible to claim reward for your latest submission";
    public static final String ZK_MESSAGE_CLAIM_CREDENTIAL_VERIFIED = "Your credentials are verified, proceeding to computing new q";
    public static final String ZK_MESSAGE_CLAIM_CREDENTIAL_NOT_VERIFIED = "Your credentials can't be verified. Please contact our support";
    public static final String ZK_MESSAGE_CLAIM_Q_VERIFIED = "Your q are verified, proceeding to claiming reward";
    public static final String ZK_MESSAGE_CLAIM_Q_NOT_VERIFIED = "Your q can't be verified. Please contact our support";
    public static final String ZK_MESSAGE_CLAIM_REWARD_SUCCESS = "Reward claim success, your balance is now:";
    public static final String ZK_MESSAGE_CLAIM_REWARD_FAILED = "Your reward claim failed. Please contact our support";


    public static final String RABBIT_HOST = "103.219.249.10";
    public static final String RABBIT_HOST_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.AMQP.HOST";
    public static final String RABBIT_PORT_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.AMQP.PORT";
    public static final int  RABBIT_PORT = 5672;
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

    // ZK credentials and required values
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_S = "au.edu.anu.cs.sparkee.shared_preference.zk.s"; // secret s
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_Q = "au.edu.anu.cs.sparkee.shared_preference.zk.q"; // uid q
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_B = "au.edu.anu.cs.sparkee.shared_preference.zk.b"; //balance b
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_SIGN_Q = "au.edu.anu.cs.sparkee.shared_preference.zk.signq"; // sign q

    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_COMMITMENT_S = "au.edu.anu.cs.sparkee.shared_preference.zk.commitment.s"; // commitment s
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_COMMITMENT_Q = "au.edu.anu.cs.sparkee.shared_preference.zk.commitment.q"; // commitment q
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_COMMITMENT_B = "au.edu.anu.cs.sparkee.shared_preference.zk.commitment.b"; // commitment b

    // s_ used for first registration
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_S_ = "au.edu.anu.cs.sparkee.shared_preference.zk.s_";

    // mask q used in credit claiming
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_MASK_Q= "au.edu.anu.cs.sparkee.shared_preference.zk.maskq";

    // nzkpCm[s] data
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_COMMITMENT_RANDOM_S = "au.edu.anu.cs.sparkee.shared_preference.zk.commitment.random_s"; // commitment random_s
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_ZK_ZS = "au.edu.anu.cs.sparkee.shared_preference.zk.zs"; // secret s




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