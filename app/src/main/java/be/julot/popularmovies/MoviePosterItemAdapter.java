package be.julot.popularmovies;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

public class MoviePosterItemAdapter extends ArrayAdapter<MoviePosterItem> {

    private final String LOG_TAG = MoviePosterItemAdapter.class.getSimpleName();

    public MoviePosterItemAdapter(Activity context, Collection moviePosterItems) {
        super(context, 0, (List<MoviePosterItem>) moviePosterItems);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){

        MoviePosterItem moviePosterItem = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.movies_grid_item, parent, false);
        }

        ImageView posterImage = (ImageView) view.findViewById(R.id.posterImage);
        //posterImage.setImageResource(moviePosterItem.moviePoster);
        Picasso.with(getContext()).load(moviePosterItem.moviePoster)
                .into(posterImage);

        TextView movieTitle = (TextView) view.findViewById(R.id.movie_title);
        TextView averageVote = (TextView) view.findViewById(R.id.movie_average_vote);
        movieTitle.setText(moviePosterItem.movieTitle);
        averageVote.setText(moviePosterItem.movieAverageVote);

        return view;
    }


}
