package be.julot.popularmovies;

//Defines each item in the movie poster grid. Each item shows a title, the average of votes and the
//movie poster in background. See movies_grid_item.xml

import java.io.Serializable;

public class MoviePosterItem implements Serializable {

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

