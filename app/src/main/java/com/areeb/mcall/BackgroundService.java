package com.areeb.mcall;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.areeb.mcall.App.CHANNEL_ID;
import static com.areeb.mcall.MainActivity.PREFSS;
import static com.areeb.mcall.MainActivity.STATUSS;

public class BackgroundService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this , MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        Notification notification = new NotificationCompat.Builder(this , CHANNEL_ID)
                .setContentTitle("MCall")
                .setContentText("MCall is running")
                .setSmallIcon(R.drawable.running)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1 , notification);


        //Fetching the current message
        StudentDbHelper dbHelper = new StudentDbHelper(getApplicationContext());
        SQLiteDatabase db1 = dbHelper.getReadableDatabase();
        String projection[] = {"content"};
        Cursor c =  db1.query("message",projection,null,null,null,null,null);
        c.moveToLast();
        final String sendMsg = c.getString(0);

        //Managing call
        Boolean flag = false;
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        PhoneStateListener callStateListner = new PhoneStateListener()
        {
            @Override
            public void onCallStateChanged(int state, String phoneNumber)
            {

                if (state == TelephonyManager.CALL_STATE_RINGING)
                {
                    //getting shared preference value
                    SharedPreferences spp = getSharedPreferences(PREFSS , Context.MODE_PRIVATE);
                    int sc = spp.getInt(STATUSS,0);
                    if(sc == 0)
                    {
                        //Set shared preference
                        spp.edit().putInt(STATUSS, 1).commit();
                    }

                    //sending SMS
                    String no = phoneNumber;
                    String msg = sendMsg;
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
                    SmsManager sms = SmsManager.getDefault();
                    int scs = spp.getInt(STATUSS,0);
                        sms.sendTextMessage(no, null, msg, pi, null);
                        //confirmation after SMS is send
                        Toast.makeText(getApplicationContext(), "Message sent successfully!", Toast.LENGTH_SHORT).show();

                        //setting shared preference
                        spp.edit().putInt(STATUSS , 2).commit();
                    }

                if (state == TelephonyManager.CALL_STATE_OFFHOOK)
                {
                    Toast.makeText(getApplicationContext(), "Phone is in call or call picked", Toast.LENGTH_LONG).show();
                }
                if(state == TelephonyManager.CALL_STATE_IDLE)
                {
                    SharedPreferences spp = getSharedPreferences(PREFSS , Context.MODE_PRIVATE);
                    spp.edit().putInt(STATUSS , 0).commit();
                }
            }
        };
        telephonyManager.listen(callStateListner,PhoneStateListener.LISTEN_CALL_STATE);


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}