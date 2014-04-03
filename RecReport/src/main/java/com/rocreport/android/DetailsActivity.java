package com.rocreport.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rocreport.android.data.MainData;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;

public class DetailsActivity extends Activity {

    private Context ctx;

    private TextView tvCategory, tvAddress, tvDescription;
    private ImageView ivPhoto;
    private ImageButton btnVote;
    private GoogleMap mMap;

    private String datCategory, datPicture, datId, datLongitude, datLatitude, datLocname, datEmail, datCreated, datDetails, datScore, datVote, datInform;
    private Boolean datVoted, datIninform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));
        getActionBar().setTitle("Report Details");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ctx = this;

        setUi();
        getBundleData(getIntent().getExtras());
        setData();
        setFont();

        /*btn_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVote(id);
            }
        });

        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] latlng = loc_coord.split(";");
                String uriBegin = "geo:" + latlng[0] + "," + latlng[1];
                String query = latlng[0] + "," + latlng[1];
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery;
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });*/
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(this.getAssets(), "Roboto-Light.ttf");
        tvCategory.setTypeface(font);
        tvAddress.setTypeface(font);
        tvDescription.setTypeface(font);
    }

    private void setData() {
        tvCategory.setText(datCategory);
        tvAddress.setText(datLocname);
        tvDescription.setText(datDetails);

        Picasso.with(ctx).load(datPicture).into(ivPhoto);

        LatLng location = new LatLng(Double.parseDouble(datLatitude), Double.parseDouble(datLongitude));
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,18));
        mMap.addMarker(new MarkerOptions().position(location).draggable(false));
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    private void setUi() {
        tvCategory = (TextView) findViewById(R.id.category);
        tvAddress = (TextView) findViewById(R.id.address);
        tvDescription = (TextView) findViewById(R.id.description);
        ivPhoto = (ImageView) findViewById(R.id.photo);
        btnVote = (ImageButton) findViewById(R.id.vote);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    }

    private void getBundleData(Bundle bundle) {
        datCategory = bundle.getString("category");
        datPicture = bundle.getString("picture");
        datId = bundle.getString("id");
        datLongitude = bundle.getString("longitude");
        datLatitude = bundle.getString("latitude");
        datLocname = bundle.getString("loc_name");
        datEmail = bundle.getString("email");
        datCreated = bundle.getString("created");
        datDetails = bundle.getString("details");
        datScore = bundle.getString("score");
        datVote = bundle.getString("vote_count");
        datInform = bundle.getString("inform_count");
        datVoted = bundle.getBoolean("has_votes");
        datIninform = bundle.getBoolean("in_inform");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
                break;
            default:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }


    public void sendVote(String id) {
    /*    SharedPreferences sp = getSharedPreferences(SP_USER_AUTH, MODE_PRIVATE);
        String emails = sp.getString(SP_USER_EMAIL, null);
        String passwords = sp.getString(SP_USER_PASS, null);

        RequestParams params = new RequestParams();
        params.put("ismobile", "yes");
        params.put("id", id);
        params.put("passwords", passwords);
        params.put("emails", emails);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT_UPVOTE, params, new AsyncHttpResponseHandler(){

            ProgressDialog pDialog = new ProgressDialog(ctx);

            @Override
            public void onStart() {
                pDialog.setMessage("Saving your vote. Please wait ...");
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                String resp = new String(responseBody);
                ((ImageButton) findViewById(R.id.vote)).setEnabled(false);
                ((ImageButton) findViewById(R.id.vote)).setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                Log.v("Login response", resp);
                pDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                //tv_status.setText("Something went wrong :(");
                String resp = new String(responseBody);
                Log.v("Login response", resp);
                pDialog.dismiss();
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
    }
}
