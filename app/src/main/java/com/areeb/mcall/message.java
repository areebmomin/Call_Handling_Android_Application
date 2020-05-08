package com.areeb.mcall;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class message extends AppCompatActivity {

    EditText newMessage;
    TextView currentMessage;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //add mobile ads
        AdView mAdView1;
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView1 = (AdView) findViewById(R.id.adView4);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest);

        invalidateOptionsMenu();
        newMessage = (EditText) findViewById(R.id.newMessage);
        save = (Button) findViewById(R.id.save);
        currentMessage = (TextView) findViewById(R.id.currentMessage);

        //menu back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //save button clicklistner
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Animate textview
                YoYo.with(Techniques.RubberBand)
                        .duration(500)
                        .playOn(currentMessage);

                //database writable
                StudentDbHelper dbHelper = new StudentDbHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String content = newMessage.getText().toString();
                ContentValues values = new ContentValues();
                values.put("content",content);
                db.insert("message",null,values);
                newMessage.setText("");

                //get readable database
                SQLiteDatabase db1 = dbHelper.getReadableDatabase();
                String projection[] = {"content"};
                Cursor c =  db1.query("message",projection,null,null,null,null,null);
                c.moveToLast();
                String currentMsg = c.getString(0);
                currentMessage.setText(currentMsg);
            }
        });

        //get readable database
        StudentDbHelper dbHelper = new StudentDbHelper(getApplicationContext());
        SQLiteDatabase db1 = dbHelper.getReadableDatabase();
        String projection[] = {"content"};
        Cursor c =  db1.query("message",projection,null,null,null,null,null);
        c.moveToLast();
        String currentMsg = c.getString(0);
        currentMessage.setText(currentMsg);
    }

    //For back button
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                return true;

                default:
                    return super.onOptionsItemSelected(item);
        }
    }
}