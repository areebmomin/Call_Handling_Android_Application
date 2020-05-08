package com.areeb.mcall;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class info extends AppCompatActivity {

    Button info1,info2;
    TextView t1,t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //add mobile ads
        AdView mAdView3;
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView3 = (AdView) findViewById(R.id.adView3);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView3.loadAd(adRequest);

        //menu back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //getting value form activity
        info1 = (Button) findViewById(R.id.inf1);
        t1 = (TextView) findViewById(R.id.t1);
        info2 = (Button) findViewById(R.id.inf2);
        t2 = (TextView) findViewById(R.id.t2);

        //Buttton info1 onclicklistner
        info1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t1.getVisibility() == View.INVISIBLE) {
                    t1.setVisibility(View.VISIBLE);
                    info2.performClick();
                }
                else
                    {
                        t1.setVisibility(View.INVISIBLE);
                        info2.performClick();
                    }
            }
        }
        );

        //button info2 onclicklistner
        info2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t2.getVisibility() == View.INVISIBLE )
                {
                    t2.setVisibility(View.VISIBLE);
                }
                else
                {
                    t2.setVisibility(View.INVISIBLE);
                }
                if(t1.getVisibility() == View.VISIBLE )
                {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(580,ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW , R.id.t1);
                    //params.setMarginStart(60);
                    params.setMargins(60,60,0,0);
                    v.setLayoutParams(params);
                }
                if(t1.getVisibility() == View.INVISIBLE )
                {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(580,ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW , R.id.inf1);
                    //params.setMarginStart(60);
                    params.setMargins(60,60,0,0);
                    v.setLayoutParams(params);
                }
            }
        });
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
