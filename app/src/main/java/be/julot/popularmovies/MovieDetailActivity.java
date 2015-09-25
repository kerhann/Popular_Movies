package be.julot.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    public MoviePosterItem movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        movie = (MoviePosterItem) getIntent().getParcelableArrayListExtra(Intent.EXTRA_TEXT);
        fillMovieFields(movie);
        updateFavoriteButton(movie.favorite);

    }

    private void fillMovieFields(MoviePosterItem movie) {

        TextView titleTextView = (TextView) this.findViewById(R.id.titleTextView);
        TextView yearTextView = (TextView) this.findViewById(R.id.yearTextView);
        TextView voteCountTextView = (TextView) this.findViewById(R.id.voteCountTextView);
        TextView overviewTextView = (TextView) this.findViewById(R.id.overviewDetailTextView);
        ImageView posterImageView = (ImageView) this.findViewById(R.id.posterImageDetail);
        RatingBar ratingBar = (RatingBar) this.findViewById(R.id.ratingBar);

        Picasso.with(getApplicationContext()).load(movie.moviePoster).into(posterImageView);
        titleTextView.setText(movie.movieTitle);
        if(movie.movieYear != 0) {
            yearTextView.setText(Integer.toString(movie.movieYear));
        }
        voteCountTextView.setText("(" + Integer.toString(movie.movieVoteCount) + ")");
        if(movie.movieOverview != null) {
            overviewTextView.setText(movie.movieOverview); //allows to display the default value in
                                                           // strings.xml (not displaying "null")
                                                           // if no overview is available
        }
        ratingBar.setRating(movie.movieRating / 2); //converting a 10-based value to 5-star rating
    }

    public void updateFavorites() {

        if(movie.favorite) {
            new Delete().from(DB_Favorite_Movies.class).where("tmdb_ID = ?", movie.tmdb_ID).execute();
            updateFavoriteButton(false);
        }
        else {
            DB_Favorite_Movies favorite = new DB_Favorite_Movies();
            favorite.tmdb_ID = movie.tmdb_ID;
            favorite.save();
            updateFavoriteButton(true);
        }

    }

    public void updateFavoriteButton(boolean favorite) {
        if(favorite)
        {
            Button favButton = (Button) this.findViewById(R.id.button_Favorite);
            favButton.setText(R.string.remove_favorite);
        }
    }

}
