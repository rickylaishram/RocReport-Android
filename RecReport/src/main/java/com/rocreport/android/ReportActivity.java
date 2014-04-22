package com.rocreport.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.util.Base64;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationRequestCreator;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rocreport.utils.utils.Utils;
import com.rocreport.utils.utils.Constants;

import static com.rocreport.utils.utils.Constants.API_GEOCODE;
import static com.rocreport.utils.utils.Constants.API_ENDPOINT;
import static com.rocreport.utils.utils.Constants.API_IMAGE_ADD;
import static com.rocreport.utils.utils.Constants.API_REPORT_ADD;
import static com.rocreport.utils.utils.Constants.CLIENT_ID;
import static com.rocreport.utils.utils.Constants.SP_AUTH;
import static com.rocreport.utils.utils.Constants.SP_AUTH_TOKEN;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private static int PIC_REQUEST;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private File IMAGE;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double longitude, latitude;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private String IMAGE_URL, FORMATTED_ADDRESS, COUNTRY, ADMIN_AREA_LEVEL_1, ADMIN_AREA_LEVEL_2, LOCALITY;

    private ProgressDialog pDialog;
    private Context CTX;

    private Spinner spinner;
    private ImageButton btnCamera, btnSend;
    private EditText etLocation, details;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long UPDATE_INTERVAL = 1000*UPDATE_INTERVAL_IN_SECONDS;
    private static final long FASTEST_INTERVAL = 1000*FASTEST_INTERVAL_IN_SECONDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));
        getActionBar().setTitle("Report");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        CTX = this;
        mLocationClient = new LocationClient(this, this, this);
        mLocationRequest = new LocationRequest();

        setUI();
        initializeLocationListener();
    }

    private void initializeLocationListener() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    private void setUI() {
        spinner = (Spinner) findViewById(R.id.category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnCamera = (ImageButton) findViewById(R.id.camera);
        btnSend = (ImageButton) findViewById(R.id.send);
        etLocation = (EditText) findViewById(R.id.address);
        details = (EditText) findViewById(R.id.details);

        btnCamera.setOnClickListener(cameraHandler);
        btnSend.setOnClickListener(sendHandler);
    }

    private View.OnClickListener cameraHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PIC_REQUEST = (int) System.currentTimeMillis();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "rocreport");
            if(!dir.exists()) {
                dir.mkdirs();
            }

            IMAGE = new File(dir.getPath() + File.separator + PIC_REQUEST + ".jpg");
            fileUri = Uri.fromFile(IMAGE); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    };

    private View.OnClickListener sendHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            uploadImage();
        }
    };

    @Override
    public void onConnected(Bundle dataBundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
        mLocationClient.requestLocationUpdates(mLocationRequest, this);

        getAddress(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(CTX, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        getAddress(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "rocreport");
                File ori_image = new File(dir.getPath() + File.separator + PIC_REQUEST + ".jpg");
                IMAGE = ori_image;

                Bitmap scaledPhoto = Utils.decodeSampledBitmapFromFilePath(ori_image.toString(), 500, 500);

                // Save the scaled image
                try{
                    File scaled_image = new File(getCacheDir(), PIC_REQUEST + "_s.jpg");

                    //Compress image below 500kb
                    int i = 0;
                    do {
                        FileOutputStream out = new FileOutputStream(scaled_image);
                        scaledPhoto.compress(Bitmap.CompressFormat.JPEG, (100 - 10*i), out);
                        i++;
                    } while (scaled_image.length() > 50000);

                    IMAGE = scaled_image;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ImageView photo = (ImageView) findViewById(R.id.photo);
                photo.setImageBitmap(scaledPhoto);
            }else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }

    public void getAddress(double lat, double lng) {
        AsyncHttpClient geocodeclient = new AsyncHttpClient();
        geocodeclient.get(API_GEOCODE+lat+","+lng, new AsyncHttpResponseHandler(){
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                try {
                    JSONObject json = new JSONObject(new String(responseBody));
                    JSONObject result = json.getJSONArray("results").getJSONObject(0);
                    FORMATTED_ADDRESS = result.getString("formatted_address");
                    JSONArray components = result.getJSONArray("address_components");

                    for (int i = 0; i < components.length(); i++) {
                        JSONObject item = components.getJSONObject(i);
                        String type = item.getJSONArray("types").getString(0);

                        if(type.equals("locality") || type.equals("sublocality")) {
                            LOCALITY = item.getString("long_name");
                        } else if(type.equals("administrative_area_level_2")) {
                            ADMIN_AREA_LEVEL_2 = item.getString("long_name");
                        } else if(type.equals("administrative_area_level_1")) {
                            ADMIN_AREA_LEVEL_1 = item.getString("long_name");
                        } else if(type.equals("country")) {
                            COUNTRY = item.getString("long_name");
                        }
                    }

                    etLocation.setText(FORMATTED_ADDRESS);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                //tv_status.setText("Something went wrong :(");
            }
        });
    }

    private void sendReport() {
        SharedPreferences sp = CTX.getSharedPreferences(SP_AUTH, MODE_PRIVATE);
        String token = sp.getString(SP_AUTH_TOKEN, null);

        RequestParams params = new RequestParams();
        params.put("id", CLIENT_ID);
        params.put("token", token);
        params.put("formatted_address", FORMATTED_ADDRESS);
        params.put("country", COUNTRY);
        params.put("admin_level_1", ADMIN_AREA_LEVEL_1);
        params.put("admin_level_2", ADMIN_AREA_LEVEL_2);
        params.put("locality", LOCALITY);
        params.put("latitude", mCurrentLocation.getLatitude()+"");
        params.put("longitude", mCurrentLocation.getLongitude()+"");
        params.put("category", (spinner.getSelectedItemPosition()+1)+"");
        params.put("description", details.getText().toString());
        params.put("picture", IMAGE_URL);
        params.put("novote", "true"); //Set to true for the time being

        final ProgressDialog pDialog = new ProgressDialog(CTX);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT+API_REPORT_ADD, params, new AsyncHttpResponseHandler(){
            @Override
            public void onStart() {
                pDialog.setMessage("Sending report");
                pDialog.show();
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    Boolean status = response.getString("status").equals("true");

                    if(status) {
                        JSONObject data = response.getJSONObject("data");
                        //No duplicate detection for time being
                        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                String response = new String(responseBody);
                //Log.e("Error", response);
                pDialog.dismiss();
                Toast.makeText(CTX, "Something has gone wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadImage() {

        SharedPreferences sp = CTX.getSharedPreferences(SP_AUTH, MODE_PRIVATE);
        String token = sp.getString(SP_AUTH_TOKEN, null);

        RequestParams params = new RequestParams();
        params.put("id", CLIENT_ID);
        params.put("token", token);
        try {
            params.put("image", IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ProgressDialog pDialog = new ProgressDialog(CTX);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT+API_IMAGE_ADD, params, new AsyncHttpResponseHandler(){
            @Override
            public void onStart() {
                pDialog.setMessage("Uploading Photo");
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    Boolean status = response.getString("status").equals("true");

                    if(status) {
                        JSONObject data = response.getJSONObject("data");
                        IMAGE_URL = data.getString("image_url");

                        sendReport();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                pDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                pDialog.dismiss();
                Toast.makeText(CTX, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }
}
