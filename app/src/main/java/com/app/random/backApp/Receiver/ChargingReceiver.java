package com.app.random.backApp.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.Time;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class ChargingReceiver extends BroadcastReceiver {
    private static final String TAG = "ChargingReceiver";

    private EventBus bus = EventBus.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ChargingReceiver - onReceive() started");
        MessageEvent.OnChargingDischargingEvent event = null;

        // Get current time
        Time now = new Time();
        now.setToNow();
        String timeOfEvent = now.format("%H:%M:%S");

        String eventData = "@" + timeOfEvent + " this device started ";
        if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
            event = new MessageEvent.OnChargingDischargingEvent(eventData + "charging");
        } else if(intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)){
            event = new MessageEvent.OnChargingDischargingEvent(eventData + "discharging");
        }

        // Post the event
        bus.post(event);
    }

}