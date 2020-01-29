package au.edu.anu.cs.sparkee.helper;

import android.content.Intent;
import android.os.StrictMode;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import au.edu.anu.cs.sparkee.Constants;

public class AMQPConnectionHelper {
    private static Connection connection;
    private static Channel channel;

    public Connection getConnection() {
        return connection;
    }

    public Channel getChannel() {
        return channel;
    }

    private AMQPConnectionHelper() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(Constants.SERVER_HOST);
            factory.setUsername(Constants.RABBIT_USER);
            factory.setPassword(Constants.RABBIT_PASS);
            factory.setAutomaticRecoveryEnabled(true);
            factory.setNetworkRecoveryInterval(10000);
            connection = factory.newConnection();

            // enable automatic recovery (e.g. Java client prior 4.0.0)
//            factory.setAutomaticRecoveryEnabled(true);

            channel = connection.createChannel();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(TimeoutException te) {
            te.printStackTrace();
        }

    }
    private static AMQPConnectionHelper singleton;

    public static AMQPConnectionHelper getInstance() {
        if(singleton == null)
            singleton = new AMQPConnectionHelper();
        return singleton;
    }
}
