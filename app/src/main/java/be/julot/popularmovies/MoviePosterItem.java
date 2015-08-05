package be.julot.popularmovies;

//Defines each item in the movie poster grid. Each item shows a title, the average of votes and the
//movie poster in background. See movies_grid_item.xml

import java.util.ArrayList;

public class MoviePosterItem extends ArrayList {
    String movieTitle;
    String movieAverageVote;
    String moviePoster;

    public MoviePosterItem(String mTitle, String mAverageVote, String mPoster) {
        this.movieTitle = mTitle;
        this.movieAverageVote = mAverageVote;
        this.moviePoster = mPoster;
    }

}
