package com.rocreport.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rocreport.android.data.MainData;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;

import static com.rocreport.utils.utils.Constants.API_ENDPOINT_UPVOTE;

import static com.rocreport.utils.utils.Constants.API_ENDPOINT_LIST;
import static com.rocreport.utils.utils.Constants.SP_USER_AUTH;
import static com.rocreport.utils.utils.Constants.SP_USER_EMAIL;
import static com.rocreport.utils.utils.Constants.SP_USER_PASS;

public class DetailsActivity extends Activity {

    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));

        ctx = this;

        Bundle bundle = getIntent().getExtras();
        String category = bundle.getString("category");
        String title = bundle.getString("title");
        String picture = bundle.getString("picture");
        String details = bundle.getString("details");
        final String loc_coord = bundle.getString("loc_coord");
        final String id = bundle.getString("id");
        String loc_name = bundle.getString("loc_name");
        String email = bundle.getString("email");
        String created = bundle.getString("created");

        TextView tv_category = (TextView) findViewById(R.id.category);
        TextView tv_title = (TextView) findViewById(R.id.title);
        TextView tv_address = (TextView) findViewById(R.id.address);
        TextView tv_created = (TextView) findViewById(R.id.date);
        TextView tv_description = (TextView) findViewById(R.id.description);
        ImageView iv_photo = (ImageView) findViewById(R.id.photo);
        ImageButton btn_vote = (ImageButton) findViewById(R.id.vote);
        ImageButton btn_map = (ImageButton) findViewById(R.id.map);

        getActionBar().setTitle(category);

        tv_address.setText(loc_name);
        tv_title.setText(title);
        tv_category.setText(category);
        tv_created.setText(created);
        tv_description.setText(details);

        Picasso.with(this).load(picture).into(iv_photo);

        Typeface font_black = Typeface.createFromAsset(this.getAssets(), "Roboto-Black.ttf");
        tv_address.setTypeface(font_black);
        tv_title.setTypeface(font_black);
        tv_category.setTypeface(font_black);
        tv_created.setTypeface(font_black);
        tv_description.setTypeface(font_black);

        btn_vote.setOnClickListener(new View.OnClickListener() {
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.details, menu);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in);
    }

    public void sendVote(String id) {
        SharedPreferences sp = getSharedPreferences(SP_USER_AUTH, MODE_PRIVATE);
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
        });
    }
}
