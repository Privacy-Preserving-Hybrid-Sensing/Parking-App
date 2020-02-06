package au.edu.anu.cs.sparkee.service;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;

import com.rabbitmq.client.Envelope;

import java.io.IOException;

import au.edu.anu.cs.sparkee.Constants;
import au.edu.anu.cs.sparkee.helper.AMQPConnectionHelper;

public class SParkeeMessagingService extends IntentService {
    // Must create a default constructor
    public SParkeeMessagingService() {
        // Used to name the worker thread, important only for debugging.
        super("SParkeeMessagingService");
    }

    final AMQPConnectionHelper amqpConnectionHelper = AMQPConnectionHelper.getInstance();

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
        try {

            final Channel channel = amqpConnectionHelper.getOutgoingChannel();

            SharedPreferences sharedPref =  getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_FILE_SPARKEE, Context.MODE_PRIVATE);
            String device_uuid = sharedPref.getString(Constants.SHARED_PREFERENCE_KEY_SPARKEE_HOST_UUID, "");

//            Log.d("DEVICE UUID", device_uuid);

            String queueName = channel.queueDeclare().getQueue();
            String specificSubscriberUUIDTopic = "participant." + device_uuid;
            channel.queueBind(queueName, Constants.RABBIT_EXCHANGE_INCOMING_NAME, Constants.RABBIT_EXCHANGE_PARKING_SLOTS_TOPIC);

            channel.queueBind(queueName, Constants.RABBIT_EXCHANGE_INCOMING_NAME, specificSubscriberUUIDTopic);


            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, "",
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException {
                            String routingKey = envelope.getRoutingKey();
                            String contentType = properties.getContentType();
                            long deliveryTag = envelope.getDeliveryTag();
                            // (process the message components here ...)
//                            Log.d("Routing KEY", routingKey);
//                            Log.d("RECV DATA", new String(body));
                            channel.basicAck(deliveryTag, false);

                            Intent intent = new Intent();
                            intent.setAction(Constants.BROADCAST_ACTION_IDENTIFIER);
                            intent.putExtra(Constants.BROADCAST_ACTION_IDENTIFIER, new String(body));
                            sendBroadcast(intent);
                        }
                    });
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered

    }
}