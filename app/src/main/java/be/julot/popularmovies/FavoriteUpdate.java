package be.julot.popularmovies;


import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Delete;

import de.greenrobot.event.EventBus;

public class FavoriteUpdate {

    public final boolean update;

    public FavoriteUpdate(boolean update) {
        this.update = update;
    }

    public void Proceed(MoviePosterItem movie, View view, Context context) {
        if(movie.favorite) {
            new Delete().from(DB_Favorite_Movies.class).where("tmdb_ID = ?", movie.tmdb_ID).execute();
            movie.favorite = false;
            MovieDetailActivityFragment.updateFavoriteButton(false, view);
            Toast.makeText(context, R.string.remove_favorite_msg, Toast.LENGTH_LONG).show();
        }
        else {
            DB_Favorite_Movies favorite = new DB_Favorite_Movies();
            favorite.tmdb_ID = movie.tmdb_ID;
            favorite.movieTitle = movie.movieTitle;
            favorite.movieOverview = movie.movieOverview;
            favorite.movieYear = movie.movieYear;
            favorite.movieRating = movie.movieRating;
            favorite.movieVoteCount = movie.movieVoteCount;
            favorite.moviePoster = movie.moviePoster;
            favorite.save();
            MovieDetailActivityFragment.updateFavoriteButton(true, view);
            movie.favorite = true;
            Toast.makeText(context, R.string.add_favorite_msg, Toast.LENGTH_LONG).show();
        }
        EventBus.getDefault().post(new FavoriteUpdate(true));
    }
}
