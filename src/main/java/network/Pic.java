package network;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zy on 15-9-26.
 */
public class Pic implements Parcelable{

    private String name;
    private String picUrl;
    private String detailUrl;
    private boolean hasDown;

    public Pic(){
        hasDown = false;
    }

    public Pic(String name,String picUrl,String detailUrl){
        this.name = name;
        this.picUrl = picUrl;
        this.detailUrl = detailUrl;
        hasDown = false;
    }

    protected Pic(Parcel in) {
        name = in.readString();
        picUrl = in.readString();
        detailUrl = in.readString();
    }

    public static final Creator<Pic> CREATOR = new Creator<Pic>() {
        @Override
        public Pic createFromParcel(Parcel in) {
            return new Pic(in);
        }

        @Override
        public Pic[] newArray(int size) {
            return new Pic[size];
        }
    };

    public void setName(String name) {
        this.name = name;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public void setHasDown(boolean hasDown) {
        this.hasDown = hasDown;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public String getName() {
        return name;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public boolean getHasDown(){
        return hasDown;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(picUrl);
        dest.writeString(detailUrl);
    }
}
