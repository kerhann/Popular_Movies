package be.julot.popularmovies;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MoviePosterItemAdapter extends ArrayAdapter<MoviePosterItem> {

    private final String LOG_TAG = MoviePosterItemAdapter.class.getSimpleName();

    public MoviePosterItemAdapter(Activity context, List<MoviePosterItem> moviePosterItems) {
        super(context, 0, moviePosterItems);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){

        MoviePosterItem moviePosterItem = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.movies_grid_item, parent, false);
        }

        ImageView posterImageView = (ImageView) view.findViewById(R.id.posterImageView);

        Picasso.with(getContext()).load(moviePosterItem.moviePoster).into(posterImageView);

        String title = moviePosterItem.movieTitle;

        TextView movieTitle = (TextView) view.findViewById(R.id.movie_title);
        TextView averageVote = (TextView) view.findViewById(R.id.movie_average_vote);

        //Truncate title if too long
        if (title.length() > 35)
        {
            title = title.substring(0,35)+"...";
        }

        movieTitle.setText(title);
        averageVote.setText(Float.toString(moviePosterItem.movieRating));

        return view;
    }


}
