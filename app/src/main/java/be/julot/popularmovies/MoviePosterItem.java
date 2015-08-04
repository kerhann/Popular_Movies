package be.julot.popularmovies;

//Defines each item in the movie poster grid. Each item shows a title, the average of votes and the
//movie poster in background. See movies_grid_item.xml

public class MoviePosterItem {
    String movieTitle;
    String movieAverageVote;
    int moviePoster;

    public MoviePosterItem(String mTitle, String mAverageVote, int mPoster) {
        this.movieTitle = mTitle;
        this.movieAverageVote = mAverageVote;
        this.moviePoster = mPoster;
    }

}
