package be.julot.popularmovies;


import java.util.ArrayList;

public class VideoItem extends ArrayList{

    public String videoSite;
    public String videoName;
    public String videoKey;
    public String videoType;

    public VideoItem(String vSite, String vName, String vKey, String vType) {
        this.videoSite = vSite;
        this.videoName = vName;
        this.videoKey = vKey;
        this.videoType = vType;
    }


}
