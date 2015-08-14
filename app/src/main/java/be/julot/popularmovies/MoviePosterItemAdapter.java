package be.julot.popularmovies;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MoviePosterItemAdapter extends ArrayAdapter<MoviePosterItem> {

    public MoviePosterItemAdapter(Activity context, List<MoviePosterItem> moviePosterItems) {
        super(context, 0, moviePosterItems);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){

        final MoviePosterItem moviePosterItem;
        moviePosterItem = getItem(position);

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.movies_grid_item, parent, false);
        }

        ImageView posterImageView = (ImageView) view.findViewById(R.id.posterImageView);

        Picasso.with(getContext()).load(moviePosterItem.moviePoster).into(posterImageView);

        String title = moviePosterItem.movieTitle;

        TextView movieTitle = (TextView) view.findViewById(R.id.movie_title);
        TextView averageVote = (TextView) view.findViewById(R.id.movie_average_vote);
        RelativeLayout wholeCell = (RelativeLayout) view.findViewById(R.id.wholeCell);

        //Truncate title if too long for the TextView in grid
        if (title.length() > 35)
        {
            title = title.substring(0,35)+"…";
        }

        movieTitle.setText(title);
        averageVote.setText(Float.toString(moviePosterItem.movieRating));

        //Setting onClickListener on the whole cell (i.e. the relative layout) so that a click
        //on any cell element (poster image, title textview, rating...) sparks fire.
        wholeCell.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        Intent detailIntent = new Intent(getContext(), MovieDetailActivity.class)
                                                .putParcelableArrayListExtra(Intent.EXTRA_TEXT, moviePosterItem);
                                        getContext().startActivity(detailIntent);
                                    }

                                }
        );

        return view;
    }


}
