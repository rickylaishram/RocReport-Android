package com.rocreport.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.util.Base64;


import com.rocreport.utils.utils.Utils;
import com.rocreport.utils.utils.Constants;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends Activity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;
    private static int PIC_REQUEST;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private File IMAGE;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private double longitude, latitude;

    private String IMAGE_URL;

    private ProgressDialog pDialog;
    private Context CTX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        CTX = this;

        Spinner spinner = (Spinner) findViewById(R.id.category);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        Button btnCamera = (Button) findViewById(R.id.camera);
        Button btnSend = (Button) findViewById(R.id.send);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PIC_REQUEST = (int) System.currentTimeMillis();
                // create Intent to take a picture and return control to the calling application
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
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap scaledPhoto = Utils.decodeSampledBitmapFromFilePath(IMAGE.toString(), 500, 500);

                SendData upload = new SendData();
                upload.execute(scaledPhoto);
            }
        });

        /* Location part
		 * Continuously update while user is in activity
		 */
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        latitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
        longitude = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();

        EditText etLocation = (EditText) findViewById(R.id.address);
        etLocation.setText(latitude+","+longitude);

        locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
				/* If GPS is disable launch Locations Settings */
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                EditText etLocation = (EditText) findViewById(R.id.address);
                etLocation.setText(latitude+","+longitude);
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //File dir = new File(Environment.getExternalStorageDirectory(), "Spiders");
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "rocreport");
                File reimage = new File(dir.getPath() + File.separator + PIC_REQUEST + ".jpg");
                IMAGE = reimage;

                Bitmap scaledPhoto = Utils.decodeSampledBitmapFromFilePath(reimage.toString(), 400, 400);

                ImageView photo = (ImageView) findViewById(R.id.photo);
                photo.setImageBitmap(scaledPhoto);
            }else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }

    public class SendData extends AsyncTask<Bitmap, Void, Void> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(CTX);
            pDialog.setMessage("Sending ...");
            pDialog.show();
        }

        @Override
        public Void doInBackground(Bitmap... bmap) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmap[0].compress(Bitmap.CompressFormat.JPEG, 100, bos);
            String sPhoto = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);

            HttpPost hpost = new HttpPost(Constants.IMGUR_ENDPOINT+Constants.IMGUR_UPLOAD);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("image", sPhoto));
            nameValuePairs.add(new BasicNameValuePair("type", "base64"));

            try
            {
                hpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            } catch (UnsupportedEncodingException e) {
                Log.e("Upload", e.toString());
            }

            hpost.setHeader("Authorization", Constants.IMGUR_AUTH);

            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse resp = null;
            try
            {
                resp = client.execute(hpost);
            } catch (ClientProtocolException e)
            {
                Log.e("Upload", e.toString());
            } catch (IOException e)
            {
                Log.e("Upload", e.toString());
            }

            String result = null;
            try {
                result = EntityUtils.toString(resp.getEntity());
                JSONObject json = new JSONObject(result);
                JSONObject data = json.getJSONObject("data");
                IMAGE_URL = data.getString("link");
                Log.v("Image", IMAGE_URL+"");
                int i = 0;
            } catch (Exception e) {
                Log.e("Upload", e.toString());
            }

            //Upload Location
            /*final String temp_lat = latitude_user+"";
            final String temp_lng = longitude_user+"";

            RequestParams params = new RequestParams();
            params.put("location[latitude]",temp_lat);
            params.put("location[longitude]",temp_lng);
            params.put("location[radius]",50);*/

            /*AsyncHttpClient locationclient = new AsyncHttpClient();
            locationclient.post(API_ENDPOINT + API_NEW_LOCATION, params, new AsyncHttpResponseHandler(){

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                    AsyncHttpClient client1 = new AsyncHttpClient();
                    client1.get(API_ENDPOINT + API_GET_LOCATIONS, new AsyncHttpResponseHandler() {

                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            int id = 0;

                            try {
                                JSONArray json = new JSONArray(new String(responseBody));
                                for(int i = 0; i < json.length(); i++) {
                                    JSONObject element = json.getJSONObject(i);
                                    String lat = element.getDouble("latitude")+"";
                                    String lng = element.getDouble("longitude")+"";

                                    if(lat.equals(temp_lat) && lng.equals(temp_lng)) {

                                        locationid = element.getInt("id")+"";

                                        break;
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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
            });*/

            return null;
        }

        @Override
        public void onPostExecute(Void a) {
            pDialog.dismiss();
            //Button btn_save = (Button) findViewById(R.id.btn_save);
            //btn_save.setEnabled(true);
        }
    }

}
