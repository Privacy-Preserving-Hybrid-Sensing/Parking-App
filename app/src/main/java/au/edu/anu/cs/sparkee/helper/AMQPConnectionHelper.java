package au.edu.anu.cs.sparkee.helper;

import android.os.StrictMode;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import au.edu.anu.cs.sparkee.Constants;

public class AMQPConnectionHelper {
    private static Connection connection;
    private static Channel outgoing_channel;
    private static Channel incoming_channel;

    public Channel getOutgoingChannel() {
        return outgoing_channel;
    }

    private AMQPConnectionHelper() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(Constants.RABBIT_HOST);
            factory.setUsername(Constants.RABBIT_USER);
            factory.setPassword(Constants.RABBIT_PASS);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(10000);
            factory.setTopologyRecoveryEnabled(true);
            connection = factory.newConnection();

            outgoing_channel = connection.createChannel();
            incoming_channel = connection.createChannel();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(TimeoutException te) {
            te.printStackTrace();
        }

    }

    private static AMQPConnectionHelper singleton;

    public void send(JSONObject obj) throws NullPointerException, AlreadyClosedException, IOException {
        String str_json = obj.toString();
        outgoing_channel.basicPublish(Constants.RABBIT_EXCHANGE_OUTGOING_NAME, Constants.DEFAULT_PARTICIPANT_TO_SERVER_ROUTING_KEY, null, str_json.getBytes("UTF-8"));
    }

    public static AMQPConnectionHelper getInstance() {
        if(singleton == null)
            singleton = new AMQPConnectionHelper();
        return singleton;
    }
}
