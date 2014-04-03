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

import static com.rocreport.utils.utils.Constants.API_ENDPOINT;
import static com.rocreport.utils.utils.Constants.API_REPORT_VOTE;
import static com.rocreport.utils.utils.Constants.CLIENT_ID;
import static com.rocreport.utils.utils.Constants.SP_AUTH;
import static com.rocreport.utils.utils.Constants.SP_AUTH_TOKEN;

public class DetailsActivity extends Activity {

    private Context ctx;

    private TextView tvCategory, tvAddress, tvDescription;
    private ImageView ivPhoto;
    private ImageButton btnVote;
    private GoogleMap mMap;

    private String datCategory, datPicture, datId, datLongitude, datLatitude, datLocname, datEmail, datCreated, datDetails, datScore, datVote, datInform;
    private Boolean datVoted, datIninform;

    private Boolean hasVoted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));
        getActionBar().setTitle("Report Details");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ctx = this;

        getBundleData(getIntent().getExtras());
        setUi();
        setData();
        setFont();
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

        if(datVoted) {
            btnVote.setEnabled(false);
            btnVote.setBackgroundColor(ctx.getResources().getColor(android.R.color.holo_green_light));
        }

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

        btnVote.setOnClickListener(voteHandler);
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

    private View.OnClickListener voteHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendVote();
        }
    };

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
                Intent intent = new Intent();
                intent.getBooleanExtra("voted", hasVoted);
                setResult(Activity.RESULT_OK, intent);
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
        //super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("voted", hasVoted);
        if(getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    public void sendVote() {
        SharedPreferences sp = ctx.getSharedPreferences(SP_AUTH, MODE_PRIVATE);
        String token = sp.getString(SP_AUTH_TOKEN, null);

        RequestParams params = new RequestParams();
        params.put("report", datId);
        params.put("id", CLIENT_ID);
        params.put("token", token);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT+API_REPORT_VOTE, params, new AsyncHttpResponseHandler(){

            ProgressDialog pDialog = new ProgressDialog(ctx);

            @Override
            public void onStart() {
                pDialog.setMessage("Saving your vote. Please wait ...");
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                hasVoted = true;
                btnVote.setEnabled(false);
                btnVote.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                pDialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                //tv_status.setText("Something went wrong :(");
                String resp = new String(responseBody);
                Log.v("Login response", resp);
                pDialog.dismiss();
            }
        });
    }
}
