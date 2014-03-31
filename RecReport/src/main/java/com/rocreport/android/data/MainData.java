package com.rocreport.android.data;

import org.json.JSONArray;

/**
 * Created by rickylaishram on 2/22/14.
 */

public class MainData {
    public String category;
    public String picture;
    public String id;
    public String latitude;
    public String longitude;
    public String loc_name;
    public String email;
    public String created;
    public String details;
    public JSONArray updates;
    public String score;
    public String inform_count;
    public String vote_count;
    public Boolean has_voted;
    public Boolean in_inform;

    public void setData(String category, String picture, String id, String latitude, String longitude,
                        String loc_name, String email, String created, String details, JSONArray updates,
                        String score, String inform_count, String vote_count, Boolean has_voted,
                        Boolean in_inform ) {
        this.category = category;
        this.picture = picture;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.loc_name = loc_name;
        this.email = email;
        this.created = created;
        this.details = details;
        this.updates = updates;
        this.score = score;
        this.inform_count = inform_count;
        this.vote_count = vote_count;
        this.has_voted = has_voted;
        this.in_inform = in_inform;
    }
}
