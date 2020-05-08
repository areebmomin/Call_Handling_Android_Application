package com.areeb.mcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {

    Switch switchStatus;
    TextView displayStatus;

    //defining permission value
    private static final int SMS_PERMISSION_CODE = 100;
    private static final int TELEPONE_PERMISSION_CODE = 101;

    //First shared preference
    public static final String PREFS = "com.mcall";
    public static final String STATUS = "score";

    //Second shared preference
    public static final String PREFSS = "com.mcaall";
    public static final String STATUSS = "flags";

    //Start Notification ids
    public static final String CHANNEL_ID = "mcall";
    public static final String CHANNEL_NAME = "Mcall";
    public static final String CHANNEL_DESC = "MCALL Started";

    //Stop notification ids
    public static final String CHANNEL_IDS = "mcaall";
    public static final String CHANNEL_NAMES = "Mcaall";
    public static final String CHANNEL_DESCS = "MCALL Stopped";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting values from activity
        switchStatus = (Switch) findViewById(R.id.status);
        displayStatus = (TextView) findViewById(R.id.displayStatus);
        AdView mAdView1;

        //Mobile AdsView1
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView1 = (AdView) findViewById(R.id.adView1);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);

        //checking preference value
        SharedPreferences sp = getSharedPreferences(PREFS , Context.MODE_PRIVATE);
        int sc = sp.getInt(STATUS,0);
        if(sc == 1)
        {
            switchStatus.setChecked(true);
            displayStatus.setText("MCall started...");
        }

        //requesting for permission

        //checking SMS permission
        checkPermission(Manifest.permission.SEND_SMS,SMS_PERMISSION_CODE);

        //checking Telephony Permission
        checkPermission(Manifest.permission.READ_PHONE_STATE,TELEPONE_PERMISSION_CODE);

        //Fetching the current message
        StudentDbHelper dbHelper = new StudentDbHelper(getApplicationContext());
        SQLiteDatabase db1 = dbHelper.getReadableDatabase();
        String projection[] = {"content"};
        Cursor c =  db1.query("message",projection,null,null,null,null,null);
        if(c.getPosition() == -1)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String content = "I will call you later";
            ContentValues values = new ContentValues();
            values.put("content",content);
            db.insert("message",null,values);
        }
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
                    if(switchStatus.isChecked() && scs == 1) {
                        sms.sendTextMessage(no, null, msg, pi, null);
                        //confirmation after SMS is send
                        Toast.makeText(getApplicationContext(), "Message sent successfully!", Toast.LENGTH_SHORT).show();

                        //setting shared preference
                        spp.edit().putInt(STATUSS , 2).commit();
                    }
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

        //Switch button onclick listner
        switchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(switchStatus.isChecked()) {

                    //Start background service
                    startService();

                    //set preference value to 1
                    SharedPreferences sp = getSharedPreferences(PREFS , Context.MODE_PRIVATE);
                    sp.edit().putInt(STATUS , 1).commit();
                    int sc = sp.getInt(STATUS,0);

                    //Animate textview
                    YoYo.with(Techniques.FadeInRight)
                            .duration(500)
                            .repeat(0)
                            .playOn(displayStatus);

                    YoYo.with(Techniques.Flash)
                            .duration(200)
                            .playOn(switchStatus);

                    //change text view status
                    displayStatus.setText("MCall started...");

                    //silent the device
                    NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if(n.isNotificationPolicyAccessGranted())
                    {
                        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }
                    else
                    {
                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivityForResult(intent,0);
                    }
                }
                final  String off = "Off";
                if(switchStatus.isChecked() == false)
                {
                    displayStatus.setText("MCall stopped.");

                    //Stop background service
                    stopService();

                    //Animate the textview
                    YoYo.with(Techniques.FadeInLeft)
                            .duration(500)
                            .playOn(displayStatus);

                    YoYo.with(Techniques.Flash)
                            .duration(200)
                            .playOn(switchStatus);

                    //display stop notification
                    displayStopNotification();

                    //setting preference value to 0
                    SharedPreferences sp = getSharedPreferences(PREFS , Context.MODE_PRIVATE);
                    sp.edit().putInt(STATUS , 0).commit();
                    int sc = sp.getInt(STATUS,0);

                    //silent the device
                    NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if(n.isNotificationPolicyAccessGranted())
                    {
                        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }
                    else
                    {
                        Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivityForResult(intent,0);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.mainmenu , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id= item.getItemId();
        switch(id)
        {
            case R.id.message:
                Intent intent = new Intent(this , message.class);
                this.startActivity(intent);
                return true;
            case R.id.about:
                Intent intent1 = new Intent(this  , about.class);
                this.startActivity(intent1);
                return true;
            case R.id.info:
                Intent intent2 = new Intent(this , info.class);
                this.startActivity(intent2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    //Requesting permission
    public void checkPermission(String permission , int requestCode)
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this , permission) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(MainActivity.this , new String[]{permission},requestCode);
        }
    }

    //this function is called when the user accept or decline permission
    public void onRequestPermissionsResult(int requestCode , @NonNull String [] permissions , @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode , permissions ,grantResults);

        if(requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "You will be not able to send SMS", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == TELEPONE_PERMISSION_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {

            }
            else
            {
                Toast.makeText(this, "You will be not able to handle calls", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Creating notification
    private void displayStartNotification()
    {

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID , CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_DESC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this , CHANNEL_ID)
                        .setSmallIcon(R.drawable.running)
                        .setContentTitle("MCall")
                        .setContentText("MCall started..")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1,mBuilder.build());
    }

    private void displayStopNotification()
    {
        NotificationChannel channel = new NotificationChannel(CHANNEL_IDS,CHANNEL_NAMES,NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_DESCS);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,CHANNEL_IDS)
                        .setSmallIcon(R.drawable.stopped)
                        .setContentTitle("MCall")
                        .setContentText("MCall stopped.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(2 , mBuilder.build());
    }

    //start background service
    public void startService()
    {
        Intent intent = new Intent(this,BackgroundService.class);
        ContextCompat.startForegroundService(this , intent);
    }

    //stop background service
    public void stopService()
    {
        Intent intent  = new Intent(this,BackgroundService.class);
        stopService(intent);
    }
}
