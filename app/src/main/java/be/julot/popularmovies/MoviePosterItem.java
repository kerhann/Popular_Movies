package be.julot.popularmovies;

//Defines each item in the movie poster grid. Each item shows a title, the average of votes and the
//movie poster in background. See movies_grid_item.xml

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class MoviePosterItem implements Serializable {

    //new ArrayList<MoviePosterItem> ;

    String movieTitle;
    String movieAverageVote;
    String moviePoster;

    public MoviePosterItem(String mTitle, String mAverageVote, String mPoster) {
        this.movieTitle = mTitle;
        this.movieAverageVote = mAverageVote;
        this.moviePoster = mPoster;
    }



}

