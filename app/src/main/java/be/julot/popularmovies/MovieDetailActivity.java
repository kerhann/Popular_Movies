package be.julot.popularmovies;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends ActionBarActivity {

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
