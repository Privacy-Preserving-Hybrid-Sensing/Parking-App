package au.edu.anu.cs.sparkee.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import au.edu.anu.cs.sparkee.Constants;

public class AMQPBroadcaseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String msg = bundle.getString(Constants.BROADCAST_ACTION_IDENTIFIER);
        Log.d("Receive", msg);

    }

}
