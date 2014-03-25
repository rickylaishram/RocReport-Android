package com.rocreport.android;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rocreport.android.data.MainData;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

import static com.rocreport.utils.utils.Constants.API_ENDPOINT;
import static com.rocreport.utils.utils.Constants.API_REPORT_NEARBY;
import static com.rocreport.utils.utils.Constants.CLIENT_ID;
import static com.rocreport.utils.utils.Constants.SP_AUTH;
import static com.rocreport.utils.utils.Constants.SP_AUTH_TOKEN;

public class MainActivity extends Activity {

    private Vector<MainData> data = new Vector<MainData>();
    private Context CTX;
    private MainAdapter ADAPTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CTX = this;

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));
        //getActionBar().setTitle("");

        ADAPTER = new MainAdapter(this,R.layout.adapter_main, data);

        ListView list = (ListView) findViewById(R.id.list);
        ImageButton report = (ImageButton) findViewById(R.id.report);

        list.setAdapter(ADAPTER);
        //list.setDividerHeight(5);

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MainActivity.this, ReportActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainData d = data.get(position);

                Bundle bundle = new Bundle();
                bundle.putString("category", d.category);
                bundle.putString("title", d.title);
                bundle.putString("picture", d.picture);
                bundle.putString("id", d.id);
                bundle.putString("loc_coord", d.loc_coord);
                bundle.putString("loc_name", d.loc_name);
                bundle.putString("email", d.email);
                bundle.putString("created", d.created);
                bundle.putString("details", d.details);

                Intent mIntent = new Intent(MainActivity.this, DetailsActivity.class);
                mIntent.putExtras(bundle);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);

            }
        });

        fetchData(0,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            data.removeAllElements();
            fetchData(0,0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchData(int page, int orderby) {
        int limit = 10;
        int offset = page*limit;

        SharedPreferences sp = CTX.getSharedPreferences(SP_AUTH, MODE_PRIVATE);
        String token = sp.getString(SP_AUTH_TOKEN, null);

        RequestParams params = new RequestParams();
        params.put("id", CLIENT_ID);
        params.put("token", token);
        params.put("limit", limit);
        params.put("offset", offset);
        params.put("latitude","");
        params.put("longitude","");
        params.put("radius","");
        params.put("orderby", "");

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT+API_REPORT_NEARBY, params, new AsyncHttpResponseHandler(){

            ProgressDialog pDialog = new ProgressDialog(CTX);

            @Override
            public void onStart() {
                pDialog.setMessage("Getting data. Please wait ...");
                pDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){
                String resp = new String(responseBody);

                try {
                    JSONArray jarray = new JSONArray(new String(responseBody));

                    for (int i = 0; i < jarray.length(); i++) {
                        MainData d = new MainData();
                        d.setData(jarray.getJSONObject(i).getString("cat"),
                                jarray.getJSONObject(i).getString("title"),
                                jarray.getJSONObject(i).getString("picture"),
                                jarray.getJSONObject(i).getString("id"),
                                jarray.getJSONObject(i).getString("loc_coord"),
                                jarray.getJSONObject(i).getString("loc_name"),
                                jarray.getJSONObject(i).getString("email"),
                                jarray.getJSONObject(i).getString("created"),
                                jarray.getJSONObject(i).getString("details"));
                        data.add(d);
                    }

                    ADAPTER.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.e("Error", e.toString());
                }

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
