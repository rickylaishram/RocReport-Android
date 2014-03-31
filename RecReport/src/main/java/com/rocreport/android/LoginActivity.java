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

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import static com.rocreport.utils.utils.Constants.API_AUTH_LOGIN;
import static com.rocreport.utils.utils.Constants.API_ENDPOINT;
import static com.rocreport.utils.utils.Constants.CLIENT_ID;
import static com.rocreport.utils.utils.Constants.SP_AUTH;
import static com.rocreport.utils.utils.Constants.SP_AUTH_TOKEN;

public class LoginActivity extends Activity{

    // UI references.
    private EditText mPasswordView;
    private EditText mEmailView;
    private Button btnRegister;
    private Button mEmailSignInButton;

    private ProgressDialog pDialog;
    private Context CTX;

    private final String mEmail = null;
    private final String mPassword = null;

    private Typeface font_black;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CTX = this;

        //getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));
        getActionBar().hide();

        setUi();
        setFont();
    }

    private void setUi() {
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

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        btnRegister = (Button) findViewById(R.id.register);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                finish();
            }
        });
    }

    private void setFont() {
        font_black = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");

        mEmailSignInButton.setTypeface(font_black);
        mEmailView.setTypeface(font_black);
        mPasswordView.setTypeface(font_black);
        btnRegister.setTypeface(font_black);
    }

    public void attemptLogin() {
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
            focusView.requestFocus();
        } else {
            loginUser(email, password);
        }
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void loginUser(String email, String password) {
        RequestParams params = new RequestParams();
        params.put("id",CLIENT_ID);
        params.put("email",email);
        params.put("password", password);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT + API_AUTH_LOGIN, params, new AsyncHttpResponseHandler() {

            ProgressDialog pDialog = new ProgressDialog(CTX);

            @Override
            public void onStart() {
                pDialog.setMessage("Login. Please wait ...");
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String resp = new String(responseBody);

                try {
                    JSONObject response = new JSONObject(resp);
                    Boolean success = response.getString("status").equals("true");
                    JSONObject data = response.getJSONObject("data");

                    pDialog.dismiss();

                    if (success) {
                        String token = data.getString("token");

                        SharedPreferences sp = getSharedPreferences(SP_AUTH, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(SP_AUTH_TOKEN, token);
                        editor.commit();

                        Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(mIntent);
                        overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                        finish();
                    } else {
                        String reason = data.getString("reason");
                        Toast.makeText(CTX, reason, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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
    }
}



