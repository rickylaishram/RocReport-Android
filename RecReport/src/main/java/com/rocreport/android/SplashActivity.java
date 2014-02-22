package com.rocreport.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import static com.rocreport.utils.utils.Constants.SP_USER_AUTH;
import static com.rocreport.utils.utils.Constants.SP_USER_EMAIL;
import static com.rocreport.utils.utils.Constants.SP_USER_PASS;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getActionBar().hide();

        SharedPreferences sp = this.getSharedPreferences(SP_USER_AUTH, MODE_PRIVATE);
        if((sp.getString(SP_USER_EMAIL, null) != null) || (sp.getString(SP_USER_PASS, null) != null)) {
            Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mIntent);
            finish();
        }

        Button btnSignin = (Button) findViewById(R.id.signin);
        Button btnRegister = (Button) findViewById(R.id.register);

        Typeface font_black = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");

        btnRegister.setTypeface(font_black);
        btnSignin.setTypeface(font_black);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(SplashActivity.this, RegisterActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                finish();
            }
        });

        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                finish();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
