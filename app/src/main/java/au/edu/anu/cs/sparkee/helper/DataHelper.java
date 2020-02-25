package au.edu.anu.cs.sparkee.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import au.edu.anu.cs.sparkee.Constants;

public class DataHelper extends IntentService {

    //    private static Channel amqp_incoming_channel;
    private static DataHelper singleton;
    private Context context;

    public DataHelper(String name, Context ctx) {
        super(name);
        context = ctx;
        initAMQPConnection();
        initHTTPListener();
        mapSubscriptionToken = new HashMap<String,String>();
    }

    public static DataHelper getInstance(Context ctx) {
        if(singleton == null)
            singleton = new DataHelper("DataHelper", ctx);
        return singleton;
    }
    private void initHTTPListener() {

    }

    public boolean sendGet(String url, final String device_uuid) {
        String trx_id = UUID.randomUUID().toString();
        return sendGet(url, device_uuid, trx_id);
    }

    public boolean sendGet(String url, final String device_uuid, final String trx_id) {
        Log.d("GET", url);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest request = new StringRequest(Request.Method.GET, url, okHttpListener, errorHttpListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Subscriber-UUID", device_uuid);
                params.put("Trx-id", trx_id);
                return params;
            }
        };
        queue.add(request);
        return true;
    }

    private Response.Listener okHttpListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String msg) {
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);
            intent.putExtra(Constants.BROADCAST_DATA_STATUS_IDENTIFIER, Constants.BROADCAST_STATUS_OK);
            intent.putExtra(Constants.BROADCAST_HTTP_BODY_IDENTIFIER, msg);
            Log.d("RESP", msg);
            context.sendBroadcast(intent);
        }
    };

    private Response.ErrorListener errorHttpListener =  new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d("EROR", error.toString());
            Intent intent = new Intent();
            intent.setAction(Constants.BROADCAST_DATA_HELPER_IDENTIFIER);
            intent.putExtra(Constants.BROADCAST_DATA_STATUS_IDENTIFIER, Constants.BROADCAST_STATUS_ERR);
            intent.putExtra(Constants.BROADCAST_HTTP_BODY_IDENTIFIER, error.toString());
            context.sendBroadcast(intent);
        }
    };
    private Channel amqp_incoming_channel;
    private Connection amqp_connection;
    private String queueName;

    private void initAMQPConnection() {
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

            amqp_connection = factory.newConnection();
            amqp_incoming_channel = amqp_connection.createChannel();

            queueName = amqp_incoming_channel.queueDeclare().getQueue();

            boolean autoAck = false;
            amqp_incoming_channel.basicConsume(queueName, autoAck, "",
                    new DefaultConsumer(amqp_incoming_channel) {
                        @Override
                        public void handleDelivery(String consumerTag,
                                                   Envelope envelope,
                                                   AMQP.BasicProperties properties,
                                                   byte[] body)
                                throws IOException {

                            String routingKey = envelope.getRoutingKey();
                            String contentType = properties.getContentType();
                            long deliveryTag = envelope.getDeliveryTag();
                            amqp_incoming_channel.basicAck(deliveryTag, false);
                            handleIncomingAMQP(new String(body));
                        }
                    });

        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(TimeoutException te) {
            te.printStackTrace();
        }

    }

    private HashMap<String, String> mapSubscriptionToken;
    public void subscribeTopic(String subscribe_token) {
        try {
            if(mapSubscriptionToken.get(subscribe_token) == null) {
                mapSubscriptionToken.put(subscribe_token, subscribe_token);
                Log.d("SUBS", subscribe_token);
                amqp_incoming_channel.queueBind(queueName, Constants.RABBIT_EXCHANGE_INCOMING_NAME, subscribe_token);
            }
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void handleIncomingAMQP(String msg) {
        Log.d("AMQP", msg);
        // Handle Message Parsing
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_AMQP_IDENTIFIER);
        intent.putExtra(Constants.BROADCAST_AMQP_IDENTIFIER, msg);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
