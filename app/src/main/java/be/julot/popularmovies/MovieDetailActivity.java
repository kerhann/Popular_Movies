package be.julot.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        MoviePosterItem movie = (MoviePosterItem) getIntent().getSerializableExtra(Intent.EXTRA_TEXT);
        fillMovieFields(movie);
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
        voteCountTextView.setText("("+Integer.toString(movie.movieVoteCount)+")");
        if(movie.movieOverview != null) {
            overviewTextView.setText(movie.movieOverview); //allows to display the default value in
                                                           // strings.xml (not displaying "null")
                                                           // if no overview is available
        }
        ratingBar.setRating(movie.movieRating / 2); //converting a 10-based value to 5-star rating
    }

}
