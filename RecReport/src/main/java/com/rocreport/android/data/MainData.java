package com.rocreport.android.data;

/**
 * Created by rickylaishram on 2/22/14.
 */

public class MainData {
    public String category;
    public String title;
    public String picture;
    public String id;
    public String loc_coord;
    public String loc_name;
    public String email;
    public String created;
    public String details;

    public void setData(String category, String title, String picture, String id, String loc_coord,
                        String loc_name, String email, String created, String details) {
        this.category = category;
        this.title = title;
        this.picture = picture;
        this.id = id;
        this.loc_coord = loc_coord;
        this.loc_name = loc_name;
        this.email = email;
        this.created = created;
        this.details = details;
    }
}
