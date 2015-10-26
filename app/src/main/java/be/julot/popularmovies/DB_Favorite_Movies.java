package be.julot.popularmovies;



// We record movie details, but not the poster image (this is kept in cache though, so if you lose
// Internet connection after displaying the movie grid, you will see the poster in movie details).
// We do not record all reviews and trailers in local db, but the movie detail activity still works
// and displays movie details even when no Internet connection is available and reviews/videos cannot
// be retrieved.

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
