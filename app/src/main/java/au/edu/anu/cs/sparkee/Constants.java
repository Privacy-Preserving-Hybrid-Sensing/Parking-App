package au.edu.anu.cs.sparkee;

public class Constants {
    public static final String SERVER_HOST = "ec2-3-133-91-181.us-east-2.compute.amazonaws.com";
    public static final String RABBIT_USER = "mobile";
    public static final String RABBIT_PASS = "ieGh4thi";
    public static final String RABBIT_EXCHANGE_OUTGOING_NAME = "amq.topic";
    public static final String RABBIT_EXCHANGE_INCOMING_NAME = "amq.topic";
    public static final String RABBIT_EXCHANGE_PARKING_SLOTS_TOPIC = "parking_slot.zone.*";

    public static final String DEFAULT_PARTICIPANT_TO_SERVER_ROUTING_KEY = "PARTICIPANT_TO_SERVER";
    public static final String BROADCAST_ACTION_IDENTIFIER = "au.edu.anu.cs.sparkee.broadcast.AMQP";

    public static final String SHARED_PREFERENCE_FILE_SPARKEE = "au.edu.anu.cs.sparkee.shared_preference";
    public static final String SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID = "au.edu.anu.cs.sparkee.shared_preference.host.uuid";
    public static final String DEVICE_TYPE = "android";

    public static final int MARKER_PARTICIPATION_UNAVAILABLE_SENT = -100;
    public static final int MARKER_PARTICIPATION_AVAILABLE_SENT = 100;

    public static final int MARKER_PARTICIPATION_UNAVAILABLE_RECEIVED = -101;
    public static final int MARKER_PARTICIPATION_AVAILABLE_RECEIVED = 101;

    public static final int MARKER_PARKING_UNCONFIRMED = 0;
    public static final int MARKER_PARKING_AVAILABLE_CONFIDENT_1 = 1;
    public static final int MARKER_PARKING_AVAILABLE_CONFIDENT_2 = 2;
    public static final int MARKER_PARKING_AVAILABLE_CONFIRMED = 3;

    public static final int MARKER_PARKING_UNAVAILABLE_CONFIDENT_1 = -1;
    public static final int MARKER_PARKING_UNAVAILABLE_CONFIDENT_2 = -2;
    public static final int MARKER_PARKING_UNAVAILABLE_CONFIRMED = -3;

}