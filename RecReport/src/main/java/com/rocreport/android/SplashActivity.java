package com.rocreport.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.rocreport.utils.utils.Constants.SP_AUTH;
import static com.rocreport.utils.utils.Constants.SP_AUTH_TOKEN;

public class SplashActivity extends Activity {

    private Button btnSignin;
    private Button btnRegister;
    private Typeface font_black;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getActionBar().hide();

        SharedPreferences sp = this.getSharedPreferences(SP_AUTH, MODE_PRIVATE);
        if(sp.getString(SP_AUTH_TOKEN, null) != null) {
            Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mIntent);
            finish();
        }

        setUi();
        setFont();
    }

    private void setUi() {
        btnSignin = (Button) findViewById(R.id.signin);
        btnRegister = (Button) findViewById(R.id.register);

        btnRegister.setOnClickListener(registerHandler);
        btnSignin.setOnClickListener(signinHandler);
    }

    private void setFont() {
        font_black = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");

        btnRegister.setTypeface(font_black);
        btnSignin.setTypeface(font_black);
    }

    private View.OnClickListener signinHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mIntent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(mIntent);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
            finish();
        }
    };

    private View.OnClickListener registerHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mIntent = new Intent(SplashActivity.this, RegisterActivity.class);
            startActivity(mIntent);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
            finish();
        }
    };

}
