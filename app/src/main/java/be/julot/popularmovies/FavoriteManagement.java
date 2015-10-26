package be.julot.popularmovies;


import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import de.greenrobot.event.EventBus;


// Once the Favorite button is clicked (either to add or remove a favorite)...

public class FavoriteManagement {

    public final boolean update;

    public FavoriteManagement(boolean update) {
        this.update = update;
    }

    public static DB_Favorite_Movies getFavorite(long id) {
        return new Select()
                .from(DB_Favorite_Movies.class)
                .where("tmdb_ID = ?", id)
                .executeSingle();
    }

    //...we update the favorites in local db...
    public void ProceedUpdate(MoviePosterItem movie, View view, Context context) {

        //=> either we remove it...
        if(movie.favorite) {
            new Delete().from(DB_Favorite_Movies.class).where("tmdb_ID = ?", movie.tmdb_ID).execute();
            movie.favorite = false;
            //and we change the button caption
            MovieDetailActivityFragment.updateFavoriteButton(false, view);
            Toast.makeText(context, R.string.remove_favorite_msg, Toast.LENGTH_LONG).show();
        }
        //=> or we add it
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
            //change button caption
            MovieDetailActivityFragment.updateFavoriteButton(true, view);
            movie.favorite = true;
            Toast.makeText(context, R.string.add_favorite_msg, Toast.LENGTH_LONG).show();
        }

        //In any case, we tell the poster grid (useful mostly if in dual pane view), so that
        // the grid updates if the favorite list has changed (and if the grid is supposed to show
        //favorites). Thanks EventBus for making it so simple...
        EventBus.getDefault().post(new FavoriteManagement(true));
    }

}
