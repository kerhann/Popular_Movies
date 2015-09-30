package be.julot.popularmovies;



// I made the choice to only record locally the movie ID on TMDB because:
// 1) I need anyway to make an API call to get the right rating and number of votes (changes daily
// and can't reasonably be locally stored)
// 2) I don't want to locally store the movie poster image, hence I need to make an API call anyway
// for this too
    //So, my favorite list is locally stored, but not movie details.

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Favorites")
public class DB_Favorite_Movies extends Model {

    @Column(name = "tmdb_ID")
    public long tmdb_ID;

    @Column(name = "title")
    public String movieTitle;

    @Column(name = "overview")
    public String movieOverview;

    @Column(name = "year")
    public int movieYear;

    @Column(name = "rating")
    public float movieRating;

    @Column(name = "vote_count")
    public int movieVoteCount;

    @Column(name = "poster_path")
    public String moviePoster;

}
