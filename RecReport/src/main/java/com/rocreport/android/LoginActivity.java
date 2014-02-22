package com.rocreport.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;
import static com.rocreport.utils.utils.Constants.SP_USER_AUTH;
import static com.rocreport.utils.utils.Constants.SP_USER_TOKEN;
import static com.rocreport.utils.utils.Constants.SP_USER_EMAIL;
import static com.rocreport.utils.utils.Constants.SP_USER_PASS;
import static com.rocreport.utils.utils.Constants.API_ENDPOINT;
import static com.rocreport.utils.utils.Constants.API_LOGIN;

public class LoginActivity extends Activity{

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPasswordView;
    private EditText mEmailView;

    private ProgressDialog pDialog;
    private Context CTX;

    private final String mEmail = null;
    private final String mPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp = this.getSharedPreferences(SP_USER_AUTH, MODE_PRIVATE);
        if((sp.getString(SP_USER_EMAIL, null) != null) || (sp.getString(SP_USER_PASS, null) != null)) {
            Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mIntent);
        }

        CTX = this;

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Typeface font_black = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button btnRegister = (Button) findViewById(R.id.register);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                finish();
            }
        });

        mEmailSignInButton.setTypeface(font_black);
        mEmailView.setTypeface(font_black);
        mPasswordView.setTypeface(font_black);
        btnRegister.setTypeface(font_black);
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //mAuthTask = new UserLoginTask();
            //mAuthTask.execute();

            sendData(email, password);
        }
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean sendData(final String email, final String password) {
        final Boolean response = false;

        RequestParams params = new RequestParams();
        params.put("action",API_LOGIN);
        params.put("submit","Sign In");
        params.put("emails",email);
        params.put("passwords", password);
        params.put("ismobile", "yes");

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT, params, new AsyncHttpResponseHandler(){

            ProgressDialog pDialog = new ProgressDialog(CTX);

            @Override
            public void onStart() {
                pDialog.setMessage("Signing In. Please wait ...");
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                String resp = new String(responseBody);

                Log.v("Login response", resp);

                if(resp.equals("1")) {
                    SharedPreferences.Editor sp = CTX.getSharedPreferences(SP_USER_AUTH, MODE_PRIVATE).edit();
                    sp.putString(SP_USER_EMAIL, email);
                    sp.putString(SP_USER_PASS, password);
                    sp.commit();

                    pDialog.dismiss();

                    Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                    finish();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                //tv_status.setText("Something went wrong :(");
            }

            @Override
            public void onRetry() {
                // Request was retried
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                // Progress notification
            }

            @Override
            public void onFinish() {
            }
        });

        return response;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(CTX);
            pDialog.setMessage("Sending ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... a) {

            RequestParams params = new RequestParams();
            params.put("action",API_LOGIN);
            params.put("submit","Sign In");
            params.put("emails",mEmail);
            params.put("passwords", mPassword);

            AsyncHttpClient client = new AsyncHttpClient();
            client.post(API_ENDPOINT, params, new AsyncHttpResponseHandler(){

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                    Log.v("Login response", new String(responseBody));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                    //tv_status.setText("Something went wrong :(");
                }

                @Override
                public void onRetry() {
                    // Request was retried
                }

                @Override
                public void onProgress(int bytesWritten, int totalSize) {
                    // Progress notification
                }

                @Override
                public void onFinish() {
                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            pDialog.dismiss();

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            pDialog.dismiss();
        }
    }
}



