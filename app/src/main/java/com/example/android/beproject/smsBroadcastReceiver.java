package com.example.android.beproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class smsBroadcastReceiver extends BroadcastReceiver {

    private static MessageListener mListener;

    public static void bindListener(MessageListener listener) {
        mListener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for(int i=0; i<pdus.length; i++){
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            String sender = "Sender : " + smsMessage.getDisplayOriginatingAddress();
            //+ "Display message body: " + smsMessage.getDisplayMessageBody()
            //+ "Time in millisecond: " + smsMessage.getTimestampMillis()
            String message = "Message: " + smsMessage.getMessageBody();
            mListener.messageReceived(sender,message);
        }
    }



}
