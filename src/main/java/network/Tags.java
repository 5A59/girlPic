package network;

import android.nfc.Tag;

/**
 * Created by zy on 15-9-25.
 */
public class Tags {
    private String name;
    private String url;

    public Tags(){

    }

    public Tags(String name,String url){
        this.name = name;
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
