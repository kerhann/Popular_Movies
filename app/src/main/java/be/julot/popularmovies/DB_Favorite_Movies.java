package be.julot.popularmovies;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

// I made the choice to only record locally the movie ID on TMDB because:
// 1) I need anyway to make an API call to get the right rating and number of votes (changes daily
// and can't reasonably be locally stored)
// 2) I don't want to locally store the movie poster image, hence I need to make an API call anyway
// for this too
    //So, my favorite list is locally stored, but not movie details.

@Table(name = "Favorites")
public class DB_Favorite_Movies extends Model {

    @Column(name = "tmdb_ID")
    public long tmdb_ID;

}
