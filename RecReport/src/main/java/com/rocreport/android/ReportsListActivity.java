package com.rocreport.android;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rocreport.android.data.MainData;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Vector;

import static com.rocreport.utils.utils.Constants.API_ENDPOINT;
import static com.rocreport.utils.utils.Constants.API_REPORT_MINE;
import static com.rocreport.utils.utils.Constants.API_REPORT_NEARBY;
import static com.rocreport.utils.utils.Constants.CLIENT_ID;
import static com.rocreport.utils.utils.Constants.SP_AUTH;
import static com.rocreport.utils.utils.Constants.SP_AUTH_TOKEN;


public class ReportsListActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    private Vector<MainData> data = new Vector<MainData>();
    private Context CTX;
    private MainAdapter ADAPTER;
    private ListView list;
    private ImageButton report;
    private SwipeRefreshLayout refresh;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private int radius = 100;

    private Boolean refresh_enabled = true;

    private final static int DETAILS_RESULT_CODE = 7890;
    private int POSITION = 0;

    // For endless scroll
    private int threshold = 5;
    private int page = 3;
    private int limit = 10;
    private Boolean fetching = false;
    private Boolean enddata = false;

    private String API_NOW = API_REPORT_NEARBY;
    private String TYPE = "score";
    private Boolean first = true;
    private Boolean MAIN_REFRESH_LOCK = false;
    private int SELECTED_NAV = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        CTX = this;

        getActionBar().setBackgroundDrawable(getResources().getDrawable(android.R.color.holo_blue_dark));
        mLocationClient = new LocationClient(this, this, this);

        setUI();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(!first) {
            Intent mIntent;
            switch (position) {
                case 0:
                    SELECTED_NAV = position;
                    MAIN_REFRESH_LOCK = false;
                    mTitle = getResources().getStringArray(R.array.drawer_nav)[position];
                    API_NOW = API_REPORT_NEARBY;
                    TYPE = "score";
                    refresh_enabled = true;
                    restoreActionBar();
                    refreshData();
                    break;
                case 1:
                    SELECTED_NAV = position;
                    MAIN_REFRESH_LOCK = true;
                    mTitle = getResources().getStringArray(R.array.drawer_nav)[position];
                    API_NOW = API_REPORT_MINE;
                    TYPE = "new";
                    refresh_enabled = true;
                    restoreActionBar();
                    refreshData();
                    break;
                case 2:
                    mTitle = getResources().getStringArray(R.array.drawer_nav)[SELECTED_NAV];
                    mIntent = new Intent(ReportsListActivity.this, SettingsActivity.class);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                    break;
                case 3:
                    mTitle = getResources().getStringArray(R.array.drawer_nav)[SELECTED_NAV];
                    mIntent = new Intent(ReportsListActivity.this, AboutActivity.class);
                    startActivity(mIntent);
                    overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                    break;
            }
        } else {
            first = false;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void refreshData() {
        if(refresh_enabled) {
            data.removeAllElements();
            page = 0;
            enddata = false;
            fetchData(page, TYPE, mCurrentLocation, radius, limit);
        }
    }

    private void setUI(){
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp( R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        ADAPTER = new MainAdapter(this,R.layout.adapter_main, data);

        list = (ListView) findViewById(R.id.list);
        report = (ImageButton) findViewById(R.id.report);
        refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mTitle = getResources().getStringArray(R.array.drawer_nav)[0];
        restoreActionBar();

        refresh.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        list.setAdapter(ADAPTER);
        list.setOnScrollListener(scrollHandler);

        report.setOnClickListener(reportHandler);
        list.setOnItemClickListener(itemHandler);
        refresh.setOnRefreshListener(refreshHandler);
    }

    private SwipeRefreshLayout.OnRefreshListener refreshHandler = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refresh_enabled = true;
            refreshData();
        }
    };

    private View.OnClickListener reportHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mIntent = new Intent(ReportsListActivity.this, ReportActivity.class);
            startActivity(mIntent);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
        }
    };

    private AbsListView.OnScrollListener scrollHandler = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if(((firstVisibleItem + visibleItemCount + threshold) > data.size())
                    && (data.size() != 0) && !fetching && !enddata && !MAIN_REFRESH_LOCK) {
                page ++;
                fetchData(page, TYPE, mCurrentLocation, radius, limit);
            }
        }
    };

    private AdapterView.OnItemClickListener itemHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            POSITION = position;
            MainData d = data.get(position);

            Bundle bundle = new Bundle();
            bundle.putString("category", d.category);
            bundle.putString("picture", d.picture);
            bundle.putString("id", d.id);
            bundle.putString("longitude", d.longitude);
            bundle.putString("latitude", d.latitude);
            bundle.putString("loc_name", d.loc_name);
            bundle.putString("email", d.email);
            bundle.putString("created", d.created);
            bundle.putString("details", d.details);
            bundle.putString("score", d.score);
            bundle.putString("vote_count", d.vote_count);
            bundle.putString("inform_count", d.inform_count);
            bundle.putBoolean("has_votes", d.has_voted);
            bundle.putBoolean("in_inform", d.in_inform);

            Intent mIntent = new Intent(ReportsListActivity.this, DetailsActivity.class);
            mIntent.putExtras(bundle);
            startActivityForResult(mIntent, DETAILS_RESULT_CODE);
            overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
        }
    };

    @Override
    public void onActivityResult(int request_code, int result_code, Intent mdata) {
        super.onActivityResult(request_code, result_code, mdata);

        switch (request_code) {
            case (DETAILS_RESULT_CODE): {
                if (result_code == Activity.RESULT_OK) {
                    Boolean voted = mdata.getBooleanExtra("voted", false);

                    if(voted) {
                        data.elementAt(POSITION).has_voted = true;
                        ADAPTER.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        mCurrentLocation = mLocationClient.getLastLocation();
        refreshData();
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
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(CTX, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        refresh_enabled = false;
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent mIntent;

        switch (id) {
            case R.id.action_refresh:
                refresh_enabled = true;
                refreshData();
                return true;
            case R.id.action_settings:
                mIntent = new Intent(ReportsListActivity.this, SettingsActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                return true;
            case R.id.action_about:
                mIntent = new Intent(ReportsListActivity.this, AboutActivity.class);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_right_in,R.anim.slide_right_out);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    Fetches the reports from the server
    radius is in km
     */
    private void fetchData(int page, String orderby , Location location, int radius, int limit) {
        int offset = page*limit;

        SharedPreferences sp = CTX.getSharedPreferences(SP_AUTH, MODE_PRIVATE);
        String token = sp.getString(SP_AUTH_TOKEN, null);

        RequestParams params = new RequestParams();
        params.put("id", CLIENT_ID);
        params.put("token", token);
        params.put("limit", limit+"");
        params.put("offset", offset+"");
        params.put("latitude", location.getLatitude()+"");
        params.put("longitude", location.getLongitude()+"");
        params.put("radius",radius*1000+"");
        params.put("orderby", orderby);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_ENDPOINT+API_NOW, params, new AsyncHttpResponseHandler(){

            @Override
            public void onStart() {
                fetching = true;
                refresh.setRefreshing(true);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody){

                try {
                    String str = new String(responseBody);
                    JSONObject response = new JSONObject(new String(responseBody));

                    Boolean status = (response.getString("status")).equals("true");
                    JSONArray repdata = response.getJSONArray("data");

                    if (status) {
                        for (int i = 0; i < repdata.length(); i++) {
                            JSONObject report = repdata.getJSONObject(i);

                            MainData d = new MainData();
                            d.setData(
                                    report.getString("category"),
                                    report.getString("picture"),
                                    report.getString("report_id"),
                                    report.getString("latitude"),
                                    report.getString("longitude"),
                                    report.getString("formatted_address"),
                                    report.getString("email"),
                                    report.getString("added_at"),
                                    report.getString("description"),
                                    (new JSONArray()),
                                    //report.getJSONArray("update"),
                                    report.getString("score"),
                                    report.getString("inform_count"),
                                    report.getString("vote_count"),
                                    report.getString("hasVotes").equals("true"),
                                    report.getString("inInform").equals("true")
                            );
                            data.add(d);
                        }

                        if(repdata.length() == 0) {
                            enddata = true;
                        }

                        ADAPTER.notifyDataSetChanged();
                    } else {
                        Toast.makeText(CTX, "Error", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Log.e("Error", e.toString());
                }
                refresh.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){
                String resp = new String(responseBody);
                Log.e("Fetch Error", resp);
                refresh.setRefreshing(false);
            }

            @Override
            public void onFinish() {
                fetching = false;
            }
        });
    }
}
