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
    String moviePoster;
    String movieOverview;
    int movieYear;
    int movieVoteCount;
    float movieRating;

    public MoviePosterItem(String mTitle, String mPoster, int mYear,
                           String mOverview, int mVoteCount, float mRating) {
        this.movieTitle = mTitle;
        this.moviePoster = mPoster;
        this.movieOverview = mOverview;
        this.movieYear = mYear;
        this.movieVoteCount = mVoteCount;
        this.movieRating = mRating;
    }



}

